package com.example.farmsimulator

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.farmsimulator.ui.settings.SettingsPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            SettingsPage(settingsRepository = FakeSettingsStore(composeTestRule.activity))
        }
    }

    @Test
    fun verify_SettingsPage() {
        composeTestRule.onNodeWithTag("settingsPage").assertExists()
    }

    @Test
    fun verify_LocalePicker() {
        composeTestRule.onNodeWithTag("localePicker").performClick()
        composeTestRule.onNodeWithTag("localeDropdown").assertExists()
    }
}