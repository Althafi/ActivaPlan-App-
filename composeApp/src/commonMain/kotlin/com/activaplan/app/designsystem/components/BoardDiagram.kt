package com.activaplan.app.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.model.BoardHole
import com.activaplan.app.domain.model.BoardLayout
import com.activaplan.app.domain.model.BoardZone

private fun DrawScope.drawBoard(
    columns: Int,
    highlighted: Set<BoardHole>,
    boardColor: Color,
    holeColor: Color,
    zoneColor: Color,
    cornerRadius: Dp,
    padXFraction: Float,
    holeRadiusFraction: Float,
) {
    drawRoundRect(boardColor, cornerRadius = CornerRadius(cornerRadius.toPx()))
    val padX = size.width * padXFraction
    val step = (size.width - 2 * padX) / (columns - 1)
    val radius = size.height * holeRadiusFraction
    for (row in 0 until BoardLayout.ROWS) {
        val y = size.height * (0.3f + 0.4f * row)
        for (column in 0 until columns) {
            val lit = BoardHole(row, column) in highlighted
            drawCircle(if (lit) zoneColor else holeColor, radius, Offset(padX + step * column, y))
        }
    }
}

/** Diagram board 2 × 6 lubang untuk hero Detail; lubang milik [zone] disorot dengan warna zonanya. Dekoratif. */
@Composable
fun BoardDiagram(zone: BoardZone?, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    Canvas(modifier.aspectRatio(310f / 120f)) {
        drawBoard(
            columns = BoardLayout.COLUMNS,
            highlighted = zone?.let(BoardLayout::highlightedHoles).orEmpty(),
            boardColor = c.board,
            holeColor = c.boardHole,
            zoneColor = zone?.let(c::zone) ?: c.boardHole,
            cornerRadius = 24.dp,
            padXFraction = 0.097f,
            holeRadiusFraction = 0.117f,
        )
    }
}

/** Thumbnail board 2 × 3 untuk kartu latihan. [zone] null menggambar board tanpa sorotan. Dekoratif. */
@Composable
fun BoardThumbnail(zone: BoardZone?, modifier: Modifier = Modifier, size: Dp = 76.dp) {
    val c = ActivaPlanTheme.colors
    Canvas(modifier.size(size)) {
        drawBoard(
            columns = BoardLayout.COMPACT_COLUMNS,
            highlighted = zone?.let(BoardLayout::compactHighlightedHoles).orEmpty(),
            boardColor = c.board,
            holeColor = c.boardHole,
            zoneColor = zone?.let(c::zone) ?: c.boardHole,
            cornerRadius = 16.dp,
            padXFraction = 0.2f,
            holeRadiusFraction = 0.092f,
        )
    }
}
