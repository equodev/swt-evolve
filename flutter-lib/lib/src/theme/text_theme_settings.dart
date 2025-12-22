import 'package:flutter/material.dart';
import 'text_theme_extension.dart';
import '../gen/text.dart';
import '../gen/swt.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../impl/color_utils.dart';
import '../impl/utils/font_utils.dart';
import '../impl/widget_config.dart';

TextThemeExtension getTextLightTheme() {
  return TextThemeExtension(
    // Text colors - from light theme images
    textColor: const Color(0xFF111827), // Dark gray text (from images showing "Sample value")
    disabledTextColor: const Color(0xFF9CA3AF), // Light gray disabled text (from disabled state images)
    placeholderColor: const Color(0xFF9CA3AF), // Light gray placeholder (from placeholder images)
    helperTextColor: const Color(0xFF6B7280), // Medium gray helper text (from helper text in images)
    errorTextColor: const Color(0xFFDC2626), // Red error text (from error state images)
    
    // Background colors
    backgroundColor: const Color(0xFFFFFFFF), // White background
    disabledBackgroundColor: const Color(0xFFF9FAFB), // Light gray disabled background
    hoverBackgroundColor: const Color(0xFFFFFFFF), // White hover (same as normal)
    focusedBackgroundColor: const Color(0xFFFFFFFF), // White focused (same as normal)
    
    // Border colors - from images showing light gray borders
    borderColor: const Color(0xFFD1D5DB), // Light gray border
    hoverBorderColor: const Color(0xFF6366F1), // Violet border on hover
    focusedBorderColor: const Color(0xFF4338CA), // Darker violet border on focus
    errorBorderColor: const Color(0xFFDC2626), // Red error border
    disabledBorderColor: const Color(0xFFE5E7EB), // Very light gray disabled border
    
    // Icon colors
    prefixIconColor: const Color(0xFF6B7280), // Medium gray icon
    suffixIconColor: const Color(0xFF6B7280), // Medium gray icon
    disabledIconColor: const Color(0xFF9CA3AF), // Light gray disabled icon
    errorIconColor: const Color(0xFFDC2626), // Red error icon
    
    // Dimensions - from images showing input fields
    height: 40.0, // Standard height for text input
    minHeight: 40.0,
    maxHeight: null, // No max height for single line
    minWidth: null, // No min width (can be fullWidth or intrinsic)
    maxWidth: null, // No max width
    fullWidth: false, // Default to intrinsic width
    
    // Border properties
    borderRadius: 4.0, // Slightly rounded corners from images
    borderWidth: 1.0, // Standard border width
    hoverBorderWidth: 2.0, // 1px more on hover
    focusedBorderWidth: 2.0, // 1px more on focus
    errorBorderWidth: 1.0, // Same width but red color
    
    // Padding and spacing
    contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 10.0),
    prefixPadding: const EdgeInsets.only(left: 12.0),
    suffixPadding: const EdgeInsets.only(right: 12.0),
    prefixIconSpacing: 8.0, // Space between prefix icon and text
    suffixIconSpacing: 8.0, // Space between text and suffix icon
    
    // Typography
    fontSize: 14.0, // Standard font size
    fontWeight: FontWeight.w400, // Normal weight
    fontFamily: 'System',
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    // Helper text
    helperTextFontSize: 12.0, // Smaller font for helper text
    helperTextSpacing: 4.0, // Space between input and helper text
    
    // Icon sizes
    prefixIconSize: 20.0, // Standard icon size
    suffixIconSize: 20.0, // Standard icon size
    errorIconSize: 16.0, // Error icon size
    
    // Interactive properties
    focusAnimationDuration: const Duration(milliseconds: 200),
    focusAnimationCurve: Curves.easeInOut,
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    
    // Password field
    passwordToggleColor: const Color(0xFF6B7280), // Medium gray for password toggle
    passwordToggleHoverColor: const Color(0xFF374151), // Darker gray on hover
    passwordToggleSize: 20.0, // Same as suffix icon size
  );
}

