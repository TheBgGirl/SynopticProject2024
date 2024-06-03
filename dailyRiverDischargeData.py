import openmeteo_requests

import requests_cache
import pandas as pd
from retry_requests import retry


class DailyRiverDischargeData:
    def __init__(self, date_start, date_end):
        self.date_start = date_start
        self.date_end = date_end
        self.data = pd.DataFrame()

    def getData(self):
        # Setup the Open-Meteo API client with cache and retry on error
        cache_session = requests_cache.CachedSession('.cache', expire_after=3600)
        retry_session = retry(cache_session, retries=5, backoff_factor=0.2)
        openmeteo = openmeteo_requests.Client(session=retry_session)

        # Make sure all required weather variables are listed here
        # The order of variables in hourly or daily is important to assign them correctly below
        url = "https://flood-api.open-meteo.com/v1/flood"
        params = {
            "latitude": 12.59,
            "longitude": 106.89,
            "daily": "river_discharge",
            "start_date": self.date_start,
            "end_date": self.date_end
        }
        responses = openmeteo.weather_api(url, params=params)

        # Process first location. Add a for-loop for multiple locations or weather models
        response = responses[0]
        print(f"Coordinates {response.Latitude()}°N {response.Longitude()}°E")
        print(f"Elevation {response.Elevation()} m asl")
        print(f"Timezone {response.Timezone()} {response.TimezoneAbbreviation()}")
        print(f"Timezone difference to GMT+0 {response.UtcOffsetSeconds()} s")

        # Process daily data. The order of variables needs to be the same as requested.
        daily = response.Daily()
        daily_river_discharge = daily.Variables(0).ValuesAsNumpy()

        daily_data = {"date": pd.date_range(
            start=pd.to_datetime(daily.Time(), unit="s", utc=True),
            end=pd.to_datetime(daily.TimeEnd(), unit="s", utc=True),
            freq=pd.Timedelta(seconds=daily.Interval()),
            inclusive="left"
        ), "river_discharge": daily_river_discharge}

        daily_dataframe = pd.DataFrame(data=daily_data)
        self.data = daily_dataframe

    def __main__(self): self.getData()
