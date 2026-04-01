import 'package:flutter/material.dart';
import '../styles.dart';
import '../impl/config_flags.dart';
import '../impl/widget_config.dart';
import 'theme_settings/button_theme_settings.dart';
import 'theme_settings/label_theme_settings.dart';
import 'theme_settings/text_theme_settings.dart';
import 'theme_settings/tabfolder_theme_settings.dart';
import 'theme_settings/tabitem_theme_settings.dart';
import 'theme_settings/tree_theme_settings.dart';
import 'theme_settings/ctabfolder_theme_settings.dart';
import 'theme_settings/ctabitem_theme_settings.dart';
import 'theme_settings/toolbar_theme_settings.dart';
import 'theme_settings/toolitem_theme_settings.dart';
import 'theme_settings/table_theme_settings.dart';
import 'theme_settings/combo_theme_settings.dart';
import 'theme_extensions/color_scheme_extension.dart';
import 'theme_settings/ccombo_theme_settings.dart';
import 'theme_settings/progressbar_theme_settings.dart';
import 'theme_settings/list_theme_settings.dart';
import 'theme_settings/clabel_theme_settings.dart';
import 'theme_settings/link_theme_settings.dart';
import 'theme_settings/group_theme_settings.dart';
import 'theme_settings/expandbar_theme_settings.dart';
import 'theme_settings/expanditem_theme_settings.dart';
import 'theme_settings/slider_theme_settings.dart';
import 'theme_settings/spinner_theme_settings.dart';
import 'theme_settings/scrolledcomposite_theme_settings.dart';
import 'theme_settings/scale_theme_settings.dart';
import 'theme_settings/menu_theme_settings.dart';
import 'theme_settings/menuitem_theme_settings.dart';
import 'theme_settings/coolbar_theme_settings.dart';
import 'theme_settings/coolitem_theme_settings.dart';
import 'theme_settings/tooltip_theme_settings.dart';
import 'theme_settings/sash_theme_settings.dart';
import 'theme_settings/canvas_theme_settings.dart';
import 'theme_settings/styledtext_theme_settings.dart';
import 'theme_settings/theme_color_palette_theme_settings.dart';

ColorScheme _resolveWidgetColorScheme(
  String widgetKey,
  ColorScheme fallbackColorScheme,
  Brightness brightness,
) {
  final flags = getConfigFlags();
  final widgetHex = _themeColorForWidget(flags, widgetKey);
  final color = _parseThemeColor(widgetHex);
  if (color == null) {
    return fallbackColorScheme;
  }
  return ColorScheme.fromSeed(seedColor: color, brightness: brightness);
}

ColorSchemeExtension _resolveWidgetColorSchemeExtension(
  String widgetKey,
  ColorScheme fallbackColorScheme,
  Brightness brightness,
) {
  return createColorSchemeExtension(
    _resolveWidgetColorScheme(widgetKey, fallbackColorScheme, brightness),
  );
}

String? _themeColorForWidget(ConfigFlags flags, String widgetKey) {
  final map = flags.theme_colors_by_widget;
  if (map == null || map.isEmpty) {
    return null;
  }
  return map[widgetKey.trim().toLowerCase()];
}

Color? _parseThemeColor(String? colorHex) {
  if (colorHex == null) {
    return null;
  }
  final sanitized = colorHex.trim();
  String argbHex;
  if (RegExp(r'^[0-9a-fA-F]{6}$').hasMatch(sanitized)) {
    argbHex = 'FF$sanitized';
  } else if (RegExp(r'^[0-9a-fA-F]{8}$').hasMatch(sanitized)) {
    argbHex = sanitized;
  } else {
    return null;
  }
  final value = int.tryParse(argbHex, radix: 16);
  if (value == null) {
    return null;
  }
  return Color(value);
}

Color? parseGlobalSeedColor(ConfigFlags flags) {
  return _parseThemeColor(flags.theme_color);
}

Color? parseThemeColorFromHex(String? colorHex) => _parseThemeColor(colorHex);

Color calculateBackgroundColor(int? backgroundColor, bool useDarkTheme) {
  return backgroundColor != null
      ? Color(0xFF000000 | backgroundColor)
      : (useDarkTheme ? const Color(0xFF2C2C2C) : const Color(0xFFF2F4F7));
}

ColorScheme createLightColorScheme() {
  return ColorScheme.fromSeed(
    seedColor: const Color(0xFF626262),
    brightness: Brightness.light,
  ).copyWith(
    primary: const Color(0xFF5959EB), // #5959EB
    onPrimary: const Color(0xFFFCFCFC), // #FCFCFC
    primaryContainer: const Color(0xFF2F2F2F),
    
    secondary: const Color(0xFFDEDEFD), // #DEDEFD
    onSecondary: const Color(0xFF4545BF), // #4545BF
    secondaryContainer: const Color(0xFFDEDEFD),// #DEDEFD
    onSecondaryContainer: const Color(0xFF4545BF), // #4545BF
    
    tertiary: const Color(0x00FFFFFF), // #FFFFFF
    onTertiary: const Color(0xFF0F1866), // #0F1866
    
    surface: const Color(0xFFFFFFFF), // #FFFFFF
    onSurface: const Color(0xFF171819), // #171819
    onSurfaceVariant: const Color(0xFF797B80), // #797B80
    surfaceContainerHighest: const Color(0xFFFAFBFC), // #FAFBFC
    surfaceContainerHigh: const Color(0xFFF7F8FA), // #F7F8FA
    surfaceContainerLow: const Color(0xFFF5F5F5), // #F5F5F5
    
    outline: const Color(0x1A171819), // #1A171819
    outlineVariant: const Color(0x4D171819), // #4D171819
    
    error: const Color(0xFFDC0A56), // #DC0A56
    onError: const Color(0xFFFFFFFF), // #FFFFFF
    errorContainer: const Color(0xFFFAF2F4), // #FAF2F4
    onErrorContainer: const Color(0xFF59040D), // #59040D
    
    surfaceVariant: const Color(0xFFF0F2F5), // #F0F2F5
    
    // Other
    shadow: const Color(0xFF000000), // #000000
    scrim: const Color(0xFF000000), // #000000
    inverseSurface: const Color(0xFF1F2937), // #1F2937
    onInverseSurface: const Color(0xFFF9FAFB), // #F9FAFB
    inversePrimary: const Color(0xFF626262),
    surfaceTint: const Color(0xFF626262),
  );
}

