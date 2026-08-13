// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'colordialog_theme_extension.dart';

// **************************************************************************
// TailorAnnotationsGenerator
// **************************************************************************

mixin _$ColorDialogThemeExtensionTailorMixin
    on ThemeExtension<ColorDialogThemeExtension> {
  TextStyle? get titleStyle;
  TextStyle? get labelStyle;
  Color get backgroundColor;
  double get borderRadius;
  double get maxWidth;
  EdgeInsets get padding;
  double get titleContentSpacing;
  double get sectionSpacing;
  double get contentButtonsSpacing;
  double get buttonSpacing;
  double get saturationValueHeight;
  double get thumbRadius;
  double get thumbOutlineRadius;
  double get thumbStrokeWidth;
  Color get thumbColor;
  Color get thumbOutlineColor;
  double get hueSliderHeight;
  double get hueThumbWidth;
  double get hueThumbStrokeWidth;
  Color get hueThumbColor;
  double get swatchSize;
  double get swatchSpacing;
  double get swatchBorderRadius;
  double get swatchBorderWidth;
  Color get swatchBorderColor;
  double get selectedSwatchBorderWidth;
  Color get selectedSwatchBorderColor;
  double get previewWidth;
  double get previewHeight;
  double get previewBorderRadius;
  Color get previewBorderColor;
  double get previewSpacing;
  double get hexFieldWidth;

  @override
  ColorDialogThemeExtension copyWith({
    TextStyle? titleStyle,
    TextStyle? labelStyle,
    Color? backgroundColor,
    double? borderRadius,
    double? maxWidth,
    EdgeInsets? padding,
    double? titleContentSpacing,
    double? sectionSpacing,
    double? contentButtonsSpacing,
    double? buttonSpacing,
    double? saturationValueHeight,
    double? thumbRadius,
    double? thumbOutlineRadius,
    double? thumbStrokeWidth,
    Color? thumbColor,
    Color? thumbOutlineColor,
    double? hueSliderHeight,
    double? hueThumbWidth,
    double? hueThumbStrokeWidth,
    Color? hueThumbColor,
    double? swatchSize,
    double? swatchSpacing,
    double? swatchBorderRadius,
    double? swatchBorderWidth,
    Color? swatchBorderColor,
    double? selectedSwatchBorderWidth,
    Color? selectedSwatchBorderColor,
    double? previewWidth,
    double? previewHeight,
    double? previewBorderRadius,
    Color? previewBorderColor,
    double? previewSpacing,
    double? hexFieldWidth,
  }) {
    return ColorDialogThemeExtension(
      titleStyle: titleStyle ?? this.titleStyle,
      labelStyle: labelStyle ?? this.labelStyle,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      borderRadius: borderRadius ?? this.borderRadius,
      maxWidth: maxWidth ?? this.maxWidth,
      padding: padding ?? this.padding,
      titleContentSpacing: titleContentSpacing ?? this.titleContentSpacing,
      sectionSpacing: sectionSpacing ?? this.sectionSpacing,
      contentButtonsSpacing:
          contentButtonsSpacing ?? this.contentButtonsSpacing,
      buttonSpacing: buttonSpacing ?? this.buttonSpacing,
      saturationValueHeight:
          saturationValueHeight ?? this.saturationValueHeight,
      thumbRadius: thumbRadius ?? this.thumbRadius,
      thumbOutlineRadius: thumbOutlineRadius ?? this.thumbOutlineRadius,
      thumbStrokeWidth: thumbStrokeWidth ?? this.thumbStrokeWidth,
      thumbColor: thumbColor ?? this.thumbColor,
      thumbOutlineColor: thumbOutlineColor ?? this.thumbOutlineColor,
      hueSliderHeight: hueSliderHeight ?? this.hueSliderHeight,
      hueThumbWidth: hueThumbWidth ?? this.hueThumbWidth,
      hueThumbStrokeWidth: hueThumbStrokeWidth ?? this.hueThumbStrokeWidth,
      hueThumbColor: hueThumbColor ?? this.hueThumbColor,
      swatchSize: swatchSize ?? this.swatchSize,
      swatchSpacing: swatchSpacing ?? this.swatchSpacing,
      swatchBorderRadius: swatchBorderRadius ?? this.swatchBorderRadius,
      swatchBorderWidth: swatchBorderWidth ?? this.swatchBorderWidth,
      swatchBorderColor: swatchBorderColor ?? this.swatchBorderColor,
      selectedSwatchBorderWidth:
          selectedSwatchBorderWidth ?? this.selectedSwatchBorderWidth,
      selectedSwatchBorderColor:
          selectedSwatchBorderColor ?? this.selectedSwatchBorderColor,
      previewWidth: previewWidth ?? this.previewWidth,
      previewHeight: previewHeight ?? this.previewHeight,
      previewBorderRadius: previewBorderRadius ?? this.previewBorderRadius,
      previewBorderColor: previewBorderColor ?? this.previewBorderColor,
      previewSpacing: previewSpacing ?? this.previewSpacing,
      hexFieldWidth: hexFieldWidth ?? this.hexFieldWidth,
    );
  }

  @override
  ColorDialogThemeExtension lerp(
    covariant ThemeExtension<ColorDialogThemeExtension>? other,
    double t,
  ) {
    if (other is! ColorDialogThemeExtension)
      return this as ColorDialogThemeExtension;
    return ColorDialogThemeExtension(
      titleStyle: TextStyle.lerp(titleStyle, other.titleStyle, t),
      labelStyle: TextStyle.lerp(labelStyle, other.labelStyle, t),
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      borderRadius: t < 0.5 ? borderRadius : other.borderRadius,
      maxWidth: t < 0.5 ? maxWidth : other.maxWidth,
      padding: t < 0.5 ? padding : other.padding,
      titleContentSpacing: t < 0.5
          ? titleContentSpacing
          : other.titleContentSpacing,
      sectionSpacing: t < 0.5 ? sectionSpacing : other.sectionSpacing,
      contentButtonsSpacing: t < 0.5
          ? contentButtonsSpacing
          : other.contentButtonsSpacing,
      buttonSpacing: t < 0.5 ? buttonSpacing : other.buttonSpacing,
      saturationValueHeight: t < 0.5
          ? saturationValueHeight
          : other.saturationValueHeight,
      thumbRadius: t < 0.5 ? thumbRadius : other.thumbRadius,
      thumbOutlineRadius: t < 0.5
          ? thumbOutlineRadius
          : other.thumbOutlineRadius,
      thumbStrokeWidth: t < 0.5 ? thumbStrokeWidth : other.thumbStrokeWidth,
      thumbColor: Color.lerp(thumbColor, other.thumbColor, t)!,
      thumbOutlineColor: Color.lerp(
        thumbOutlineColor,
        other.thumbOutlineColor,
        t,
      )!,
      hueSliderHeight: t < 0.5 ? hueSliderHeight : other.hueSliderHeight,
      hueThumbWidth: t < 0.5 ? hueThumbWidth : other.hueThumbWidth,
      hueThumbStrokeWidth: t < 0.5
          ? hueThumbStrokeWidth
          : other.hueThumbStrokeWidth,
      hueThumbColor: Color.lerp(hueThumbColor, other.hueThumbColor, t)!,
      swatchSize: t < 0.5 ? swatchSize : other.swatchSize,
      swatchSpacing: t < 0.5 ? swatchSpacing : other.swatchSpacing,
      swatchBorderRadius: t < 0.5
          ? swatchBorderRadius
          : other.swatchBorderRadius,
      swatchBorderWidth: t < 0.5 ? swatchBorderWidth : other.swatchBorderWidth,
      swatchBorderColor: Color.lerp(
        swatchBorderColor,
        other.swatchBorderColor,
        t,
      )!,
      selectedSwatchBorderWidth: t < 0.5
          ? selectedSwatchBorderWidth
          : other.selectedSwatchBorderWidth,
      selectedSwatchBorderColor: Color.lerp(
        selectedSwatchBorderColor,
        other.selectedSwatchBorderColor,
        t,
      )!,
      previewWidth: t < 0.5 ? previewWidth : other.previewWidth,
      previewHeight: t < 0.5 ? previewHeight : other.previewHeight,
      previewBorderRadius: t < 0.5
          ? previewBorderRadius
          : other.previewBorderRadius,
      previewBorderColor: Color.lerp(
        previewBorderColor,
        other.previewBorderColor,
        t,
      )!,
      previewSpacing: t < 0.5 ? previewSpacing : other.previewSpacing,
      hexFieldWidth: t < 0.5 ? hexFieldWidth : other.hexFieldWidth,
    );
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is ColorDialogThemeExtension &&
            const DeepCollectionEquality().equals(
              titleStyle,
              other.titleStyle,
            ) &&
            const DeepCollectionEquality().equals(
              labelStyle,
              other.labelStyle,
            ) &&
            const DeepCollectionEquality().equals(
              backgroundColor,
              other.backgroundColor,
            ) &&
            const DeepCollectionEquality().equals(
              borderRadius,
              other.borderRadius,
            ) &&
            const DeepCollectionEquality().equals(maxWidth, other.maxWidth) &&
            const DeepCollectionEquality().equals(padding, other.padding) &&
            const DeepCollectionEquality().equals(
              titleContentSpacing,
              other.titleContentSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              sectionSpacing,
              other.sectionSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              contentButtonsSpacing,
              other.contentButtonsSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              buttonSpacing,
              other.buttonSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              saturationValueHeight,
              other.saturationValueHeight,
            ) &&
            const DeepCollectionEquality().equals(
              thumbRadius,
              other.thumbRadius,
            ) &&
            const DeepCollectionEquality().equals(
              thumbOutlineRadius,
              other.thumbOutlineRadius,
            ) &&
            const DeepCollectionEquality().equals(
              thumbStrokeWidth,
              other.thumbStrokeWidth,
            ) &&
            const DeepCollectionEquality().equals(
              thumbColor,
              other.thumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              thumbOutlineColor,
              other.thumbOutlineColor,
            ) &&
            const DeepCollectionEquality().equals(
              hueSliderHeight,
              other.hueSliderHeight,
            ) &&
            const DeepCollectionEquality().equals(
              hueThumbWidth,
              other.hueThumbWidth,
            ) &&
            const DeepCollectionEquality().equals(
              hueThumbStrokeWidth,
              other.hueThumbStrokeWidth,
            ) &&
            const DeepCollectionEquality().equals(
              hueThumbColor,
              other.hueThumbColor,
            ) &&
            const DeepCollectionEquality().equals(
              swatchSize,
              other.swatchSize,
            ) &&
            const DeepCollectionEquality().equals(
              swatchSpacing,
              other.swatchSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              swatchBorderRadius,
              other.swatchBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              swatchBorderWidth,
              other.swatchBorderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              swatchBorderColor,
              other.swatchBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              selectedSwatchBorderWidth,
              other.selectedSwatchBorderWidth,
            ) &&
            const DeepCollectionEquality().equals(
              selectedSwatchBorderColor,
              other.selectedSwatchBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              previewWidth,
              other.previewWidth,
            ) &&
            const DeepCollectionEquality().equals(
              previewHeight,
              other.previewHeight,
            ) &&
            const DeepCollectionEquality().equals(
              previewBorderRadius,
              other.previewBorderRadius,
            ) &&
            const DeepCollectionEquality().equals(
              previewBorderColor,
              other.previewBorderColor,
            ) &&
            const DeepCollectionEquality().equals(
              previewSpacing,
              other.previewSpacing,
            ) &&
            const DeepCollectionEquality().equals(
              hexFieldWidth,
              other.hexFieldWidth,
            ));
  }

  @override
  int get hashCode {
    return Object.hashAll([
      runtimeType.hashCode,
      const DeepCollectionEquality().hash(titleStyle),
      const DeepCollectionEquality().hash(labelStyle),
      const DeepCollectionEquality().hash(backgroundColor),
      const DeepCollectionEquality().hash(borderRadius),
      const DeepCollectionEquality().hash(maxWidth),
      const DeepCollectionEquality().hash(padding),
      const DeepCollectionEquality().hash(titleContentSpacing),
      const DeepCollectionEquality().hash(sectionSpacing),
      const DeepCollectionEquality().hash(contentButtonsSpacing),
      const DeepCollectionEquality().hash(buttonSpacing),
      const DeepCollectionEquality().hash(saturationValueHeight),
      const DeepCollectionEquality().hash(thumbRadius),
      const DeepCollectionEquality().hash(thumbOutlineRadius),
      const DeepCollectionEquality().hash(thumbStrokeWidth),
      const DeepCollectionEquality().hash(thumbColor),
      const DeepCollectionEquality().hash(thumbOutlineColor),
      const DeepCollectionEquality().hash(hueSliderHeight),
      const DeepCollectionEquality().hash(hueThumbWidth),
      const DeepCollectionEquality().hash(hueThumbStrokeWidth),
      const DeepCollectionEquality().hash(hueThumbColor),
      const DeepCollectionEquality().hash(swatchSize),
      const DeepCollectionEquality().hash(swatchSpacing),
      const DeepCollectionEquality().hash(swatchBorderRadius),
      const DeepCollectionEquality().hash(swatchBorderWidth),
      const DeepCollectionEquality().hash(swatchBorderColor),
      const DeepCollectionEquality().hash(selectedSwatchBorderWidth),
      const DeepCollectionEquality().hash(selectedSwatchBorderColor),
      const DeepCollectionEquality().hash(previewWidth),
      const DeepCollectionEquality().hash(previewHeight),
      const DeepCollectionEquality().hash(previewBorderRadius),
      const DeepCollectionEquality().hash(previewBorderColor),
      const DeepCollectionEquality().hash(previewSpacing),
      const DeepCollectionEquality().hash(hexFieldWidth),
    ]);
  }
}

