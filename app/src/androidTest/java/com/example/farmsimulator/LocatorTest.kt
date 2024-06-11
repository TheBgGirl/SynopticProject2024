package com.example.farmsimulator

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.farmsimulator.ui.farm.FarmView
import com.example.farmsimulator.ui.farm.LocatorPage
import com.example.farmsimulator.ui.home.HomePage
import com.example.farmsimulator.ui.settings.SettingsPage
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocatorTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            LocatorPage(settingsRepository = FakeSettingsStore(composeTestRule.activity), onCropPlannerClick = {_, _, _, _->}, previousFarms = emptyList())
        }
    }
}