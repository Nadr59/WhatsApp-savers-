package com.example.whatsappsaver.ui.theme
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val Dark = darkColorScheme(primary = PrimaryDark, secondary = SecondaryDark, background = BackgroundDark, surface = SurfaceDark, onPrimary = OnPrimaryDark, onBackground = OnBackgroundDark, onSurface = OnBackgroundDark)
private val Light = lightColorScheme(primary = PrimaryLight, secondary = SecondaryLight, background = BackgroundLight, surface = SurfaceLight, onPrimary = OnPrimaryLight, onBackground = OnBackgroundLight, onSurface = OnBackgroundLight)
@Composable fun WhatsAppSaverTheme(dark: Boolean = isSystemInDarkTheme(), dyn: Boolean = true, content: @Composable () -> Unit) {
    val cs = when { dyn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { val c = LocalContext.current; if (dark) dynamicDarkColorScheme(c) else dynamicLightColorScheme(c) }; dark -> Dark; else -> Light }
    val v = LocalView.current; if (!v.isInEditMode) SideEffect { val w = (v.context as Activity).window; w.statusBarColor = cs.primary.toArgb(); WindowCompat.getInsetsController(w, v).isAppearanceLightStatusBars = !dark }
    MaterialTheme(colorScheme = cs, typography = Typography, content = content)
}
