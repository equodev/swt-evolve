import 'package:flutter/material.dart';
import '../gen/color.dart';
import 'widget_config.dart';

Color colorFromVColor(VColor? vColor, {Color? defaultColor}) {
  if (vColor == null) return defaultColor ?? Colors.transparent;
  return Color.fromARGB(vColor.alpha, vColor.red, vColor.green, vColor.blue);
}

Color getBackground() {
  return getCurrentTheme() ? const Color(0xFF1E1E1E) : Colors.white;
}

Color getForeground() {
  return getCurrentTheme() ? Colors.white : const Color(0xFF1F1F1F);
}

Color getBackgroundSelected() {
  return getCurrentTheme() ? const Color(0xFF2F2F4C) : const Color(0xFFDDDCF9);
}

Color getForegroundDisabled() {
  return getCurrentTheme() ? const Color(0xFF6D6D6D) : const Color(0xFFA7A7A7);
}

Color getBorderColor() {
  return getCurrentTheme() ? const Color(0xFF3C3C3C) : const Color(0xFFD4D4D4);
}

Color getBorderColorFocused() {
  return getCurrentTheme() ? Colors.white : Colors.black;
}

Color getHintColor() {
  return getCurrentTheme() ? Colors.grey.shade600 : Colors.grey.shade400;
}

Color getIconColor() {
  return getCurrentTheme() ? Colors.white70 : Colors.black54;
}

Color getLinkColor() {
  return getCurrentTheme() ? Colors.lightBlue : Colors.blue;
}

Color getShellBackground() {
  return AppColors.getBackgroundColor();
}

Color getAccentColor() {
  return getCurrentTheme() ? const Color(0xFF9D7FF5) : const Color(0xFF7E57C2);
}


