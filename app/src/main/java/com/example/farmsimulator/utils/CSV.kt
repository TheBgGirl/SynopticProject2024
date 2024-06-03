package com.example.farmsimulator.utils

import java.io.File
import java.io.OutputStream

data class Farm(
    val long: Double,
    val lat: Double,
    val height: Int,
    val width: Int
)

fun OutputStream.writeCsv(farm: Farm) {
    val writer = bufferedWriter()
    writer.write("$farm")
    writer.flush()
}


fun fileExists(filePath:String):Boolean {
    val file = File(filePath)
    return file.exists()
}


