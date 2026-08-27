import 'package:flutter/material.dart';
import '../../gen/rectangle.dart';
import '../../gen/swt.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../gen/image.dart';
import '../../impl/color_utils.dart';
import '../../impl/widget_config.dart';
import 'font_utils.dart';

/// Checks if a style flag is set in the given style value
bool hasStyle(int style, int flag) {
  return (style & flag) != 0;
}

/// The BorderSide a bordered, focus-aware Control's outline uses, normal or focused. Shared so
/// unrelated widget types (a Text field's own Material decoration, a Composite drawing SWT.BORDER
/// itself) that each want this same "swap color/width when focused" behavior stay in sync without
/// one borrowing the other's theme -- each caller supplies its own theme's values.
BorderSide focusAwareBorderSide({
  required bool focused,
  required Color color,
  required double width,
  required Color focusedColor,
  double? focusedWidth,
}) {
  return focused
      ? BorderSide(color: focusedColor, width: focusedWidth ?? width)
      : BorderSide(color: color, width: width);
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
  BuildContext? context,
}) {
  final useSwtColors = (getConfigFlags().use_swt_colors ?? false) ||
      (context != null && SwtColorScope.isActive(context));

  if (useSwtColors && background != null) {
    return colorFromVColor(background, defaultColor: defaultColor);
  }

  return defaultColor;
}

/// Gets foreground/text color with SWT font support
Color getForegroundColor({
  required VColor? foreground,
  required Color defaultColor,
  BuildContext? context,
}) {
  final useSwtForeground = (getConfigFlags().use_swt_fonts ?? false) ||
      (context != null && SwtColorScope.isActive(context));

  if (useSwtForeground && foreground != null) {
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
    return FontUtils.textStyleFromVFont(font, context, color: textColor, inherit: false);
  }

  TextStyle result =
      baseTextStyle?.copyWith(color: textColor) ?? TextStyle(color: textColor);
  if (font != null &&
      font.fontData != null &&
      font.fontData!.isNotEmpty &&
      (font.fontData!.first.style) != 0) {
    final (fontWeight, fontStyle) = FontUtils.convertSwtFontStyle(
      font.fontData!.first.style,
    );
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

/// Whether the text carries explicit line delimiters.
///
/// SWT breaks text on its delimiters regardless of SWT.WRAP: the style only
/// governs whether the widget also wraps on its own once the text runs past the
/// available width. So a widget without SWT.WRAP still has to lay the text out
/// over as many lines as it has delimiters.
bool hasLineDelimiter(String text) =>
    text.contains('\n') || text.contains('\r');

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

/// Marks a subtree whose Controls keep the colors the application gave them instead of the
/// theme's, published by a Canvas that paints with those colors itself ([canvasUsesThemeColors]).
/// A Control the application hosts over such a Canvas -- a grid's cell editor is the usual case --
/// is part of that drawing, so a themed editor would otherwise land on an application-colored grid.
class SwtColorScope extends InheritedWidget {
  const SwtColorScope({super.key, required super.child});

  static bool isActive(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<SwtColorScope>() != null;

  @override
  bool updateShouldNotify(SwtColorScope oldWidget) => false;
}

class ParentBackgroundScope extends InheritedWidget {
  final Color? background;
  final VImage? backgroundImage;

  const ParentBackgroundScope({
    super.key,
    required this.background,
    this.backgroundImage,
    required super.child,
  });

  static Color? backgroundOf(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ParentBackgroundScope>()?.background;

  static VImage? backgroundImageOf(BuildContext context) => context
      .dependOnInheritedWidgetOfExactType<ParentBackgroundScope>()
      ?.backgroundImage;

  // Same as backgroundImageOf but doesn't subscribe -- for a one-time build-time read.
  static VImage? peekBackgroundImageOf(BuildContext context) {
    final element = context.getElementForInheritedWidgetOfExactType<ParentBackgroundScope>();
    return (element?.widget as ParentBackgroundScope?)?.backgroundImage;
  }

  @override
  bool updateShouldNotify(ParentBackgroundScope oldWidget) =>
      background != oldWidget.background ||
      backgroundImage != oldWidget.backgroundImage;
}

/// Re-publishes background *color* inheritance for a Composite/Canvas's own children, mirroring
/// SWT's per-Composite backgroundMode instead of leaving a single ancestor's ParentBackgroundScope
/// (e.g. a Shell's) visible unchanged to every descendant no matter how many plain composites sit
/// in between. INHERIT_NONE (the default) stops the color here, so a composite that never opted in
/// doesn't leak a distant ancestor's color to its own children; INHERIT_DEFAULT/FORCE passes this
/// composite's own effective color onward instead.
///
/// backgroundImage is a different concern -- purely "don't repaint/occlude what an ancestor
/// already tiled" -- and is not gated by backgroundMode: it always keeps propagating (this
/// composite's own image if it set one, otherwise whatever was already in scope), so a plain,
/// INHERIT_NONE composite sitting between an image-painting Shell and a deeper transparent child
/// doesn't block that child from seeing the image and staying transparent for it.
Widget wrapBackgroundInheritanceScope({
  required BuildContext context,
  required int? backgroundMode,
  required Color effectiveBackground,
  required VImage? backgroundImage,
  required Widget child,
}) {
  final inheritable = (backgroundMode ?? SWT.INHERIT_NONE) != SWT.INHERIT_NONE;
  return ParentBackgroundScope(
    background: inheritable ? effectiveBackground : null,
    backgroundImage: backgroundImage ?? ParentBackgroundScope.backgroundImageOf(context),
    child: child,
  );
}

/// Depth of the nearest Control ancestor in the SWT widget tree (root Shell
/// is 0), used by HoverExclusivityArbiter to pick the deepest hovered one.
class ControlNestingScope extends InheritedWidget {
  final int depth;

  const ControlNestingScope({
    super.key,
    required this.depth,
    required super.child,
  });

  static int depthOf(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ControlNestingScope>()?.depth ??
      0;

  @override
  bool updateShouldNotify(ControlNestingScope oldWidget) =>
      depth != oldWidget.depth;
}
