package com.activaplan.app.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ikon garis (kotak 24) untuk tombol dan alert. Dekoratif: teks di sebelahnya yang dibaca screen reader. */
enum class ActivaIconKind { CHECK, CLOSE, PLUS, TRASH, CALENDAR, WARNING, INFO, HOME, SEARCH, LIST, SLIDERS }

@Composable
fun ActivaIcon(kind: ActivaIconKind, tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier.size(size)) {
        val k = this.size.width / 24f
        val stroke = Stroke(width = 1.9f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun o(x: Float, y: Float) = Offset(x * k, y * k)
        fun DrawScope.line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(tint, o(x1, y1), o(x2, y2), stroke.width, StrokeCap.Round)
        fun DrawScope.dot(x: Float, y: Float) = drawCircle(tint, 1.2f * k, o(x, y))
        when (kind) {
            ActivaIconKind.CHECK -> drawPath(
                Path().apply { moveTo(5 * k, 12.5f * k); lineTo(10 * k, 17 * k); lineTo(19 * k, 7.5f * k) }, tint, style = stroke,
            )
            ActivaIconKind.CLOSE -> { line(6f, 6f, 18f, 18f); line(18f, 6f, 6f, 18f) }
            ActivaIconKind.PLUS -> { line(12f, 5f, 12f, 19f); line(5f, 12f, 19f, 12f) }
            ActivaIconKind.TRASH -> {
                line(4f, 7f, 20f, 7f)
                drawPath(
                    Path().apply { moveTo(9 * k, 7 * k); lineTo(9 * k, 4.5f * k); lineTo(15 * k, 4.5f * k); lineTo(15 * k, 7 * k) },
                    tint, style = stroke,
                )
                drawPath(
                    Path().apply { moveTo(6 * k, 7 * k); lineTo(7 * k, 20 * k); lineTo(17 * k, 20 * k); lineTo(18 * k, 7 * k) },
                    tint, style = stroke,
                )
                line(10f, 11f, 10f, 16f); line(14f, 11f, 14f, 16f)
            }
            ActivaIconKind.CALENDAR -> {
                drawRoundRect(tint, o(4f, 5f), Size(16 * k, 15 * k), CornerRadius(3 * k), style = stroke)
                line(4f, 10f, 20f, 10f); line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f)
            }
            ActivaIconKind.WARNING -> {
                drawPath(
                    Path().apply { moveTo(12 * k, 3.5f * k); lineTo(21.5f * k, 20 * k); lineTo(2.5f * k, 20 * k); close() },
                    tint, style = stroke,
                )
                line(12f, 9.5f, 12f, 14f); dot(12f, 17f)
            }
            ActivaIconKind.INFO -> {
                drawCircle(tint, 9f * k, o(12f, 12f), style = stroke)
                line(12f, 11f, 12f, 16.5f); dot(12f, 7.8f)
            }
            ActivaIconKind.HOME -> drawPath(
                Path().apply {
                    moveTo(3.5f * k, 11f * k); lineTo(12 * k, 3.5f * k); lineTo(20.5f * k, 11f * k)
                    moveTo(5.5f * k, 9.5f * k); lineTo(5.5f * k, 20 * k); lineTo(9.5f * k, 20 * k)
                    lineTo(9.5f * k, 14 * k); lineTo(14.5f * k, 14 * k); lineTo(14.5f * k, 20 * k)
                    lineTo(18.5f * k, 20 * k); lineTo(18.5f * k, 9.5f * k)
                },
                tint, style = stroke,
            )
            ActivaIconKind.SEARCH -> { drawCircle(tint, 6.5f * k, o(10.5f, 10.5f), style = stroke); line(15.3f, 15.3f, 20f, 20f) }
            ActivaIconKind.LIST -> {
                line(9f, 7f, 20f, 7f); line(9f, 12f, 20f, 12f); line(9f, 17f, 20f, 17f)
                dot(4.8f, 7f); dot(4.8f, 12f); dot(4.8f, 17f)
            }
            ActivaIconKind.SLIDERS -> {
                line(6f, 3.5f, 6f, 9f); line(6f, 15f, 6f, 20.5f)
                line(12f, 3.5f, 12f, 5f); line(12f, 11f, 12f, 20.5f)
                line(18f, 3.5f, 18f, 12f); line(18f, 18f, 18f, 20.5f)
                drawCircle(tint, 3f * k, o(6f, 12f), style = stroke)
                drawCircle(tint, 3f * k, o(12f, 8f), style = stroke)
                drawCircle(tint, 3f * k, o(18f, 15f), style = stroke)
            }
        }
    }
}
