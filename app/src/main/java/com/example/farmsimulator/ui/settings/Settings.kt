package com.example.farmsimulator.ui.settings

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.example.farmsimulator.R

val availableLocales = listOf(
    java.util.Locale("en", "US"),
    java.util.Locale("km", "KH"),
)

@Composable
fun SettingsPage() {
    val context = LocalContext.current
    val locale = remember { mutableStateOf(context.resources.configuration.locales[0]) }

    Column(modifier = Modifier
        .padding(16.dp)
        .testTag("settingsPage")) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )
        LocalePicker(locale = locale, context = context)
    }
}

@Composable
fun LocalePicker(
    locale: MutableState<java.util.Locale>,
    modifier: Modifier = Modifier,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = stringResource(
                    id = R.string.current_locale,
                    locale.value.getDisplayName(locale.value)
                )
            )

            Text(
                text = stringResource(id = R.string.select_locale),
                modifier = Modifier.testTag("localePicker").clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.testTag("localeDropdown")
            ) {
                availableLocales.forEach { availableLocale ->
                    DropdownMenuItem(
                        modifier = Modifier.testTag("locale_${availableLocale.language}_${availableLocale.country}"),
                        onClick = {
                            locale.value = availableLocale
                            expanded = false
                            updateLocale(availableLocale)
                        },
                        text = { Text(text = availableLocale.getDisplayName(availableLocale)) }
                    )
                }
            }
        }
    }
}


fun updateLocale(locale: java.util.Locale) {
    val appLocale: LocaleListCompat = LocaleListCompat.create(locale)
    AppCompatDelegate.setApplicationLocales(appLocale)
}


fun restartActivity(context: Context) {
    val intent = Intent(context, context.javaClass)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}