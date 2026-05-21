package com.example.waqt.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waqt.ui.theme.CardShape
import com.example.waqt.ui.theme.GoldBright
import com.example.waqt.ui.theme.GoldGlow
import com.example.waqt.ui.theme.GoldSoft
import com.example.waqt.ui.theme.HeroCardShape
import com.example.waqt.ui.theme.NavBarShape
import com.example.waqt.ui.theme.NavyDeep
import com.example.waqt.ui.theme.NavyMid
import com.example.waqt.ui.theme.OutlineSoft
import com.example.waqt.ui.theme.PrimaryNavy
import com.example.waqt.ui.theme.SecondaryGold
import com.example.waqt.ui.theme.SurfaceGlass

@Composable
fun WaqtGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEFF6FF),
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x33D97706),
                            Color.Transparent
                        ),
                        radius = 500f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x120F172A),
                            Color.Transparent,
                            Color(0x08DBEAFE)
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun WaqtScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "وقت",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryGold,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            badge?.let {
                Surface(
                    shape = CircleShape,
                    color = GoldGlow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

enum class WaqtCardVariant { Glass, Elevated, Hero }

@Composable
fun WaqtCard(
    modifier: Modifier = Modifier,
    variant: WaqtCardVariant = WaqtCardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = when (variant) {
        WaqtCardVariant.Hero -> HeroCardShape
        else -> CardShape
    }
    val containerColor = when (variant) {
        WaqtCardVariant.Glass -> SurfaceGlass
        WaqtCardVariant.Hero -> Color.Transparent
        WaqtCardVariant.Elevated -> MaterialTheme.colorScheme.surface
    }
    val elevation = when (variant) {
        WaqtCardVariant.Hero -> 12.dp
        WaqtCardVariant.Elevated -> 6.dp
        WaqtCardVariant.Glass -> 2.dp
    }
    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(elevation, shape, clip = false)
        .clip(shape)
        .then(
            if (variant == WaqtCardVariant.Hero) {
                Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(NavyDeep, NavyMid, PrimaryNavy)
                    )
                )
            } else {
                Modifier.background(containerColor)
            }
        )
        .border(
            width = 1.dp,
            color = if (variant == WaqtCardVariant.Hero) {
                GoldSoft.copy(alpha = 0.35f)
            } else {
                OutlineSoft
            },
            shape = shape
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )

    Column(
        modifier = baseModifier.padding(horizontal = 20.dp, vertical = 20.dp),
        content = content
    )
}

@Composable
fun WaqtPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = CardShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryGold,
            contentColor = PrimaryNavy,
            disabledContainerColor = GoldSoft.copy(alpha = 0.4f),
            disabledContentColor = PrimaryNavy.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WaqtSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
    )
}

@Composable
fun WaqtEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    WaqtCard(modifier = modifier, variant = WaqtCardVariant.Glass) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WaqtLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = SecondaryGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WaqtPulsingDot(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "dot")
    val pulse by transition.animateFloat(
        initialValue = if (active) 0.85f else 1f,
        targetValue = if (active) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val color by animateColorAsState(
        targetValue = if (active) GoldBright else PrimaryNavy,
        animationSpec = tween(300),
        label = "dotColor"
    )
    Box(
        modifier = modifier
            .scale(if (active) pulse else 1f)
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (active) {
                    Modifier.border(2.dp, GoldSoft.copy(alpha = 0.6f), CircleShape)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun WaqtFloatingNavBar(
    destinations: List<Triple<String, String, ImageVector>>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(16.dp, NavBarShape),
        shape = NavBarShape,
        color = SurfaceGlass,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { (route, label, icon) ->
                val selected = currentRoute == route
                val itemColor by animateColorAsState(
                    targetValue = if (selected) SecondaryGold else TextMuted,
                    animationSpec = tween(250),
                    label = "navColor"
                )
                Column(
                    modifier = Modifier
                        .clip(CardShape)
                        .clickable { onNavigate(route) }
                        .background(
                            if (selected) GoldGlow else Color.Transparent,
                            CardShape
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PrimaryNavy else TextMuted,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private val TextMuted = com.example.waqt.ui.theme.TextMuted
