package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldGreen
import kotlin.math.sin

@Composable
fun WaveRouteAnimation(
    isSellingActive: Boolean,
    mbSold: Double = 0.0,
    modifier: Modifier = Modifier
) {
    // Infinite transition for phase shift when active
    val infiniteTransition = rememberInfiniteTransition(label = "SineWaveTransition")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseShift"
    )

    val pingMs = if (isSellingActive) (115 + ((phaseShift * 7).toInt() % 14)) else 0
    val totalBytes = (mbSold * 1024 * 1024).toLong()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "USA Proxy/VPN Tunnel Stream",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSellingActive) {
                        Text(
                            text = "Ping: ${pingMs}ms • Route Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    } else {
                        Text(
                            text = "Route Disconnected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSellingActive) EmeraldGreen.copy(alpha = 0.2f)
                            else Color.Gray.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSellingActive) "● LIVE 14 MB/s" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSellingActive) EmeraldGreen else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Flags and Wave Animation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pakistan Flag 🇵🇰 (Left Node)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, if (isSellingActive) EmeraldGreen else Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇵🇰", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pakistan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sine-Wave Line Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val lineColor = if (isSellingActive) AccentGold else Color.Gray.copy(alpha = 0.5f)
                    val activePhase = if (isSellingActive) phaseShift else 0f

                    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f
                        val waveAmplitude = 14.dp.toPx()
                        val waveFrequency = 3.5f // number of full sine cycles

                        val path = Path()
                        path.moveTo(0f, midY)

                        var x = 0f
                        val step = 4f
                        while (x <= width) {
                            val relativeX = x / width
                            val y = midY + sin(relativeX * waveFrequency * 2 * Math.PI - activePhase).toFloat() * waveAmplitude
                            path.lineTo(x, y)
                            x += step
                        }

                        // Draw base sine wave
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(EmeraldGreen, AccentGold, Color(0xFF002868))
                            ),
                            style = Stroke(
                                width = if (isSellingActive) 4.dp.toPx() else 2.dp.toPx()
                            )
                        )

                        // If active, draw flowing packets (dots) along the wave
                        if (isSellingActive) {
                            val dotCount = 4
                            for (i in 0 until dotCount) {
                                val offsetFraction = ((activePhase / (2 * Math.PI.toFloat()) + i * (1f / dotCount)) % 1f)
                                val dotX = offsetFraction * width
                                val dotY = midY + sin((dotX / width) * waveFrequency * 2 * Math.PI - activePhase).toFloat() * waveAmplitude
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                                )
                                drawCircle(
                                    color = AccentGold,
                                    radius = 3.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                                )
                            }
                        }
                    }
                }

                // USA Flag 🇺🇸 (Right Node)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, if (isSellingActive) AccentGold else Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇺🇸", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "USA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secure Data Relay Simulation Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔒 US Relay:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isSellingActive) "AES-256 Tunnel" else "Inactive",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSellingActive) EmeraldGreen else Color.Gray
                    )
                }
                Text(
                    text = "%,d Bytes".format(totalBytes),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGold
                )
            }
        }
    }
}
