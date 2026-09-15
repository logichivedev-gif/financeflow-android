package com.example.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ThreadLocalRandom

private class SparkParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color
)

@Composable
fun SparklingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    trackColor: Color = FinanceLightBlue,
    progressColor: Color = FinanceTeal
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }

    // Lista interna sin desencadenar recomposiciones innecesarias
    val particles = remember { mutableStateListOf<SparkParticle>() }

    val sparkColors = remember {
        listOf(
            Color(0xFFFFD700), // Dorado
            Color(0xFFFF8C00), // Naranja chispa
            Color(0xFFFFFFFF), // Blanco incandescente
            progressColor
        )
    }

    // Animación del progreso y físicas de partículas optimizadas
    LaunchedEffect(clampedProgress) {
        val animationJob = launch {
            animatedProgress.animateTo(
                targetValue = clampedProgress,
                animationSpec = tween(durationMillis = 1200)
            )
        }

        var lastTime = System.nanoTime()

        while (isActive && (animationJob.isActive || particles.isNotEmpty())) {
            withFrameNanos { frameTime ->
                val dt = ((frameTime - lastTime) / 1_000_000_000f).coerceAtMost(0.05f)
                lastTime = frameTime

                // 1. Generar chispas ÚNICAMENTE durante el movimiento
                if (animationJob.isActive && animatedProgress.value > 0.02f) {
                    val rand = ThreadLocalRandom.current()
                    repeat(3) {
                        particles.add(
                            SparkParticle(
                                x = 0f,
                                y = 0f,
                                vx = rand.nextDouble(-140.0, -20.0).toFloat(),
                                vy = rand.nextDouble(-160.0, 80.0).toFloat(),
                                alpha = 1.0f,
                                size = rand.nextDouble(2.5, 5.0).toFloat(),
                                color = sparkColors[rand.nextInt(sparkColors.size)]
                            )
                        )
                    }
                }

                // 2. Actualizar posición y desvanecimiento
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.vy += 350f * dt // Gravedad
                    p.alpha -= 2.2f * dt // Extinción rápida

                    if (p.alpha <= 0f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 16.dp)
    ) {
        val barHeight = height.toPx()
        val barTop = (size.height - barHeight) / 2f
        val totalWidth = size.width
        val currentProgressPx = totalWidth * animatedProgress.value

        // Fondo (Track)
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, barTop),
            size = Size(totalWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
        )

        // Relleno (Progress) con degradado
        if (currentProgressPx > 0f) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(progressColor.copy(alpha = 0.7f), progressColor),
                startX = 0f,
                endX = currentProgressPx
            )

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(0f, barTop),
                size = Size(currentProgressPx, barHeight),
                cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
            )

            // Destello incandescente en el punto de contacto (frente de la sierra)
            if (animatedProgress.isRunning) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = barHeight / 1.8f,
                    center = Offset(currentProgressPx, barTop + (barHeight / 2f))
                )
            }
        }

        // Dibujar chispas
        clipRect(0f, 0f, size.width, size.height) {
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(currentProgressPx + p.x, (barTop + barHeight / 2f) + p.y)
                )
            }
        }
    }
}
