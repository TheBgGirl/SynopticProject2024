import unittest

from sklearn.ensemble import RandomForestClassifier

from floodPrediction import FloodPrediction


class TestFloodPrediction(unittest.TestCase):
    def setUp(self):
        self.prediction_instance = FloodPrediction()

    def test_prediction(self):
        self.prediction_instance.prediction()
        # Add assertions to check if prediction method works as expected

    def test_cross_validation(self):
        self.prediction_instance.prediction()  # Ensure prediction is done before cross-validation
        self.prediction_instance.cross_validation()
        # Add assertions to check if cross-validation works as expected

    def test_accuracy(self):
        self.prediction_instance.prediction()  # Ensure prediction is done before calculating accuracy
        self.prediction_instance.accuracy()
        # Add assertions to check if accuracy calculation works as expected

    def test_pickle_data(self):
        self.prediction_instance.prediction()  # Ensure prediction is done before pickling the model
        self.prediction_instance.pickle_data()
        # Add assertions to check if pickling data works as expected


if __name__ == '__main__':
    unittest.main()
