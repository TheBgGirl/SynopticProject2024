package com.wales.farmsimulator

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLandingPageDisplaysCorrectly() {
        composeTestRule.onNodeWithTag("welcomeText").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome to Farm Sim").assertIsDisplayed()
        composeTestRule.onNodeWithTag("landingImage").assertIsDisplayed()
    }
}
