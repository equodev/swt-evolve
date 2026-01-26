// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'text_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TextThemeExtension _$TextThemeExtensionFromJson(Map<String, dynamic> json) =>
    TextThemeExtension(
      textColor: const ColorConverter().fromJson(json['textColor'] as String),
      disabledTextColor: const ColorConverter().fromJson(
        json['disabledTextColor'] as String,
      ),
      placeholderColor: const ColorConverter().fromJson(
        json['placeholderColor'] as String,
      ),
      helperTextColor: const ColorConverter().fromJson(
        json['helperTextColor'] as String,
      ),
      errorTextColor: const ColorConverter().fromJson(
        json['errorTextColor'] as String,
      ),
      backgroundColor: const ColorConverter().fromJson(
        json['backgroundColor'] as String,
      ),
      disabledBackgroundColor: const ColorConverter().fromJson(
        json['disabledBackgroundColor'] as String,
      ),
      hoverBackgroundColor: const ColorConverter().fromJson(
        json['hoverBackgroundColor'] as String,
      ),
      focusedBackgroundColor: const ColorConverter().fromJson(
        json['focusedBackgroundColor'] as String,
      ),
      borderColor: const ColorConverter().fromJson(
        json['borderColor'] as String,
      ),
      hoverBorderColor: const ColorConverter().fromJson(
        json['hoverBorderColor'] as String,
      ),
      focusedBorderColor: const ColorConverter().fromJson(
        json['focusedBorderColor'] as String,
      ),
      errorBorderColor: const ColorConverter().fromJson(
        json['errorBorderColor'] as String,
      ),
      disabledBorderColor: const ColorConverter().fromJson(
        json['disabledBorderColor'] as String,
      ),
      borderRadius: (json['borderRadius'] as num).toDouble(),
      borderWidth: (json['borderWidth'] as num).toDouble(),
      hoverBorderWidth: (json['hoverBorderWidth'] as num).toDouble(),
      focusedBorderWidth: (json['focusedBorderWidth'] as num).toDouble(),
      errorBorderWidth: (json['errorBorderWidth'] as num).toDouble(),
      contentPadding: const EdgeInsetsConverter().fromJson(
        json['contentPadding'] as Map<String, dynamic>,
      ),
      fontSize: (json['fontSize'] as num).toDouble(),
      fontWeight: const FontWeightConverter().fromJson(
        (json['fontWeight'] as num).toInt(),
      ),
      fontFamily: json['fontFamily'] as String?,
      letterSpacing: (json['letterSpacing'] as num).toDouble(),
      lineHeight: (json['lineHeight'] as num).toDouble(),
      helperTextFontSize: (json['helperTextFontSize'] as num).toDouble(),
      helperTextSpacing: (json['helperTextSpacing'] as num).toDouble(),
      focusAnimationDuration: Duration(
        microseconds: (json['focusAnimationDuration'] as num).toInt(),
      ),
      focusAnimationCurve: const CurveConverter().fromJson(
        json['focusAnimationCurve'] as String,
      ),
      hoverAnimationDuration: Duration(
        microseconds: (json['hoverAnimationDuration'] as num).toInt(),
      ),
      hoverAnimationCurve: const CurveConverter().fromJson(
        json['hoverAnimationCurve'] as String,
      ),
      passwordToggleColor: const ColorConverter().fromJson(
        json['passwordToggleColor'] as String,
      ),
      passwordToggleHoverColor: const ColorConverter().fromJson(
        json['passwordToggleHoverColor'] as String,
      ),
      passwordToggleSize: (json['passwordToggleSize'] as num).toDouble(),
    );

Map<String, dynamic> _$TextThemeExtensionToJson(
  TextThemeExtension instance,
) => <String, dynamic>{
  'textColor': const ColorConverter().toJson(instance.textColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'placeholderColor': const ColorConverter().toJson(instance.placeholderColor),
  'helperTextColor': const ColorConverter().toJson(instance.helperTextColor),
  'errorTextColor': const ColorConverter().toJson(instance.errorTextColor),
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'focusedBackgroundColor': const ColorConverter().toJson(
    instance.focusedBackgroundColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'hoverBorderColor': const ColorConverter().toJson(instance.hoverBorderColor),
  'focusedBorderColor': const ColorConverter().toJson(
    instance.focusedBorderColor,
  ),
  'errorBorderColor': const ColorConverter().toJson(instance.errorBorderColor),
  'disabledBorderColor': const ColorConverter().toJson(
    instance.disabledBorderColor,
  ),
  'borderRadius': instance.borderRadius,
  'borderWidth': instance.borderWidth,
  'hoverBorderWidth': instance.hoverBorderWidth,
  'focusedBorderWidth': instance.focusedBorderWidth,
  'errorBorderWidth': instance.errorBorderWidth,
  'contentPadding': const EdgeInsetsConverter().toJson(instance.contentPadding),
  'fontSize': instance.fontSize,
  'fontWeight': const FontWeightConverter().toJson(instance.fontWeight),
  'fontFamily': instance.fontFamily,
  'letterSpacing': instance.letterSpacing,
  'lineHeight': instance.lineHeight,
  'helperTextFontSize': instance.helperTextFontSize,
  'helperTextSpacing': instance.helperTextSpacing,
  'focusAnimationDuration': instance.focusAnimationDuration.inMicroseconds,
  'focusAnimationCurve': const CurveConverter().toJson(
    instance.focusAnimationCurve,
  ),
  'hoverAnimationDuration': instance.hoverAnimationDuration.inMicroseconds,
  'hoverAnimationCurve': const CurveConverter().toJson(
    instance.hoverAnimationCurve,
  ),
  'passwordToggleColor': const ColorConverter().toJson(
    instance.passwordToggleColor,
  ),
  'passwordToggleHoverColor': const ColorConverter().toJson(
    instance.passwordToggleHoverColor,
  ),
  'passwordToggleSize': instance.passwordToggleSize,
};
