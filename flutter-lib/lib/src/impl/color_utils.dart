import 'package:flutter/material.dart';
import 'widget_config.dart';

Color getBackground() {
  return getCurrentTheme() ? const Color(0xFF1E1E1E) : Colors.white;
}

Color getForeground() {
  return getCurrentTheme() ? Colors.white : const Color(0xFF1F1F1F);
}

Color getBackgroundSelected() {
  return getCurrentTheme() ? const Color(0xFF094771) : const Color(0xFFE5F3FF);
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