TextThemeExtension getTextDarkTheme() {
  return TextThemeExtension(
    // Text colors - from dark theme images
    textColor: const Color(0xFFF9FAFB), // Light text
    disabledTextColor: const Color(0xFF6B7280), // Medium gray disabled text
    placeholderColor: const Color(0xFF9CA3AF), // Medium gray placeholder
    helperTextColor: const Color(0xFF9CA3AF), // Medium gray helper text
    errorTextColor: const Color(0xFFF87171), // Light red error text
    
    // Background colors
    backgroundColor: const Color(0xFF1F2937), // Dark background (gray-800)
    disabledBackgroundColor: const Color(0xFF374151), // Darker gray disabled background (gray-700)
    hoverBackgroundColor: const Color(0xFF1F2937), // Same as normal
    focusedBackgroundColor: const Color(0xFF1F2937), // Same as normal
    
    // Border colors - from dark theme images
    borderColor: const Color(0xFF6B7280), // Lighter gray border (gray-500) - more visible
    hoverBorderColor: const Color(0xFF6366F1), // Violet border on hover
    focusedBorderColor: const Color(0xFF4338CA), // Darker violet border on focus
    errorBorderColor: const Color(0xFFF87171), // Light red error border
    disabledBorderColor: const Color(0xFF4B5563), // Darker gray disabled border (gray-600)
    
    // Icon colors
    prefixIconColor: const Color(0xFF9CA3AF), // Medium gray icon
    suffixIconColor: const Color(0xFF9CA3AF), // Medium gray icon
    disabledIconColor: const Color(0xFF6B7280), // Darker gray disabled icon
    errorIconColor: const Color(0xFFF87171), // Light red error icon
    
    // Dimensions - same as light theme
    height: 40.0,
    minHeight: 40.0,
    maxHeight: null,
    minWidth: null,
    maxWidth: null,
    fullWidth: false,
    
    // Border properties
    borderRadius: 4.0,
    borderWidth: 1.0,
    hoverBorderWidth: 2.0, // 1px more on hover
    focusedBorderWidth: 2.0, // 1px more on focus
    errorBorderWidth: 1.0,
    
    // Padding and spacing
    contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 10.0),
    prefixPadding: const EdgeInsets.only(left: 12.0),
    suffixPadding: const EdgeInsets.only(right: 12.0),
    prefixIconSpacing: 8.0,
    suffixIconSpacing: 8.0,
    
    // Typography
    fontSize: 14.0,
    fontWeight: FontWeight.w400,
    fontFamily: 'System',
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    // Helper text
    helperTextFontSize: 12.0,
    helperTextSpacing: 4.0,
    
    // Icon sizes
    prefixIconSize: 20.0,
    suffixIconSize: 20.0,
    errorIconSize: 16.0,
    
    // Interactive properties
    focusAnimationDuration: const Duration(milliseconds: 200),
    focusAnimationCurve: Curves.easeInOut,
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    
    // Password field
    passwordToggleColor: const Color(0xFF9CA3AF), // Medium gray for password toggle
    passwordToggleHoverColor: const Color(0xFFD1D5DB), // Lighter gray on hover
    passwordToggleSize: 20.0,
  );
}

// Helper functions to get values based on state and theme

TextAlign getTextAlign(int style) {
  if ((style & SWT.CENTER) != 0) return TextAlign.center;
  if ((style & SWT.RIGHT) != 0) return TextAlign.right;
  return TextAlign.left;
}

TextStyle getTextStyle(
  BuildContext context,
  VText state,
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
  TextTheme textTheme,
) {
  final enabled = state.editable ?? true;
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;
  
  final finalTextColor = useSwtColors && state.foreground != null
      ? colorFromVColor(state.foreground, defaultColor: enabled
          ? (widgetTheme?.textColor ?? textTheme.bodyMedium?.color ?? colorScheme.onSurface)
          : (widgetTheme?.disabledTextColor ?? colorScheme.onSurface.withOpacity(0.38)))
      : (enabled
          ? (widgetTheme?.textColor ?? textTheme.bodyMedium?.color ?? colorScheme.onSurface)
          : (widgetTheme?.disabledTextColor ?? colorScheme.onSurface.withOpacity(0.38)));

  // Base text style from theme
  TextStyle baseStyle = TextStyle(
    color: finalTextColor,
    fontSize: widgetTheme?.fontSize ?? textTheme.bodyMedium?.fontSize ?? 14.0,
    fontWeight: widgetTheme?.fontWeight ?? textTheme.bodyMedium?.fontWeight ?? FontWeight.w400,
    fontFamily: widgetTheme?.fontFamily ?? textTheme.bodyMedium?.fontFamily,
    letterSpacing: widgetTheme?.letterSpacing ?? textTheme.bodyMedium?.letterSpacing ?? 0.0,
    height: widgetTheme?.lineHeight ?? textTheme.bodyMedium?.height ?? 1.4,
  );

  if (useSwtFonts && state.font != null) {
    return FontUtils.textStyleFromVFont(
      state.font,
      context,
      color: finalTextColor,
    );
  }

  return baseStyle;
}

