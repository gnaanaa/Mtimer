package com.gnaanaa.mtimer.ui.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont

@Composable
fun WeeklyMinutesChart(
    minutes: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (minutes.size < 2) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                "CONTINUE SITTING TO SEE PROGRESS CHART",
                fontFamily = InterFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        return
    }

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    val meditationGreen = Color(0xFF4CAF50)
    val chartColor = if (isDark) meditationGreen else MaterialTheme.colorScheme.primary

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceArgb = onSurfaceColor.toArgb()
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(minutes) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
        )
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val maxMinutes = (minutes.maxOrNull() ?: 1).coerceAtLeast(1)
            val paddingX = 20.dp.toPx()
            val paddingY = 20.dp.toPx()
            
            val chartWidth = width - (paddingX * 2)
            val chartHeight = height - (paddingY * 2)
            
            val stepX = chartWidth / (minutes.size - 1)
            
            // Draw horizontal guide lines
            val guideLines = 4
            for (i in 0..guideLines) {
                val y = paddingY + (chartHeight / guideLines) * i
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.05f),
                    start = Offset(paddingX, y),
                    end = Offset(width - paddingX, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = minutes.mapIndexed { index, min ->
                val x = paddingX + index * stepX
                val y = paddingY + chartHeight - (min.toFloat() / maxMinutes * chartHeight)
                Offset(x, y)
            }

            val path = Path().apply {
                if (points.isNotEmpty()) {
                    points.firstOrNull()?.let { moveTo(it.x, it.y) }
                    for (i in 1 until points.size) {
                        // Drawing path based on animation progress (left to right)
                        if (i.toFloat() / (points.size - 1) <= animationProgress.value) {
                            lineTo(points[i].x, points[i].y)
                        } else {
                            // Partial segment drawing
                            val prev = points[i - 1]
                            val curr = points[i]
                            val segmentProgress = (animationProgress.value - (i - 1).toFloat() / (points.size - 1)) * (points.size - 1)
                            val interpX = prev.x + (curr.x - prev.x) * segmentProgress
                            val interpY = prev.y + (curr.y - prev.y) * segmentProgress
                            lineTo(interpX, interpY)
                            break
                        }
                    }
                }
            }

            // Draw area under the path (very subtle)
            val fillPath = Path().apply {
                addPath(path)
                if (points.isNotEmpty()) {
                    // Find the last point actually reached by animation
                    val lastX = if (animationProgress.value >= 1f) {
                        points.last().x
                    } else {
                        val segmentCount = points.size - 1
                        val i = (animationProgress.value * segmentCount).toInt() + 1
                        val prev = points[i - 1]
                        val curr = points[if (i < points.size) i else points.size - 1]
                        val segmentProgress = (animationProgress.value - (i - 1).toFloat() / segmentCount) * segmentCount
                        prev.x + (curr.x - prev.x) * segmentProgress
                    }
                    lineTo(lastX, paddingY + chartHeight)
                    lineTo(points[0].x, paddingY + chartHeight)
                    close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(chartColor.copy(alpha = 0.15f), Color.Transparent),
                    startY = paddingY,
                    endY = paddingY + chartHeight
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = chartColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw distinct dot for the last point if animation finished
            if (animationProgress.value >= 1f) {
                val lastPoint = points.last()
                drawCircle(
                    color = surfaceColor,
                    radius = 4.dp.toPx(),
                    center = lastPoint
                )
                drawCircle(
                    color = chartColor,
                    radius = 3.dp.toPx(),
                    center = lastPoint
                )
                drawCircle(
                    color = onSurfaceColor,
                    radius = 1.dp.toPx(),
                    center = lastPoint
                )
            }

            // Draw abbreviated labels
            val paint = android.graphics.Paint().apply {
                color = onSurfaceArgb
                alpha = 100
                textSize = 9.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            }

            // Show labels for first, mid, and last
            val labelIndices = listOf(0, minutes.size / 2, minutes.size - 1)
            labelIndices.distinct().forEach { index ->
                if (index < labels.size) {
                    drawContext.canvas.nativeCanvas.drawText(
                        labels[index].uppercase(),
                        points[index].x,
                        height,
                        paint
                    )
                }
            }
        }
    }
}
