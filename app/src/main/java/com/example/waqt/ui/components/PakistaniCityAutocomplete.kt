package com.example.waqt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waqt.ui.theme.CardShape
import com.example.waqt.ui.theme.OutlineSoft
import com.example.waqt.ui.theme.SecondaryGold

internal const val PakistaniCitySuggestionTag = "pakistani_city_suggestion"

@Composable
fun PakistaniCityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "City",
    placeholder: String = "e.g. Karachi",
    enabled: Boolean = true,
    fieldTestTag: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (fieldTestTag != null) {
                        Modifier.testTag(fieldTestTag)
                    } else {
                        Modifier
                    }
                ),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            supportingText = {
                Text(
                    text = "Type to search all Pakistan cities, then tap a suggestion",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine = true,
            enabled = enabled,
            shape = CardShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SecondaryGold,
                focusedLabelColor = SecondaryGold,
                cursorColor = SecondaryGold
            )
        )
        if (suggestions.isNotEmpty() && enabled) {
            WaqtCard(
                modifier = Modifier.padding(top = 8.dp),
                variant = WaqtCardVariant.Glass
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(suggestions, key = { it }) { city ->
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(city) }
                                .padding(vertical = 10.dp)
                                .testTag("$PakistaniCitySuggestionTag:$city")
                        )
                    }
                }
            }
        }
    }
}
