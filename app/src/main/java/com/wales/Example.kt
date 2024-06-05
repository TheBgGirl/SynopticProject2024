package com.wales

import java.util.concurrent.CountDownLatch
import java.io.FileWriter

fun main() {
    val weatherDataFetcher = WeatherDataFetcher()
    val boundingBox = "12.3276539,106.684917,12.8276539,107.184917"
    val startDate = "2024-05-19"
    val endDate = "2024-06-02"

    val latch = CountDownLatch(1)

    weatherDataFetcher.fetchWeatherData(boundingBox, startDate, endDate) { success ->
        if (success) {
            println("Data fetched successfully.")
        } else {
            println("Failed to fetch data.")
        }
        latch.countDown()
    }

    latch.await()
}


fun saveDataToCSV(weatherData: HashMap<Key, List<Double?>>, dateData: HashMap<Key, List<String?>>) {
    val csvFile = "app/src/main/res/weather.csv"
    FileWriter(csvFile).use { writer ->
        writer.append("Latitude,Longitude,Date,Temperature\n")
        weatherData.forEach { (key, temperatures) ->
            dateData[key]?.forEachIndexed { index, date ->
                if (date != null && temperatures[index] != null) {
                    writer.append("${key.latitude},${key.longitude},$date,${temperatures[index]}\n")
                }
            }
        }
    }
    println("Data saved to CSV: $csvFile")
}
