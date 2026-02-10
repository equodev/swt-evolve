import 'package:flutter/material.dart';
import '../../gen/rectangle.dart';
import '../../gen/swt.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/widget_config.dart';
import 'font_utils.dart';

/// Checks if a style flag is set in the given style value
bool hasStyle(int style, int flag) {
  return (style & flag) != 0;
}

/// Checks if a VRectangle has valid bounds (non-null and positive dimensions)
bool hasBounds(VRectangle? bounds) {
  return bounds != null && bounds.width > 0 && bounds.height > 0;
}

/// Converts VRectangle bounds to BoxConstraints with exact dimensions
BoxConstraints? getConstraintsFromBounds(VRectangle? bounds) {
  if (hasBounds(bounds)) {
    return BoxConstraints(
      minWidth: bounds!.width.toDouble(),
      maxWidth: bounds.width.toDouble(),
      minHeight: bounds.height.toDouble(),
      maxHeight: bounds.height.toDouble(),
    );
  }
  return null;
}

/// Determines TextAlign based on SWT style flags
TextAlign getTextAlignFromStyle(int style, TextAlign defaultAlign) {
  if (hasStyle(style, SWT.CENTER)) {
    return TextAlign.center;
  } else if (hasStyle(style, SWT.RIGHT)) {
    return TextAlign.right;
  } else if (hasStyle(style, SWT.LEFT)) {
    return TextAlign.left;
  }
  return defaultAlign;
}

/// Gets background color with SWT color support
Color? getBackgroundColor({
  required VColor? background,
  required Color? defaultColor,
}) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;

  if (useSwtColors && background != null) {
    return colorFromVColor(background, defaultColor: defaultColor);
  }

  return defaultColor;
}

/// Gets foreground/text color with SWT font support
Color getForegroundColor({
  required VColor? foreground,
  required Color defaultColor,
}) {
  final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;

  if (useSwtFonts && foreground != null) {
    return colorFromVColor(foreground, defaultColor: defaultColor);
  }

  return defaultColor;
}

/// Gets TextStyle with SWT font support
TextStyle getTextStyle({
  required BuildContext context,
  required VFont? font,
  required Color textColor,
  TextStyle? baseTextStyle,
}) {
  final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;

  if (useSwtFonts && font != null) {
    return FontUtils.textStyleFromVFont(font, context, color: textColor) ??
        baseTextStyle?.copyWith(color: textColor) ??
        TextStyle(color: textColor);
  }

  TextStyle result = baseTextStyle?.copyWith(color: textColor) ??
      TextStyle(color: textColor);
  if (font != null &&
      font.fontData != null &&
      font.fontData!.isNotEmpty &&
      (font.fontData!.first.style ?? 0) != 0) {
    final (fontWeight, fontStyle) =
        FontUtils.convertSwtFontStyle(font.fontData!.first.style ?? 0);
    result = result.copyWith(fontWeight: fontWeight, fontStyle: fontStyle);
  }

  return result;
}

/// Determines if text should wrap based on style
bool shouldWrapText({
  required int style,
  required bool hasValidBounds,
  required String text,
}) {
  return hasStyle(style, SWT.WRAP);
}

/// Converts TextAlign to MainAxisAlignment
MainAxisAlignment getMainAxisAlignmentFromTextAlign(
  TextAlign textAlign,
  MainAxisAlignment defaultAlign,
) {
  switch (textAlign) {
    case TextAlign.center:
      return MainAxisAlignment.center;
    case TextAlign.right:
    case TextAlign.end:
      return MainAxisAlignment.end;
    case TextAlign.left:
    case TextAlign.start:
    default:
      return defaultAlign;
  }
}

/// Converts TextAlign to Alignment
Alignment getAlignmentFromTextAlign(TextAlign textAlign) {
  switch (textAlign) {
    case TextAlign.center:
      return Alignment.center;
    case TextAlign.right:
    case TextAlign.end:
      return Alignment.centerRight;
    case TextAlign.left:
    case TextAlign.start:
    default:
      return Alignment.centerLeft;
  }
}

EdgeInsets adjustPaddingForAlignment({
  required EdgeInsets basePadding,
  required int? alignment,
  double leftPadding = 0.0,
  double extraPadding = 0.0,
}) {
  if (alignment == null || alignment == SWT.LEFT) {
    return EdgeInsets.only(
      left: basePadding.left + leftPadding + extraPadding,
      right: basePadding.right,
      top: basePadding.top,
      bottom: basePadding.bottom,
    );
  } else if (alignment == SWT.RIGHT) {
    return EdgeInsets.only(
      left: basePadding.left + leftPadding,
      right: basePadding.right + extraPadding,
      top: basePadding.top,
      bottom: basePadding.bottom,
    );
  } else {
    // CENTER or other
    return EdgeInsets.only(
      left: basePadding.left + leftPadding,
      right: basePadding.right,
      top: basePadding.top,
      bottom: basePadding.bottom,
    );
  }
}
