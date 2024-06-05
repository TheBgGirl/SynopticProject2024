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
    val dataPath = "/Users/jamie/Dev/synop/app/src/main/java/com/wales/weatherdata.csv"
    val data: DataFrame = readCsv(dataPath)
    val formula = Formula.lhs("Temperature")
    val model = RandomForest.fit( formula, data)
    val prediction = predictTemperature(model, 51.4816, -3.1791, LocalDate.of(2024, 6, 5))
    println("Predicted Temperature: $prediction°C")
}

@RequiresApi(Build.VERSION_CODES.O)
fun readCsv(filePath: String): DataFrame {
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