ColorScheme createDarkColorScheme() {
  return ColorScheme.fromSeed(
    seedColor: const Color(0xFF626262),
    brightness: Brightness.dark,
  ).copyWith(
    primary: const Color(0xFF9A9A9A), 
    onPrimary: const Color(0xFF141414),
    primaryContainer: const Color(0xFF2A2A2A),
    secondary: const Color(0xFF626262),
    onSecondary: const Color(0xFFFFFFFF),
    secondaryContainer: const Color(0xFF3A3A3A),
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
    inversePrimary: const Color(0xFF626262),
    surfaceTint: const Color(0xFF626262),
  );
}

ColorSchemeExtension createColorSchemeExtension([ColorScheme? colorScheme]) {
  if (colorScheme != null) {
    return _createColorSchemeExtensionFromColorScheme(colorScheme);
  }

  return ColorSchemeExtension(
    primaryHovered: const Color(0xFF4A4A4A),
    primaryBorder: const Color(0xFF3B3B3B),
    primaryBorderDisabled: const Color(0x33171819), // #33171819
    onPrimaryVariantDisabled: const Color(0x80171819), // #80171819
    
    secondaryPressed: const Color(0xFFDCDCDC),
    secondaryBold: const Color(0xFFE9E9E9),
    secondaryBorder: const Color(0xFFD0D0D0),
    secondaryBorderDisabled: const Color(0x33171819), // #33171819
    onSecondaryVariantDisabled: const Color(0x80171819), // #80171819
    
    primaryVariant: const Color(0xFF626262),
    onPrimaryVariant: const Color(0xFFFCFCFC), // #FCFCFC
    primaryVariantDisabled: const Color(0xFFF4F4F8), // #F4F4F8
    secondaryVariant: const Color(0xFFF0F0F0),
    onSecondaryVariant: const Color(0xFF4A4A4A),
    secondaryVariantBorder: const Color(0xFFDDDDDD),
    
    tertiaryPressed: const Color(0x33171819), // #33171819
    tertiaryHovered: const Color(0x1A171819), // #1A171819
    tertiaryBorder: const Color(0x00FFFFFF), // #00FFFFFF
    tertiaryBorderDisabled: const Color(0x00FFFFFF), // #00FFFFFF
    onTertiaryVariantDisabled: const Color(0x80171819), // #80171819
    
    surfaceFocused: const Color(0xFFF2F3FA), // #F2F3FA
    surfaceBorderEnabled: const Color(0x1A171819), // #1A171819
    surfaceBorderHovered: const Color(0x4D171819), // #4D171819
    surfaceBorderFocused: const Color(0xFF4A4A4A),
    surfaceBorderDisabled: const Color(0x33171819), // #33171819
    surfaceBorderBoldEnabled: const Color(0x80171819), // #80171819
    surfaceBorderDataDisplay: const Color(0xFFFFFFFF), // #FFFFFFFF
    surfacePlaceholder: const Color(0xFF797B7F), // #797B7F
    onSurfaceVariantError: const Color(0xFFDC0A56), // #DC0A56
    onSurfaceVariantWarning: const Color(0xFFDE7A2D), // #DE7A2D
    onSurfaceVariantDisabled: const Color(0x17181980), // #17181980
    onSurfaceVariantSmallError: const Color(0xFF99073C), // #99073C
    
    errorHovered: const Color(0xFFB20846), // #B20846
    errorBorder: const Color(0xFF99073C), // #99073C
    errorBorderDisabled: const Color(0x33171819), // #33171819
    onErrorVariantDisabled: const Color(0x80171819), // #80171819
    errorContainerPressed: const Color(0xFFF0C9D4), // #F0C9D4
    errorContainerHovered: const Color(0xFFF5E1E6), // #F5E1E6
    onErrorContainerVariantError: const Color(0xFF99073C), // #99073C
    onErrorContainerVariantDisabled: const Color(0x80171819), // #80171819
    onErrorContainerBorder: const Color(0xFFF0C9D4), // #F0C9D4
    onErrorContainerBorderDisabled: const Color(0x33171819), // #33171819
    onErrorContainerBorderHovered: const Color(0xFFDC0A56), // #DC0A56
    onErrorContainerBorderEnabled: const Color(0xFFF0C9D4), // #F0C9D4
    onErrorContainerBorderFocused: const Color(0xFF59040D), // #59040D
    onErrorContainerPlaceholder: const Color(0xFFDC0A56), // #DC0A56
    errorTextEnabled: const Color(0x00FFFFFF), // #00FFFFFF
    errorTextHovered: const Color(0xFFFAF2F4), // #FAF2F4
    errorTextPressed: const Color(0xFFF5E1E6), // #F5E1E6
    onErrorTextVariant: const Color(0xFF59040D), // #59040D
    onErrorTextVariantDisabled: const Color(0x80171819), // #80171819
    
    warning: const Color(0xFFDE7A2D), // #DE7A2D
    warningPressed: const Color(0xFF99541F), // #99541F
    warningHovered: const Color(0xFFB26224), // #B26224
    onWarning: const Color(0xFFFFFFFF), // #FFFFFF
    onWarningVariantDisabled: const Color(0x80171819), // #80171819
    warningBorder: const Color(0xFF99541F), // #99541F
    warningBorderDisabled: const Color(0x33171819), // #33171819
    warningTextEnabled: const Color(0x00FFFFFF), // #00FFFFFF
    warningTextPressed: const Color(0xFFF5EAE1), // #F5EAE1
    warningTextHovered: const Color(0xFFFAF3ED), // #FAF3ED
    onWarningTextVariant: const Color(0xFF4D2100), // #4D2100
    onWarningTextVariantDisabled: const Color(0x80171819), // #80171819
    warningContainer: const Color(0xFFFAF3ED), // #FAF3ED
    warningContainerPressed: const Color(0xFFF0DAC9), // #F0DAC9
    warningContainerHovered: const Color(0xFFF5EAE1), // #F5EAE1
    onWarningContainerVariant: const Color(0xFF59040D), // #59040D
    onWarningContainerVariantDisabled: const Color(0x80171819), // #80171819
    onWarningContainerBorder: const Color(0xFFF0DAC9), // #F0DAC9
    onWarningContainerBorderDisabled: const Color(0x33171819), // #33171819
    
    success: const Color(0xFF1BBB77), // #1BBB77
    successPressed: const Color(0xFF128051), // #128051
    successHovered: const Color(0xFF169961), // #169961
    onSuccess: const Color(0xFFFFFFFF), // #FFFFFF
    onSuccessVariantDisabled: const Color(0x80171819),
    successBorder: const Color(0xFF128051), // #128051
    successBorderDisabled: const Color(0x33171819), // #33171819
    successContainer: const Color(0xFFE6F2ED), // #E6F2ED
    successContainerPressed: const Color(0xFFC1E5D6), // #C1E5D6
    successContainerHovered: const Color(0xFFDAEDE5), // #DAEDE5
    onSuccessContainerVariant: const Color(0xFF0C3322), // #0C3322
    onSuccessContainerVariantDisabled: const Color(0x80171819), // #80171819
    onSuccessContainerBorder: const Color(0xFFC1E5D6), // #C1E5D6
    onSuccessContainerBorderDisabled: const Color(0x33171819), // #33171819
    
    statusDefault: const Color(0xFF606266), // #606266
    statusOnDefault: const Color(0xFFFFFFFF), // #FFFFFF
    statusDefaultContainer: const Color(0xFFE9EBF0), // #E9EBF0
    statusOnDefaultContainer: const Color(0xFF2F3033), // #2F3033
    statusInform: const Color(0xFF626262),
    statusOnInform: const Color(0xFFFFFFFF), // #FFFFFF
    statusInformContainer: const Color(0xFFE9EBF0),
    statusOnInformContainer: const Color(0xFF2F3033),
    statusOnErrorContainer: const Color(0xFF59040D), // #59040D
    statusSuccess: const Color(0xFF1BBB77), // #1BBB77
    statusOnSuccessContainer: const Color(0xFF0C3322),
    statusWarning: const Color(0xFFDE7A2D), // #DE7A2D
    statusWarningContainer: const Color(0xFFFAF3ED), // #FAF3ED
    statusOnWarningContainer: const Color(0xFF4D2100), // #4D2100
    statusCaution: const Color(0xFFD4DB2D), // #D4DB2D
    statusOnCaution: const Color(0xFF4A4D00), // #4A4D00
    statusCautionContainer: const Color(0xFFF9FAED), // #F9FAED
    statusOnCautionContainer: const Color(0xFF4A4D00), // #4A4D00
    
    neutral: const Color(0xFFF0F2F5), // #F0F2F5
    onNeutralBorder: const Color(0xFFFFFFFF), // #FFFFFF
    onNeutralVariant: const Color(0xFF171819), // #171819
    
    stateDefaultEnabled: const Color(0x00FFFFFF), // #00FFFFFF
    stateDefaultHovered: const Color(0x1F000000), // #1F000000
    stateDefaultPressed: const Color(0x33000000), // #33000000
    stateOnContainerEnabled: const Color(0x00FFFFFF), // #00FFFFFF
    stateOnContainerHovered: const Color(0x0A000000), // #0A000000
    stateOnContainerPressed: const Color(0x14000000), // #14000000
    
    labelInputDefault: const Color(0xFF47494D), // #47494D
    labelInputDisabled: const Color(0xFF47494D), // #47494D

    ctabFolderSelectedTextColor: const Color(0xFFFFFFFF), // #FFFFFFFF
    ctabFolderHighlightColor: const Color(0xFF626262),
    ctabFolderUnselectedColor: const Color(0xFFE7E8E8), // #E7E8E8
    surfaceToolbar: const Color(0xFFF4F4F4), // #F4F4F4 light
    toolbarDivider: const Color(0xFFDBDBDB), // #DBDBDB light
  );
}

