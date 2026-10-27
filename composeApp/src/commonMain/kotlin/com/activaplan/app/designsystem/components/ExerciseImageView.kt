package com.activaplan.app.designsystem.components

import activaplan.composeapp.generated.resources.Res
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.activaplan.app.domain.model.ExerciseImage
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Gambar langkah latihan dari `composeResources/drawable/[ExerciseImage.file]`.
 * [placeholder] tampil selama memuat, bila [image] null, atau bila file tidak ada/gagal didekode.
 */
@Composable
fun ExerciseImageView(
    image: ExerciseImage?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit,
) {
    val bitmap by produceState<ImageBitmap?>(null, image?.file) {
        value = image?.let { runCatching { Res.readBytes("drawable/${it.file}").decodeToImageBitmap() }.getOrNull() }
    }
    val loaded = bitmap
    if (image == null || loaded == null) {
        placeholder()
    } else {
        Image(
            loaded,
            contentDescription = image.alt,
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}
