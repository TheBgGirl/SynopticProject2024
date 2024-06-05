package com.example.farmsimulator.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.farmsimulator.R

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    onValueError: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon : @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            onValueError("")
        },
        label = {
            val text = if (error.isNotEmpty()) stringResource(
                id = R.string.input_error,
                label,
                error
            ) else label
            Text(text = text)
        },
        modifier = modifier,
        isError = error.isNotEmpty(),
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTextField(
    modifier: Modifier = Modifier,
    selectedValue: String = "",
    onValueChange: (String) -> Unit,
    label: String,
    selectOptions: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
        expanded = it
    }, modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            selectOptions.forEach { option: String ->
                DropdownMenuItem(onClick = {
                    onValueChange(option)
                    expanded = false
                }, text = {
                    Text(option)
                })
            }
        }
    }
}

@Composable
fun SelectTextField(
    modifier: Modifier = Modifier,
    @StringRes selectedValue: Int,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    selectOptions: List<Int>,
) {
    SelectTextField(
        modifier = modifier,
        selectedValue = stringResource(id = selectedValue),
        onValueChange = onValueChange,
        label = stringResource(id = label),
        selectOptions = selectOptions.map { stringResource(id = it) }
    )
}

