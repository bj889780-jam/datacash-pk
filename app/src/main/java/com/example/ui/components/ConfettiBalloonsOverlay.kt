package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldGreen
import kotlin.random.Random

private data class Particle(
    val xRatio: Float,
    val startYRatio: Float,
    val speed: Float,
    val sizePx: Float,
    val color: Color,
    val isBalloon: Boolean,
    val swayAmplitude: Float
)

@Composable
fun ConfettiBalloonsOverlay(
    message: String,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
    }

    // Generate fixed set of 60 colourful confetti and balloon particles
    val particles = remember {
        val colors = listOf(
            EmeraldGreen, AccentGold, Color(0xFFDC2626), Color(0xFF0284C7),
            Color(0xFF9333EA), Color(0xFFEA580C), Color(0xFF16A34A)
        )
        List(70) {
            Particle(
                xRatio = Random.nextFloat(),
                startYRatio = if (Random.nextBoolean()) -0.2f else 1.2f,
                speed = Random.floatInRange(0.8f, 1.6f),
                sizePx = Random.floatInRange(12f, 32f),
                color = colors[Random.nextInt(colors.size)],
                isBalloon = Random.nextBoolean(),
                swayAmplitude = Random.floatInRange(15f, 40f)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        // Particles Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val currentProgress = progress.value

            particles.forEach { particle ->
                val sway = kotlin.math.sin(currentProgress * Math.PI * 4 + particle.xRatio * 10).toFloat() * particle.swayAmplitude
                val curX = particle.xRatio * canvasWidth + sway

                if (particle.isBalloon) {
                    // Balloon floats upwards
                    val curY = canvasHeight * 1.1f - (currentProgress * particle.speed * canvasHeight * 1.2f)
                    val balloonWidth = particle.sizePx * 1.2f
                    val balloonHeight = particle.sizePx * 1.6f

                    // Draw balloon oval
                    drawOval(
                        color = particle.color,
                        topLeft = Offset(curX - balloonWidth / 2, curY - balloonHeight / 2),
                        size = Size(balloonWidth, balloonHeight)
                    )
                    // Draw balloon string line
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(curX, curY + balloonHeight / 2),
                        end = Offset(curX + sway / 2, curY + balloonHeight / 2 + 25f),
                        strokeWidth = 2f
                    )
                } else {
                    // Confetti piece falls downwards
                    val curY = -0.1f * canvasHeight + (currentProgress * particle.speed * canvasHeight * 1.3f)
                    val rotationDeg = currentProgress * 720f

                    drawRect(
                        color = particle.color,
                        topLeft = Offset(curX, curY),
                        size = Size(particle.sizePx, particle.sizePx * 0.6f)
                    )
                }
            }
        }

        // Central Celebration Card
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🎉 CASH OUT SUCCESS!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Adding earnings to your Main Balance...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun Random.Default.floatInRange(min: Float, max: Float): Float {
    return min + nextFloat() * (max - min)
}
