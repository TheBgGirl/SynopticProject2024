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

@RequiresApi(Build.VERSION_CODES.O)
class WeatherPredictor(private val dataPath: String) {
    private var sunshineModel: RandomForest? = null
    private var tempModel: RandomForest? = null
    private var rainfallModel: RandomForest? = null

    init {
        val modelPath = "models"
        File(modelPath).mkdirs()
        val fullData: DataFrame = readCsv()
        sunshineModel = loadOrCreateModel("$modelPath/sunshineModel.ser", "SunshineDuration ~ Latitude + Longitude + DayOfYear", fullData)
        tempModel = loadOrCreateModel("$modelPath/tempModel.ser", "MeanTemp ~ Latitude + Longitude + DayOfYear", fullData)
        rainfallModel = loadOrCreateModel("$modelPath/rainfallModel.ser", "PrecipitationSum ~ Latitude + Longitude + DayOfYear", fullData)
    }

    private fun loadOrCreateModel(fileName: String, formulaString: String, fullData: DataFrame): RandomForest {
        return if (File(fileName).exists()) {
            deserializeModel(fileName)
        } else {
            val model = RandomForest.fit(Formula.of(formulaString), fullData)
            serializeModel(model, fileName)
            model
        }
    }

        private fun trainModels(fullData: DataFrame) {
        sunshineModel = RandomForest.fit(
            Formula.of("SunshineDuration ~ Latitude + Longitude + DayOfYear"),
            fullData
        )
        tempModel = RandomForest.fit(Formula.of("MeanTemp ~ Latitude + Longitude + DayOfYear"), fullData)
        rainfallModel = RandomForest.fit(Formula.of("PrecipitationSum ~ Latitude + Longitude + DayOfYear"), fullData)
        println("Models Fitted")
    }

    fun serializeModels() {
        sunshineModel?.let { serializeModel(it, "sunshineModel.ser") }
        tempModel?.let { serializeModel(it, "tempModel.ser") }
        rainfallModel?.let { serializeModel(it, "rainfallModel.ser") }
    }

    private fun serializeModel(model: RandomForest, fileName: String) {
        ObjectOutputStream(FileOutputStream(fileName)).use { it.writeObject(model) }
    }

    fun deserializeModel(fileName: String): RandomForest =
        ObjectInputStream(FileInputStream(fileName)).use { it.readObject() as RandomForest }
    fun predictSunshine(latitude: Double, longitude: Double, dayOfYear: Int): Double? =
        sunshineModel?.predict(createInputDataFrame(latitude, longitude, dayOfYear, "SunshineDuration"))?.get(0)

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
            sunshineDurations[index] = tokens[3].toDouble()
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
}
