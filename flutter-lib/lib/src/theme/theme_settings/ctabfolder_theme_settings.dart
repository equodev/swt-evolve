import 'package:flutter/material.dart';
import '../theme_extensions/ctabfolder_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CTabFolderThemeExtension getCTabFolderLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabFolderTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabFolderThemeExtension getCTabFolderDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabFolderTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabFolderThemeExtension _getCTabFolderTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  
  return CTabFolderThemeExtension(
    // Tab bar colors
    tabBarBackgroundColor: colorScheme.surface,
    tabBarBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    
    // Tab colors
    tabBackgroundColor: colorScheme.surface,
    tabSelectedBackgroundColor: colorScheme.surface,
    tabHoverBackgroundColor: colorSchemeExtension.stateDefaultHovered,
    tabDisabledBackgroundColor: colorScheme.surfaceVariant,
    
    // Tab border colors
    tabBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    tabSelectedBorderColor: colorScheme.primary,
    tabHoverBorderColor: colorSchemeExtension.surfaceBorderHovered,
    tabDisabledBorderColor: colorSchemeExtension.surfaceBorderDisabled,
    
    // Tab text colors
    tabTextColor: colorScheme.onSurfaceVariant,
    tabSelectedTextColor: isDark ? Colors.white : const Color(0xFF000000),
    tabHoverTextColor: colorScheme.onSurface,
    tabDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    tabUnselectedTextOpacity: 0.4,
    
    // Tab close button opacity
    tabCloseButtonSelectedOpacity: 0.9,
    tabCloseButtonUnselectedOpacity: 0.7,
    
    // Highlight color (for selected tab top border)
    tabHighlightColor: colorScheme.primary,
    
    // Tab border properties
    tabBorderWidth: 1.0,
    tabSelectedBorderWidth: 1.0,
    tabHighlightBorderWidth: 2.0,
    tabBorderRadius: 0.0,
    
    // Tab padding
    tabHorizontalPadding: 6.0,
    tabVerticalPadding: 0.0,
    
    // Tab typography
    tabTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.normal,
    ),
    tabSelectedTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.w600,
    ),
    
    // Tab content area
    tabContentBackgroundColor: colorScheme.surface,
    tabContentBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    
    // Tab sizes
    tabIconSize: 14.0,
    tabCloseIconSize: 12.0,
    controlButtonSize: 14.0,
    
    // Tab spacing
    tabIconTextSpacing: 3.0,
    tabCloseButtonSpacing: 6.0,
    
    // Tab icon/text padding
    tabIconBottomPadding: 1.0,
    tabTextBottomPadding: 2.0,
    tabCloseIconBottomPadding: 1.0,
    
    // Control buttons
    controlButtonColor: colorScheme.onSurface,
    controlButtonHoverColor: colorSchemeExtension.stateDefaultHovered,
    controlButtonHorizontalPadding: 2.0,
    controlButtonSpacing: 2.0,
    controlButtonScale: 0.5,
    
    // Hover reveal animation
    hoverRevealDuration: const Duration(milliseconds: 250),
    hoverHideDuration: const Duration(milliseconds: 500),
    
    // Scrollbar
    tabScrollbarThickness: 2.0,
    
    // Top right controls overlay shadow
    topRightControlsShadowColor: Colors.black,
    topRightControlsShadowOpacity: 0.1,
    topRightControlsShadowBlurRadius: 4.0,
    topRightControlsShadowOffset: const Offset(0, 2),
    
    // Top right controls padding
    topRightControlsPadding: const EdgeInsets.symmetric(horizontal: 4.0),
    
    // Scrollbar hide delay
    scrollbarHideDelay: const Duration(milliseconds: 800),
  );
}

