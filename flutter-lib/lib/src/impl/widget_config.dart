import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/config_flags.dart';

final bool useDarkTheme = false;

ConfigFlags configFlags = ConfigFlags();

bool getCurrentTheme() {
  return useDarkTheme;
}

ConfigFlags getConfigFlags() {
  return configFlags;
}

void setConfigFlags(ConfigFlags newFlags) {
  configFlags = newFlags;
}

class AppSizes {
  static const double fontAwesomeIcon = 10.0;
  static const double icon = 12.0;
  static const double toolbarIconLarge = 16.0;
  static const double toolbarTextSize = 10.0;
  static const double toolbarMinSize = 20.0;
  static const double tabIconSize = 16.0;
  static const double tabTextSize = 12.0;
  static const double tabCloseIconSize = 14.0;
  static const double controlButtonSize = 16.0;
  static const double borderRadius = 4.0;
  static const double separatorWidth = 20.0;
  static const double separatorThickness = 1.0;
  static const double separatorIndent = 8.0;
}

class AppConstraints {
  static const BoxConstraints toolbarConstraints = BoxConstraints(
    minWidth: 20,
    minHeight: 20,
  );

  static const BoxConstraints toolbarSmallConstraints = BoxConstraints(
    minWidth: 10,
    minHeight: 10,
  );
}

class AppColors {
  static const Color lightDisabled = Color(0xFFBDBDBD);
  static const Color darkDisabled = Color(0xFF3D3D3D);

  static const Color lightHover = Color(0xFFE0E0E0);
  static const Color darkHover = Color(0xFF3D3D3D);

  static const Color darkBackground = Color(0xFF1E1E1E);
  static const Color lightBackground = Color(0xFFF2F2F2);

  static const Color darkSelected = Color(0xFF2D2D2D);
  static const Color lightSelected = Color(0xFFFFFFFF);

  static const Color lightBorder = Color(0xFFDDDDDD);
  static const Color darkBorder = Color(0xFF333333);
  static const Color highlight = Color(0xFF6366F1);

  static const Color darkTextColor = Color(0xFFBDBDBD);
  static const Color lightTextColor = Color(0xFF616161);

  static const Color darkSelectedTextColor = Color(0xFFFFFFFF);
  static const Color lightSelectedTextColor = Color(0xFF212121);

  static const Color darkEnabledColor = Color(0xFFF2F2F2);
  static const Color lightEnabledColor = Color(0xFF1E1E1E);

  static const Color toolbarBackground = Colors.transparent;

  static Color getHoverColor() => useDarkTheme ? darkHover : lightHover;
  static Color getBackgroundColor() =>
      useDarkTheme ? darkBackground : lightBackground;
  static Color getSelectedColor() =>
      useDarkTheme ? darkSelected : lightSelected;
  static Color getBorderColor() => useDarkTheme ? darkBorder : lightBorder;
  static Color getEnabledColor() =>
      useDarkTheme ? darkEnabledColor : lightEnabledColor;
  static Color getDisabledColor() =>
      useDarkTheme ? darkDisabled : lightDisabled;
  static Color getTextColor() => useDarkTheme ? darkTextColor : lightTextColor;
  static Color getColor(bool enabled) =>
      enabled ? getEnabledColor() : getDisabledColor();
  static Color getSelectedTextColor() =>
      useDarkTheme ? darkSelectedTextColor : lightSelectedTextColor;
}
