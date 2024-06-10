package com.wales

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Month

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val dataPath = "/Users/jamie/Dev/synop/app/src/main/java/com/wales/wd.csv"
    val predictor = WeatherPredictor(dataPath)

    // Test data - you should replace these with relevant test values
    val latitude = 12.532608854954955
    val longitude = 106.88876457344239
    val localDate = LocalDate.of(2022, Month.MAY, 20)
    val dayOfYear = localDate.dayOfYear

    // Predictions
    val sunshine = predictor.predictSunshine(latitude, longitude, dayOfYear)
    val temperature = predictor.predictTemperature(latitude, longitude, dayOfYear)
    val rainfall = predictor.predictRainfall(latitude, longitude, dayOfYear)

    // Output results
    println("Predicted Sunshine Duration: $sunshine hours")
    println("Predicted Temperature: $temperature °C")
    println("Predicted Rainfall: $rainfall mm")
}
