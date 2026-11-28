import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'view_rails_theme_extension.tailor.dart';

/// The view rails: a rail's width, its buttons' and their icons', each for the small, medium and
/// large sizes, and how a button is drawn.
@TailorMixin()
class ViewRailsThemeExtension extends ThemeExtension<ViewRailsThemeExtension>
    with _$ViewRailsThemeExtensionTailorMixin {
  final List<double> buttonSizes;

  /// The room between a button and the window's edge, where the active view's indicator sits, and
  /// between a button and the views, for each size.
  final List<double> outerPaddings;
  final List<double> innerPaddings;
  final List<double> iconSizes;

  /// The height a rail asks for when it holds a view.
  final double minHeight;
  final double endPadding;
  final double buttonSpacing;
  final double buttonRadius;
  final Color activeBackground;
  final Color shownBackground;
  final Color hoverBackground;
  final double idleOpacity;

  /// A button being dragged shows at this opacity in its place.
  final double draggingOpacity;

  /// How long the pointer rests on a button before its view's name shows.
  final Duration tooltipWait;

  /// The initial of a view without an icon.
  final TextStyle letterStyle;
  final Color indicatorColor;
  final double indicatorWidth;
  final double indicatorRadius;

  /// How far the active view's indicator sits outside its button, and from its top and bottom.
  final double indicatorOffset;
  final double indicatorInset;
  final Color separatorColor;
  final double separatorWidth;
  final double separatorThickness;
  final double separatorSpacing;

  /// A dragged button, and a rail end it can be dropped on, take this ground; the line shows where
  /// it lands among the buttons.
  final Color dropBackground;
  final double dropLineWidth;

  /// The room past a rail end's last button that takes a drop.
  final double dropRoom;

  const ViewRailsThemeExtension({
    required this.buttonSizes,
    required this.outerPaddings,
    required this.innerPaddings,
    required this.iconSizes,
    required this.minHeight,
    required this.endPadding,
    required this.buttonSpacing,
    required this.buttonRadius,
    required this.activeBackground,
    required this.shownBackground,
    required this.hoverBackground,
    required this.idleOpacity,
    required this.draggingOpacity,
    required this.tooltipWait,
    required this.letterStyle,
    required this.indicatorColor,
    required this.indicatorWidth,
    required this.indicatorRadius,
    required this.indicatorOffset,
    required this.indicatorInset,
    required this.separatorColor,
    required this.separatorWidth,
    required this.separatorThickness,
    required this.separatorSpacing,
    required this.dropBackground,
    required this.dropLineWidth,
    required this.dropRoom,
  });

  /// The rail's width at [size]: its buttons and the room on both sides of them.
  double railWidth(int size) => buttonSizes[size] + outerPaddings[size] + innerPaddings[size];
}
