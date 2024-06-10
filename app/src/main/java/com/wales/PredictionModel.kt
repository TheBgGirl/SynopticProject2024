package com.wales

import android.os.Build
import androidx.annotation.RequiresApi
import smile.regression.RandomForest
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.data.vector.DoubleVector
import smile.data.vector.IntVector
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FarmElement(val weather: Weather, val yield: Double)
data class Weather(var temp: Double, val sunshine: Double, val precipitation: Double)

enum class Crop {
    RICE,
    PUMPKIN,
    LEAFY
}

@RequiresApi(Build.VERSION_CODES.O)
class WeatherPredictor(private val dataPath: String) {
    private var sunshineModel: RandomForest? = null
    private var tempModel: RandomForest? = null
    private var rainfallModel: RandomForest? = null

    init {
        val modelPath = "models"
        File(modelPath).mkdirs()
        val fullData: DataFrame = readCsv()
        sunshineModel = loadOrCreateModel(
            "$modelPath/sunshineModel.ser",
            "SunshineDuration ~ Latitude + Longitude + DayOfYear",
            fullData
        )
        tempModel = loadOrCreateModel(
            "$modelPath/tempModel.ser",
            "MeanTemp ~ Latitude + Longitude + DayOfYear",
            fullData
        )
        rainfallModel = loadOrCreateModel(
            "$modelPath/rainfallModel.ser",
            "PrecipitationSum ~ Latitude + Longitude + DayOfYear",
            fullData
        )
    }

    private fun loadOrCreateModel(
        fileName: String,
        formulaString: String,
        fullData: DataFrame
    ): RandomForest {
        return if (File(fileName).exists()) {
            deserializeModel(fileName)
        } else {
            val model = RandomForest.fit(Formula.of(formulaString), fullData)
            serializeModel(model, fileName)
            model
        }
    }

    fun serializeModels() {
        sunshineModel?.let { serializeModel(it, "models/sunshineModel.ser") }
        tempModel?.let { serializeModel(it, "models/tempModel.ser") }
        rainfallModel?.let { serializeModel(it, "models/rainfallModel.ser") }
    }

    private fun serializeModel(model: RandomForest, fileName: String) {
        ObjectOutputStream(FileOutputStream(fileName)).use { it.writeObject(model) }
    }

    fun deserializeModel(fileName: String): RandomForest =
        ObjectInputStream(FileInputStream(fileName)).use { it.readObject() as RandomForest }

    fun getWeatherData(latitude: Double, longitude: Double, dayOfYear: Int): Weather? {
        val temp = predictTemperature(latitude, longitude, dayOfYear) ?: return null
        val sunshine = predictSunshine(latitude, longitude, dayOfYear) ?: return null
        val rainfall = predictRainfall(latitude, longitude, dayOfYear) ?: return null
        return Weather(temp, sunshine, rainfall)
    }

    fun predictSunshine(latitude: Double, longitude: Double, dayOfYear: Int): Double? =
        sunshineModel?.predict(
            createInputDataFrame(
                latitude,
                longitude,
                dayOfYear,
                "SunshineDuration"
            )
        )?.get(0)

    fun predictTemperature(latitude: Double, longitude: Double, dayOfYear: Int): Double? =
        tempModel?.predict(createInputDataFrame(latitude, longitude, dayOfYear, "MeanTemp"))?.get(0)

    fun predictRainfall(latitude: Double, longitude: Double, dayOfYear: Int): Double? =
        rainfallModel?.predict(
            createInputDataFrame(
                latitude,
                longitude,
                dayOfYear,
                "PrecipitationSum"
            )
        )?.get(0)

    private fun createInputDataFrame(
        latitude: Double,
        longitude: Double,
        dayOfYear: Int,
        type: String
    ): DataFrame =
        DataFrame.of(
            DoubleVector.of("Latitude", doubleArrayOf(latitude)),
            DoubleVector.of("Longitude", doubleArrayOf(longitude)),
            IntVector.of("DayOfYear", intArrayOf(dayOfYear)),
            DoubleVector.of(type, doubleArrayOf(0.0))
        )

    private fun readCsv(): DataFrame {
        val path = Paths.get(dataPath)
        val lines = Files.readAllLines(path)
        val headers = lines.first().split(",").map(String::trim)

        val dates = IntArray(lines.size - 1)
        val latitudes = DoubleArray(lines.size - 1)
        val longitudes = DoubleArray(lines.size - 1)
        val sunshineDurations = DoubleArray(lines.size - 1)
        val precipitationSums = DoubleArray(lines.size - 1)
        val meanTemps = DoubleArray(lines.size - 1)

        for ((index, line) in lines.drop(1).withIndex()) {
            val tokens = line.split(",").map(String::trim)
            val date = LocalDate.parse(tokens[0], DateTimeFormatter.ISO_LOCAL_DATE)
            dates[index] = date.dayOfYear
            latitudes[index] = tokens[1].toDouble()
            longitudes[index] = tokens[2].toDouble()
            sunshineDurations[index] = tokens[3].toDouble() / 3600 // Convert seconds to hours
            precipitationSums[index] = tokens[4].toDouble()
            meanTemps[index] = tokens[5].toDouble()
        }

        return DataFrame.of(
            IntVector.of("DayOfYear", dates),
            DoubleVector.of("Latitude", latitudes),
            DoubleVector.of("Longitude", longitudes),
            DoubleVector.of("SunshineDuration", sunshineDurations),
            DoubleVector.of("PrecipitationSum", precipitationSums),
            DoubleVector.of("MeanTemp", meanTemps)
        )
    }

