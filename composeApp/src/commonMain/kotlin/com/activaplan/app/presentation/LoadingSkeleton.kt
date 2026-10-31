package com.activaplan.app.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes
import com.activaplan.app.domain.i18n.StringKey
import kotlinx.coroutines.delay
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Lama minimal kerangka tampil sekali ia muncul. Data lokal sering siap dalam beberapa milidetik; tanpa batas ini
 * kerangka hanya berkedip sekejap dan tidak terbaca sebagai "sedang memuat".
 */
const val SKELETON_MIN_VISIBLE_MS = 350L

private const val PULSE_MS = 900
private const val PULSE_MIN_ALPHA = 0.45f

private val LocalSkeletonAlpha = compositionLocalOf<State<Float>> { mutableFloatStateOf(1f) }

private class SkeletonClock(var shownAt: TimeMark?)

/**
 * True selama kerangka harus tampil: sejak [loading] mulai sampai selesai, dan paling sedikit [minVisibleMillis]
 * sejak kerangka tampil. Layar memakainya untuk memilih antara [ScreenListSkeleton] dan isinya.
 *
 * Kerangka hanya untuk pemuatan pertama. Setelah [hasContent] pernah true (isi sudah tampil sekali), kembali ke
 * layar dari tab lain tidak menampilkannya lagi, walau state layar sempat berstatus memuat; isinya sudah dikenal
 * pengguna dan datang dalam sekejap. Catatan itu disimpan bersama state layar, jadi ikut dipulihkan oleh navigasi.
 */
@Composable
fun rememberShowSkeleton(loading: Boolean, hasContent: Boolean, minVisibleMillis: Long = SKELETON_MIN_VISIBLE_MS): Boolean {
    val clock = remember { SkeletonClock(if (loading) TimeSource.Monotonic.markNow() else null) }
    var seenContent by rememberSaveable { mutableStateOf(hasContent) }
    // Sudah true sejak loading mulai, jadi saat loading selesai tidak ada bingkai yang menampilkan isi lebih awal.
    var hold by remember { mutableStateOf(loading) }
    LaunchedEffect(loading) {
        if (loading) {
            if (clock.shownAt == null) clock.shownAt = TimeSource.Monotonic.markNow()
            hold = true
        } else {
            val shown = clock.shownAt
            if (shown != null) delay(minVisibleMillis - shown.elapsedNow().inWholeMilliseconds)
            clock.shownAt = null
            hold = false
            if (hasContent) seenContent = true
        }
    }
    return (loading || hold) && !seenContent
}

/**
 * Membungkus [content] (susunan [SkeletonBlock]) sebagai kerangka muat yang berdenyut pelan; denyut berhenti bila
 * pengguna meminta gerak dikurangi. Dibaca pembaca layar sebagai satu pesan "Memuat", bukan deretan kotak kosong.
 */
@Composable
fun SkeletonGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val reduceMotion = rememberReduceMotion()
    val alpha: State<Float> = if (reduceMotion) {
        remember { mutableFloatStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "skeleton").animateFloat(
            initialValue = 1f,
            targetValue = PULSE_MIN_ALPHA,
            animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearEasing), RepeatMode.Reverse),
            label = "skeletonAlpha",
        )
    }
    val description = ActivaPlanTheme.strings[StringKey.LOADING]
    CompositionLocalProvider(LocalSkeletonAlpha provides alpha) {
        Column(modifier.semantics { contentDescription = description }) { content() }
    }
}

/** Kotak abu pengganti satu elemen yang belum dimuat; ikut denyut [SkeletonGroup] terdekat. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val alpha = LocalSkeletonAlpha.current
    Box(modifier.clip(shape).background(ActivaPlanTheme.colors.surfaceVar).graphicsLayer { this.alpha = alpha.value })
}

/** Kerangka kartu daftar: gambar kecil di kiri, tiga baris teks di kanan; bentuk sama dengan kartu latihan Jelajah. */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier, thumbnail: Dp = 76.dp) {
    val c = ActivaPlanTheme.colors
    Row(
        modifier.fillMaxWidth().clip(ActivaShapes.card).background(c.surface).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(Modifier.size(thumbnail), ActivaShapes.thumbnail)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(16.dp))
            SkeletonBlock(Modifier.width(88.dp).height(12.dp), ActivaShapes.chip)
            SkeletonBlock(Modifier.fillMaxWidth().height(12.dp))
        }
    }
}

/** Judul layar dan [cardCount] kartu; kerangka umum untuk layar tab berisi daftar. */
@Composable
fun ScreenListSkeleton(
    modifier: Modifier = Modifier,
    cardCount: Int = 5,
    header: @Composable () -> Unit = {},
) {
    SkeletonGroup(modifier.fillMaxSize().padding(horizontal = ActivaDimens.screenPadding, vertical = 16.dp)) {
        SkeletonBlock(Modifier.width(140.dp).height(28.dp))
        header()
        Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(cardCount) { SkeletonCard() }
        }
    }
}

@Preview(widthDp = 390, heightDp = 600, showBackground = true)
@Composable
private fun ScreenListSkeletonLightPreview() = ActivaPlanTheme(darkTheme = false) {
    Column(Modifier.background(ActivaPlanTheme.colors.bg)) { ScreenListSkeleton() }
}

@Preview(widthDp = 390, heightDp = 600, showBackground = true)
@Composable
private fun ScreenListSkeletonDarkPreview() = ActivaPlanTheme(darkTheme = true) {
    Column(Modifier.background(ActivaPlanTheme.colors.bg)) { ScreenListSkeleton() }
}
