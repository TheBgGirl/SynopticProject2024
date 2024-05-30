import pickle
import numpy as np
from datetime import date
import pandas as pd

from dailyClimateData import DailyClimateData
from dailyRiverDischargeData import DailyRiverDischargeData

today = date.today()

# Load the trained model
with open('models/trained_model.pkl', 'rb') as file:
    model = pickle.load(file)

climate_data = DailyClimateData(today)
climate_data.getData()
river_discharge_data = DailyRiverDischargeData()
river_discharge_data.getData()

new_data = pd.merge(river_discharge_data.data, climate_data.data, on='date', how='outer')
new_data.drop(columns='date', inplace=True)
print(new_data)

print(new_array)

# Make predictions
predictions = model.predict(new_data)

print(predictions)
