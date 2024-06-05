package com.wales

import android.os.Build
import androidx.annotation.RequiresApi
import smile.regression.RandomForest
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.data.vector.DoubleVector
import smile.data.vector.IntVector
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val dataPath = "app/src/main/res/weather_data.csv"
    val data: DataFrame = readCsv(dataPath)
    val formula = Formula.lhs("Precipitation Sum")

    val model = RandomForest.fit(formula, data)
    val prediction = predictPrecipitation(model, 51.4816, -3.1791, LocalDate.of(2024, 6, 5))
    println("Predicted precipitation: $prediction mm")
}

@RequiresApi(Build.VERSION_CODES.O)
fun readTemperatureCSV(filePath: String): DataFrame {
    val path = Paths.get(filePath)
    val lines = Files.readAllLines(path)
    val headers = lines[0].split(",").map { it.trim() }

    val latitudes = DoubleArray(lines.size - 1)
    val longitudes = DoubleArray(lines.size - 1)
    val dates = IntArray(lines.size - 1)
    val temperatures = DoubleArray(lines.size - 1)

    for ((index, line) in lines.drop(1).withIndex()) {
        val tokens = line.split(",").map { it.trim() }
        latitudes[index] = tokens[0].toDouble()
        longitudes[index] = tokens[1].toDouble()
        val date = LocalDate.parse(tokens[2], DateTimeFormatter.ISO_LOCAL_DATE)
        dates[index] = date.dayOfYear
        temperatures[index] = tokens[3].toDouble()
    }

    return DataFrame.of(
        DoubleVector.of("Latitude", latitudes),
        DoubleVector.of("Longitude", longitudes),
        IntVector.of("Day of Year", dates),
        DoubleVector.of("Temperature", temperatures)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun readCsv(filePath: String): DataFrame {
    val path = Paths.get(filePath)
    val lines = Files.readAllLines(path)
    val headers = lines[0].split(",").map { it.trim() }

    val latitudes = DoubleArray(lines.size - 1)
    val longitudes = DoubleArray(lines.size - 1)
    val sunshineDurations = DoubleArray(lines.size - 1)
    val precipitationSums = DoubleArray(lines.size - 1)
    val dates = IntArray(lines.size - 1)

    for ((index, line) in lines.drop(1).withIndex()) {
        val tokens = line.split(",").map { it.trim() }
        latitudes[index] = tokens[1].toDoubleOrNull() ?: 0.0
        longitudes[index] = tokens[2].toDoubleOrNull() ?: 0.0
        sunshineDurations[index] = tokens[3].toDoubleOrNull() ?: 0.0
        precipitationSums[index] = tokens[4].toDoubleOrNull() ?: 0.0
        val date = if (tokens[0].isNotEmpty()) LocalDate.parse(tokens[0], DateTimeFormatter.ISO_LOCAL_DATE) else LocalDate.now()
        dates[index] = date.dayOfYear
    }

    return DataFrame.of(
        DoubleVector.of("Latitude", latitudes),
        DoubleVector.of("Longitude", longitudes),
        DoubleVector.of("Precipitation Sum", precipitationSums),
        IntVector.of("Day of Year", dates)
    )
}
@RequiresApi(Build.VERSION_CODES.O)
fun predictTemperature(model: RandomForest, latitude: Double, longitude: Double, date: LocalDate): Double {
    val dayOfYear = date.dayOfYear

    val input = DataFrame.of(
        DoubleVector.of("Latitude", doubleArrayOf(latitude)),
        DoubleVector.of("Longitude", doubleArrayOf(longitude)),
        IntVector.of("Day of Year", intArrayOf(dayOfYear))
    )

    val prediction = model.predict(input)
    return prediction[0]
}
@RequiresApi(Build.VERSION_CODES.O)
fun predictPrecipitation(model: RandomForest, latitude: Double, longitude: Double, date: LocalDate): Double {
    val dayOfYear = date.dayOfYear
    val input = DataFrame.of(
        DoubleVector.of("Latitude", doubleArrayOf(latitude)),
        DoubleVector.of("Longitude", doubleArrayOf(longitude)),
        IntVector.of("Day of Year", intArrayOf(dayOfYear))
    )
    return model.predict(input)[0]
}
