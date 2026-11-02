import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'styledtext_theme_extension.tailor.dart';

@TailorMixin()
class StyledTextThemeExtension
    extends ThemeExtension<StyledTextThemeExtension>
    with _$StyledTextThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color foregroundColor;
  final double minTextContrast;

  const StyledTextThemeExtension({
    required this.backgroundColor,
    required this.foregroundColor,
    required this.minTextContrast,
  });
}
