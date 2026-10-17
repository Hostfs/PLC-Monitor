package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GridLineColor
import com.example.ui.theme.IndustrialSurface
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.max

@Composable
fun TelemetryChart(
    dataPoints: List<Float>,
    unit: String,
    title: String,
    modifier: Modifier = Modifier,
    lineColor: Color = CyanPrimary,
    warningLimit: Float? = null,
    criticalLimit: Float? = null,
    minYOverride: Float? = null,
    maxYOverride: Float? = null
) {
    val points = if (dataPoints.isEmpty()) listOf(0f) else dataPoints
    val rawMin = minYOverride ?: (points.minOrNull() ?: 0f)
    val rawMax = maxYOverride ?: (points.maxOrNull() ?: 100f)
    val spread = max(1f, rawMax - rawMin)
    val minY = rawMin - (spread * 0.1f)
    val maxY = rawMax + (spread * 0.15f)
    val range = max(0.1f, maxY - minY)

    val latestValue = points.lastOrNull() ?: 0f
    val maxValue = points.maxOrNull() ?: 0f
    val minValue = points.minOrNull() ?: 0f
    val avgValue = points.average().toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(IndustrialSurface)
            .padding(12.dp)
    ) {
        // Chart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "최신: ${String.format("%.1f", latestValue)} $unit",
                    color = lineColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Stats
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MAX: ${String.format("%.1f", maxValue)} | MIN: ${String.format("%.1f", minValue)}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "AVG: ${String.format("%.1f", avgValue)} $unit",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw 4 Horizontal Grid Lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = GridLineColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Warning Limit Line if provided
                if (warningLimit != null && warningLimit in minY..maxY) {
                    val warnY = height - ((warningLimit - minY) / range) * height
                    drawLine(
                        color = StatusWarning.copy(alpha = 0.8f),
                        start = Offset(0f, warnY),
                        end = Offset(width, warnY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Draw Critical Limit Line if provided
                if (criticalLimit != null && criticalLimit in minY..maxY) {
                    val critY = height - ((criticalLimit - minY) / range) * height
                    drawLine(
                        color = StatusCritical.copy(alpha = 0.85f),
                        start = Offset(0f, critY),
                        end = Offset(width, critY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                if (points.size < 2) return@Canvas

                val stepX = width / (points.size - 1)
                val linePath = Path()
                val fillPath = Path()

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normalized = ((value - minY) / range).coerceIn(0f, 1f)
                    val y = height - (normalized * height)

                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth cubic bezier curve
                        val prevX = (index - 1) * stepX
                        val prevNorm = ((points[index - 1] - minY) / range).coerceIn(0f, 1f)
                        val prevY = height - (prevNorm * height)
                        val cx = (prevX + x) / 2f
                        linePath.cubicTo(cx, prevY, cx, y, x, y)
                        fillPath.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                // Gradient area under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.0f)
                        )
                    )
                )

                // The curve line itself
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Highlight latest point
                val lastX = width
                val lastY = height - (((latestValue - minY) / range).coerceIn(0f, 1f) * height)
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
            }
        }
    }
}
