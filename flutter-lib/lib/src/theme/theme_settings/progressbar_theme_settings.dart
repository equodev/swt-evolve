import 'package:flutter/material.dart';
import '../theme_extensions/progressbar_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ProgressBarThemeExtension getProgressBarLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getProgressBarTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ProgressBarThemeExtension getProgressBarDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getProgressBarTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ProgressBarThemeExtension _getProgressBarTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ProgressBarThemeExtension(
    // Colors
    backgroundColor: colorScheme.surfaceVariant,
    progressColor: colorScheme.primary,
    disabledProgressColor: colorSchemeExtension.onSurfaceVariantDisabled,
    borderColor: colorScheme.outline,
    
    // Sizing
    defaultWidth: 200.0,
    defaultHeight: 20.0,
    borderWidth: 1.0,
    borderRadius: 4.0,
    
    // Animation settings
    indeterminateDuration: const Duration(milliseconds: 1000),
    indeterminateBarWidthFactor: 0.3,
    indeterminateCurve: Curves.linear,
    indeterminateEnableSizeAnimation: true,
    
    // Type A: Starts large, accelerates and shrinks
    indeterminateSizeStartA: 0.7,      
    indeterminateSizeMidA: 0.3,        
    indeterminateSizeEndA: 0.1,        
    indeterminateSpeedFirstA: 1.4,     
    indeterminateSpeedSecondA: 1.8,    
    
    // Type B: Starts medium, decelerates and grows
    indeterminateSizeStartB: 0.2,      
    indeterminateSizeMidB: 0.6,        
    indeterminateSizeEndB: 0.9,        
    indeterminateSpeedFirstB: 1.2,     
    indeterminateSpeedSecondB: 0.9,    
  );
}