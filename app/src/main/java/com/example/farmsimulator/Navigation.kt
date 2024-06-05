package com.example.farmsimulator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
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
    @StringRes val title: Int,
    val icon: ImageVector,
    val onNavBar: Boolean = true,
    val testTag: String = route,
    val parent: Screen? = null,
    val children: List<Screen> = emptyList()
) {
    data object Home : Screen(
        route = "home",
        title = R.string.home_title,
        icon = Icons.Default.Home,
        testTag = "homeButton"
    )

    data object Locator : Screen(
        route = "locator",
        title = R.string.locator_title,
        icon = Icons.Default.AddCircle,
        children = listOf(CropPlanner),
        testTag = "locatorButton"
    )

    data object CropPlanner : Screen(
        route = "cropPlanner",
        title = R.string.crop_planner_title,
        icon = Icons.Default.AddCircle,
        onNavBar = false,
        parent = Locator
    )

    data object Settings : Screen(
        route = "settings",
        title = R.string.settings_title,
        icon = Icons.Default.AddCircle,
        testTag = "settingsButton"
    )

    private object Initializer {
        val items = listOf(Home, Locator, CropPlanner, Settings)
    }

    companion object {
        val items: List<Screen> by lazy { Initializer.items }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FarmSimNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Home.route) {
            HomePage()
        }
        composable(Screen.Locator.route) {
            LocatorPage(
                onCropPlannerClick = { height: Int, width: Int, latLng: LatLng ->
                    navController.navigate(
                        "${Screen.CropPlanner.route}?height=$height&width=$width&lat=${latLng.latitude}&long=${latLng.longitude}"
                    )
                }
            )
        }

        composable(route = "${Screen.CropPlanner.route}?height={height}&width={width}&lat={lat}&long={long}",
            arguments = listOf(
                navArgument("height") {
                    type = NavType.StringType; defaultValue = "0.0"; nullable = true
                },
                navArgument("width") {
                    type = NavType.StringType; defaultValue = "0.0"; nullable = true
                },
                navArgument("lat") {
                    type = NavType.StringType; defaultValue = "0.0"; nullable = true
                },
                navArgument("long") {
                    type = NavType.StringType; defaultValue = "0.0"; nullable = true
                }
            )) {

            val height = it.arguments?.getString("height")?.toIntOrNull() ?: 0
            val width = it.arguments?.getString("width")?.toIntOrNull() ?: 0
            val lat = it.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val long = it.arguments?.getString("long")?.toDoubleOrNull() ?: 0.0

            val latLng = LatLng(lat, long)

            PlannerPage(latLng = latLng, height = height, width = width, onBackNavigation = {
                navController.navigate(Screen.Locator.route)
            })
        }

        composable(route = Screen.Settings.route) {
            SettingsPage()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    navigateUp: () -> Unit
) {
    val title = stringResource(id = R.string.app_name)

    TopAppBar(title = {
        Text(text = title)
    },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        }, modifier = modifier
    )
}

@Composable
fun BottomNav(navController: NavController) {
    val navBarStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBarStackEntry?.destination

    NavigationBar {
        Screen.items.forEach { screen ->
            if (screen.onNavBar) {
                val selected = currentRoute?.hierarchy?.any { it.route == screen.route } == true
                NavigationBarItem(
                    modifier = Modifier.testTag(screen.testTag),
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route)
                    },
                    label = {
                        Text(text = stringResource(id = screen.title), maxLines = 1)
                    },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(id = screen.title)
                        )
                    }
                )
            }
        }
    }
}