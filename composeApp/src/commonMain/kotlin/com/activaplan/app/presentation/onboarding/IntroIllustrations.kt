package com.activaplan.app.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes
import com.activaplan.app.designsystem.components.ActivaIcon
import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.designsystem.components.BoardDiagram
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.time.formatClock
import com.activaplan.app.presentation.rememberReduceMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ilustrasi vektor tiga halaman Perkenalan. Semuanya dekoratif (teks di bawahnya yang dibaca pembaca layar)
 * dan bergerak halus berulang lewat transisi Compose, yang ikut skala animator sistem.
 */

/** Halaman 1: zona papan menyala bergantian beserta nama zonanya. */
@Composable
internal fun BoardZonesIllustration(modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val animated by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = BoardZone.entries.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 4800, easing = LinearEasing)),
    )
    val step = if (rememberReduceMotion()) 0f else animated
    val zone = BoardZone.entries[step.toInt().coerceIn(0, BoardZone.entries.lastIndex)]
    Column(modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally) {
        BoardDiagram(zone, Modifier.fillMaxWidth(0.82f))
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.background(c.surface, ActivaShapes.chip).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).background(c.zone(zone), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(strings.zoneChip(zone), style = ActivaPlanTheme.type.label, color = c.text)
        }
    }
}

private val PlannedDays = listOf(1, 3, 5)

/** Halaman 2: hari terjadwal menyala bergiliran dan lonceng pengingat bergoyang. */
@Composable
internal fun WeekPlannerIllustration(modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val transition = rememberInfiniteTransition()
    val animatedStep by transition.animateFloat(
        initialValue = 0f,
        targetValue = PlannedDays.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 4500, easing = LinearEasing)),
    )
    val animatedSwing by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 450), RepeatMode.Reverse),
    )
    val reduceMotion = rememberReduceMotion()
    val step = if (reduceMotion) 0f else animatedStep
    val swing = if (reduceMotion) 0f else animatedSwing
    val activeDay = PlannedDays[step.toInt().coerceIn(0, PlannedDays.lastIndex)]
    Column(modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (day in 1..7) {
                val planned = day in PlannedDays
                val active = day == activeDay
                Surface(
                    color = when {
                        active -> c.primary
                        planned -> c.primarySoft
                        else -> c.surface
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (active) c.primary else c.border),
                ) {
                    Box(Modifier.size(width = 36.dp, height = 52.dp), contentAlignment = Alignment.Center) {
                        Text(
                            strings.dayShort(day),
                            style = ActivaPlanTheme.type.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (active) c.onPrimary else if (planned) c.primary else c.textMuted,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Surface(
            color = c.surface,
            shape = ActivaShapes.card,
            border = BorderStroke(1.dp, c.border),
        ) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                ActivaIcon(ActivaIconKind.DUMBBELL, c.primary, size = 24.dp)
                Spacer(Modifier.width(12.dp))
                Text(formatClock(16 * 60 + 30), style = ActivaPlanTheme.type.cardTitle, color = c.text)
                Spacer(Modifier.width(16.dp))
                ActivaIcon(ActivaIconKind.BELL, c.primary, Modifier.rotate(swing), size = 22.dp)
            }
        }
    }
}

// Rangka push-up pada kotak 300 x 210, gaya sama dengan `docs/exercise-animation.md`.
private const val FRAME_W = 300f
private const val FRAME_H = 210f
private val Ankle = Offset(240f, 186f)
private val Hand = Offset(70f, 188f)
private const val BODY_LENGTH = 165.7f
private const val ARM_SEGMENT = 35f
private const val GROUND_Y = 192f
private const val UP_DEGREES = 23.5f
private const val DOWN_DEGREES = 9f

/** Halaman 3: rangka garis melakukan push-up, naik turun tanpa henti. */
@Composable
internal fun PushUpIllustration(modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val animated by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    )
    val p = if (rememberReduceMotion()) 0f else animated
    Canvas(modifier.clearAndSetSemantics {}.fillMaxWidth(0.85f).aspectRatio(FRAME_W / FRAME_H)) {
        val k = size.width / FRAME_W
        fun o(v: Offset) = Offset(v.x * k, v.y * k)

        val radians = (UP_DEGREES + (DOWN_DEGREES - UP_DEGREES) * p) * (PI.toFloat() / 180f)
        val shoulder = Offset(Ankle.x - BODY_LENGTH * cos(radians), Ankle.y - BODY_LENGTH * sin(radians))
        val toHead = (shoulder - Ankle) / BODY_LENGTH
        val head = shoulder + toHead * 22f + Offset(0f, -6f)
        val elbow = elbowFor(shoulder, Hand)

        drawLine(c.border, o(Offset(36f, GROUND_Y)), o(Offset(274f, GROUND_Y)), 3f * k, StrokeCap.Round)
        drawLine(c.text.copy(alpha = 0.45f), o(shoulder), o(elbow), 9f * k, StrokeCap.Round)
        drawLine(c.text.copy(alpha = 0.45f), o(elbow), o(Hand), 9f * k, StrokeCap.Round)
        drawLine(c.text, o(Ankle), o(shoulder), 13f * k, StrokeCap.Round)
        drawCircle(c.text, 11f * k, o(head))
        drawCircle(c.primary, 6f * k, o(Hand))
    }
}

/** Siku dua-tulang: lengan atas dan bawah sama panjang, siku menekuk ke arah punggung (kanan). */
private fun elbowFor(shoulder: Offset, hand: Offset): Offset {
    val span = hand - shoulder
    val distance = span.getDistance().coerceIn(0.001f, 2 * ARM_SEGMENT - 0.01f)
    val direction = span / span.getDistance().coerceAtLeast(0.001f)
    val bend = sqrt(max(ARM_SEGMENT * ARM_SEGMENT - (distance / 2) * (distance / 2), 0f))
    var normal = Offset(-direction.y, direction.x)
    if (normal.x < 0f) normal = Offset(-normal.x, -normal.y)
    return shoulder + span / 2f + normal * bend
}
