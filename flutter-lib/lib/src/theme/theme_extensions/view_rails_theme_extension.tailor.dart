// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'view_rails_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ViewRailsThemeExtensionTailorMixin
    on ThemeExtension<ViewRailsThemeExtension> {
  List<double> get buttonSizes;
  List<double> get outerPaddings;
  List<double> get innerPaddings;
  List<double> get iconSizes;
  double get minHeight;
  double get endPadding;
  double get buttonSpacing;
  double get buttonRadius;
  Color get activeBackground;
  Color get shownBackground;
  Color get hoverBackground;
  double get idleOpacity;
  double get draggingOpacity;
  Duration get tooltipWait;
  TextStyle get letterStyle;
  Color get indicatorColor;
  double get indicatorWidth;
  double get indicatorRadius;
  double get indicatorOffset;
  double get indicatorInset;
  Color get separatorColor;
  double get separatorWidth;
  double get separatorThickness;
  double get separatorSpacing;
  Color get dropBackground;
  double get dropLineWidth;
  double get dropRoom;

  @override
  ViewRailsThemeExtension copyWith({
    List<double>? buttonSizes,
    List<double>? outerPaddings,
    List<double>? innerPaddings,
    List<double>? iconSizes,
    double? minHeight,
    double? endPadding,
    double? buttonSpacing,
    double? buttonRadius,
    Color? activeBackground,
    Color? shownBackground,
    Color? hoverBackground,
    double? idleOpacity,
    double? draggingOpacity,
    Duration? tooltipWait,
    TextStyle? letterStyle,
    Color? indicatorColor,
    double? indicatorWidth,
    double? indicatorRadius,
    double? indicatorOffset,
    double? indicatorInset,
    Color? separatorColor,
    double? separatorWidth,
    double? separatorThickness,
    double? separatorSpacing,
    Color? dropBackground,
    double? dropLineWidth,
    double? dropRoom,
  }) {
    return ViewRailsThemeExtension(
      buttonSizes: buttonSizes ?? this.buttonSizes,
      outerPaddings: outerPaddings ?? this.outerPaddings,
      innerPaddings: innerPaddings ?? this.innerPaddings,
      iconSizes: iconSizes ?? this.iconSizes,
      minHeight: minHeight ?? this.minHeight,
      endPadding: endPadding ?? this.endPadding,
      buttonSpacing: buttonSpacing ?? this.buttonSpacing,
      buttonRadius: buttonRadius ?? this.buttonRadius,
      activeBackground: activeBackground ?? this.activeBackground,
      shownBackground: shownBackground ?? this.shownBackground,
      hoverBackground: hoverBackground ?? this.hoverBackground,
      idleOpacity: idleOpacity ?? this.idleOpacity,
      draggingOpacity: draggingOpacity ?? this.draggingOpacity,
      tooltipWait: tooltipWait ?? this.tooltipWait,
      letterStyle: letterStyle ?? this.letterStyle,
      indicatorColor: indicatorColor ?? this.indicatorColor,
      indicatorWidth: indicatorWidth ?? this.indicatorWidth,
      indicatorRadius: indicatorRadius ?? this.indicatorRadius,
      indicatorOffset: indicatorOffset ?? this.indicatorOffset,
      indicatorInset: indicatorInset ?? this.indicatorInset,
      separatorColor: separatorColor ?? this.separatorColor,
      separatorWidth: separatorWidth ?? this.separatorWidth,
      separatorThickness: separatorThickness ?? this.separatorThickness,
      separatorSpacing: separatorSpacing ?? this.separatorSpacing,
      dropBackground: dropBackground ?? this.dropBackground,
      dropLineWidth: dropLineWidth ?? this.dropLineWidth,
      dropRoom: dropRoom ?? this.dropRoom,
    );
  }

  @override
  ViewRailsThemeExtension lerp(
    covariant ThemeExtension<ViewRailsThemeExtension>? other,
    double t,
  ) {
    if (other is! ViewRailsThemeExtension)
      return this as ViewRailsThemeExtension;
    return ViewRailsThemeExtension(
      buttonSizes: t < 0.5 ? buttonSizes : other.buttonSizes,
      outerPaddings: t < 0.5 ? outerPaddings : other.outerPaddings,
      innerPaddings: t < 0.5 ? innerPaddings : other.innerPaddings,
      iconSizes: t < 0.5 ? iconSizes : other.iconSizes,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      endPadding: t < 0.5 ? endPadding : other.endPadding,
      buttonSpacing: t < 0.5 ? buttonSpacing : other.buttonSpacing,
      buttonRadius: t < 0.5 ? buttonRadius : other.buttonRadius,
      activeBackground: Color.lerp(
        activeBackground,
        other.activeBackground,
        t,
      )!,
      shownBackground: Color.lerp(shownBackground, other.shownBackground, t)!,
      hoverBackground: Color.lerp(hoverBackground, other.hoverBackground, t)!,
      idleOpacity: t < 0.5 ? idleOpacity : other.idleOpacity,
      draggingOpacity: t < 0.5 ? draggingOpacity : other.draggingOpacity,
      tooltipWait: t < 0.5 ? tooltipWait : other.tooltipWait,
      letterStyle: TextStyle.lerp(letterStyle, other.letterStyle, t)!,
      indicatorColor: Color.lerp(indicatorColor, other.indicatorColor, t)!,
      indicatorWidth: t < 0.5 ? indicatorWidth : other.indicatorWidth,
      indicatorRadius: t < 0.5 ? indicatorRadius : other.indicatorRadius,
      indicatorOffset: t < 0.5 ? indicatorOffset : other.indicatorOffset,
      indicatorInset: t < 0.5 ? indicatorInset : other.indicatorInset,
      separatorColor: Color.lerp(separatorColor, other.separatorColor, t)!,
      separatorWidth: t < 0.5 ? separatorWidth : other.separatorWidth,
      separatorThickness: t < 0.5
          ? separatorThickness
          : other.separatorThickness,
      separatorSpacing: t < 0.5 ? separatorSpacing : other.separatorSpacing,
      dropBackground: Color.lerp(dropBackground, other.dropBackground, t)!,
      dropLineWidth: t < 0.5 ? dropLineWidth : other.dropLineWidth,
      dropRoom: t < 0.5 ? dropRoom : other.dropRoom,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ViewRailsThemeExtension &&
            const DeepCollectionEquality().equals(
              buttonSizes,
              other.buttonSizes,
            ) &&
            const DeepCollectionEquality().equals(
              outerPaddings,
              other.outerPaddings,
            ) &&
            const DeepCollectionEquality().equals(
              innerPaddings,
              other.innerPaddings,
            ) &&
            const DeepCollectionEquality().equals(iconSizes, other.iconSizes) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
            const DeepCollectionEquality().equals(
              endPadding,
              other.endPadding,
            ) &&
            const DeepCollectionEquality().equals(
              buttonSpacing,
              other.buttonSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              buttonRadius,
              other.buttonRadius,
            ) &&
            const DeepCollectionEquality().equals(
              activeBackground,
              other.activeBackground,
            ) &&
            const DeepCollectionEquality().equals(
              shownBackground,
              other.shownBackground,
            ) &&
            const DeepCollectionEquality().equals(
              hoverBackground,
              other.hoverBackground,
            ) &&
            const DeepCollectionEquality().equals(
              idleOpacity,
              other.idleOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              draggingOpacity,
              other.draggingOpacity,
            ) &&
            const DeepCollectionEquality().equals(
              tooltipWait,
              other.tooltipWait,
            ) &&
            const DeepCollectionEquality().equals(
              letterStyle,
              other.letterStyle,
            ) &&
            const DeepCollectionEquality().equals(
              indicatorColor,
              other.indicatorColor,
            ) &&
            const DeepCollectionEquality().equals(
              indicatorWidth,
              other.indicatorWidth,
            ) &&
            const DeepCollectionEquality().equals(
              indicatorRadius,
              other.indicatorRadius,
            ) &&
            const DeepCollectionEquality().equals(
              indicatorOffset,
              other.indicatorOffset,
            ) &&
            const DeepCollectionEquality().equals(
              indicatorInset,
              other.indicatorInset,
            ) &&
            const DeepCollectionEquality().equals(
              separatorColor,
              other.separatorColor,
            ) &&
            const DeepCollectionEquality().equals(
              separatorWidth,
              other.separatorWidth,
            ) &&
            const DeepCollectionEquality().equals(
              separatorThickness,
              other.separatorThickness,
            ) &&
            const DeepCollectionEquality().equals(
              separatorSpacing,
              other.separatorSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              dropBackground,
              other.dropBackground,
            ) &&
            const DeepCollectionEquality().equals(
              dropLineWidth,
              other.dropLineWidth,
            ) &&
            const DeepCollectionEquality().equals(dropRoom, other.dropRoom));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(buttonSizes),
      const DeepCollectionEquality().hash(outerPaddings),
      const DeepCollectionEquality().hash(innerPaddings),
      const DeepCollectionEquality().hash(iconSizes),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(endPadding),
      const DeepCollectionEquality().hash(buttonSpacing),
      const DeepCollectionEquality().hash(buttonRadius),
      const DeepCollectionEquality().hash(activeBackground),
      const DeepCollectionEquality().hash(shownBackground),
      const DeepCollectionEquality().hash(hoverBackground),
      const DeepCollectionEquality().hash(idleOpacity),
      const DeepCollectionEquality().hash(draggingOpacity),
      const DeepCollectionEquality().hash(tooltipWait),
      const DeepCollectionEquality().hash(letterStyle),
      const DeepCollectionEquality().hash(indicatorColor),
      const DeepCollectionEquality().hash(indicatorWidth),
      const DeepCollectionEquality().hash(indicatorRadius),
      const DeepCollectionEquality().hash(indicatorOffset),
      const DeepCollectionEquality().hash(indicatorInset),
      const DeepCollectionEquality().hash(separatorColor),
      const DeepCollectionEquality().hash(separatorWidth),
      const DeepCollectionEquality().hash(separatorThickness),
      const DeepCollectionEquality().hash(separatorSpacing),
      const DeepCollectionEquality().hash(dropBackground),
      const DeepCollectionEquality().hash(dropLineWidth),
      const DeepCollectionEquality().hash(dropRoom),
    ]);
  }
}

