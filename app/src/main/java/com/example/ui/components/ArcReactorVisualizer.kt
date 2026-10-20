package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.JarvisState
import com.example.ui.theme.ArcGold
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    state: JarvisState = JarvisState.STANDBY,
    audioAmplitude: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ReactorAnim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    val innerRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val primaryColor = when (state) {
        JarvisState.LISTENING -> MatrixGreen
        JarvisState.THINKING -> NeonAmber
        JarvisState.SPEAKING -> ElectricBlue
        JarvisState.EXECUTING -> ArcGold
        JarvisState.STANDBY -> CyberCyan
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.85f
            val dynamicRadius = baseRadius * pulseScale * (1f + audioAmplitude * 0.25f)

            // 1. Ambient Glow Radial Backdrop
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha * 0.45f),
                        primaryColor.copy(alpha = glowAlpha * 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 1.3f
                ),
                radius = dynamicRadius * 1.25f,
                center = center
            )

            // 2. Outer Orbit Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = dynamicRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Rotating Outer Ring Segments
            val tickCount = 24
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((i * (360f / tickCount) + rotationAngle).toDouble())
                val startR = dynamicRadius - 6.dp.toPx()
                val endR = dynamicRadius + 4.dp.toPx()
                val startX = (center.x + startR * cos(angleRad)).toFloat()
                val startY = (center.y + startR * sin(angleRad)).toFloat()
                val endX = (center.x + endR * cos(angleRad)).toFloat()
                val endY = (center.y + endR * sin(angleRad)).toFloat()

                val tickAlpha = if (i % 3 == 0) 0.9f else 0.4f
                drawLine(
                    color = primaryColor.copy(alpha = tickAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                )
            }

            // 4. Middle Concentric Ring
            val midRadius = dynamicRadius * 0.72f
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = midRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 5. Counter-rotating inner markers
            val innerTicks = 12
            for (i in 0 until innerTicks) {
                val angleRad = Math.toRadians((i * (360f / innerTicks) + innerRotationAngle).toDouble())
                val r1 = midRadius - 4.dp.toPx()
                val r2 = midRadius + 4.dp.toPx()
                val x1 = (center.x + r1 * cos(angleRad)).toFloat()
                val y1 = (center.y + r1 * sin(angleRad)).toFloat()
                val x2 = (center.x + r2 * cos(angleRad)).toFloat()
                val y2 = (center.y + r2 * sin(angleRad)).toFloat()

                drawLine(
                    color = primaryColor.copy(alpha = 0.8f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 6. Central Stark Arc Triangle / Core
            val coreRadius = dynamicRadius * 0.42f
            val trianglePath = Path().apply {
                val angleOffset = -90.0 // point facing up
                for (j in 0..2) {
                    val deg = angleOffset + j * 120.0
                    val rad = Math.toRadians(deg)
                    val x = (center.x + coreRadius * cos(rad)).toFloat()
                    val y = (center.y + coreRadius * sin(rad)).toFloat()
                    if (j == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            // Inner triangle fill glow
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.7f),
                        primaryColor.copy(alpha = 0.2f)
                    ),
                    center = center,
                    radius = coreRadius
                )
            )

            // Triangle border stroke
            drawPath(
                path = trianglePath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )

            // Center energy dot
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx() * (1f + audioAmplitude * 0.5f),
                center = center
            )
        }
    }
}
