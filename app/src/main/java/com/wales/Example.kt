package com.wales

fun main() {
    val weatherDataFetcher = WeatherDataFetcher()
    val boundingBox = "12.3276539,106.684917,12.8276539,107.184917"
    val startDate = "2024-05-19"
    val endDate = "2024-06-02"

    weatherDataFetcher.fetchWeatherData(boundingBox, startDate, endDate)
}