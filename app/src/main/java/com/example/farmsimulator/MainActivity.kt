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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.utils.fileExists
import com.wales.WeatherPredictor
import com.wales.deserialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import smile.regression.RandomForest
import java.io.FileInputStream
import java.io.ObjectInputStream

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Get the path to the weather data file
        val path = getPath(R.raw.wd, this, "wd.csv")
        super.onCreate(savedInstanceState)

        // Initialize the settings repository
        val settingsRepository = SettingsRepository(this)

        // Set the Compose content for the activity
        setContent {
            // Mutable state to hold the WeatherPredictor instance
            var predictor by remember { mutableStateOf<WeatherPredictor?>(null) }

            // Launch a coroutine to initialize the WeatherPredictor
            LaunchedEffect(Unit) {
                lifecycleScope.launch(Dispatchers.IO) {
                    predictor = WeatherPredictor(path, filesDir.path + "/models")
                }
            }

            // Pass the settings repository and predictor to the FarmSimulatorApp Composable
            FarmSimulatorApp(settingsRepository = settingsRepository, predictor = predictor)
        }
    }
}

/**
 * Get the path to a file in the app's internal storage. If the file does not exist,
 * copy it from the raw resources.
 *
 * @param model Resource ID of the raw resource
 * @param activity The activity context
 * @param filename The name of the file in internal storage
 * @return The path to the file in internal storage
 */
fun getPath(@RawRes model: Int, activity: ComponentActivity, filename: String): String {
    val modelPath = activity.filesDir.path + "/" + filename
    // Check if the file exists in internal storage
    if (!fileExists(modelPath)) {
        // Copy the file from raw resources to internal storage
        activity.resources.openRawResource(model).use { input ->
            activity.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE).use { output ->
                input.copyTo(output)
            }
        }
    }
    return modelPath
}

/**
 * Deserialize an object from a file.
 *
 * @param T The type of the object to deserialize
 * @param modelPath The path to the file
 * @return The deserialized object
 */
fun <T> deserialize(modelPath: String): T {
    return ObjectInputStream(FileInputStream(modelPath)).use { it.readObject() as T }
}
