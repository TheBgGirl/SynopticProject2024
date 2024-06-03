import openmeteo_requests

import requests_cache
import pandas as pd
from retry_requests import retry

from datetime import date

today = date.today()


class DailyClimateData:
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
        url = "https://climate-api.open-meteo.com/v1/climate"
        params = {
            "latitude": 12.5776539,
            "longitude": 106.9349172,
            "start_date": self.date_start,
            "end_date": self.date_end,
            "models": "MRI_AGCM3_2_S",
            "daily": ["temperature_2m_mean", "temperature_2m_max", "temperature_2m_min", "relative_humidity_2m_mean",
                      "precipitation_sum", "soil_moisture_0_to_10cm_mean"]
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
        daily_temperature_2m_mean = daily.Variables(0).ValuesAsNumpy()
        daily_temperature_2m_max = daily.Variables(1).ValuesAsNumpy()
        daily_temperature_2m_min = daily.Variables(2).ValuesAsNumpy()
        daily_relative_humidity_2m_mean = daily.Variables(3).ValuesAsNumpy()
        daily_precipitation_sum = daily.Variables(4).ValuesAsNumpy()
        daily_soil_moisture_0_to_10cm_mean = daily.Variables(5).ValuesAsNumpy()

        daily_data = {"date": pd.date_range(
            start=pd.to_datetime(daily.Time(), unit="s", utc=True),
            end=pd.to_datetime(daily.TimeEnd(), unit="s", utc=True),
            freq=pd.Timedelta(seconds=daily.Interval()),
            inclusive="left"
        ), "temperature_2m_mean": daily_temperature_2m_mean, "temperature_2m_max": daily_temperature_2m_max,
            "temperature_2m_min": daily_temperature_2m_min,
            "relative_humidity_2m_mean": daily_relative_humidity_2m_mean,
            "precipitation_sum": daily_precipitation_sum,
            "soil_moisture_0_to_10cm_mean": daily_soil_moisture_0_to_10cm_mean}

        daily_dataframe = pd.DataFrame(data=daily_data)
        self.data = daily_dataframe

    def __main__(self):
        self.getData()

