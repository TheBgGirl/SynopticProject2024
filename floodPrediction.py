import pickle
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import KFold, cross_val_score
import matplotlib.pyplot as plt


class FloodPrediction:
    def __init__(self):
        self.climate_model = None
        self.river_discharge = None
        self.y_test = None
        self.y_train = None
        self.X_test = None
        self.X_train = None
        self.y = None
        self.X = None
        self.model = None

    def prediction(self):
        # Loading models
        self.river_discharge = pd.read_csv('models/river_discharge.csv')
        self.climate_model = pd.read_csv('models/climate_model_data.csv')

        # Merge both models
        data = pd.merge(self.river_discharge, self.climate_model, on='time')

        flood_threshold = data['river_discharge'].quantile(0.95)

        data['flood_occured'] = (data['river_discharge'] > flood_threshold)

        # Preprocess data
        data.ffill(inplace=True)

        # Feature engineering
        data['discharge_rolling_mean'] = data['river_discharge'].rolling(window=3).mean()

        self.X = data[['river_discharge',
                       'temperature_2m_mean',
                       'temperature_2m_max',
                       'temperature_2m_min',
                       'relative_humidity_2m_mean',
                       'precipitation_sum',
                       'soil_moisture_0_to_10cm_mean']]
        self.y = data['flood_occured']

        self.X_train, self.X_test, self.y_train, self.y_test = train_test_split(self.X, self.y, test_size=0.2,
                                                                                random_state=42)

        self.model = RandomForestClassifier(n_estimators=100, random_state=42)
        self.model.fit(self.X_train, self.y_train)

    def cross_validation(self):
        kfold = KFold(n_splits=5, shuffle=True, random_state=42)

        cv_results = cross_val_score(self.model, self.X, self.y, cv=kfold, scoring='accuracy')

        print("Cross-Validation Accuracy Scores:", cv_results)
        print("Mean Accuracy:", cv_results.mean())
        print("Standard Deviation:", cv_results.std())

    def accuracy(self):
        y_pred = self.model.predict(self.X_test)

        acc = accuracy_score(self.y_test, y_pred)
        print(f'Accuracy: {acc:.2f}')
        print(classification_report(self.y_test, y_pred))

        feature_importance = self.model.feature_importances_
        indices = np.argsort(feature_importance)

        plt.figure(figsize=(10, 6))
        plt.title("Feature Importance")
        plt.barh(range(len(indices)), feature_importance[indices], color='b', align='center')
        plt.yticks(range(len(indices)), [self.X.columns[i] for i in indices])
        plt.xlabel('Relative Importance')
        plt.show()

    def pickle_data(self):
        with open('models/trained_model.pkl', 'wb') as file:
            pickle.dump(self.model, file)

    def __main__(self):
        self.prediction()
        self.cross_validation()
        self.accuracy()
        self.pickle_data()


a = FloodPrediction()
a.__main__()
