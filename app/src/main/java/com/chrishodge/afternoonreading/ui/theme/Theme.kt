package com.chrishodge.afternoonreading.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryWhite,
    secondary = SecondaryWhite,
    tertiary = TertiaryWhite,
    background = PrimaryBlack
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlack,
    secondary = SecondaryBlack,
    tertiary = TertiaryBlack,
    background = PrimaryWhite,
    /*
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AfternoonReadingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    forceDarkMode: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (forceDarkMode || darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}