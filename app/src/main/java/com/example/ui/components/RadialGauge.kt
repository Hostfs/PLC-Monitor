package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    warningThreshold: Float? = null,
    criticalThreshold: Float? = null
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(minValue, maxValue),
        animationSpec = tween(durationMillis = 600),
        label = "gauge_val"
    )

    val progress = ((animatedValue - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val startAngle = 140f
    val sweepTotal = 260f

    val gaugeColor = when {
        criticalThreshold != null && value >= criticalThreshold -> StatusCritical
        warningThreshold != null && value >= warningThreshold -> StatusWarning
        else -> StatusRunning
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.toPx() - strokeWidth * 2
            val topLeft = Offset(strokeWidth, strokeWidth)

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Sweep Arc
            val activeSweep = sweepTotal * progress
            if (activeSweep > 0) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to CyanPrimary,
                        0.7f to gaugeColor,
                        1.0f to gaugeColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Ticks
            val radius = arcSize / 2f
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val tickCount = 10
            for (i in 0..tickCount) {
                val tickFraction = i.toFloat() / tickCount
                val angleDeg = startAngle + sweepTotal * tickFraction
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = radius - strokeWidth * 0.8f
                val outerR = radius - strokeWidth * 0.2f

                val startOffset = Offset(
                    (center.x + innerR * cos(angleRad)).toFloat(),
                    (center.y + innerR * sin(angleRad)).toFloat()
                )
                val endOffset = Offset(
                    (center.x + outerR * cos(angleRad)).toFloat(),
                    (center.y + outerR * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = Color(0xFF475569),
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center readout
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", animatedValue),
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = unit,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = gaugeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
