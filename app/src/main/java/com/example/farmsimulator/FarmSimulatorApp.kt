package com.example.farmsimulator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.ui.theme.FarmSimulator
import com.example.farmsimulator.utils.fileExists

@Composable
fun FarmSimulatorApp() {
    FarmSimulator {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
            BottomNav(navController = navController)
        }) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavGraph(navController = navController)
                if (!fileExists("./res/user.csv")) {
                    navController.navigate("locator")
                }
            }
        }
    }
}
