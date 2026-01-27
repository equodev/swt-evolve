// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'scale_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ScaleThemeExtensionTailorMixin on ThemeExtension<ScaleThemeExtension> {
  Duration get animationDuration;
  Color get activeTrackColor;
  Color get inactiveTrackColor;
  Color get disabledActiveTrackColor;
  Color get disabledInactiveTrackColor;
  Color get thumbColor;
  Color get thumbHoverColor;
  Color get disabledThumbColor;
  Color get tickMarkColor;
  Color get disabledTickMarkColor;
  Color get overlayColor;
  double get trackHeight;
  double get thumbRadius;
  double get thumbHoverRadius;
  double get tickMarkHeight;
  double get tickMarkWidth;
  int get tickMarkCount;
  double get minWidth;
  double get minHeight;
  double get minVerticalWidth;
  double get minVerticalHeight;
  double get horizontalPadding;
  int get defaultMinimum;
  int get defaultMaximum;
  int get defaultSelection;
  int get defaultIncrement;
  int get defaultPageIncrement;

  @override
  ScaleThemeExtension copyWith({
    Duration? animationDuration,
    Color? activeTrackColor,
    Color? inactiveTrackColor,
    Color? disabledActiveTrackColor,
    Color? disabledInactiveTrackColor,
    Color? thumbColor,
    Color? thumbHoverColor,
    Color? disabledThumbColor,
    Color? tickMarkColor,
    Color? disabledTickMarkColor,
    Color? overlayColor,
    double? trackHeight,
    double? thumbRadius,
    double? thumbHoverRadius,
    double? tickMarkHeight,
    double? tickMarkWidth,
    int? tickMarkCount,
    double? minWidth,
    double? minHeight,
    double? minVerticalWidth,
    double? minVerticalHeight,
    double? horizontalPadding,
    int? defaultMinimum,
    int? defaultMaximum,
    int? defaultSelection,
    int? defaultIncrement,
    int? defaultPageIncrement,
  }) {
    return ScaleThemeExtension(
      animationDuration: animationDuration ?? this.animationDuration,
      activeTrackColor: activeTrackColor ?? this.activeTrackColor,
      inactiveTrackColor: inactiveTrackColor ?? this.inactiveTrackColor,
      disabledActiveTrackColor:
          disabledActiveTrackColor ?? this.disabledActiveTrackColor,
      disabledInactiveTrackColor:
          disabledInactiveTrackColor ?? this.disabledInactiveTrackColor,
      thumbColor: thumbColor ?? this.thumbColor,
      thumbHoverColor: thumbHoverColor ?? this.thumbHoverColor,
      disabledThumbColor: disabledThumbColor ?? this.disabledThumbColor,
      tickMarkColor: tickMarkColor ?? this.tickMarkColor,
      disabledTickMarkColor:
          disabledTickMarkColor ?? this.disabledTickMarkColor,
      overlayColor: overlayColor ?? this.overlayColor,
      trackHeight: trackHeight ?? this.trackHeight,
      thumbRadius: thumbRadius ?? this.thumbRadius,
      thumbHoverRadius: thumbHoverRadius ?? this.thumbHoverRadius,
      tickMarkHeight: tickMarkHeight ?? this.tickMarkHeight,
      tickMarkWidth: tickMarkWidth ?? this.tickMarkWidth,
      tickMarkCount: tickMarkCount ?? this.tickMarkCount,
      minWidth: minWidth ?? this.minWidth,
      minHeight: minHeight ?? this.minHeight,
      minVerticalWidth: minVerticalWidth ?? this.minVerticalWidth,
      minVerticalHeight: minVerticalHeight ?? this.minVerticalHeight,
      horizontalPadding: horizontalPadding ?? this.horizontalPadding,
      defaultMinimum: defaultMinimum ?? this.defaultMinimum,
      defaultMaximum: defaultMaximum ?? this.defaultMaximum,
      defaultSelection: defaultSelection ?? this.defaultSelection,
      defaultIncrement: defaultIncrement ?? this.defaultIncrement,
      defaultPageIncrement: defaultPageIncrement ?? this.defaultPageIncrement,
    );
  }

  @override
  ScaleThemeExtension lerp(
    covariant ThemeExtension<ScaleThemeExtension>? other,
    double t,
  ) {
    if (other is! ScaleThemeExtension) return this as ScaleThemeExtension;
    return ScaleThemeExtension(
      animationDuration: t < 0.5 ? animationDuration : other.animationDuration,
      activeTrackColor: Color.lerp(
        activeTrackColor,
        other.activeTrackColor,
        t,
      )!,
      inactiveTrackColor: Color.lerp(
        inactiveTrackColor,
        other.inactiveTrackColor,
        t,
      )!,
      disabledActiveTrackColor: Color.lerp(
        disabledActiveTrackColor,
        other.disabledActiveTrackColor,
        t,
      )!,
      disabledInactiveTrackColor: Color.lerp(
        disabledInactiveTrackColor,
        other.disabledInactiveTrackColor,
        t,
      )!,
      thumbColor: Color.lerp(thumbColor, other.thumbColor, t)!,
      thumbHoverColor: Color.lerp(thumbHoverColor, other.thumbHoverColor, t)!,
      disabledThumbColor: Color.lerp(
        disabledThumbColor,
        other.disabledThumbColor,
        t,
      )!,
      tickMarkColor: Color.lerp(tickMarkColor, other.tickMarkColor, t)!,
      disabledTickMarkColor: Color.lerp(
        disabledTickMarkColor,
        other.disabledTickMarkColor,
        t,
      )!,
      overlayColor: Color.lerp(overlayColor, other.overlayColor, t)!,
      trackHeight: t < 0.5 ? trackHeight : other.trackHeight,
      thumbRadius: t < 0.5 ? thumbRadius : other.thumbRadius,
      thumbHoverRadius: t < 0.5 ? thumbHoverRadius : other.thumbHoverRadius,
      tickMarkHeight: t < 0.5 ? tickMarkHeight : other.tickMarkHeight,
      tickMarkWidth: t < 0.5 ? tickMarkWidth : other.tickMarkWidth,
      tickMarkCount: t < 0.5 ? tickMarkCount : other.tickMarkCount,
      minWidth: t < 0.5 ? minWidth : other.minWidth,
      minHeight: t < 0.5 ? minHeight : other.minHeight,
      minVerticalWidth: t < 0.5 ? minVerticalWidth : other.minVerticalWidth,
      minVerticalHeight: t < 0.5 ? minVerticalHeight : other.minVerticalHeight,
      horizontalPadding: t < 0.5 ? horizontalPadding : other.horizontalPadding,
      defaultMinimum: t < 0.5 ? defaultMinimum : other.defaultMinimum,
      defaultMaximum: t < 0.5 ? defaultMaximum : other.defaultMaximum,
      defaultSelection: t < 0.5 ? defaultSelection : other.defaultSelection,
      defaultIncrement: t < 0.5 ? defaultIncrement : other.defaultIncrement,
      defaultPageIncrement: t < 0.5
          ? defaultPageIncrement
          : other.defaultPageIncrement,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ScaleThemeExtension &&
            const DeepCollectionEquality().equals(
              animationDuration,
              other.animationDuration,
            ) &&
            const DeepCollectionEquality().equals(
              activeTrackColor,
              other.activeTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              inactiveTrackColor,
              other.inactiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledActiveTrackColor,
              other.disabledActiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledInactiveTrackColor,
              other.disabledInactiveTrackColor,
            ) &&
            const DeepCollectionEquality().equals(
              thumbColor,
              other.thumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              thumbHoverColor,
              other.thumbHoverColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledThumbColor,
              other.disabledThumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              tickMarkColor,
              other.tickMarkColor,
            ) &&
            const DeepCollectionEquality().equals(
              disabledTickMarkColor,
              other.disabledTickMarkColor,
            ) &&
            const DeepCollectionEquality().equals(
              overlayColor,
              other.overlayColor,
            ) &&
            const DeepCollectionEquality().equals(
              trackHeight,
              other.trackHeight,
            ) &&
            const DeepCollectionEquality().equals(
              thumbRadius,
              other.thumbRadius,
            ) &&
            const DeepCollectionEquality().equals(
              thumbHoverRadius,
              other.thumbHoverRadius,
            ) &&
            const DeepCollectionEquality().equals(
              tickMarkHeight,
              other.tickMarkHeight,
            ) &&
            const DeepCollectionEquality().equals(
              tickMarkWidth,
              other.tickMarkWidth,
            ) &&
            const DeepCollectionEquality().equals(
              tickMarkCount,
              other.tickMarkCount,
            ) &&
            const DeepCollectionEquality().equals(minWidth, other.minWidth) &&
            const DeepCollectionEquality().equals(minHeight, other.minHeight) &&
            const DeepCollectionEquality().equals(
              minVerticalWidth,
              other.minVerticalWidth,
            ) &&
            const DeepCollectionEquality().equals(
              minVerticalHeight,
              other.minVerticalHeight,
            ) &&
            const DeepCollectionEquality().equals(
              horizontalPadding,
              other.horizontalPadding,
            ) &&
            const DeepCollectionEquality().equals(
              defaultMinimum,
              other.defaultMinimum,
            ) &&
            const DeepCollectionEquality().equals(
              defaultMaximum,
              other.defaultMaximum,
            ) &&
            const DeepCollectionEquality().equals(
              defaultSelection,
              other.defaultSelection,
            ) &&
            const DeepCollectionEquality().equals(
              defaultIncrement,
              other.defaultIncrement,
            ) &&
            const DeepCollectionEquality().equals(
              defaultPageIncrement,
              other.defaultPageIncrement,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(animationDuration),
      const DeepCollectionEquality().hash(activeTrackColor),
      const DeepCollectionEquality().hash(inactiveTrackColor),
      const DeepCollectionEquality().hash(disabledActiveTrackColor),
      const DeepCollectionEquality().hash(disabledInactiveTrackColor),
      const DeepCollectionEquality().hash(thumbColor),
      const DeepCollectionEquality().hash(thumbHoverColor),
      const DeepCollectionEquality().hash(disabledThumbColor),
      const DeepCollectionEquality().hash(tickMarkColor),
      const DeepCollectionEquality().hash(disabledTickMarkColor),
      const DeepCollectionEquality().hash(overlayColor),
      const DeepCollectionEquality().hash(trackHeight),
      const DeepCollectionEquality().hash(thumbRadius),
      const DeepCollectionEquality().hash(thumbHoverRadius),
      const DeepCollectionEquality().hash(tickMarkHeight),
      const DeepCollectionEquality().hash(tickMarkWidth),
      const DeepCollectionEquality().hash(tickMarkCount),
      const DeepCollectionEquality().hash(minWidth),
      const DeepCollectionEquality().hash(minHeight),
      const DeepCollectionEquality().hash(minVerticalWidth),
      const DeepCollectionEquality().hash(minVerticalHeight),
      const DeepCollectionEquality().hash(horizontalPadding),
      const DeepCollectionEquality().hash(defaultMinimum),
      const DeepCollectionEquality().hash(defaultMaximum),
      const DeepCollectionEquality().hash(defaultSelection),
      const DeepCollectionEquality().hash(defaultIncrement),
      const DeepCollectionEquality().hash(defaultPageIncrement),
    ]);
  }
}