Color getBackgroundColor(
  VText state,
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
) {
  final enabled = state.editable ?? true;
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.background != null) {
    return colorFromVColor(state.background, defaultColor: enabled
        ? const Color(0xFFFFFFFF)
        : const Color(0xFFF9FAFB));
  }
  
  return enabled
      ? const Color(0xFFFFFFFF)
      : const Color(0xFFF9FAFB);
}

Color getIconColor(
  VText state,
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
) {
  final enabled = state.editable ?? true;
  
  return enabled
      ? (widgetTheme?.prefixIconColor ?? colorScheme.onSurfaceVariant)
      : (widgetTheme?.disabledIconColor ?? colorScheme.onSurface.withOpacity(0.38));
}

Color getHintColor(
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
) {
  return widgetTheme?.placeholderColor ?? colorScheme.onSurfaceVariant;
}

Color getCursorColor(
  VText state,
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
  TextTheme textTheme,
) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.foreground != null) {
    return colorFromVColor(state.foreground, defaultColor: widgetTheme?.textColor ?? textTheme.bodyMedium?.color ?? colorScheme.onSurface);
  }
  
  return widgetTheme?.textColor ?? textTheme.bodyMedium?.color ?? colorScheme.onSurface;
}

InputDecoration getInputDecoration(
  BuildContext context,
  VText state,
  TextThemeExtension? widgetTheme,
  ColorScheme colorScheme,
  TextEditingController controller,
  Function() onClear,
) {
  final enabled = state.editable ?? true;
  
  // Background color
  final bgColor = getBackgroundColor(state, widgetTheme, colorScheme);
  
  // Icon colors
  final iconColor = getIconColor(state, widgetTheme, colorScheme);
  final hintColor = getHintColor(widgetTheme, colorScheme);
  
  // Icon size and constraints
  final iconSize = widgetTheme?.prefixIconSize ?? 20.0;
  final iconConstraints = widgetTheme?.height ?? 40.0;
  
  final normalBorderColor = const Color(0xFFD1D5DB);
  final hoverBorderColor = const Color(0xFF6366F1);
  final focusedBorderColor = const Color(0xFF4338CA);
  final disabledBorderColor = const Color(0xFFE5E7EB);
  final borderWidth = widgetTheme?.borderWidth ?? 1.0;
  final hoverBorderWidth = widgetTheme?.hoverBorderWidth ?? 2.0;
  final focusedBorderWidth = widgetTheme?.focusedBorderWidth ?? 2.0;
  final borderRadius = widgetTheme?.borderRadius ?? 4.0;

  return InputDecoration(
    hintText: state.message,
    hintStyle: TextStyle(color: hintColor),
    isDense: true,
    contentPadding: widgetTheme?.contentPadding ?? const EdgeInsets.symmetric(horizontal: 12.0, vertical: 10.0),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: normalBorderColor, width: borderWidth),
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: normalBorderColor, width: borderWidth),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: focusedBorderColor, width: focusedBorderWidth),
    ),
    disabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(borderRadius),
      borderSide: BorderSide(color: disabledBorderColor, width: borderWidth),
    ),
    fillColor: bgColor,
    filled: true,
    counterText: '',
    prefixIcon: (state.style & SWT.SEARCH) != 0
        ? Icon(Icons.search, size: iconSize, color: iconColor)
        : null,
    prefixIconConstraints: BoxConstraints(
      minHeight: iconConstraints,
      minWidth: iconConstraints,
    ),
    suffixIcon: ((state.style & SWT.SEARCH) != 0 &&
            state.text != null &&
            state.text!.isNotEmpty)
        ? IconButton(
            icon: Icon(Icons.clear, size: iconSize, color: iconColor),
            padding: EdgeInsets.zero,
            constraints: BoxConstraints(
              minHeight: iconConstraints,
              minWidth: iconConstraints,
            ),
            onPressed: onClear,
          )
        : null,
    suffixIconConstraints: BoxConstraints(
      minHeight: iconConstraints,
      minWidth: iconConstraints,
    ),
  );
}

double getTextFieldHeight(
  VText state,
  TextThemeExtension? widgetTheme,
  bool isMultiLine,
) {
  if (isMultiLine) {
    return state.bounds?.height?.toDouble() ?? widgetTheme?.height ?? 40.0;
  }
  return state.bounds?.height?.toDouble() ?? widgetTheme?.height ?? 40.0;
}

