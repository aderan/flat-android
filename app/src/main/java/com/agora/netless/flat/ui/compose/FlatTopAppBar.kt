package com.agora.netless.flat.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agora.netless.flat.R
import com.agora.netless.flat.ui.activity.ui.theme.FlatAndroidTheme
import com.agora.netless.flat.ui.activity.ui.theme.FlatTitleTextStyle

@Composable
fun FlatTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(backgroundColor = Color.Transparent, elevation = 0.dp) {
        if (navigationIcon == null) {
            Spacer(TitleInsetWithoutIcon)
        } else {
            Row(
                TitleIconModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationIcon()
            }
        }
        Row(
            Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            title()
        }

        Row(
            Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun BackTopAppBar(title: String, onBackPressed: () -> Unit) {
    FlatTopAppBar(
        title = {
            Text(text = title, style = FlatTitleTextStyle)
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = null)
            }
        },
        actions = {
            IconButton(
                onClick = { /*TODO*/ }) {
                Image(
                    modifier = Modifier
                        .size(24.dp, 24.dp)
                        .clip(shape = RoundedCornerShape(12.dp)),
                    painter = painterResource(id = R.drawable.header),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    )
}

@Preview
@Composable
fun DefaultPreview() {
    FlatAndroidTheme {
        BackTopAppBar("Hello", {})
    }
}

private val AppBarHorizontalPadding = 8.dp

private val TitleInsetWithoutIcon = Modifier.width(16.dp)

private val TitleIconModifier = Modifier
    .fillMaxHeight()