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
    fun setupNavHost() {
        composeTestRule.setContent {
            context = LocalContext.current
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            FarmSimNavGraph(navController = navController)
        }
    }

    @Test
    fun farmSimulatorApp_verifyStartDestination() {
        composeTestRule.onNodeWithTag("homePage").assertExists()
    }

    @Test
    fun farmSimulatorApp_verifyHomePage() {
        composeTestRule.onNodeWithText(context.getString(R.string.home_welcome)).assertExists()
    }

    @Test
    fun farmSimulatorApp_verifyLocatorPage() {
        navController.navigate("locator")
        composeTestRule.onNodeWithTag("locatorPage").assertExists()
    }

}