    fun evaluateYield(latitude: Double, longitude: Double, cropType: Crop): Map<String, Double> {
        val cropCondition = getCropCondition(cropType)
        val yieldMap = mutableMapOf<String, Double>()

        for (month in 1..12) {
            val weatherList = mutableListOf<Weather>()
            val daysInMonth = LocalDate.of(2024, month, 1).lengthOfMonth()
            var totalRainfall = 0.0
            for (day in 1..daysInMonth) {
                val dayOfYear = LocalDate.of(2024, month, day).dayOfYear
                val weather = getWeatherData(latitude, longitude, dayOfYear)
                if (weather != null) {
                    weatherList.add(weather)
                    totalRainfall += weather.precipitation
                }
            }
            if (weatherList.isNotEmpty()) {
                val avgTemp = weatherList.map { it.temp }.average()
                val avgSunshine = weatherList.map { it.sunshine }.average()
                val avgWeather = Weather(avgTemp, avgSunshine, totalRainfall)
                val yield = when {
                    cropCondition.isGood(avgWeather) -> 75.0
                    cropCondition.isMedium(avgWeather) -> 50.0
                    cropCondition.isBad(avgWeather) -> 25.0
                    else -> 0.0
                }
                yieldMap["2024-${month.toString().padStart(2, '0')}"] = yield
                println("Month: $month, Average Weather: $avgWeather, Yield: $yield")
            } else {
                println("No weather data available for Month: $month")
            }
        }
        return yieldMap
    }

}
data class CropCondition(
    val tempGood: ClosedRange<Double>,
    val tempMedium: ClosedRange<Double>,
    val tempBad: ClosedRange<Double>,
    val sunshineGood: ClosedRange<Double>,
    val sunshineMedium: ClosedRange<Double>,
    val sunshineBad: ClosedRange<Double>,
    val precipitationGood: ClosedRange<Double>,
    val precipitationMedium: ClosedRange<Double>,
    val precipitationBad: ClosedRange<Double>
) {
    fun isGood(weather: Weather) =
        weather.temp in tempGood &&
                weather.sunshine in sunshineGood &&
                weather.precipitation in precipitationGood

    fun isMedium(weather: Weather) =
        weather.temp in tempMedium &&
                weather.sunshine in sunshineMedium &&
                weather.precipitation in precipitationMedium

    fun isBad(weather: Weather) =
        weather.temp in tempBad &&
                weather.sunshine in sunshineBad &&
                weather.precipitation in precipitationBad
}

private fun getCropCondition(cropType: Crop): CropCondition {
    return when (cropType) {
        Crop.RICE -> CropCondition(
            tempGood = 25.0..30.0,
            tempMedium = 20.0..25.0,
            tempBad = -Double.MAX_VALUE..20.0,
            sunshineGood = 150.0..250.0,
            sunshineMedium = 100.0..150.0,
            sunshineBad = -Double.MAX_VALUE..100.0,
            precipitationGood = 200.0..300.0,
            precipitationMedium = 100.0..200.0,
            precipitationBad = -Double.MAX_VALUE..100.0
        )
        Crop.PUMPKIN -> CropCondition(
            tempGood = 20.0..30.0,
            tempMedium = 15.0..20.0,
            tempBad = -Double.MAX_VALUE..15.0,
            sunshineGood = 200.0..300.0,
            sunshineMedium = 150.0..200.0,
            sunshineBad = -Double.MAX_VALUE..150.0,
            precipitationGood = 60.0..100.0,
            precipitationMedium = 40.0..60.0,
            precipitationBad = -Double.MAX_VALUE..40.0
        )
        Crop.LEAFY -> CropCondition(
            tempGood = 18.0..24.0,
            tempMedium = 15.0..18.0,
            tempBad = -Double.MAX_VALUE..15.0,
            sunshineGood = 120.0..200.0,
            sunshineMedium = 80.0..120.0,
            sunshineBad = -Double.MAX_VALUE..80.0,
            precipitationGood = 40.0..80.0,
            precipitationMedium = 20.0..40.0,
            precipitationBad = -Double.MAX_VALUE..20.0
        )
    }
}
