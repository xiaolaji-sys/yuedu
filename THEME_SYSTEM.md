# Dynamic Theme System

This project implements a Material You dynamic theme system with support for:

- **Material You Design**: Dynamic color generation based on user's wallpaper
- **Dark/Light Theme Support**: Automatic switching based on system settings
- **Compose UI Integration**: Seamless integration with Jetpack Compose
- **Custom Color Schemes**: Predefined light and dark color palettes

## Files Created/Modified

### Core Theme Package (`com.legado.core.theme`)
- **ColorScheme.kt**: Defines light and dark color schemes following Material 3 design guidelines
- **Theme.kt**: Main theme composable with dynamic color support (Android 12+)

### Existing Theme Files (Updated)
- **app/src/main/java/com/legado/app/ui/theme/Type.kt**: Enhanced typography definitions
- **app/src/main/java/com/legado/app/MainActivity.kt**: Updated to use dynamic theme

## Features

### Dynamic Colors (Android 12+)
When running on Android 12 (API 31+) or higher, the app automatically generates colors from the user's wallpaper using Material You principles.

### Manual Theme Control
You can control themes programmatically:
```kotlin
LegadoDynamicTheme(
    darkTheme = true, // Force dark theme
    dynamicColor = false // Disable dynamic colors
) {
    // Your content
}
```

### Color Scheme Structure
Each color scheme includes:
- Primary colors (main brand colors)
- Secondary colors (supporting colors)
- Tertiary colors (accent colors)
- Error colors
- Background and surface colors
- Outline and inverse variants

## Usage

Replace your existing theme usage with:

```kotlin
import com.legado.core.theme.LegadoDynamicTheme

// In your Composable
@Composable
fun MyScreen() {
    LegadoDynamicTheme {
        // Your content here
        Text("Hello, Material You!")
    }
}
```

## Requirements

- Android API 21+ (minimum SDK)
- Android 12+ for dynamic color support
- Jetpack Compose BOM 2023.10.01+
- Material 3 library

## Backward Compatibility

The system gracefully falls back to predefined color schemes when:
- Running on Android versions below 12
- Dynamic color is disabled
- No wallpaper color extraction available

All existing code continues to work without changes, but you'll get enhanced theming by using the new `LegadoDynamicTheme`.