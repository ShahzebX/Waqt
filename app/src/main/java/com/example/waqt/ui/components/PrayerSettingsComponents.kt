package com.example.waqt.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waqt.repository.PrayerRepository
import com.example.waqt.ui.theme.GoldGlow
import com.example.waqt.ui.theme.GoldSoft
import com.example.waqt.ui.theme.PrimaryNavy
import com.example.waqt.ui.theme.SecondaryGold

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        WaqtSectionTitle(text = "Calculation method")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(calculationMethodOptions, key = { it.id }) { method ->
                val selected = selectedMethod == method.id
                FilterChip(
                    selected = selected,
                    onClick = { onMethodChange(method.id) },
                    label = {
                        Text(
                            text = method.label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = GoldGlow,
                        selectedLabelColor = PrimaryNavy
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = SecondaryGold,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledSelectedBorderColor = SecondaryGold
                    )
                )
            }
        }
    }
}