ColorSchemeExtension _createColorSchemeExtensionFromColorScheme(ColorScheme colorScheme) {
  return ColorSchemeExtension(
    primaryHovered: colorScheme.primary,
    primaryBorder: colorScheme.primary,
    primaryBorderDisabled: colorScheme.outlineVariant,
    onPrimaryVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    secondaryPressed: colorScheme.secondaryContainer,
    secondaryBold: colorScheme.secondaryContainer,
    secondaryBorder: colorScheme.secondary,
    secondaryBorderDisabled: colorScheme.outlineVariant,
    onSecondaryVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    primaryVariant: colorScheme.primary,
    onPrimaryVariant: colorScheme.onPrimary,
    primaryVariantDisabled: colorScheme.surfaceContainerHigh,
    secondaryVariant: colorScheme.secondary,
    onSecondaryVariant: colorScheme.onSecondaryContainer,
    secondaryVariantBorder: colorScheme.outlineVariant,
    tertiaryPressed: colorScheme.tertiary.withOpacity(0.2),
    tertiaryHovered: colorScheme.tertiary.withOpacity(0.12),
    tertiaryBorder: colorScheme.tertiary.withOpacity(0),
    tertiaryBorderDisabled: colorScheme.tertiary.withOpacity(0),
    onTertiaryVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    surfaceFocused: colorScheme.surfaceContainerHighest,
    surfaceBorderEnabled: colorScheme.outline.withOpacity(0.12),
    surfaceBorderHovered: colorScheme.outline.withOpacity(0.3),
    surfaceBorderFocused: colorScheme.primary,
    surfaceBorderDisabled: colorScheme.outlineVariant,
    surfaceBorderBoldEnabled: colorScheme.outline,
    surfaceBorderDataDisplay: colorScheme.surface,
    surfacePlaceholder: colorScheme.onSurfaceVariant,
    onSurfaceVariantError: colorScheme.error,
    onSurfaceVariantWarning: colorScheme.tertiary,
    onSurfaceVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    onSurfaceVariantSmallError: colorScheme.error,
    errorHovered: colorScheme.error.withOpacity(0.85),
    errorBorder: colorScheme.error,
    errorBorderDisabled: colorScheme.outlineVariant,
    onErrorVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    errorContainerPressed: colorScheme.errorContainer.withOpacity(0.8),
    errorContainerHovered: colorScheme.errorContainer.withOpacity(0.9),
    onErrorContainerVariantError: colorScheme.onErrorContainer,
    onErrorContainerVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    onErrorContainerBorder: colorScheme.errorContainer,
    onErrorContainerBorderDisabled: colorScheme.outlineVariant,
    onErrorContainerBorderHovered: colorScheme.error,
    onErrorContainerBorderEnabled: colorScheme.errorContainer,
    onErrorContainerBorderFocused: colorScheme.onErrorContainer,
    onErrorContainerPlaceholder: colorScheme.error,
    errorTextEnabled: colorScheme.error.withOpacity(0),
    errorTextHovered: colorScheme.errorContainer,
    errorTextPressed: colorScheme.errorContainer.withOpacity(0.85),
    onErrorTextVariant: colorScheme.onErrorContainer,
    onErrorTextVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    warning: colorScheme.tertiary,
    warningPressed: colorScheme.tertiary.withOpacity(0.8),
    warningHovered: colorScheme.tertiary.withOpacity(0.9),
    onWarning: colorScheme.onTertiary,
    onWarningVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    warningBorder: colorScheme.tertiary,
    warningBorderDisabled: colorScheme.outlineVariant,
    warningTextEnabled: colorScheme.tertiary.withOpacity(0),
    warningTextPressed: colorScheme.tertiaryContainer.withOpacity(0.8),
    warningTextHovered: colorScheme.tertiaryContainer.withOpacity(0.9),
    onWarningTextVariant: colorScheme.onTertiaryContainer,
    onWarningTextVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    warningContainer: colorScheme.tertiaryContainer,
    warningContainerPressed: colorScheme.tertiaryContainer.withOpacity(0.8),
    warningContainerHovered: colorScheme.tertiaryContainer.withOpacity(0.9),
    onWarningContainerVariant: colorScheme.onTertiaryContainer,
    onWarningContainerVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    onWarningContainerBorder: colorScheme.tertiaryContainer,
    onWarningContainerBorderDisabled: colorScheme.outlineVariant,
    success: colorScheme.primary,
    successPressed: colorScheme.primary.withOpacity(0.8),
    successHovered: colorScheme.primary.withOpacity(0.9),
    onSuccess: colorScheme.onPrimary,
    onSuccessVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    successBorder: colorScheme.primary,
    successBorderDisabled: colorScheme.outlineVariant,
    successContainer: colorScheme.primaryContainer,
    successContainerPressed: colorScheme.primaryContainer.withOpacity(0.8),
    successContainerHovered: colorScheme.primaryContainer.withOpacity(0.9),
    onSuccessContainerVariant: colorScheme.onPrimaryContainer,
    onSuccessContainerVariantDisabled: colorScheme.onSurface.withOpacity(0.5),
    onSuccessContainerBorder: colorScheme.primaryContainer,
    onSuccessContainerBorderDisabled: colorScheme.outlineVariant,
    statusDefault: colorScheme.outline,
    statusOnDefault: colorScheme.surface,
    statusDefaultContainer: colorScheme.surfaceContainerHigh,
    statusOnDefaultContainer: colorScheme.onSurface,
    statusInform: colorScheme.primary,
    statusOnInform: colorScheme.onPrimary,
    statusInformContainer: colorScheme.primaryContainer,
    statusOnInformContainer: colorScheme.onPrimaryContainer,
    statusOnErrorContainer: colorScheme.onErrorContainer,
    statusSuccess: colorScheme.primary,
    statusOnSuccessContainer: colorScheme.onPrimaryContainer,
    statusWarning: colorScheme.tertiary,
    statusWarningContainer: colorScheme.tertiaryContainer,
    statusOnWarningContainer: colorScheme.onTertiaryContainer,
    statusCaution: colorScheme.secondary,
    statusOnCaution: colorScheme.onSecondary,
    statusCautionContainer: colorScheme.secondaryContainer,
    statusOnCautionContainer: colorScheme.onSecondaryContainer,
    neutral: colorScheme.surfaceVariant,
    onNeutralBorder: colorScheme.surface,
    onNeutralVariant: colorScheme.onSurface,
    stateDefaultEnabled: colorScheme.primary.withOpacity(0),
    stateDefaultHovered: colorScheme.onSurface.withOpacity(0.12),
    stateDefaultPressed: colorScheme.onSurface.withOpacity(0.2),
    stateOnContainerEnabled: colorScheme.surface.withOpacity(0),
    stateOnContainerHovered: colorScheme.onSurface.withOpacity(0.04),
    stateOnContainerPressed: colorScheme.onSurface.withOpacity(0.08),
    labelInputDefault: colorScheme.onSurfaceVariant,
    labelInputDisabled: colorScheme.onSurfaceVariant,
    ctabFolderSelectedTextColor: colorScheme.onSurface,
    ctabFolderHighlightColor: colorScheme.primary,
    ctabFolderUnselectedColor: colorScheme.surfaceContainerHigh,
    surfaceToolbar: colorScheme.surfaceContainerLow,
    toolbarDivider: colorScheme.outlineVariant,
  );
}

