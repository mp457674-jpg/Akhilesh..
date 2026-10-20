package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.IntelHeadline
import com.example.ui.components.CyberCard
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SatLinkIntelScreen(
    headlines: List<IntelHeadline>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Sat-Link Feed Header
        Text(
            text = "SAT-LINK FEED • SATELLITE STREAM",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Radar Stream Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MatrixGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ORBITAL TELEMETRY LOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MatrixGreen
                        )
                    }

                    Text(
                        text = "LAT: 28.6139° N  LON: 77.2090° E",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                TacticalRadarCanvas(modifier = Modifier.size(200.dp))

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IntelStat(label = "SAT PING", value = "18 ms")
                    IntelStat(label = "UPLINK", value = "1.2 Gbps")
                    IntelStat(label = "NODE STATUS", value = "OPTIMAL")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Today Headlines / Intel Feed
        Text(
            text = "TODAY HEADLINES • GLOBAL INTEL",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            headlines.forEachIndexed { index, item ->
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "0${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (item.severity) {
                                        "WARN" -> WarningRed
                                        "ALERT" -> NeonAmber
                                        else -> MatrixGreen
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TacticalRadarCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.9f

        // Background circle
        drawCircle(
            color = CardNavy,
            radius = radius,
            center = center
        )

        // Concentric Rings
        for (i in 1..4) {
            drawCircle(
                color = CyberCyan.copy(alpha = 0.25f),
                radius = radius * (i / 4f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Crosshairs
        drawLine(
            color = CyberCyan.copy(alpha = 0.3f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = CyberCyan.copy(alpha = 0.3f),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1.dp.toPx()
        )

        // Rotating Sweep Line
        val sweepRad = Math.toRadians(sweepAngle.toDouble())
        val endX = (center.x + radius * cos(sweepRad)).toFloat()
        val endY = (center.y + radius * sin(sweepRad)).toFloat()

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(MatrixGreen, MatrixGreen.copy(alpha = 0f)),
                start = center,
                end = Offset(endX, endY)
            ),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )

        // Radar Target Blips
        val blips = listOf(
            Pair(0.4f, 45.0),
            Pair(0.7f, 130.0),
            Pair(0.55f, 220.0),
            Pair(0.85f, 310.0)
        )
        for ((dist, deg) in blips) {
            val rad = Math.toRadians(deg)
            val bx = (center.x + radius * dist * cos(rad)).toFloat()
            val by = (center.y + radius * dist * sin(rad)).toFloat()
            drawCircle(
                color = MatrixGreen,
                radius = 3.dp.toPx(),
                center = Offset(bx, by)
            )
        }
    }
}

@Composable
fun IntelStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = CyberCyan)
    }
}
