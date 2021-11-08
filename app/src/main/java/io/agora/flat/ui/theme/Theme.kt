package io.agora.flat.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider


@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = FlatColorBlue,
    primaryVariant = FlatColorBlue,
    secondary = FlatColorBlue,
    secondaryVariant = FlatColorBlue,

    // behind scrollable content
    background = FlatColorBlack,
    // cards, sheets, and menus
    surface = FlatColorBlack,
)

/**
 * See <a href="https://material.io/design/material-theming/implementing-your-theme.html">Material Theming</a>
 */
@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = FlatColorBlue,
    primaryVariant = FlatColorBlue,
    secondary = FlatColorBlue,
    secondaryVariant = FlatColorBlue,

    // behind scrollable content
    background = FlatColorWhite,
    // cards, sheets, and menus
    surface = FlatColorWhite,
    error = FlatColorRed,

    onPrimary = FlatColorTextPrimary,
    onSecondary = FlatColorTextSecondary,
    onBackground = FlatColorBlack,
    onSurface = FlatColorBlack,
    onError = FlatColorWhite
)

@Composable
fun FlatAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit,
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

//    val context = LocalContext.current
//    val conf = context.resources.configuration.apply {
//        if (Build.VERSION.SDK_INT >= 24) {
//            setLocales(LocaleList(Locale.CHINESE))
//        } else {
//            setLocale(Locale.CHINESE)
//        }
//    }
//    context.resources.updateConfiguration(conf, context.resources.displayMetrics);

    CompositionLocalProvider(
        // LocalContext provides context.createConfigurationContext(conf)
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}