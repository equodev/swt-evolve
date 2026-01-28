import 'package:flutter/material.dart';
import '../theme_extensions/menuitem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

MenuItemThemeExtension getMenuItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMenuItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MenuItemThemeExtension getMenuItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMenuItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MenuItemThemeExtension _getMenuItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return MenuItemThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 150),

    // Background colors
    backgroundColor: Colors.transparent,
    hoverBackgroundColor: isDark
        ? Colors.white.withOpacity(0.08)
        : Colors.black.withOpacity(0.04),
    selectedBackgroundColor: colorScheme.primaryContainer,
    disabledBackgroundColor: Colors.transparent,

    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    acceleratorTextColor: colorScheme.onSurfaceVariant,

    // Icon colors
    iconColor: colorScheme.onSurfaceVariant,
    disabledIconColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Separator colors
    separatorColor: colorScheme.outlineVariant,

    // Checkbox colors
    checkboxColor: Colors.transparent,
    checkboxSelectedColor: colorScheme.primary,
    checkboxHoverColor: isDark
        ? Colors.white.withOpacity(0.05)
        : Colors.black.withOpacity(0.03),
    checkboxBorderColor: colorScheme.outline,
    checkboxCheckmarkColor: colorScheme.onPrimary,

    // Radio button colors
    radioButtonColor: Colors.transparent,
    radioButtonSelectedColor: colorScheme.primary,
    radioButtonHoverColor: isDark
        ? Colors.white.withOpacity(0.05)
        : Colors.black.withOpacity(0.03),
    radioButtonSelectedHoverColor: Color.lerp(colorScheme.primary, isDark ? Colors.white : Colors.black, 0.1) ?? colorScheme.primary,
    radioButtonBorderColor: colorScheme.outline,
    radioButtonInnerColor: colorScheme.onPrimary,

    // Border styling
    borderRadius: 4.0,

    // Sizing
    itemHeight: 32.0,
    minItemWidth: 140.0,
    iconSize: 16.0,
    separatorHeight: 1.0,
    separatorMargin: 4.0,
    itemPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 6.0),
    iconTextSpacing: 8.0,
    textAcceleratorSpacing: 24.0,

    // Checkbox sizes
    checkboxSize: 16.0,
    checkboxBorderRadius: 3.0,
    checkboxBorderWidth: 1.5,
    checkboxCheckmarkSize: 12.0,

    // Radio button sizes
    radioButtonSize: 16.0,
    radioButtonBorderWidth: 1.5,
    radioButtonInnerSize: 8.0,

    // Disabled opacity
    disabledOpacity: 0.38,

    // Font styles
    textStyle: textTheme.bodyMedium?.copyWith(fontSize: 14),
    acceleratorTextStyle: textTheme.bodySmall?.copyWith(fontSize: 12),
  );
}

/// Gets the effective text color for the menu item
Color getMenuItemTextColor(
  MenuItemThemeExtension widgetTheme, {
  bool isEnabled = true,
}) {
  return isEnabled ? widgetTheme.textColor : widgetTheme.disabledTextColor;
}

/// Gets the text style for the menu item
TextStyle getMenuItemTextStyle(
  MenuItemThemeExtension widgetTheme, {
  bool isEnabled = true,
}) {
  final textColor = getMenuItemTextColor(widgetTheme, isEnabled: isEnabled);
  return widgetTheme.textStyle?.copyWith(color: textColor) ??
      TextStyle(fontSize: 14, color: textColor);
}

/// Gets the accelerator text style
TextStyle getMenuItemAcceleratorTextStyle(
  MenuItemThemeExtension widgetTheme, {
  bool isEnabled = true,
}) {
  return widgetTheme.acceleratorTextStyle?.copyWith(
    color: isEnabled ? widgetTheme.acceleratorTextColor : widgetTheme.disabledTextColor,
  ) ?? TextStyle(
    fontSize: 12,
    color: isEnabled ? widgetTheme.acceleratorTextColor : widgetTheme.disabledTextColor,
  );
}

/// Formats an SWT accelerator key to a display string
String formatAccelerator(int accelerator) {
  if (accelerator == 0) return '';

  final List<String> parts = [];

  // Check for modifier keys (SWT modifier masks)
  const int swtCtrl = 1 << 18;  // SWT.CTRL
  const int swtAlt = 1 << 16;   // SWT.ALT
  const int swtShift = 1 << 17; // SWT.SHIFT
  const int swtCommand = 1 << 22; // SWT.COMMAND

  if ((accelerator & swtCtrl) != 0) {
    parts.add('Ctrl');
  }
  if ((accelerator & swtAlt) != 0) {
    parts.add('Alt');
  }
  if ((accelerator & swtShift) != 0) {
    parts.add('Shift');
  }
  if ((accelerator & swtCommand) != 0) {
    parts.add('Cmd');
  }

  // Get the key code (lower 16 bits)
  final keyCode = accelerator & 0xFFFF;

  // Map common key codes to names
  String keyName;
  if (keyCode >= 65 && keyCode <= 90) {
    keyName = String.fromCharCode(keyCode);
  } else if (keyCode >= 97 && keyCode <= 122) {
    keyName = String.fromCharCode(keyCode - 32);
  } else if (keyCode >= 48 && keyCode <= 57) {
    keyName = String.fromCharCode(keyCode);
  } else {
    switch (keyCode) {
      case 0x1000001: keyName = 'F1'; break;
      case 0x1000002: keyName = 'F2'; break;
      case 0x1000003: keyName = 'F3'; break;
      case 0x1000004: keyName = 'F4'; break;
      case 0x1000005: keyName = 'F5'; break;
      case 0x1000006: keyName = 'F6'; break;
      case 0x1000007: keyName = 'F7'; break;
      case 0x1000008: keyName = 'F8'; break;
      case 0x1000009: keyName = 'F9'; break;
      case 0x100000a: keyName = 'F10'; break;
      case 0x100000b: keyName = 'F11'; break;
      case 0x100000c: keyName = 'F12'; break;
      case 13: keyName = 'Enter'; break;
      case 27: keyName = 'Esc'; break;
      case 32: keyName = 'Space'; break;
      case 127: keyName = 'Del'; break;
      case 8: keyName = 'Backspace'; break;
      case 9: keyName = 'Tab'; break;
      default: keyName = '';
    }
  }

  if (keyName.isNotEmpty) {
    parts.add(keyName);
  }

  return parts.join('+');
}
