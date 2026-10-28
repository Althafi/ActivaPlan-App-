package com.activaplan.app.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.components.ActivaButton
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.presentation.PlatformBackHandler
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

internal const val INTRO_PAGE_COUNT = 3

private class IntroPageContent(val title: StringKey, val body: StringKey, val illustration: @Composable (Modifier) -> Unit)

private val introPages = listOf(
    IntroPageContent(StringKey.INTRO_1_TITLE, StringKey.INTRO_1_BODY) { BoardZonesIllustration(it) },
    IntroPageContent(StringKey.INTRO_2_TITLE, StringKey.INTRO_2_BODY) { WeekPlannerIllustration(it) },
    IntroPageContent(StringKey.INTRO_3_TITLE, StringKey.INTRO_3_BODY) { PushUpIllustration(it) },
)

@Composable
internal fun rememberIntroPagerState(): PagerState = rememberPagerState { INTRO_PAGE_COUNT }

/**
 * Tiga layar Perkenalan yang bisa digeser. [onSkip] dipanggil oleh "Lewati", [onFinish] oleh tombol "Mulai" di halaman terakhir.
 * Tombol Kembali sistem di halaman 2-3 menggeser satu halaman; di halaman 1 dibiarkan ke perilaku bawaan pemanggil.
 */
@Composable
fun IntroScreen(
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberIntroPagerState(),
) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage
    val isLast = page == INTRO_PAGE_COUNT - 1

    PlatformBackHandler(enabled = page > 0) { scope.launch { pagerState.animateScrollToPage(page - 1) } }

    Column(modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = ActivaDimens.screenPadding).height(56.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.defaultMinSize(minHeight = ActivaDimens.minTouchTarget, minWidth = ActivaDimens.minTouchTarget)
                    .clickable(role = Role.Button, onClick = onSkip)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(strings[StringKey.INTRO_SKIP], style = ActivaPlanTheme.type.cardTitle, color = c.primary)
            }
        }

        HorizontalPager(pagerState, Modifier.weight(1f).fillMaxWidth()) { index ->
            IntroPage(introPages[index])
        }

        PageIndicator(page, Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))

        ActivaButton(
            text = strings[if (isLast) StringKey.INTRO_START else StringKey.INTRO_NEXT],
            onClick = { if (isLast) onFinish() else scope.launch { pagerState.animateScrollToPage(page + 1) } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = ActivaDimens.screenPadding).padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun IntroPage(content: IntroPageContent) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Column(Modifier.fillMaxSize().padding(horizontal = ActivaDimens.screenPadding)) {
        Box(
            Modifier.fillMaxWidth().fillMaxHeight(0.45f).background(c.primarySoft, RoundedCornerShape(28.dp)).padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            content.illustration(Modifier)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 24.dp)) {
            Text(
                strings[content.title],
                style = ActivaPlanTheme.type.largeTitle.copy(fontSize = 26.sp, lineHeight = 32.sp),
                color = c.text,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                strings[content.body],
                style = ActivaPlanTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = c.textMuted,
            )
        }
    }
}

/** Tiga titik; yang aktif memanjang dan berwarna primary. Dibaca pembaca layar sebagai satu keterangan halaman. */
@Composable
private fun PageIndicator(current: Int, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val description = ActivaPlanTheme.strings.format(StringKey.INTRO_PAGE, current + 1, INTRO_PAGE_COUNT)
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(INTRO_PAGE_COUNT) { index ->
            val active = index == current
            Box(
                Modifier.height(8.dp).width(if (active) 20.dp else 8.dp)
                    .background(if (active) c.primary else c.border, CircleShape),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun IntroLightPreview() = ActivaPlanTheme(darkTheme = false) { IntroScreen({}, {}) }

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun IntroDarkPreview() = ActivaPlanTheme(darkTheme = true) { IntroScreen({}, {}) }