extension ViewRailsThemeExtensionBuildContextProps on BuildContext {
  ViewRailsThemeExtension get viewRailsThemeExtension =>
      Theme.of(this).extension<ViewRailsThemeExtension>()!;
  List<double> get buttonSizes => viewRailsThemeExtension.buttonSizes;

  /// The room between a button and the window's edge, where the active view's indicator sits, and
  /// between a button and the views, for each size.
  List<double> get outerPaddings => viewRailsThemeExtension.outerPaddings;
  List<double> get innerPaddings => viewRailsThemeExtension.innerPaddings;
  List<double> get iconSizes => viewRailsThemeExtension.iconSizes;

  /// The height a rail asks for when it holds a view.
  double get minHeight => viewRailsThemeExtension.minHeight;
  double get endPadding => viewRailsThemeExtension.endPadding;
  double get buttonSpacing => viewRailsThemeExtension.buttonSpacing;
  double get buttonRadius => viewRailsThemeExtension.buttonRadius;
  Color get activeBackground => viewRailsThemeExtension.activeBackground;
  Color get shownBackground => viewRailsThemeExtension.shownBackground;
  Color get hoverBackground => viewRailsThemeExtension.hoverBackground;
  double get idleOpacity => viewRailsThemeExtension.idleOpacity;

