package com.example.farmsimulator.ui.home

import android.content.Intent
import android.provider.Settings.Global.getString
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmsimulator.R
import com.example.farmsimulator.opengl.OpenGLActivity
import com.example.farmsimulator.opengl.OpenGLComposeView
import com.example.farmsimulator.stores.SettingsRepository

@Composable
fun HomePage(settingsRepository: SettingsRepository) {
    val scrollState = rememberScrollState()
    var show by remember {
        mutableStateOf(false)
    }
    var selected by remember {
        mutableStateOf<Pair<Int,Int>>(Pair(0,0))
    }
    if (show) {
        Column(modifier = Modifier.fillMaxSize()) {
            OpenGLComposeView(width = 5, height = 5, crops = listOf(), onClick = { x, y ->
                selected = x to y;
            })

        }} else {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("homePage"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {




            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.home_welcome),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 2.dp)
                            .testTag("welcomeText")
                    )
                    Text(text = "Clicked ${selected.first} ${selected.second}")
                    Image(
                        painter = painterResource(id = R.drawable.hero),
                        contentDescription = "Landing Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 2.dp)
                            .testTag("landingImage")
                    )
                    Text(
                        text = stringResource(id = R.string.home_welcome2),
                        style = TextStyle(fontSize = 20.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 2.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.home_intro),
                        style = TextStyle(fontSize = 20.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 2.dp)
                    )


                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val intent = Intent(context, OpenGLActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("navigateButton")
                    ) {
                        Text("Go to Simulation")
                    }
                }
            }
        }

    }
    Button(onClick = {
        show = !show
    }) {
        Text(text = "show")
    }

}

