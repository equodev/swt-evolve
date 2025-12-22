import 'package:flutter/material.dart';
import '../styles.dart';
import '../impl/widget_config.dart';
import 'button_theme_settings.dart';
import 'label_theme_settings.dart';
import 'text_theme_settings.dart';
import 'list_theme_settings.dart';
import 'tree_theme_settings.dart';

Color calculateBackgroundColor(int? backgroundColor, bool useDarkTheme) {
  return backgroundColor != null
      ? Color(0xFF000000 | backgroundColor)
      : (useDarkTheme ? const Color(0xFF2C2C2C) : const Color(0xFFF2F4F7));
}

ThemeData createLightTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: const Color(0xFF1847CA),
    brightness: Brightness.light,
  ).copyWith(
    primary: const Color(0xFF1847CA),
    onPrimary: const Color(0xFFFFFFFF),
    primaryContainer: const Color(0xFF002DAD),
    secondary: const Color(0xFF4338CA),
    onSecondary: const Color(0xFFFFFFFF),
    secondaryContainer: const Color(0xFF3730A3),
    error: const Color(0xFFDC2626),
    onError: const Color(0xFFFFFFFF),
    errorContainer: const Color(0xFFFEE2E2),
    surface: const Color(0xFFFFFFFF),
    onSurface: const Color(0xFF374151),
    onSurfaceVariant: const Color(0xFF6B7280),
    outline: const Color(0xFFD1D5DB),
    outlineVariant: const Color(0xFFE5E7EB),
    shadow: const Color(0xFF000000),
    scrim: const Color(0xFF000000),
    inverseSurface: const Color(0xFF1F2937),
    onInverseSurface: const Color(0xFFF9FAFB),
    inversePrimary: const Color(0xFF60A5FA),
    surfaceTint: const Color(0xFF1847CA),
  );

  final buttonTheme = getButtonLightTheme();
  final labelTheme = getLabelLightTheme();
  final textTheme = getTextLightTheme();
  final listTheme = getListLightTheme();
  final treeTheme = getTreeLightTheme();

  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    textTheme: TextTheme(
      displayLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 57.0,
        fontWeight: FontWeight.w400,
        letterSpacing: -0.25,
        color: colorScheme.onSurface,
      ),
      displayMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 45.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      displaySmall: TextStyle(
        fontFamily: 'System',
        fontSize: 36.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 32.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 28.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 24.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      titleLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 22.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      titleMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 16.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.15,
        color: colorScheme.onSurface,
      ),
      titleSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onSurface,
      ),
      labelLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onSurface,
      ),
      labelMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 12.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      labelSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 11.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      bodyLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 16.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      bodyMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.25,
        color: colorScheme.onSurface,
      ),
      bodySmall: TextStyle(
        fontFamily: 'System',
        fontSize: 12.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.4,
        color: colorScheme.onSurfaceVariant,
      ),
    ),
    primaryTextTheme: TextTheme(
      labelLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onPrimary,
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: colorScheme.surface,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.outline),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.outline),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.primary, width: 2.0),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.error),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.error, width: 2.0),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    ),
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.primary,
      minWidth: 64.0,
      height: 36.0,
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(6.0),
      ),
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: colorScheme.primary,
        foregroundColor: colorScheme.onPrimary,
        elevation: 0.0,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: colorScheme.primary,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        side: BorderSide(color: colorScheme.outline),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: colorScheme.primary,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    checkboxTheme: CheckboxThemeData(
      fillColor: MaterialStateProperty.resolveWith<Color?>((Set<MaterialState> states) {
        if (states.contains(MaterialState.selected)) {
          return colorScheme.primary;
        }
        return null;
      }),
      checkColor: MaterialStateProperty.all<Color>(colorScheme.onPrimary),
      side: BorderSide(color: colorScheme.outline, width: 2.0),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(3.0),
      ),
    ),
    radioTheme: RadioThemeData(
      fillColor: MaterialStateProperty.resolveWith<Color?>((Set<MaterialState> states) {
        if (states.contains(MaterialState.selected)) {
          return colorScheme.primary;
        }
        return colorScheme.outline;
      }),
    ),
    cardTheme: CardThemeData(
      color: colorScheme.surface,
      elevation: 0.0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(4.0),
        side: BorderSide(color: colorScheme.outlineVariant),
      ),
    ),
    dividerTheme: DividerThemeData(
      color: colorScheme.outlineVariant,
      thickness: 1.0,
      space: 1.0,
    ),
    extensions: <ThemeExtension<dynamic>>[
      buttonTheme,
      labelTheme,
      textTheme,
      listTheme,
      treeTheme,
    ],
  );
}

