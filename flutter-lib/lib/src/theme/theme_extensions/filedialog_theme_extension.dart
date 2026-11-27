import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'filedialog_theme_extension.tailor.dart';

/// The web file chooser's own look; its buttons, rows, name field and filter take the Button,
/// List, Text and Combo themes.
@TailorMixin()
class FileDialogThemeExtension extends ThemeExtension<FileDialogThemeExtension>
    with _$FileDialogThemeExtensionTailorMixin {
  // Dialog chrome
  final TextStyle? titleStyle;
  final TextStyle? messageStyle;
  final TextStyle? labelStyle;
  final Color backgroundColor;
  final double borderRadius;
  final double width;
  final double height;
  final double promptWidth;
  final EdgeInsets padding;
  final double titleContentSpacing;
  final double sectionSpacing;
  final double contentButtonsSpacing;
  final double buttonSpacing;

  // Location bar
  final TextStyle? pathStyle;
  final Color iconColor;
  final Color disabledIconColor;
  final double iconSize;
  final double iconButtonSize;

  // Listing
  final Color folderIconColor;
  final Color fileIconColor;
  final double rowIconSize;
  final double rowIconSpacing;
  final double selectedOverlayOpacity;
  final double hoverOverlayOpacity;
  final Color progressColor;

  const FileDialogThemeExtension({
    this.titleStyle,
    this.messageStyle,
    this.labelStyle,
    this.pathStyle,
    required this.backgroundColor,
    required this.borderRadius,
    required this.width,
    required this.height,
    required this.promptWidth,
    required this.padding,
    required this.titleContentSpacing,
    required this.sectionSpacing,
    required this.contentButtonsSpacing,
    required this.buttonSpacing,
    required this.iconColor,
    required this.disabledIconColor,
    required this.iconSize,
    required this.iconButtonSize,
    required this.folderIconColor,
    required this.fileIconColor,
    required this.rowIconSize,
    required this.rowIconSpacing,
    required this.selectedOverlayOpacity,
    required this.hoverOverlayOpacity,
    required this.progressColor,
  });
}
