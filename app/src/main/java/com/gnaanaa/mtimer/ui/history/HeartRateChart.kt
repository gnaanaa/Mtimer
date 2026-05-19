package com.gnaanaa.mtimer.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.HeartRateRecord
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits

@Composable
fun HeartRateChart(
    samples: List<HeartRateRecord.Sample>,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) return

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    val meditationGreen = Color(0xFF4CAF50)
    val accentColor = if (isDark) meditationGreen else MaterialTheme.colorScheme.primary
    
    val bpmValues = samples.map { it.beatsPerMinute.toFloat() }
    val minBpm = bpmValues.minOrNull() ?: 0f
    val maxBpm = bpmValues.maxOrNull() ?: 100f
    val avgBpm = bpmValues.average().toInt()
    
    // Add some padding to the range for better visualization
    val range = (maxBpm - minBpm).coerceAtLeast(10f)
    val chartMin = (minBpm - 5).coerceAtLeast(0f)
    val chartMax = maxBpm + 5

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "HEART RATE",
                    fontFamily = DotMatrix,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = avgBpm.toString().styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = " BPM AVG",
                    fontFamily = InterFont,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            if (samples.size < 2) return@Canvas

            val width = size.width
            val height = size.height
            val stepX = width / (samples.size - 1)

            val points = bpmValues.mapIndexed { index, bpm ->
                val x = index * stepX
                val y = height - ((bpm - chartMin) / (chartMax - chartMin) * height)
                Offset(x, y)
            }

            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                points.forEach { lineTo(it.x, it.y) }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            // Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = if (isDark) 0.2f else 0.1f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Line
            drawPath(
                path = path,
                color = accentColor.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${minBpm.toInt()} MIN".styleDottedDigits(),
                fontFamily = InterFont,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "${maxBpm.toInt()} MAX".styleDottedDigits(),
                fontFamily = InterFont,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
