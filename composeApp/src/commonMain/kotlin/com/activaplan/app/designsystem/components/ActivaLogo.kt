package com.activaplan.app.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Warna merek logo dan ikon peluncur; sama dengan `primary` terang dan gradien ke bawah (`docs/brand-logo.md`). */
object ActivaBrand {
    val orangeTop = Color(0xFFE8531A)
    val orangeBottom = Color(0xFFFF7F45)
    val gradient: Brush get() = Brush.verticalGradient(listOf(orangeTop, orangeBottom))
}

private const val VIEWPORT = 108f
private val LeftFoot = Offset(31f, 74f)
private val RightFoot = Offset(77f, 74f)
private val Apex = Offset(54f, 33f)
private val Spark = Offset(54f, 24f)

/** Pusat titik zona 2 × 2: Biru, Merah, Kuning, Hijau; urutannya sama dengan zona di `BoardDiagram`. */
private val DotCenters = listOf(Offset(48.8f, 60.5f), Offset(59.2f, 60.5f), Offset(48.8f, 70.9f), Offset(59.2f, 70.9f))

private const val DOT_RADIUS = 4.8f
private const val RING_WIDTH = 0.6f
private const val STROKE_WIDTH = 6f
private const val GLYPH_WIDTH = 0.85f
private const val DOT_STAGGER = 0.2f
private const val DOT_SPAN = 0.4f

/** Bagian ikon di dalam titik, koordinatnya relatif ke pusat titik (satuan kotak 108). */
private sealed interface Glyph {
    class Line(val points: List<Offset>) : Glyph
    class Blob(val center: Offset, val radius: Float) : Glyph
}

/** Garis bersambung dari pasangan x, y berurutan. */
private fun line(vararg xy: Float): Glyph = Glyph.Line(List(xy.size / 2) { Offset(xy[it * 2], xy[it * 2 + 1]) })

/** Ikon tiap titik, urutan sama dengan [DotCenters]: barbel (kekuatan), pelari (lari), kalender (rencana), denyut (kebugaran). */
private val DotGlyphs: List<List<Glyph>> = listOf(
    listOf(
        line(-2.3f, 0f, 2.3f, 0f), line(-1.6f, -1.7f, -1.6f, 1.7f), line(1.6f, -1.7f, 1.6f, 1.7f),
        line(-2.5f, -0.9f, -2.5f, 0.9f), line(2.5f, -0.9f, 2.5f, 0.9f),
    ),
    listOf(
        Glyph.Blob(Offset(0.8f, -2f), 0.75f), line(0.3f, -1.1f, -0.3f, 0.5f),
        line(0.1f, -0.7f, 1.4f, -0.1f, 2f, -1f), line(0.1f, -0.7f, -1.1f, -0.2f, -1.6f, 0.9f),
        line(-0.3f, 0.5f, 1.1f, 1.2f, 0.8f, 2.5f), line(-0.3f, 0.5f, -1.3f, 1.6f, -2.4f, 1.3f),
    ),
    listOf(
        line(-2.1f, -1.3f, 2.1f, -1.3f, 2.1f, 2.2f, -2.1f, 2.2f, -2.1f, -1.3f), line(-2.1f, 0f, 2.1f, 0f),
        line(-1f, -2.5f, -1f, -1.3f), line(1f, -2.5f, 1f, -1.3f),
    ),
    listOf(line(-2.6f, 0.3f, -1f, 0.3f, -0.2f, -1.9f, 0.8f, 1.9f, 1.6f, 0.3f, 2.6f, 0.3f)),
)

/**
 * Logo ActivaPlan: huruf A dari dua garis dengan empat titik zona di dalamnya, masing-masing memuat ikon latihan,
 * digambar di kotak 108 × 108 (sama dengan ikon peluncur Android) sehingga ukurannya tetap sebanding dengan ikon OS.
 *
 * Semua parameter animasi bernilai 0..1 dan berasal dari luar, jadi komposabel ini tanpa state dan mudah dipratinjau:
 * [legs] menggambar kedua kaki A dari bawah ke puncak, [dots] memunculkan keempat titik berurutan dengan sedikit
 * memantul (ikonnya ikut muncul), [spark] memainkan percikan di puncak (0 atau 1 = tidak tampak).
 * Dekoratif; teks di sebelahnya yang dibaca pembaca layar.
 */
