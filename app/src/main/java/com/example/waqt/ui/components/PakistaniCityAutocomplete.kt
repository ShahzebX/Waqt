package com.example.waqt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

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
            enabled = enabled
        )
        if (suggestions.isNotEmpty() && enabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(suggestions, key = { it }) { city ->
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(city) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("$PakistaniCitySuggestionTag:$city")
                        )
                    }
                }
            }
        }
    }
}
