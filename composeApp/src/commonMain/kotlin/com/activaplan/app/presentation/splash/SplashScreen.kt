package com.activaplan.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.components.ActivaBrand
import com.activaplan.app.designsystem.components.ActivaLogo
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.presentation.rememberReduceMotion
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Lama animasi (ms) sebelum splash boleh berpindah; garis waktu per tahap ada di [SplashContent]. */
const val SPLASH_DURATION_MS = 1800

/** Logo statis tanpa animasi saat gerak dikurangi, lalu lanjut. */
private const val REDUCED_MOTION_HOLD_MS = 600L

/**
 * Splash animasi tiap cold start. Berpindah lewat [onFinished] setelah animasi selesai **dan** [ready] true
 * (setelan sudah terbaca), sehingga Beranda atau Perkenalan langsung tahu tujuannya tanpa berkedip.
 *
 * Latar selalu oranye merek, bukan warna tema: splash bawaan OS tidak tahu pilihan tema di Setelan, jadi keduanya
 * memakai warna yang sama agar tidak berkedip saat serah-terima.
 */
@Composable
fun SplashScreen(ready: Boolean, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val time = remember { Animatable(0f) }
    var animationDone by remember { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            time.snapTo(SPLASH_DURATION_MS.toFloat())
            delay(REDUCED_MOTION_HOLD_MS)
        } else {
            time.animateTo(SPLASH_DURATION_MS.toFloat(), tween(SPLASH_DURATION_MS, easing = LinearEasing))
        }
        animationDone = true
    }
    LaunchedEffect(animationDone, ready) {
        if (animationDone && ready) currentOnFinished()
    }

    SplashContent(time.value, modifier)
}

/** Isi splash pada waktu [timeMs]; tanpa state agar bisa dipratinjau di titik waktu mana pun. */
@Composable
fun SplashContent(timeMs: Float, modifier: Modifier = Modifier) {
    val strings = ActivaPlanTheme.strings
    val description = strings[StringKey.SPLASH_LOGO_DESCRIPTION]

    val legs = FastOutSlowInEasing.transform(phase(timeMs, 0f, 450f))
    val dots = phase(timeMs, 350f, 860f)
    val spark = phase(timeMs, 860f, 1100f)
    val wordmark = phase(timeMs, 1100f, 1400f)
    val tagline = phase(timeMs, 1250f, 1550f)

    Box(
        modifier.fillMaxSize().background(ActivaBrand.orangeTop).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // Kotak logo 288 dp sama dengan ikon splash OS (Android 12+: ikon 288 dp di tengah), jadi posisinya tepat sama.
        ActivaLogo(size = LOGO_BOX, legs = legs, dots = dots, spark = spark)
        Column(
            Modifier.align(Alignment.Center).offset(y = WORDMARK_OFFSET).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "ActivaPlan",
                style = ActivaPlanTheme.type.largeTitle.copy(fontSize = 32.sp, lineHeight = 38.sp),
                color = Color.White,
                modifier = Modifier.alpha(wordmark).offset(y = (12f * (1f - wordmark)).dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings[StringKey.SPLASH_TAGLINE],
                style = ActivaPlanTheme.type.body.copy(fontSize = 15.sp),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(tagline),
            )
        }
    }
}

private val LOGO_BOX = 288.dp

/** Jarak tulisan dari pusat layar: di bawah kaki A (±64 dp dari pusat) dengan jeda lega. */
private val WORDMARK_OFFSET = 110.dp

/** Kemajuan 0..1 sebuah tahap yang berjalan dari [start] ke [end] ms. */
private fun phase(timeMs: Float, start: Float, end: Float): Float = ((timeMs - start) / (end - start)).coerceIn(0f, 1f)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SplashStartPreview() = ActivaPlanTheme(darkTheme = false) { SplashContent(timeMs = 250f) }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SplashDotsPreview() = ActivaPlanTheme(darkTheme = false) { SplashContent(timeMs = 700f) }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SplashEndPreview() = ActivaPlanTheme(darkTheme = false) { SplashContent(timeMs = SPLASH_DURATION_MS.toFloat()) }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SplashEndDarkPreview() = ActivaPlanTheme(darkTheme = true) { SplashContent(timeMs = SPLASH_DURATION_MS.toFloat()) }
