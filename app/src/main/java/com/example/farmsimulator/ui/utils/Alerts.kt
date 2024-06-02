package com.example.farmsimulator.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
@Preview
fun Notification(
    modifier: Modifier = Modifier,
    message: String = "This is a notification",
    backgroundColor: Color = Color.Gray,
    textColor: Color = Color.White,
    duration: Long = 3000L,
    onEnd: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(duration)
        isVisible = false
        onEnd()
    }

    AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
        NotificationContent(
            modifier = modifier,
            message = message,
            backgroundColor = backgroundColor,
            textColor = textColor
        )
    }
}

@Composable
private fun NotificationContent(
    modifier: Modifier = Modifier,
    message: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Text(
            text = message,
            color = textColor
        )
    }
}