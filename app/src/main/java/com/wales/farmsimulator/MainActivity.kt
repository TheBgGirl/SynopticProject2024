package com.wales.farmsimulator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wales.farmsimulator.ui.theme.FarmSimulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FarmSimulatorTheme{
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "landing") {
                    composable("landing") { LandingPage(navController) }
                }
            }
        }
    }
}

@Composable
fun LandingPage(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Farm Sim",
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("welcomeText")
        )
        Image(
            painter = painterResource(id = R.drawable.hero),
            contentDescription = "Landing Image",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("landingImage")
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                        val intent = Intent(context, Obj3DView::class.java)
                        context.startActivity(intent)
                      },
            modifier = Modifier.testTag("navigateButton")
        ) {
            Text("Go to Simulation")
        }
    }
}