package com.example.farmsimulator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Map : Screen("map", "Map")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            LandingPage()
        }
        composable(Screen.Map.route) {
            DetailsScreen()
        }
    }
}



@Composable
fun BottomNav(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Map
    )
    val navBarStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBarStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            val selected = currentRoute == screen.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                label = {
                    Text(text = screen.title)
                },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Map -> Icons.Default.AddCircle
                        },
                        contentDescription = screen.title
                    )
                }
            )
        }
    }
}