  /// A button being dragged shows at this opacity in its place.
  double get draggingOpacity => viewRailsThemeExtension.draggingOpacity;

  /// How long the pointer rests on a button before its view's name shows.
  Duration get tooltipWait => viewRailsThemeExtension.tooltipWait;

  /// The initial of a view without an icon.
  TextStyle get letterStyle => viewRailsThemeExtension.letterStyle;
  Color get indicatorColor => viewRailsThemeExtension.indicatorColor;
  double get indicatorWidth => viewRailsThemeExtension.indicatorWidth;
  double get indicatorRadius => viewRailsThemeExtension.indicatorRadius;

  /// How far the active view's indicator sits outside its button, and from its top and bottom.
  double get indicatorOffset => viewRailsThemeExtension.indicatorOffset;
  double get indicatorInset => viewRailsThemeExtension.indicatorInset;
  Color get separatorColor => viewRailsThemeExtension.separatorColor;
  double get separatorWidth => viewRailsThemeExtension.separatorWidth;
  double get separatorThickness => viewRailsThemeExtension.separatorThickness;
  double get separatorSpacing => viewRailsThemeExtension.separatorSpacing;

  /// A dragged button, and a rail end it can be dropped on, take this ground; the line shows where
  /// it lands among the buttons.
  Color get dropBackground => viewRailsThemeExtension.dropBackground;
  double get dropLineWidth => viewRailsThemeExtension.dropLineWidth;

  /// The room past a rail end's last button that takes a drop.
  double get dropRoom => viewRailsThemeExtension.dropRoom;
}
