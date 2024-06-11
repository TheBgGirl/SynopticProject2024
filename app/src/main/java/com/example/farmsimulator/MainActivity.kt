package com.example.farmsimulator

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.utils.fileExists
import com.wales.WeatherPredictor
import com.wales.deserialize
import smile.regression.RandomForest
import java.io.FileInputStream
import java.io.ObjectInputStream

class MainActivity : AppCompatActivity() {
    private lateinit var predictor: WeatherPredictor

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        /*
        val sunshineModelSer = R.raw.sunshine_model
        val temperatureModelSer = R.raw.temp_model
        val rainfallModelSer = R.raw.rainfall_model

        // deserialize the models
        val sunshineModelPath = getPath(sunshineModelSer, this, "/sunshine_model.ser")
        val temperatureModelPath = getPath(temperatureModelSer, this, "/temp_model.ser")
        val rainfallModelPath = getPath(rainfallModelSer, this, "/rainfall_model.ser")

        val sunshineModel = deserialize<RandomForest>(sunshineModelPath)
         */
        val path = getPath(R.raw.wd, this, "/wd.csv")
        predictor = WeatherPredictor(path , filesDir.path + "/models")

        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
        setContent {
            FarmSimulatorApp(settingsRepository = settingsRepository, predictor = predictor)
        }
    }
}

fun getPath(@RawRes model: Int, activity: ComponentActivity, filename: String): String {
    val modelPath = activity.filesDir.path + filename
    if (!fileExists(modelPath)) {
        activity.resources.openRawResource(model).use { input ->
            activity.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE).use { output ->
                input.copyTo(output)
            }
        }
    }
    return modelPath
}


fun <T> deserialize(modelPath: String): T {
    return ObjectInputStream(FileInputStream(modelPath)).use { it.readObject() as T }
}
