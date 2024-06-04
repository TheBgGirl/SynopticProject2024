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
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.ui.theme.FarmSimulator
import com.example.farmsimulator.utils.fileExists

// ill fix this
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FarmSimulatorApp(navController: NavHostController = rememberNavController()) {
    FarmSimulator {
        Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNav(navController = navController)
        }) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                FarmSimNavGraph(navController = navController)
                if (!fileExists("./res/user.csv")) {
                    navController.navigate("locator")
                }
            }
        }
    }
}
