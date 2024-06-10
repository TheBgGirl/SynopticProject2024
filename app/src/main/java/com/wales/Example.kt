package com.wales

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val dataPath = "/Users/jamie/Dev/synop/app/src/main/java/com/wales/wd.csv"
    val predictor = WeatherPredictor(dataPath)

    val latitude = 12.532608854954955
    val longitude = 106.28876457344239
    val cropType = Crop.RICE

    val yieldMap = predictor.evaluateYield(latitude, longitude, cropType)

    println("Crop Yield Predictions for $cropType:")
    for ((month, yield) in yieldMap) {
        println("$month: $yield")
    }
}
