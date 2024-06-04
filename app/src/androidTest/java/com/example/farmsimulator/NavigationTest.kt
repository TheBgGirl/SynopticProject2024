package com.example.farmsimulator

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraph
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var context: Context

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            context = LocalContext.current
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            FarmSimNavGraph(navController = navController)
        }
    }

    @Test
    fun farmSimulatorApp_verifyStartDestination() {
        composeTestRule.onNodeWithTag("homePage").assertExists()
    }

    /*
    @Test
    fun farmSimulatorApp_verifyLocatorPage() {
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Locator.route)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("locatorPage").assertExists()
    }
     */


    @Test
    fun farmSimulatorApp_verifySettingsPage() {
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Settings.route)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settingsPage").assertExists()
    }

    /*
    @Test
    fun farmSimulatorApp_verifyHomeButton() {
        navController.navigate(Screen.Locator.route)
        composeTestRule.onNodeWithStringId(R.string.home_title).performClick()
        navController.assertCurrentRouteName(Screen.Home.route)
    }

    @Test
    fun farmSimulatorApp_verifyLocatorButton() {
        navController.navigate(Screen.Home.route)
        composeTestRule.onNodeWithStringId(R.string.locator_title).performClick()
        navController.assertCurrentRouteName(Screen.Locator.route)
    }

    @Test
    fun farmSimulatorApp_verifySettingsButton() {
        navController.navigate(Screen.Home.route)
        composeTestRule.onNodeWithStringId(R.string.settings_title).performClick()
        navController.assertCurrentRouteName(Screen.Settings.route)
    }

     */
}