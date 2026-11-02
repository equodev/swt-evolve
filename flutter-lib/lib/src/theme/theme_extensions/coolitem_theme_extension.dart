import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'coolitem_theme_extension.tailor.dart';

@TailorMixin()
class CoolItemThemeExtension extends ThemeExtension<CoolItemThemeExtension>
    with _$CoolItemThemeExtensionTailorMixin {
  final TextStyle? textStyle;
  final EdgeInsets contentPadding;

  const CoolItemThemeExtension({
    this.textStyle,
    required this.contentPadding,
  });
}
