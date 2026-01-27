// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scale_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScaleThemeExtension _$ScaleThemeExtensionFromJson(Map<String, dynamic> json) =>
    ScaleThemeExtension(
      animationDuration: Duration(
        microseconds: (json['animationDuration'] as num).toInt(),
      ),
      activeTrackColor: const ColorConverter().fromJson(
        json['activeTrackColor'] as String,
      ),
      inactiveTrackColor: const ColorConverter().fromJson(
        json['inactiveTrackColor'] as String,
      ),
      disabledActiveTrackColor: const ColorConverter().fromJson(
        json['disabledActiveTrackColor'] as String,
      ),
      disabledInactiveTrackColor: const ColorConverter().fromJson(
        json['disabledInactiveTrackColor'] as String,
      ),
      thumbColor: const ColorConverter().fromJson(json['thumbColor'] as String),
      thumbHoverColor: const ColorConverter().fromJson(
        json['thumbHoverColor'] as String,
      ),
      disabledThumbColor: const ColorConverter().fromJson(
        json['disabledThumbColor'] as String,
      ),
      tickMarkColor: const ColorConverter().fromJson(
        json['tickMarkColor'] as String,
      ),
      disabledTickMarkColor: const ColorConverter().fromJson(
        json['disabledTickMarkColor'] as String,
      ),
      overlayColor: const ColorConverter().fromJson(
        json['overlayColor'] as String,
      ),
      trackHeight: (json['trackHeight'] as num).toDouble(),
      thumbRadius: (json['thumbRadius'] as num).toDouble(),
      thumbHoverRadius: (json['thumbHoverRadius'] as num).toDouble(),
      tickMarkHeight: (json['tickMarkHeight'] as num).toDouble(),
      tickMarkWidth: (json['tickMarkWidth'] as num).toDouble(),
      tickMarkCount: (json['tickMarkCount'] as num).toInt(),
      minWidth: (json['minWidth'] as num).toDouble(),
      minHeight: (json['minHeight'] as num).toDouble(),
      minVerticalWidth: (json['minVerticalWidth'] as num).toDouble(),
      minVerticalHeight: (json['minVerticalHeight'] as num).toDouble(),
      horizontalPadding: (json['horizontalPadding'] as num).toDouble(),
      defaultMinimum: (json['defaultMinimum'] as num).toInt(),
      defaultMaximum: (json['defaultMaximum'] as num).toInt(),
      defaultSelection: (json['defaultSelection'] as num).toInt(),
      defaultIncrement: (json['defaultIncrement'] as num).toInt(),
      defaultPageIncrement: (json['defaultPageIncrement'] as num).toInt(),
    );

Map<String, dynamic> _$ScaleThemeExtensionToJson(
  ScaleThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'activeTrackColor': const ColorConverter().toJson(instance.activeTrackColor),
  'inactiveTrackColor': const ColorConverter().toJson(
    instance.inactiveTrackColor,
  ),
  'disabledActiveTrackColor': const ColorConverter().toJson(
    instance.disabledActiveTrackColor,
  ),
  'disabledInactiveTrackColor': const ColorConverter().toJson(
    instance.disabledInactiveTrackColor,
  ),
  'thumbColor': const ColorConverter().toJson(instance.thumbColor),
  'thumbHoverColor': const ColorConverter().toJson(instance.thumbHoverColor),
  'disabledThumbColor': const ColorConverter().toJson(
    instance.disabledThumbColor,
  ),
  'tickMarkColor': const ColorConverter().toJson(instance.tickMarkColor),
  'disabledTickMarkColor': const ColorConverter().toJson(
    instance.disabledTickMarkColor,
  ),
  'overlayColor': const ColorConverter().toJson(instance.overlayColor),
  'trackHeight': instance.trackHeight,
  'thumbRadius': instance.thumbRadius,
  'thumbHoverRadius': instance.thumbHoverRadius,
  'tickMarkHeight': instance.tickMarkHeight,
  'tickMarkWidth': instance.tickMarkWidth,
  'tickMarkCount': instance.tickMarkCount,
  'minWidth': instance.minWidth,
  'minHeight': instance.minHeight,
  'minVerticalWidth': instance.minVerticalWidth,
  'minVerticalHeight': instance.minVerticalHeight,
  'horizontalPadding': instance.horizontalPadding,
  'defaultMinimum': instance.defaultMinimum,
  'defaultMaximum': instance.defaultMaximum,
  'defaultSelection': instance.defaultSelection,
  'defaultIncrement': instance.defaultIncrement,
  'defaultPageIncrement': instance.defaultPageIncrement,
};