ThemeData createDarkTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: const Color(0xFF1847CA),
    brightness: Brightness.dark,
  ).copyWith(
    primary: const Color(0xFF1847CA),
    onPrimary: const Color(0xFFFFFFFF),
    primaryContainer: const Color(0xFF002DAD),
    secondary: const Color(0xFF5B21B6),
    onSecondary: const Color(0xFFFFFFFF),
    secondaryContainer: const Color(0xFF4C1D95),
    error: const Color(0xFFF87171),
    onError: const Color(0xFFFFFFFF),
    errorContainer: const Color(0xFF7F1D1D),
    surface: const Color(0xFF1F2937),
    onSurface: const Color(0xFFF9FAFB),
    onSurfaceVariant: const Color(0xFF9CA3AF),
    outline: const Color(0xFF6B7280),
    outlineVariant: const Color(0xFF4B5563),
    shadow: const Color(0xFF000000),
    scrim: const Color(0xFF000000),
    inverseSurface: const Color(0xFFF9FAFB),
    onInverseSurface: const Color(0xFF1F2937),
    inversePrimary: const Color(0xFF3B82F6),
    surfaceTint: const Color(0xFF1847CA),
  );

  final buttonTheme = getButtonDarkTheme();
  final labelTheme = getLabelDarkTheme();
  final textTheme = getTextDarkTheme();
  final listTheme = getListDarkTheme();
  final treeTheme = getTreeDarkTheme();

  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    textTheme: TextTheme(
      displayLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 57.0,
        fontWeight: FontWeight.w400,
        letterSpacing: -0.25,
        color: colorScheme.onSurface,
      ),
      displayMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 45.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      displaySmall: TextStyle(
        fontFamily: 'System',
        fontSize: 36.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 32.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 28.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      headlineSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 24.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      titleLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 22.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.0,
        color: colorScheme.onSurface,
      ),
      titleMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 16.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.15,
        color: colorScheme.onSurface,
      ),
      titleSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onSurface,
      ),
      labelLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onSurface,
      ),
      labelMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 12.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      labelSmall: TextStyle(
        fontFamily: 'System',
        fontSize: 11.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      bodyLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 16.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.5,
        color: colorScheme.onSurface,
      ),
      bodyMedium: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.25,
        color: colorScheme.onSurface,
      ),
      bodySmall: TextStyle(
        fontFamily: 'System',
        fontSize: 12.0,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.4,
        color: colorScheme.onSurfaceVariant,
      ),
    ),
    primaryTextTheme: TextTheme(
      labelLarge: TextStyle(
        fontFamily: 'System',
        fontSize: 14.0,
        fontWeight: FontWeight.w500,
        letterSpacing: 0.1,
        color: colorScheme.onPrimary,
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: colorScheme.surface,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.outline),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.outline),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.primary, width: 2.0),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.error),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6.0),
        borderSide: BorderSide(color: colorScheme.error, width: 2.0),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    ),
    buttonTheme: ButtonThemeData(
      textTheme: ButtonTextTheme.primary,
      minWidth: 64.0,
      height: 36.0,
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(6.0),
      ),
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: colorScheme.primary,
        foregroundColor: colorScheme.onPrimary,
        elevation: 0.0,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: colorScheme.primary,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        side: BorderSide(color: colorScheme.outline),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: colorScheme.primary,
        minimumSize: const Size(64.0, 36.0),
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(6.0),
        ),
        textStyle: const TextStyle(
          fontFamily: 'System',
          fontSize: 14.0,
          fontWeight: FontWeight.w500,
          letterSpacing: 0.0,
        ),
      ),
    ),
    checkboxTheme: CheckboxThemeData(
      fillColor: MaterialStateProperty.resolveWith<Color?>((Set<MaterialState> states) {
        if (states.contains(MaterialState.selected)) {
          return colorScheme.primary;
        }
        return null;
      }),
      checkColor: MaterialStateProperty.all<Color>(colorScheme.onPrimary),
      side: BorderSide(color: colorScheme.outline, width: 2.0),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(3.0),
      ),
    ),
    radioTheme: RadioThemeData(
      fillColor: MaterialStateProperty.resolveWith<Color?>((Set<MaterialState> states) {
        if (states.contains(MaterialState.selected)) {
          return colorScheme.primary;
        }
        return colorScheme.outline;
      }),
    ),
    cardTheme: CardThemeData(
      color: colorScheme.surface,
      elevation: 0.0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(4.0),
        side: BorderSide(color: colorScheme.outlineVariant),
      ),
    ),
    dividerTheme: DividerThemeData(
      color: colorScheme.outlineVariant,
      thickness: 1.0,
      space: 1.0,
    ),
    extensions: <ThemeExtension<dynamic>>[
      buttonTheme,
      labelTheme,
      textTheme,
      listTheme,
      treeTheme,
    ],
  );
}