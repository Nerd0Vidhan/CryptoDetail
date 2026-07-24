package `in`.mato.cryptodetail.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CryptoDarkColorScheme = darkColorScheme(
    primary = orange,
    onPrimary = darkBlack,
    primaryContainer = brown,
    onPrimaryContainer = yellow,
    secondary = lightGreen,
    onSecondary = darkBlack,
    secondaryContainer = grassGreen,
    onSecondaryContainer = lightGreen,
    tertiary = red,
    onTertiary = darkBlack,
    background = darkBlack,
    onBackground = tintedYellow,
    surface = darkGrey,
    onSurface = tintedYellow,
    surfaceVariant = lightGray,
    onSurfaceVariant = lightYellow,
    outline = borderGray,
    error = red,
    onError = darkBlack,
)

@Composable
fun CryptoDetailTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CryptoDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
