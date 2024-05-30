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

enum class Screen(val title: String) {
    HOME("Home"),
    MAP("Map"),
}

@Composable
fun BottomNav(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar {
        Screen.entries.forEach { screen ->
            val selected = selectedScreen == screen

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onScreenSelected(screen)
                },
                label = {
                    Text(text = screen.title)
                },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.HOME -> Icons.Default.Home
                            Screen.MAP -> Icons.Default.AddCircle
                        },
                        contentDescription = screen.title
                    )
                }
            )
        }
    }
}