TextTheme createMaterialTextTheme(ColorScheme colorScheme) {
  return TextTheme(
    displayLarge: TextStyle(
      fontFamily: 'Inter',
      fontSize: 48.0,
      fontWeight: FontWeight.w500,
      letterSpacing: -0.96,
      height: 56.0 / 48.0, 
      color: colorScheme.onSurface,
    ),
    headlineLarge: TextStyle(
      fontFamily: 'Inter',
      fontSize: 32.0,
      fontWeight: FontWeight.w600,
      letterSpacing: -1.0,
      height: 40.0 / 32.0, 
      color: colorScheme.onSurface,
    ),
    titleLarge: TextStyle(
      fontFamily: 'Inter',
      fontSize: 24.0,
      fontWeight: FontWeight.w600,
      letterSpacing: 0.0,
      height: 32.0 / 24.0, 
      color: colorScheme.onSurface,
      ),
    titleMedium: TextStyle(
      fontFamily: 'Inter',
      fontSize: 16.0,
      fontWeight: FontWeight.w600,
      letterSpacing: 0.32,
      height: 24.0 / 16.0, 
      color: colorScheme.onSurface,
    ),
    titleSmall: TextStyle(
      fontFamily: 'Inter',
      fontSize: 14.0,
      fontWeight: FontWeight.w600,
      letterSpacing: 0.14,
      height: 20.0 / 14.0, 
      color: colorScheme.onSurface,
    ),
    labelMedium: TextStyle(
      fontFamily: 'Inter',
      fontSize: 14.0,
      fontWeight: FontWeight.w500,
      letterSpacing: 0.0,
      height: 20.0 / 14.0, 
      color: colorScheme.onSurface,
    ),
    labelSmall: TextStyle(
      fontFamily: 'Inter',
      fontSize: 11.0,
      fontWeight: FontWeight.w600,
      letterSpacing: 0.0,
      height: 16.0 / 11.0,
      color: colorScheme.onSurface,
    ),
    bodyLarge: TextStyle(
      fontFamily: 'Inter',
      fontSize: 14.0,
      fontWeight: FontWeight.w500,
      height: 20.0 / 14.0,
      letterSpacing: 0.0,
      color: colorScheme.onSurface,
    ),
    bodyMedium: TextStyle(
      fontFamily: 'Inter',
      fontSize: 14.0,
      fontWeight: FontWeight.w500,
      letterSpacing: 0.0,
      height: 16.0 / 14.0,
      color: colorScheme.onSurface,
    ),
    bodySmall: TextStyle(
      fontFamily: 'Inter',
      fontSize: 12.0,
      fontWeight: FontWeight.w400,
      letterSpacing: 0.24,
      height: 16.0 / 12.0,
      color: colorScheme.onSurfaceVariant,
    ),
  );
}

