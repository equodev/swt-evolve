import 'package:flutter/material.dart';
import '../styles.dart';
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

Color calculateBackgroundColor(int? backgroundColor, bool useDarkTheme) {
  return backgroundColor != null
      ? Color(0xFF000000 | backgroundColor)
      : (useDarkTheme ? const Color(0xFF2C2C2C) : const Color(0xFFF2F4F7));
}

ColorScheme createLightColorScheme() {
  return ColorScheme.fromSeed(
    seedColor: const Color(0xFF2236E5),
    brightness: Brightness.light,
  ).copyWith(
    primary: const Color(0xFF2236E5), // #2236E5
    onPrimary: const Color(0xFFFFFFFF), // #FFFFFF
    primaryContainer: const Color(0xFF1B2AB2), // #1B2AB2
    
    secondary: const Color(0xFFF2F3FA), // #F2F3FA
    onSecondary: const Color(0xFF0F1866), // #0F1866
    secondaryContainer: const Color(0xFFE1E3F5), // #E1E3F5
    onSecondaryContainer: const Color(0xFFC9CDF0), // #C9CDF0
    
    tertiary: const Color(0x00FFFFFF), // #FFFFFF
    onTertiary: const Color(0xFF0F1866), // #0F1866
    
    surface: const Color(0xFFFFFFFF), // #FFFFFF
    onSurface: const Color(0xFF171819), // #171819
    onSurfaceVariant: const Color(0xFF797B80), // #797B80
    surfaceContainerHighest: const Color(0xFFFAFBFC), // #FAFBFC
    surfaceContainerHigh: const Color(0xFFF7F8FA), // #F7F8FA
    surfaceContainerLow: const Color(0xFFF0F2F5), // #F0F2F5
    
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
    inversePrimary: const Color(0xFF2236E5), // #2236E5
    surfaceTint: const Color(0xFF2236E5), // #2236E5
  );
}

ColorScheme createDarkColorScheme() {
  return ColorScheme.fromSeed(
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
}

ColorSchemeExtension createColorSchemeExtension() {
  return ColorSchemeExtension(
    primaryHovered: const Color(0xFF1E30CC), // #1E30CC
    primaryBorder: const Color(0xFF1B2AB2), // #1B2AB2
    primaryBorderDisabled: const Color(0x33171819), // #33171819
    onPrimaryVariantDisabled: const Color(0x80171819), // #80171819
    
    secondaryPressed: const Color(0xFFC9CDF0), // #C9CDF0
    secondaryBold: const Color(0xFFE1E3F5), // #E1E3F5
    secondaryBorder: const Color(0xFFC9CDF0), // #C9CDF0
    secondaryBorderDisabled: const Color(0x33171819), // #33171819
    onSecondaryVariantDisabled: const Color(0x80171819), // #80171819
    
    tertiaryPressed: const Color(0x33171819), // #33171819
    tertiaryHovered: const Color(0x1A171819), // #1A171819
    tertiaryBorder: const Color(0x00FFFFFF), // #00FFFFFF
    tertiaryBorderDisabled: const Color(0x00FFFFFF), // #00FFFFFF
    onTertiaryVariantDisabled: const Color(0x80171819), // #80171819
    
    surfaceFocused: const Color(0xFFF2F3FA), // #F2F3FA
    surfaceBorderEnabled: const Color(0x1A171819), // #1A171819
    surfaceBorderHovered: const Color(0x4D171819), // #4D171819
    surfaceBorderFocused: const Color(0xFF0F1866), // #0F1866
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
    statusInform: const Color(0xFF2236E5), // #2236E5
    statusOnInform: const Color(0xFFFFFFFF), // #FFFFFF
    statusInformContainer: const Color(0xFFF2F3FA), // #F2F3FA
    statusOnInformContainer: const Color(0xFF0F1866), // #0F1866
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
      fontSize: 12.0,
      fontWeight: FontWeight.w500,
      letterSpacing: 0.24,
      height: 16.0 / 12.0, 
      color: colorScheme.onSurface,
    ),
    bodyLarge: TextStyle(
      fontFamily: 'Inter',
      fontSize: 16.0,
      fontWeight: FontWeight.w400,
      letterSpacing: 0.0,
      height: 24.0 / 16.0, 
      color: colorScheme.onSurface,
    ),
    bodyMedium: TextStyle(
      fontFamily: 'Inter',
      fontSize: 14.0,
      fontWeight: FontWeight.w400,
      letterSpacing: 0.0,
      height: 20.0 / 14.0, 
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

ThemeData createLightDefaultTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: const Color(0xFF1847CA),
    brightness: Brightness.light,
  );

  final defaultTextTheme = ThemeData(useMaterial3: true, colorScheme: colorScheme).textTheme;
  final colorSchemeExtension = createColorSchemeExtension();
  
  final textThemeExtension = getTextLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final buttonTheme = getButtonLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final labelTheme = getLabelLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabFolderTheme = getTabFolderLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabItemTheme = getTabItemLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final treeTheme = getTreeLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabFolderTheme = getCTabFolderLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabItemTheme = getCTabItemLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolbarTheme = getToolBarLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolItemTheme = getToolItemLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tableTheme = getTableLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final comboTheme = getComboLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ccomboTheme = getCComboLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final progressBarTheme = getProgressBarLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final listTheme = getListLightTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
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
    ],
  );
}

ThemeData createLightNonDefaultTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = createLightColorScheme();
  final colorSchemeExtension = createColorSchemeExtension();

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
    ],
  );
}

ThemeData createDarkDefaultTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = ColorScheme.fromSeed(
    seedColor: const Color(0xFF1847CA),
    brightness: Brightness.dark,
  );

  final defaultTextTheme = ThemeData(useMaterial3: true, colorScheme: colorScheme).textTheme;
  final colorSchemeExtension = createColorSchemeExtension();
  
  final textThemeExtension = getTextDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final buttonTheme = getButtonDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final labelTheme = getLabelDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabFolderTheme = getTabFolderDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tabItemTheme = getTabItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final treeTheme = getTreeDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabFolderTheme = getCTabFolderDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ctabItemTheme = getCTabItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolbarTheme = getToolBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final toolItemTheme = getToolItemDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final tableTheme = getTableDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final comboTheme = getComboDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final ccomboTheme = getCComboDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final progressBarTheme = getProgressBarDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
  final listTheme = getListDarkTheme(
    colorScheme: colorScheme,
    textTheme: defaultTextTheme,
    colorSchemeExtension: colorSchemeExtension,
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
    ],
  );
}

ThemeData createDarkNonDefaultTheme(int? backgroundColor) {
  final bool useDarkTheme = getCurrentTheme();
  final Color finalBackgroundColor = calculateBackgroundColor(backgroundColor, useDarkTheme);
  
  final colorScheme = createDarkColorScheme();
  final colorSchemeExtension = createColorSchemeExtension();

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
    ],
  );
}