extension ColorDialogThemeExtensionBuildContextProps on BuildContext {
  ColorDialogThemeExtension get colorDialogThemeExtension =>
      Theme.of(this).extension<ColorDialogThemeExtension>()!;
  TextStyle? get titleStyle => colorDialogThemeExtension.titleStyle;
  TextStyle? get labelStyle => colorDialogThemeExtension.labelStyle;
  Color get backgroundColor => colorDialogThemeExtension.backgroundColor;
  double get borderRadius => colorDialogThemeExtension.borderRadius;
  double get maxWidth => colorDialogThemeExtension.maxWidth;
  EdgeInsets get padding => colorDialogThemeExtension.padding;
  double get titleContentSpacing =>
      colorDialogThemeExtension.titleContentSpacing;
  double get sectionSpacing => colorDialogThemeExtension.sectionSpacing;
  double get contentButtonsSpacing =>
      colorDialogThemeExtension.contentButtonsSpacing;
  double get buttonSpacing => colorDialogThemeExtension.buttonSpacing;
  double get saturationValueHeight =>
      colorDialogThemeExtension.saturationValueHeight;
  double get thumbRadius => colorDialogThemeExtension.thumbRadius;
  double get thumbOutlineRadius => colorDialogThemeExtension.thumbOutlineRadius;
  double get thumbStrokeWidth => colorDialogThemeExtension.thumbStrokeWidth;
  Color get thumbColor => colorDialogThemeExtension.thumbColor;
  Color get thumbOutlineColor => colorDialogThemeExtension.thumbOutlineColor;
  double get hueSliderHeight => colorDialogThemeExtension.hueSliderHeight;
  double get hueThumbWidth => colorDialogThemeExtension.hueThumbWidth;
  double get hueThumbStrokeWidth =>
      colorDialogThemeExtension.hueThumbStrokeWidth;
  Color get hueThumbColor => colorDialogThemeExtension.hueThumbColor;
  double get swatchSize => colorDialogThemeExtension.swatchSize;
  double get swatchSpacing => colorDialogThemeExtension.swatchSpacing;
  double get swatchBorderRadius => colorDialogThemeExtension.swatchBorderRadius;
  double get swatchBorderWidth => colorDialogThemeExtension.swatchBorderWidth;
  Color get swatchBorderColor => colorDialogThemeExtension.swatchBorderColor;
  double get selectedSwatchBorderWidth =>
      colorDialogThemeExtension.selectedSwatchBorderWidth;
  Color get selectedSwatchBorderColor =>
      colorDialogThemeExtension.selectedSwatchBorderColor;
  double get previewWidth => colorDialogThemeExtension.previewWidth;
  double get previewHeight => colorDialogThemeExtension.previewHeight;
  double get previewBorderRadius =>
      colorDialogThemeExtension.previewBorderRadius;
  Color get previewBorderColor => colorDialogThemeExtension.previewBorderColor;
  double get previewSpacing => colorDialogThemeExtension.previewSpacing;
  double get hexFieldWidth => colorDialogThemeExtension.hexFieldWidth;
}
