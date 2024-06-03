import pickle
from datetime import date, timedelta
import pandas as pd

from dailyClimateData import DailyClimateData
from dailyRiverDischargeData import DailyRiverDischargeData

predicted_data = pd.DataFrame()

start_date = date.today()
end_date = start_date + timedelta(days=182.5)


def numOfDays(date1, date2):
    if date2 > date1:
        return (date2 - date1).days
    else:
        return (date1 - date2).days


date_list = []
for i in range(0, numOfDays(start_date, end_date) + 1):
    date_list.append(start_date + timedelta(days=i))
predicted_data["date"] = date_list

# Load the trained model
with open('models/trained_model.pkl', 'rb') as file:
    model = pickle.load(file)

climate_data = DailyClimateData(start_date, end_date)
climate_data.getData()
river_discharge_data = DailyRiverDischargeData(start_date, end_date)
river_discharge_data.getData()

new_data = pd.merge(river_discharge_data.data, climate_data.data, on='date', how='outer')
new_data.drop(columns='date', inplace=True)

# Make predictions
predictions = model.predict(new_data)
print(predictions)

pred = []
for prediction in predictions:
    pred.append(prediction)

predicted_data["prediction"] = pred

predicted_data['Date'] = pd.to_datetime(predicted_data['date'], errors='coerce')

predicted_data['month'] = predicted_data['Date'].dt.month
grouped_data = predicted_data.groupby('month')

monthly_probabilities = grouped_data['prediction'].mean()

print(monthly_probabilities)