extension ScaleThemeExtensionBuildContextProps on BuildContext {
  ScaleThemeExtension get scaleThemeExtension =>
      Theme.of(this).extension<ScaleThemeExtension>()!;
  Duration get animationDuration => scaleThemeExtension.animationDuration;
  Color get activeTrackColor => scaleThemeExtension.activeTrackColor;
  Color get inactiveTrackColor => scaleThemeExtension.inactiveTrackColor;
  Color get disabledActiveTrackColor =>
      scaleThemeExtension.disabledActiveTrackColor;
  Color get disabledInactiveTrackColor =>
      scaleThemeExtension.disabledInactiveTrackColor;
  Color get thumbColor => scaleThemeExtension.thumbColor;
  Color get thumbHoverColor => scaleThemeExtension.thumbHoverColor;
  Color get disabledThumbColor => scaleThemeExtension.disabledThumbColor;
  Color get tickMarkColor => scaleThemeExtension.tickMarkColor;
  Color get disabledTickMarkColor => scaleThemeExtension.disabledTickMarkColor;
  Color get overlayColor => scaleThemeExtension.overlayColor;
  double get trackHeight => scaleThemeExtension.trackHeight;
  double get thumbRadius => scaleThemeExtension.thumbRadius;
  double get thumbHoverRadius => scaleThemeExtension.thumbHoverRadius;
  double get tickMarkHeight => scaleThemeExtension.tickMarkHeight;
  double get tickMarkWidth => scaleThemeExtension.tickMarkWidth;
  int get tickMarkCount => scaleThemeExtension.tickMarkCount;
  double get minWidth => scaleThemeExtension.minWidth;
  double get minHeight => scaleThemeExtension.minHeight;
  double get minVerticalWidth => scaleThemeExtension.minVerticalWidth;
  double get minVerticalHeight => scaleThemeExtension.minVerticalHeight;
  double get horizontalPadding => scaleThemeExtension.horizontalPadding;
  int get defaultMinimum => scaleThemeExtension.defaultMinimum;
  int get defaultMaximum => scaleThemeExtension.defaultMaximum;
  int get defaultSelection => scaleThemeExtension.defaultSelection;
  int get defaultIncrement => scaleThemeExtension.defaultIncrement;
  int get defaultPageIncrement => scaleThemeExtension.defaultPageIncrement;
}
