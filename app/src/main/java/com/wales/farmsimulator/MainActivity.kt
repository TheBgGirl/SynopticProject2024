package com.wales.farmsimulator

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.wales.farmsimulator.ui.theme.FarmSimulatorTheme
import com.google.android.gms.location.FusedLocationProviderClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmSimulatorTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "landing") {
                    composable("landing") { LandingPage(navController) }
                    composable("DetailsScreen") { DetailsScreen(navController)}
                }
            }
        }
    }
}

@Composable
fun LandingPage(navController: NavController) {
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
        Text(
            text = "Welcome to Farm Simulator, your essential tool for strategic farming and flood management. Through our intuitive grid-based layout, you can easily identify which areas of your farm are most susceptible to flooding and how often they are affected. Plan your crop placements with precision, as our simulator calculates potential yields based on your management decisions. Gain insights into water risks and optimize your agricultural output by understanding the dynamics of your land. Begin your journey with Farm Simulator today and turn your farming challenges into opportunities for innovation!",
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("DetailsScreen") },
            modifier = Modifier.testTag("navigateButton")
        ) {
            Text("Go To Form")
        }
    }
}

@Composable
fun DetailsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val (height, setHeight) = remember { mutableStateOf("") }
    val (width, setWidth) = remember { mutableStateOf("") }

    val (location, setLocation) = remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    // Ensure location is not null
                    loc?.let {
                        setLocation("Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
                    } ?: setLocation("Location not available")
                }.addOnFailureListener {
                    setLocation("Failed to get location")
                }
            } else {
                setLocation("Permission Denied")
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Farm Dimensions")

        OutlinedTextField(
            value = height,
            onValueChange = setHeight,
            label = { Text("Height") },
            modifier = Modifier
                .padding(8.dp)
        )

        OutlinedTextField(
            value = width,
            onValueChange = setWidth,
            label = { Text("Width") },
            modifier = Modifier
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Location")
        }

        Text(location)
        Spacer(modifier = Modifier.height(16.dp))
    }
}