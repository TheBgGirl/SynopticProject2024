package com.example.farmsimulator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.farmsimulator.ui.farm.LocatorPage
import com.example.farmsimulator.ui.farm.PlannerPage
import com.example.farmsimulator.ui.home.HomePage
import com.example.farmsimulator.ui.settings.SettingsPage
import com.google.android.gms.maps.model.LatLng

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val onNavBar: Boolean = true,
    val parent: Screen? = null,
    val children: List<Screen> = emptyList()
) {
    data object Home : Screen(route = "home", title = "Home", icon = Icons.Default.Home)
    data object Locator : Screen(
        route = "locator",
        title = "Farm Locator",
        icon = Icons.Default.AddCircle,
        children = listOf(CropPlanner)
    )

    data object CropPlanner : Screen(
        route = "cropPlanner",
        title = "Crop Planner",
        icon = Icons.Default.AddCircle,
        onNavBar = false,
        parent = Locator
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.AddCircle
    )

    companion object {
        val items = listOf(Home, Locator, CropPlanner, Settings)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomePage()
        }
        composable(Screen.Locator.route) {
            LocatorPage(
                onCropPlannerClick = { height: Double, width: Double, latLng: LatLng ->
                    navController.navigate(
                        "${Screen.CropPlanner.route}?height=$height&width=$width&lat=${latLng.latitude}&long=${latLng.longitude}"
                    )
                }
            )
        }

        composable("${Screen.CropPlanner.route}?height={height}&width={width}&lat={lat}&long={long}",
            arguments = listOf(
                navArgument("height") { type = NavType.StringType; defaultValue = "0.0"; nullable = true },
                navArgument("width") { type = NavType.StringType; defaultValue = "0.0"; nullable = true },
                navArgument("lat") { type = NavType.StringType; defaultValue = "0.0"; nullable = true },
                navArgument("long") { type = NavType.StringType; defaultValue = "0.0"; nullable = true }
            )) {

            val height = it.arguments?.getString("height")?.toDoubleOrNull() ?: 0.0
            val width = it.arguments?.getString("width")?.toDoubleOrNull() ?: 0.0
            val lat = it.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val long = it.arguments?.getString("long")?.toDoubleOrNull() ?: 0.0

            val latLng = LatLng(lat, long)

            PlannerPage(latLng = latLng, height = height, width = width, onBackNavigation = {
                navController.navigate(Screen.Locator.route)
            })
        }

        composable(Screen.Settings.route) {
            SettingsPage()
        }
    }
}

@Composable
fun BottomNav(navController: NavController) {
    val navBarStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBarStackEntry?.destination

    NavigationBar {
        Screen.items.forEach { screen ->
            if (screen.onNavBar) {
                val selected = currentRoute?.hierarchy?.any { it.route == screen.route } == true
                val localeText = stringResource(id = when (screen) {
                    is Screen.Home -> R.string.home_title
                    is Screen.Locator -> R.string.locator_title
                    is Screen.CropPlanner -> R.string.crop_planner_title
                    is Screen.Settings -> R.string.settings_title
                })

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route)
                    },
                    label = {
                        Text(text = localeText, maxLines = 1)
                    },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title
                        )
                    }
                )
            }
        }
    }
}