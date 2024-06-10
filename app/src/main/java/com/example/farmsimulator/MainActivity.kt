package com.example.farmsimulator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.utils.fileExists
import com.wales.WeatherPredictor

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
        setContent {
            FarmSimulatorApp(settingsRepository = settingsRepository)
        }
    }
}



