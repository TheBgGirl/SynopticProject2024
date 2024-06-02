import math
import openmeteo_requests
import requests_cache
import pandas as pd
from retry_requests import retry
from pathlib import Path

cache_session: requests_cache.CachedSession = requests_cache.CachedSession(
    ".cache", expire_after=3600
)
retry_session: requests_cache.CachedSession = retry(
    cache_session, retries=5, backoff_factor=0.2
)
openmeteo: openmeteo_requests.Client = openmeteo_requests.Client(session=retry_session)

latitude: float = 20.112261
longitude: float = 77.757988
distance_km: float = 2.0

lat_deg_to_km: float = 111.0
lon_deg_to_km: float = math.cos(latitude * math.pi / 180) * lat_deg_to_km

lat_shift: float = distance_km / lat_deg_to_km
lon_shift: float = distance_km / lon_deg_to_km

north: float = latitude + lat_shift
south: float = latitude - lat_shift
east: float = longitude + lon_shift
west: float = longitude - lon_shift

lat_increment: float = 0.0009
lon_increment: float = 0.0009

lat_points: list[float] = [
    south + i * lat_increment for i in range(int((north - south) / lat_increment) + 1)
]
lon_points: list[float] = [
    west + i * lon_increment for i in range(int((east - west) / lon_increment) + 1)
]

locations: list[tuple[float]] = [(lat, lon) for lat in lat_points for lon in lon_points]

batch_size: int = 5
location_batches: list[list[tuple[float, float]]] = [
    locations[i : i + batch_size] for i in range(len(locations), batch_size)
]

minor_flood_threshold: int = 10
major_flood_threshold: int = 30


def classify_flood(row: pd.series) -> str:
    if row["river_discharge_mean"] > major_flood_threshold:
        return "major flood"
    elif row["river_discharge_mean"] > minor_flood_threshold:
        return "minor flood"
    else:
        return "no flood"


def process_batch(batch: list[tuple[float, float]]) -> list[dict[str, any]]:
    latitudes = ",".join([str(lat) for lat, lon in batch])
    longitudes = ",".join([str(lon) for lat, lon in batch])
    url = "https://flood-api.open-meteo.com/v1/flood"
    params = {
        "latitude": latitudes,
        "longitude": longitudes,
        "daily": "river_discharge,river_discharge_mean,river_discharge_max,river_discharge_min",
        "start_date": "2024-05-31",
        "end_date": "2024-09-30",
        "models": "forecast_v4",
    }
    try:
        responses = openmeteo.weather_api(url, params=params)
        return responses
    except Exception as e:
        print(f"Error processing batch {batch}: {e}")
        return []


def main() -> None:
    output_csv_file: Path = Path("forecast_river_discharge_flood_severity.csv")

    if output_csv_file.exists():
        print("CSV file found. Loading data from CSV...")

        combined_dataframe = pd.read_csv(output_csv_file)
        return

    print("CSV file not found. Running API requests and processing data...")
    all_results = []

    for batch in location_batches:
        responses = process_batch(batch)
        for i, response in enumerate(responses):
            lat, lon = batch[i]
            print(f"Processing data for Coordinates {lat}°N {lon}°E")

            daily = response.Daily()
            daily_river_discharge = daily.Variables(0).ValuesAsNumpy()
            daily_river_discharge_mean = daily.Variables(1).ValuesAsNumpy()
            daily_river_discharge_max = daily.Variables(2).ValuesAsNumpy()
            daily_river_discharge_min = daily.Variables(3).ValuesAsNumpy()

            daily_data = {
                "date": pd.date_range(
                    start=pd.to_datetime(daily.Time(), unit="s", utc=True),
                    end=pd.to_datetime(daily.TimeEnd(), unit="s", utc=True),
                    freq=pd.Timedelta(seconds=daily.Interval()),
                    inclusive="left",
                ),
                "latitude": lat,
                "longitude": lon,
                "river_discharge": daily_river_discharge,
                "river_discharge_mean": daily_river_discharge_mean,
                "river_discharge_max": daily_river_discharge_max,
                "river_discharge_min": daily_river_discharge_min,
            }

            daily_dataframe = pd.DataFrame(data=daily_data)
            all_results.append(daily_dataframe)

    combined_dataframe = pd.concat(all_results, ignore_index=True)
    combined_dataframe["flood_severity"] = combined_dataframe.apply(
        classify_flood, axis=1
    )
    combined_dataframe.to_csv(output_csv_file, index=False)
    print(
        f"Data with flood severity classifications has been written to {output_csv_file}"
    )


if __name__ == "__main__":
    main()
