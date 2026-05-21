package com.example.waqt.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.waqt.repository.PrayerRepository

data class CalculationMethodOption(
    val id: Int,
    val label: String
)

val calculationMethodOptions = listOf(
    CalculationMethodOption(
        id = PrayerRepository.METHOD_KARACHI,
        label = "Karachi"
    ),
    CalculationMethodOption(
        id = PrayerRepository.METHOD_ISNA,
        label = "ISNA"
    ),
    CalculationMethodOption(
        id = PrayerRepository.METHOD_MWL,
        label = "MWL"
    )
)

@Composable
fun CalculationMethodSelector(
    selectedMethod: Int,
    onMethodChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Calculation method",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(calculationMethodOptions, key = { it.id }) { method ->
                FilterChip(
                    selected = selectedMethod == method.id,
                    onClick = { onMethodChange(method.id) },
                    label = { Text(text = method.label) }
                )
            }
        }
    }
}
