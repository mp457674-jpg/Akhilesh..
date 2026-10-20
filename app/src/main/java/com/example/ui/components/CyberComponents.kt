package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardNavyBorder,
    backgroundColor: Color = CardNavy,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        color = backgroundColor
    ) {
        content()
    }
}

@Composable
fun HudNodeButton(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .background(CardNavy.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
    }
}

@Composable
fun VoiceWaveVisualizer(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    barCount: Int = 18,
    activeColor: Color = CyberCyan,
    maxHeight: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceBars")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BarOffset"
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val fraction = if (isActive) {
                val wave = kotlin.math.sin((i.toFloat() / barCount.toFloat() * Math.PI * 2) + (animOffset * Math.PI)).toFloat()
                (0.2f + kotlin.math.abs(wave) * 0.8f).coerceIn(0.15f, 1f)
            } else {
                0.15f
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(maxHeight * fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isActive) activeColor else TextMuted.copy(alpha = 0.4f))
            )
        }
    }
}