@Composable
fun ActivaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 108.dp,
    legs: Float = 1f,
    dots: Float = 1f,
    spark: Float = 0f,
    letterColor: Color = Color.White,
    ringColor: Color = Color.White,
) {
    val c = ActivaPlanTheme.colors
    val zoneColors = listOf(c.zoneBlue, c.zoneRed, c.zoneYellow, c.zoneGreen)
    Canvas(modifier.size(size).clearAndSetSemantics {}) {
        val k = this.size.width / VIEWPORT
        fun o(p: Offset) = Offset(p.x * k, p.y * k)

        if (legs > 0f) {
            val t = legs.coerceAtMost(1f)
            for (foot in listOf(LeftFoot, RightFoot)) {
                val tip = Offset(foot.x + (Apex.x - foot.x) * t, foot.y + (Apex.y - foot.y) * t)
                drawLine(letterColor, o(foot), o(tip), STROKE_WIDTH * k, StrokeCap.Round)
            }
        }

        DotCenters.forEachIndexed { i, center ->
            val scale = backOut(((dots - i * DOT_STAGGER) / DOT_SPAN).coerceIn(0f, 1f))
            if (scale > 0f) {
                drawCircle(zoneColors[i], DOT_RADIUS * k * scale, o(center))
                // Cincin putih berpusat di tepi isi, supaya merah dan kuning tetap terbaca di atas oranye.
                drawCircle(ringColor, DOT_RADIUS * k * scale, o(center), style = Stroke(RING_WIDTH * k * scale))
                fun at(p: Offset) = o(Offset(center.x + p.x * scale, center.y + p.y * scale))
                for (glyph in DotGlyphs[i]) when (glyph) {
                    is Glyph.Blob -> drawCircle(ringColor, glyph.radius * k * scale, at(glyph.center))
                    is Glyph.Line -> glyph.points.zipWithNext { a, b ->
                        drawLine(ringColor, at(a), at(b), GLYPH_WIDTH * k * scale, StrokeCap.Round)
                    }
                }
            }
        }

        if (spark > 0f && spark < 1f) {
            drawCircle(
                letterColor.copy(alpha = 0.6f * (1f - spark)),
                3f * k * (0.5f + 2f * spark),
                o(Spark),
            )
        }
    }
}

/** 0 → sedikit melewati 1 → 1; dasar efek memantul titik zona. */
private fun backOut(t: Float): Float {
    if (t <= 0f) return 0f
    val c1 = 2.0f
    val x = t - 1f
    return 1f + (c1 + 1f) * x * x * x + c1 * x * x
}

/** Logo di atas kotak oranye bergradien (bentuk ikon aplikasi), untuk header dan layar Tentang. */
@Composable
fun ActivaLogoTile(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    Box(
        modifier.size(size).background(ActivaBrand.gradient, RoundedCornerShape(size * 0.24f)),
        contentAlignment = Alignment.Center,
    ) {
        ActivaLogo(size = size)
    }
}

@Preview(widthDp = 220, heightDp = 120, showBackground = true)
@Composable
private fun ActivaLogoTilePreview() = ActivaPlanTheme(darkTheme = false) {
    Box(Modifier.size(width = 220.dp, height = 120.dp).background(Color.White), contentAlignment = Alignment.Center) {
        ActivaLogoTile(size = 96.dp)
    }
}

@Preview(widthDp = 220, heightDp = 120, showBackground = true)
@Composable
private fun ActivaLogoSmallTileDarkPreview() = ActivaPlanTheme(darkTheme = true) {
    Box(Modifier.size(width = 220.dp, height = 120.dp).background(Color(0xFF0E1117)), contentAlignment = Alignment.Center) {
        ActivaLogoTile(size = 32.dp)
    }
}

@Preview(widthDp = 220, heightDp = 120, showBackground = true)
@Composable
private fun ActivaLogoMidAnimationPreview() = ActivaPlanTheme(darkTheme = false) {
    Box(Modifier.size(width = 220.dp, height = 120.dp).background(ActivaBrand.orangeTop), contentAlignment = Alignment.Center) {
        ActivaLogo(size = 108.dp, legs = 1f, dots = 0.5f, spark = 0.3f)
    }
}
