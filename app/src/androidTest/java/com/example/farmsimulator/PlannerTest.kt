package com.example.farmsimulator

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmsimulator.ui.farm.PlannerPage
import com.google.android.gms.maps.model.LatLng
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlannerTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PlannerPage(latLng = LatLng(0.0, 0.0), height = 0, width = 0, settingsRepository = FakeSettingsStore(composeTestRule.activity), toFarmView = {}, cropInfo = emptyList())
            }
        }

    @Test
    fun verify_PlannerPage() {
        composeTestRule.onNodeWithTag("plannerPage").assertExists()
    }

    @Test
    fun verify_AddCropButton() {
        composeTestRule.onNodeWithTag("addCropButton").assertExists()
    }

    @Test
    fun verify_BackButton() {
        composeTestRule.onNodeWithTag("backButton").assertExists()
    }

    @Test
    fun verify_AddCropWorks() {
        composeTestRule.onNodeWithTag("addCropButton").performClick()
        composeTestRule.onNodeWithTag("cropFieldInput").assertExists()
    }
}