ThemeData createLightDefaultTheme(int? backgroundColor, {Color? seedColor}) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: seedColor ?? const Color(0xFF1847CA),
    brightness: Brightness.light,
  );

  final defaultTextTheme = ThemeData(useMaterial3: true, colorScheme: colorScheme).textTheme;
  final colorSchemeExtension = createColorSchemeExtension(colorScheme);
  
  final textThemeExtension = getTextLightTheme(
    colorScheme: _resolveWidgetColorScheme('text', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('text', colorScheme, Brightness.light),
  );
  final buttonTheme = getButtonLightTheme(
    colorScheme: _resolveWidgetColorScheme('button', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('button', colorScheme, Brightness.light),
  );
  final labelTheme = getLabelLightTheme(
    colorScheme: _resolveWidgetColorScheme('label', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('label', colorScheme, Brightness.light),
  );
  final tabFolderTheme = getTabFolderLightTheme(
    colorScheme: _resolveWidgetColorScheme('tabfolder', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tabfolder', colorScheme, Brightness.light),
  );
  final tabItemTheme = getTabItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('tabitem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tabitem', colorScheme, Brightness.light),
  );
  final treeTheme = getTreeLightTheme(
    colorScheme: _resolveWidgetColorScheme('tree', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tree', colorScheme, Brightness.light),
  );
  final ctabFolderTheme = getCTabFolderLightTheme(
    colorScheme: _resolveWidgetColorScheme('ctabfolder', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ctabfolder', colorScheme, Brightness.light),
  );
  final ctabItemTheme = getCTabItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('ctabitem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ctabitem', colorScheme, Brightness.light),
  );
  final toolbarTheme = getToolBarLightTheme(
    colorScheme: _resolveWidgetColorScheme('toolbar', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('toolbar', colorScheme, Brightness.light),
  );
  final toolItemTheme = getToolItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('toolitem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('toolitem', colorScheme, Brightness.light),
  );
  final tableTheme = getTableLightTheme(
    colorScheme: _resolveWidgetColorScheme('table', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('table', colorScheme, Brightness.light),
  );
  final comboTheme = getComboLightTheme(
    colorScheme: _resolveWidgetColorScheme('combo', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('combo', colorScheme, Brightness.light),
  );
  final ccomboTheme = getCComboLightTheme(
    colorScheme: _resolveWidgetColorScheme('ccombo', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ccombo', colorScheme, Brightness.light),
  );
  final progressBarTheme = getProgressBarLightTheme(
    colorScheme: _resolveWidgetColorScheme('progressbar', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('progressbar', colorScheme, Brightness.light),
  );
  final listTheme = getListLightTheme(
    colorScheme: _resolveWidgetColorScheme('list', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('list', colorScheme, Brightness.light),
  );
  final clabelTheme = getCLabelLightTheme(
    colorScheme: _resolveWidgetColorScheme('clabel', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('clabel', colorScheme, Brightness.light),
  );
  final linkTheme = getLinkLightTheme(
    colorScheme: _resolveWidgetColorScheme('link', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('link', colorScheme, Brightness.light),
  );
  final groupTheme = getGroupLightTheme(
    colorScheme: _resolveWidgetColorScheme('group', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('group', colorScheme, Brightness.light),
  );
  final expandBarTheme = getExpandBarLightTheme(
    colorScheme: _resolveWidgetColorScheme('expandbar', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('expandbar', colorScheme, Brightness.light),
  );
  final expandItemTheme = getExpandItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('expanditem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('expanditem', colorScheme, Brightness.light),
  );
  final sliderTheme = getSliderLightTheme(
    colorScheme: _resolveWidgetColorScheme('slider', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('slider', colorScheme, Brightness.light),
  );
  final spinnerTheme = getSpinnerLightTheme(
    colorScheme: _resolveWidgetColorScheme('spinner', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('spinner', colorScheme, Brightness.light),
  );
  final scrolledCompositeTheme = getScrolledCompositeLightTheme(
    colorScheme: _resolveWidgetColorScheme('scrolledcomposite', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('scrolledcomposite', colorScheme, Brightness.light),
  );
  final scaleTheme = getScaleLightTheme(
    colorScheme: _resolveWidgetColorScheme('scale', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('scale', colorScheme, Brightness.light),
  );
  final menuTheme = getMenuLightTheme(
    colorScheme: _resolveWidgetColorScheme('menu', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('menu', colorScheme, Brightness.light),
  );
  final menuItemTheme = getMenuItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('menuitem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('menuitem', colorScheme, Brightness.light),
  );
  final coolBarTheme = getCoolBarLightTheme(
    colorScheme: _resolveWidgetColorScheme('coolbar', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('coolbar', colorScheme, Brightness.light),
  );
  final coolItemTheme = getCoolItemLightTheme(
    colorScheme: _resolveWidgetColorScheme('coolitem', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('coolitem', colorScheme, Brightness.light),
  );
  final tooltipTheme = getTooltipLightTheme(
    colorScheme: _resolveWidgetColorScheme('tooltip', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tooltip', colorScheme, Brightness.light),
  );
  final sashTheme = getSashLightTheme(
    colorScheme: _resolveWidgetColorScheme('sash', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('sash', colorScheme, Brightness.light),
  );
  final canvasTheme = getCanvasLightTheme(
    colorScheme: _resolveWidgetColorScheme('canvas', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('canvas', colorScheme, Brightness.light),
  );
  final styledTextTheme = getStyledTextLightTheme(
    colorScheme: _resolveWidgetColorScheme('styledtext', colorScheme, Brightness.light),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('styledtext', colorScheme, Brightness.light),
  );
  final themeColorPaletteTheme = getThemeColorPaletteLightTheme(
    colorScheme: _resolveWidgetColorScheme('toolbar', colorScheme, Brightness.light),
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    extensions: <ThemeExtension<dynamic>>[
      colorSchemeExtension,
      buttonTheme,
      labelTheme,
      textThemeExtension,
      tabFolderTheme,
      tabItemTheme,
      treeTheme,
      ctabFolderTheme,
      ctabItemTheme,
      toolbarTheme,
      toolItemTheme,
      tableTheme,
      comboTheme,
      ccomboTheme,
      progressBarTheme,
      listTheme,
      clabelTheme,
      linkTheme,
      groupTheme,
      expandBarTheme,
      expandItemTheme,
      sliderTheme,
      spinnerTheme,
      scrolledCompositeTheme,
      scaleTheme,
      menuTheme,
      menuItemTheme,
      coolBarTheme,
      coolItemTheme,
      tooltipTheme,
      sashTheme,
      canvasTheme,
      styledTextTheme,
      themeColorPaletteTheme,
    ],
  );
}

ThemeData createLightNonDefaultTheme(
  int? backgroundColor, {
  ColorScheme? overrideColorScheme,
  ColorSchemeExtension? overrideColorSchemeExtension,
}) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);

  final colorScheme = overrideColorScheme ?? createLightColorScheme();
  final colorSchemeExtension = overrideColorSchemeExtension ?? createColorSchemeExtension();

  final materialTextTheme = createMaterialTextTheme(colorScheme);
  final textThemeExtension = getTextLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );

  final buttonTheme = getButtonLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final labelTheme = getLabelLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabFolderTheme = getTabFolderLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabItemTheme = getTabItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final treeTheme = getTreeLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabFolderTheme = getCTabFolderLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabItemTheme = getCTabItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolbarTheme = getToolBarLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolItemTheme = getToolItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tableTheme = getTableLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final comboTheme = getComboLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ccomboTheme = getCComboDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final progressBarTheme = getProgressBarLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final listTheme = getListLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final clabelTheme = getCLabelLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final linkTheme = getLinkLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final groupTheme = getGroupLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final expandBarTheme = getExpandBarLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final expandItemTheme = getExpandItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final sliderTheme = getSliderLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final spinnerTheme = getSpinnerLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final scrolledCompositeTheme = getScrolledCompositeLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final scaleTheme = getScaleLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final menuTheme = getMenuLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final menuItemTheme = getMenuItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final coolBarTheme = getCoolBarLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final coolItemTheme = getCoolItemLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tooltipTheme = getTooltipLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final sashTheme = getSashLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final canvasTheme = getCanvasLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final styledTextTheme = getStyledTextLightTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final themeColorPaletteTheme = getThemeColorPaletteLightTheme(
    colorScheme: colorScheme,
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    textTheme: materialTextTheme,
    extensions: <ThemeExtension<dynamic>>[
      colorSchemeExtension,
      buttonTheme,
      labelTheme,
      textThemeExtension,
      tabFolderTheme,
      tabItemTheme,
      treeTheme,
      ctabFolderTheme,
      ctabItemTheme,
      toolbarTheme,
      toolItemTheme,
      tableTheme,
      comboTheme,
      ccomboTheme,
      progressBarTheme,
      listTheme,
      clabelTheme,
      linkTheme,
      groupTheme,
      expandBarTheme,
      expandItemTheme,
      sliderTheme,
      spinnerTheme,
      scrolledCompositeTheme,
      scaleTheme,
      menuTheme,
      menuItemTheme,
      coolBarTheme,
      coolItemTheme,
      tooltipTheme,
      sashTheme,
      canvasTheme,
      styledTextTheme,
      themeColorPaletteTheme,
    ],
  );
}

ThemeData createDarkDefaultTheme(int? backgroundColor, {Color? seedColor}) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: seedColor ?? const Color(0xFF1847CA),
    brightness: Brightness.dark,
  );

  final defaultTextTheme = ThemeData(useMaterial3: true, colorScheme: colorScheme).textTheme;
  final colorSchemeExtension = createColorSchemeExtension(colorScheme);
  
  final textThemeExtension = getTextDarkTheme(
    colorScheme: _resolveWidgetColorScheme('text', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('text', colorScheme, Brightness.dark),
  );
  final buttonTheme = getButtonDarkTheme(
    colorScheme: _resolveWidgetColorScheme('button', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('button', colorScheme, Brightness.dark),
  );
  final labelTheme = getLabelDarkTheme(
    colorScheme: _resolveWidgetColorScheme('label', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('label', colorScheme, Brightness.dark),
  );
  final tabFolderTheme = getTabFolderDarkTheme(
    colorScheme: _resolveWidgetColorScheme('tabfolder', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tabfolder', colorScheme, Brightness.dark),
  );
  final tabItemTheme = getTabItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('tabitem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tabitem', colorScheme, Brightness.dark),
  );
  final treeTheme = getTreeDarkTheme(
    colorScheme: _resolveWidgetColorScheme('tree', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tree', colorScheme, Brightness.dark),
  );
  final ctabFolderTheme = getCTabFolderDarkTheme(
    colorScheme: _resolveWidgetColorScheme('ctabfolder', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ctabfolder', colorScheme, Brightness.dark),
  );
  final ctabItemTheme = getCTabItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('ctabitem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ctabitem', colorScheme, Brightness.dark),
  );
  final toolbarTheme = getToolBarDarkTheme(
    colorScheme: _resolveWidgetColorScheme('toolbar', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('toolbar', colorScheme, Brightness.dark),
  );
  final toolItemTheme = getToolItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('toolitem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('toolitem', colorScheme, Brightness.dark),
  );
  final tableTheme = getTableDarkTheme(
    colorScheme: _resolveWidgetColorScheme('table', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('table', colorScheme, Brightness.dark),
  );
  final comboTheme = getComboDarkTheme(
    colorScheme: _resolveWidgetColorScheme('combo', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('combo', colorScheme, Brightness.dark),
  );
  final ccomboTheme = getCComboDarkTheme(
    colorScheme: _resolveWidgetColorScheme('ccombo', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('ccombo', colorScheme, Brightness.dark),
  );
  final progressBarTheme = getProgressBarDarkTheme(
    colorScheme: _resolveWidgetColorScheme('progressbar', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('progressbar', colorScheme, Brightness.dark),
  );
  final listTheme = getListDarkTheme(
    colorScheme: _resolveWidgetColorScheme('list', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('list', colorScheme, Brightness.dark),
  );
  final clabelTheme = getCLabelDarkTheme(
    colorScheme: _resolveWidgetColorScheme('clabel', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('clabel', colorScheme, Brightness.dark),
  );
  final linkTheme = getLinkDarkTheme(
    colorScheme: _resolveWidgetColorScheme('link', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('link', colorScheme, Brightness.dark),
  );
  final groupTheme = getGroupDarkTheme(
    colorScheme: _resolveWidgetColorScheme('group', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('group', colorScheme, Brightness.dark),
  );
  final expandBarTheme = getExpandBarDarkTheme(
    colorScheme: _resolveWidgetColorScheme('expandbar', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('expandbar', colorScheme, Brightness.dark),
  );
  final expandItemTheme = getExpandItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('expanditem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('expanditem', colorScheme, Brightness.dark),
  );
  final sliderTheme = getSliderDarkTheme(
    colorScheme: _resolveWidgetColorScheme('slider', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('slider', colorScheme, Brightness.dark),
  );
  final spinnerTheme = getSpinnerDarkTheme(
    colorScheme: _resolveWidgetColorScheme('spinner', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('spinner', colorScheme, Brightness.dark),
  );
  final scrolledCompositeTheme = getScrolledCompositeDarkTheme(
    colorScheme: _resolveWidgetColorScheme('scrolledcomposite', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('scrolledcomposite', colorScheme, Brightness.dark),
  );
  final scaleTheme = getScaleDarkTheme(
    colorScheme: _resolveWidgetColorScheme('scale', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('scale', colorScheme, Brightness.dark),
  );
  final menuTheme = getMenuDarkTheme(
    colorScheme: _resolveWidgetColorScheme('menu', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('menu', colorScheme, Brightness.dark),
  );
  final menuItemTheme = getMenuItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('menuitem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('menuitem', colorScheme, Brightness.dark),
  );
  final coolBarTheme = getCoolBarDarkTheme(
    colorScheme: _resolveWidgetColorScheme('coolbar', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('coolbar', colorScheme, Brightness.dark),
  );
  final coolItemTheme = getCoolItemDarkTheme(
    colorScheme: _resolveWidgetColorScheme('coolitem', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('coolitem', colorScheme, Brightness.dark),
  );
  final tooltipTheme = getTooltipDarkTheme(
    colorScheme: _resolveWidgetColorScheme('tooltip', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('tooltip', colorScheme, Brightness.dark),
  );
  final sashTheme = getSashDarkTheme(
    colorScheme: _resolveWidgetColorScheme('sash', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('sash', colorScheme, Brightness.dark),
  );
  final canvasTheme = getCanvasDarkTheme(
    colorScheme: _resolveWidgetColorScheme('canvas', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('canvas', colorScheme, Brightness.dark),
  );
  final styledTextTheme = getStyledTextDarkTheme(
    colorScheme: _resolveWidgetColorScheme('styledtext', colorScheme, Brightness.dark),
    textTheme: defaultTextTheme,
    colorSchemeExtension: _resolveWidgetColorSchemeExtension('styledtext', colorScheme, Brightness.dark),
  );
  final themeColorPaletteTheme = getThemeColorPaletteDarkTheme(
    colorScheme: _resolveWidgetColorScheme('toolbar', colorScheme, Brightness.dark),
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    extensions: <ThemeExtension<dynamic>>[
      colorSchemeExtension,
      buttonTheme,
      labelTheme,
      textThemeExtension,
      tabFolderTheme,
      tabItemTheme,
      treeTheme,
      ctabFolderTheme,
      ctabItemTheme,
      toolbarTheme,
      toolItemTheme,
      tableTheme,
      comboTheme,
      ccomboTheme,
      progressBarTheme,
      listTheme,
      clabelTheme,
      linkTheme,
      groupTheme,
      expandBarTheme,
      expandItemTheme,
      sliderTheme,
      spinnerTheme,
      scrolledCompositeTheme,
      scaleTheme,
      menuTheme,
      menuItemTheme,
      coolBarTheme,
      coolItemTheme,
      tooltipTheme,
      sashTheme,
      canvasTheme,
      styledTextTheme,
      themeColorPaletteTheme,
    ],
  );
}

ThemeData createDarkNonDefaultTheme(
  int? backgroundColor, {
  ColorScheme? overrideColorScheme,
  ColorSchemeExtension? overrideColorSchemeExtension,
}) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);

  final colorScheme = overrideColorScheme ?? createDarkColorScheme();
  final colorSchemeExtension = overrideColorSchemeExtension ?? createColorSchemeExtension();

  final materialTextTheme = createMaterialTextTheme(colorScheme);
  final textThemeExtension = getTextDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );

  final buttonTheme = getButtonDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final labelTheme = getLabelDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabFolderTheme = getTabFolderDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabItemTheme = getTabItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final treeTheme = getTreeDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabFolderTheme = getCTabFolderDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabItemTheme = getCTabItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolbarTheme = getToolBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolItemTheme = getToolItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tableTheme = getTableDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final comboTheme = getComboDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ccomboTheme = getCComboDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final progressBarTheme = getProgressBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final listTheme = getListDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final clabelTheme = getCLabelDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final linkTheme = getLinkDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );  
  final groupTheme = getGroupDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final expandBarTheme = getExpandBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final expandItemTheme = getExpandItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final sliderTheme = getSliderDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final spinnerTheme = getSpinnerDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final scrolledCompositeTheme = getScrolledCompositeDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final scaleTheme = getScaleDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final menuTheme = getMenuDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final menuItemTheme = getMenuItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final coolBarTheme = getCoolBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final coolItemTheme = getCoolItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tooltipTheme = getTooltipDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final sashTheme = getSashDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final canvasTheme = getCanvasDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final styledTextTheme = getStyledTextDarkTheme(
    colorScheme: colorScheme,
    textTheme: materialTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final themeColorPaletteTheme = getThemeColorPaletteDarkTheme(
    colorScheme: colorScheme,
  );
  return ThemeData(
    useMaterial3: true,
    colorScheme: colorScheme,
    scaffoldBackgroundColor: finalBackgroundColor,
    textTheme: materialTextTheme,
    extensions: <ThemeExtension<dynamic>>[
      colorSchemeExtension,
      buttonTheme,
      labelTheme,
      textThemeExtension,
      tabFolderTheme,
      tabItemTheme,
      treeTheme,
      ctabFolderTheme,
      ctabItemTheme,
      toolbarTheme,
      toolItemTheme,
      tableTheme,
      comboTheme,
      ccomboTheme,
      progressBarTheme,
      listTheme,
      clabelTheme,
      linkTheme,
      groupTheme,
      expandBarTheme,
      expandItemTheme,
      sliderTheme,
      spinnerTheme,
      scrolledCompositeTheme,
      scaleTheme,
      menuTheme,
      menuItemTheme,
      coolBarTheme,
      coolItemTheme,
      tooltipTheme,
      sashTheme,
      canvasTheme,
      styledTextTheme,
      themeColorPaletteTheme,
    ],
  );
}