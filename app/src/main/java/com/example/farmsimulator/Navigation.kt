package com.example.farmsimulator

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.models.FarmData
import com.example.farmsimulator.models.FarmDataViewModel
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.ui.farm.CropInfo
import com.example.farmsimulator.ui.farm.CropTypes
import com.example.farmsimulator.ui.farm.FarmView
import com.example.farmsimulator.ui.farm.LocatorPage
import com.example.farmsimulator.ui.farm.PlannerPage
import com.example.farmsimulator.ui.farm.ResultsPage
import com.example.farmsimulator.ui.home.HomePage
import com.example.farmsimulator.ui.settings.SettingsPage
import com.google.android.gms.maps.model.LatLng
import com.wales.Crop
import com.wales.FarmElement

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
        icon = Icons.Default.LocationOn,
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

    data object FarmView : Screen(
        route = "farmView",
        title = R.string.farm_view_title,
        icon = Icons.Default.AddCircle,
        onNavBar = false,
        parent = CropPlanner
    )

    data object Results : Screen(
        route = "results",
        title = R.string.results_title,
        icon = Icons.Default.AddCircle,
        onNavBar = false,
        parent = FarmView
    )

    data object Settings : Screen(
        route = "settings",
        title = R.string.settings_title,
        icon = Icons.Default.Settings,
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
    startDestination: String = Screen.Home.route,
    settingsRepository: SettingsRepository,
    getYield: (Double, Double, Int, Int, List<List<Crop>>) -> List<List<List<FarmElement>>> = { _, _, _, _, _ -> emptyList() }
) {
    val farmInfoViewModel: FarmDataViewModel = viewModel(modelClass = FarmDataViewModel::class.java)
    val ecoMode by settingsRepository.ecoModeFlow.collectAsState(initial = false)

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Screen.Home.route) {
            HomePage(settingsRepository)
        }
        composable(Screen.Locator.route) {
            LocatorPage(previousFarms = farmInfoViewModel.savedFarms.value.orEmpty(),
                onCropPlannerClick = { height: Int, width: Int, latLng: LatLng, cropInfo: List<CropInfo> ->
                    farmInfoViewModel.updateFarmData(FarmData(width, height, cropInfo, latLng))
                    navController.navigate(Screen.CropPlanner.route)
                }, settingsRepository = settingsRepository
            )
        }

        composable(route = Screen.CropPlanner.route) {
            val farmData = farmInfoViewModel.currentFarmData.value
            val height = farmData?.height ?: 0
            val width = farmData?.width ?: 0
            val latLng = farmData?.latLong ?: LatLng(0.0, 0.0)
            val cropInfo = farmData?.crops.orEmpty()

            PlannerPage(
                latLng = latLng, height = height, width = width, cropInfo = cropInfo,
            settingsRepository = settingsRepository, toFarmView = { crops ->
                val cropLayout = parseCrops(crops, width, height)
                farmInfoViewModel.updateYield(getYield(latLng.latitude, latLng.longitude, width, height, cropLayout))
                farmInfoViewModel.updateFarmData(FarmData(width, height, crops, latLng))
                navController.navigate(Screen.FarmView.route)
            })
        }

        composable(route = Screen.FarmView.route) {
            val farmData = farmInfoViewModel.currentFarmData.value
            val height = farmData?.height ?: 0
            val width = farmData?.width ?: 0
            val latLng = farmData?.latLong ?: LatLng(0.0, 0.0)
            val crops = farmData?.crops.orEmpty()
            val yield = farmInfoViewModel.yield.value.orEmpty()

            FarmView(latLng = latLng, width = width, height = height, crops = crops, toResults = {
                farmInfoViewModel.saveFarmData(FarmData(width, height, crops, latLng))
                navController.navigate(Screen.Results.route)
            }, settingsRepository, ecoMode = ecoMode, yield = yield)
        }

        composable(route = Screen.Results.route) {
            val farmData = farmInfoViewModel.currentFarmData.value
            val height = farmData?.height ?: 0
            val width = farmData?.width ?: 0
            val latLng = farmData?.latLong ?: LatLng(0.0, 0.0)
            val crops = farmData?.crops.orEmpty()

            ResultsPage(width = width, height = height, latLng = latLng, crops = crops)
        }

        composable(route = Screen.Settings.route) {
            SettingsPage(settingsRepository)
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

/*
enum class Crop {
    RICE,
    PUMPKIN,
    LEAFY,
    NA
}
2d list of above for each cell in farm
 */

fun parseCrops(crops: List<CropInfo>, width: Int, height: Int): List<List<Crop>> {
    val cropLayout = MutableList(height) { MutableList(width) { Crop.NA } }


        for (i in 0 until height) {
            for (j in 0 until width) {
                val crop = crops.firstOrNull() { it.x == j && it.y == i }
                val cropEnumType = when (crop?.cropType) {
                    CropTypes.Rice -> Crop.RICE
                    CropTypes.Pumpkins -> Crop.PUMPKIN
                    CropTypes.LeafyGreens -> Crop.LEAFY
                    else -> Crop.NA
                }
                cropLayout[i][j] = cropEnumType
            }
        }

    return cropLayout

        /*
    for (crop in crops) {
        val x = crop.x
        val y = crop.y
        val cropType = crop.cropType
        val cropEnum = when (cropType) {
            CropTypes.Rice -> Crop.RICE
            CropTypes.Pumpkins -> Crop.PUMPKIN
            CropTypes.LeafyGreens -> Crop.LEAFY
            else -> Crop.NA
        }
        cropLayout[y][x] = cropEnum
    }
         */
}