package com.example.farmsimulator

import android.annotation.SuppressLint
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
     private lateinit var navController: TestNavHostController

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(composeTestRule.activity)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            Scaffold (bottomBar = {
                BottomNav(navController = navController)
            }) {
                FarmSimNavGraph(navController = navController)
            }
        }
    }

    @Test
    fun verify_StartDestinationIsHomePage() {
        composeTestRule.onNodeWithTag("homePage").assertExists()
    }

    @Test
    fun verify_NavigateToSettingsPage() {
        composeTestRule.onNodeWithTag(Screen.Settings.testTag).performClick()
        composeTestRule.onNodeWithTag("settingsPage").assertExists()
    }

    fun verify_NavigateToLocatorPage() {
        composeTestRule.onNodeWithTag("locatorPage").assertExists()
    }
}