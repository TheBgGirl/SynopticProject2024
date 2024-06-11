package com.example.farmsimulator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.models.FarmData
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.ui.theme.FarmSimulator
import com.example.farmsimulator.utils.fileExists
import com.wales.Crop
import com.wales.FarmElement
import com.wales.WeatherPredictor

// ill fix this
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FarmSimulatorApp(navController: NavHostController = rememberNavController(), settingsRepository: SettingsRepository, predictor: WeatherPredictor) {
    FarmSimulator {
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route
        val navigateUp: () -> Unit = {
            navController.navigateUp()
        }
        val canNavigateUp = currentRoute != Screen.Home.route && currentRoute != Screen.Locator.route && currentRoute != Screen.Settings.route

        Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNav(navController = navController)
        }, topBar = {
            TopBar(
                navigateUp = navigateUp,
                canNavigateBack = canNavigateUp
            )
            }

        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                val startDestination = if (fileExists("./res/user.csv")) Screen.Home.route else Screen.Locator.route

                /*
                val getYield: (Double, Double, Int, Int, List<List<Crop>>, Int) -> List<List<FarmElement>> = if (predictor != null) {
                    predictor::evaluateYieldForFarm
                } else {
                    { _, _, _, _, _, _ -> emptyList() }
                }
                 */

                FarmSimNavGraph(navController = navController, startDestination = startDestination, settingsRepository = settingsRepository, getYield = predictor::evaluateYieldForFarm)
            }
        }
    }
}
