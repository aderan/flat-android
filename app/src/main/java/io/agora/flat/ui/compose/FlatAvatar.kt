package io.agora.flat.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import io.agora.flat.R
import io.agora.flat.ui.theme.FlatTheme

@OptIn(ExperimentalCoilApi::class)
@Composable
fun FlatAvatar(avatar: String?, size: Dp) {
    Image(
        painter = rememberImagePainter(avatar) {
            placeholder(R.drawable.ic_user_profile_head)
        },
        contentDescription = null,
        modifier = Modifier
            .size(size, size)
            .clip(shape = RoundedCornerShape(size / 2))
            .border(
                1.dp,
                FlatTheme.colors.divider,
                RoundedCornerShape(50)
            ),
        contentScale = ContentScale.Crop
    )
}
