package com.example.farmsimulator

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule

fun <A : AppCompatActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.onNodeWithStringId(@StringRes id: Int): SemanticsNodeInteraction = onNodeWithText(activity.getString(id))