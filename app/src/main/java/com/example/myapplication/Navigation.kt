package com.example.myapplication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class Screen(val title: String) {
    Home("Home"),
    Profile("Profile"),
    Settings("Settings")
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
                            Screen.Home -> Icons.Default.Home
                            Screen.Profile -> Icons.Default.Person
                            Screen.Settings -> Icons.Default.Settings
                        },
                        contentDescription = screen.title
                    )
                }
            )
        }
    }
}