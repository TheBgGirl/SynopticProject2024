package com.example.farmsimulator

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.utils.fileExists
import com.wales.WeatherPredictor
import com.wales.deserialize

class MainActivity : AppCompatActivity() {
    private lateinit var predictor: WeatherPredictor

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        predictor = WeatherPredictor.deserialize(R.raw.weather_predictor, this)

        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
        setContent {
            FarmSimulatorApp(settingsRepository = settingsRepository)
        }
    }
}



