package com.wales

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    // val predictor = WeatherPredictor.deserialize()
    val dataPath = "/Users/jacobedwards/University/Year2/synoptic/fuck/SynopticProject2024/app/src/main/java/com/wales/wd.csv"
    val modelPath = "/Users/jacobedwards/University/Year2/synoptic/fuck/SynopticProject2024/app/src/main/java/com/wales/models"
    val predictor = WeatherPredictor(dataPath, modelPath)

    val latitude = 12.532608854954955
    val longitude = 106.28876457344239
    val numRows = 3
    val numCols = 3
    val plantTypes = listOf(
        listOf(Crop.NA, Crop.PUMPKIN, Crop.LEAFY),
        listOf(Crop.LEAFY, Crop.RICE, Crop.PUMPKIN),
        listOf(Crop.PUMPKIN, Crop.LEAFY, Crop.RICE)
    )

    val yieldMap = predictor.evaluateYieldForFarm(latitude, longitude, numRows, numCols, plantTypes, 1)

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            println("Cell ($row, $col):")
            for (month in 0 until 12) {
                println("Month ${month + 1}: ${yieldMap[row][col]}%")
            }
        }
    }
    // predictor.serialize()
    // predictor.serialiseToJson()
}
