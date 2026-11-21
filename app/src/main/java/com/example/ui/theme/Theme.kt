package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class VinTradeExtendedColors(
    val bull: Color,
    val bear: Color,
    val gold: Color,
    val goldSubtle: Color,
    val wineAccent: Color,
    val cardElevated: Color,
    val borderSubtle: Color
)

val LocalVinTradeColors = staticCompositionLocalOf {
    VinTradeExtendedColors(
        bull = BullEmeraldDark,
        bear = BearCrimsonDark,
        gold = BurnishedGold,
        goldSubtle = BurnishedGold.copy(alpha = 0.14f),
        wineAccent = DeepBurgundy,
        cardElevated = VelvetSurfaceElevated,
        borderSubtle = HairlineBorder
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = BurnishedGold,
    onPrimary = ObsidianBg,
    primaryContainer = BordeauxContainer,
    onPrimaryContainer = ChampagneGold,
    secondary = RoseQuartz,
    onSecondary = ObsidianBg,
    secondaryContainer = VelvetSurfaceElevated,
    onSecondaryContainer = WarmIvory,
    tertiary = BullEmeraldDark,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = WarmIvory,
    surface = VelvetSurface,
    onSurface = WarmIvory,
    surfaceVariant = VelvetSurfaceElevated,
    onSurfaceVariant = MutedTaupe,
    outline = HairlineBorder,
    error = BearCrimsonDark,
    onError = ObsidianBg
)

private val LightColorScheme = lightColorScheme(
    primary = ClassicBordeaux,
    onPrimary = Color.White,
    primaryContainer = LightBordeauxContainer,
    onPrimaryContainer = EspressoInk,
    secondary = AntiqueGold,
    onSecondary = Color.White,
    secondaryContainer = WarmSandSurface,
    onSecondaryContainer = EspressoInk,
    tertiary = BullEmeraldLight,
    onTertiary = Color.White,
    background = IvoryBg,
    onBackground = EspressoInk,
    surface = AlabasterSurface,
    onSurface = EspressoInk,
    surfaceVariant = WarmSandSurface,
    onSurfaceVariant = WarmSlate,
    outline = LightBorder,
    error = BearCrimsonLight,
    onError = Color.White
)

private val VinTradeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VinTradeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        VinTradeExtendedColors(
            bull = BullEmeraldDark,
            bear = BearCrimsonDark,
            gold = BurnishedGold,
            goldSubtle = BurnishedGold.copy(alpha = 0.15f),
            wineAccent = DeepBurgundy,
            cardElevated = VelvetSurfaceElevated,
            borderSubtle = HairlineBorder
        )
    } else {
        VinTradeExtendedColors(
            bull = BullEmeraldLight,
            bear = BearCrimsonLight,
            gold = AntiqueGold,
            goldSubtle = AntiqueGold.copy(alpha = 0.12f),
            wineAccent = ClassicBordeaux,
            cardElevated = WarmSandSurface,
            borderSubtle = LightBorder
        )
    }

    CompositionLocalProvider(LocalVinTradeColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VinTradeTypography,
            shapes = VinTradeShapes,
            content = content
        )
    }
}
