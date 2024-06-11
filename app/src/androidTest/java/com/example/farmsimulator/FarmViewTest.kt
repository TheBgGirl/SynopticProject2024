package com.example.farmsimulator

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.farmsimulator.ui.farm.FarmView
import com.example.farmsimulator.ui.settings.SettingsPage
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FarmViewTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            FarmView(latLng = LatLng(0.0, 0.0), width = 100, height = 100, crops = emptyList(), toResults = {}, settingsRepository = FakeSettingsStore(composeTestRule.activity), ecoMode = false)
        }
    }
}