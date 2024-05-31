import os
import math
import openmeteo_requests
import requests_cache
import pandas as pd
from retry_requests import retry

cache_session = requests_cache.CachedSession('.cache', expire_after=3600)
retry_session = retry(cache_session, retries=5, backoff_factor=0.2)
openmeteo = openmeteo_requests.Client(session=retry_session)

latitude = 20.112261
longitude = 77.757988
distance_km = 2

lat_deg_to_km = 111
lon_deg_to_km = math.cos(latitude * math.pi / 180) * lat_deg_to_km
lat_shift = distance_km / lat_deg_to_km
lon_shift = distance_km / lon_deg_to_km
north = latitude + lat_shift
south = latitude - lat_shift
east = longitude + lon_shift
west = longitude - lon_shift
lat_increment = 0.0009
lon_increment = 0.0009

lat_points = [south + i * lat_increment for i in range(int((north - south) / lat_increment) + 1)]
lon_points = [west + i * lon_increment for i in range(int((east - west) / lon_increment) + 1)]

locations = [(lat, lon) for lat in lat_points for lon in lon_points]

batch_size = 5
location_batches = [locations[i:i + batch_size] for i in range(0, len(locations), batch_size)]

minor_flood_threshold = 10
major_flood_threshold = 30

def classify_flood(row):
    if row['river_discharge_mean'] > major_flood_threshold:
        return "major flood"
    elif row['river_discharge_mean'] > minor_flood_threshold:
        return "minor flood"
    else:
        return "no flood"

def process_batch(batch):
    latitudes = ','.join([str(lat) for lat, lon in batch])
    longitudes = ','.join([str(lon) for lat, lon in batch])
    url = "https://flood-api.open-meteo.com/v1/flood"
    params = {
        "latitude": latitudes,
        "longitude": longitudes,
        "daily": "river_discharge,river_discharge_mean,river_discharge_max,river_discharge_min",
        "start_date": "2024-05-31",
        "end_date": "2024-09-30",
        "models": "forecast_v4"
    }
    responses = openmeteo.weather_api(url, params=params)
    return responses

output_csv_file = "forecast_river_discharge_flood_severity.csv"
if not os.path.exists(output_csv_file):
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
                    inclusive="left"
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
    combined_dataframe['flood_severity'] = combined_dataframe.apply(classify_flood, axis=1)
    combined_dataframe.to_csv(output_csv_file, index=False)
    print(f"Data with flood severity classifications has been written to {output_csv_file}")
else:
    print("CSV file found. Loading data from CSV...")
    combined_dataframe = pd.read_csv(output_csv_file)
