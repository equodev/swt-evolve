// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'progressbar.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProgressBarValue _$ProgressBarValueFromJson(Map<String, dynamic> json) =>
    ProgressBarValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..orientation = (json['orientation'] as num?)?.toInt()
      ..textDirection = (json['textDirection'] as num?)?.toInt()
      ..bounds = json['bounds'] == null
          ? null
          : RectangleValue.fromJson(json['bounds'] as Map<String, dynamic>)
      ..dragDetect = json['dragDetect'] as bool?
      ..enabled = json['enabled'] as bool?
      ..layoutData = json['layoutData']
      ..menu = json['menu'] == null
          ? null
          : MenuValue.fromJson(json['menu'] as Map<String, dynamic>)
      ..toolTipText = json['toolTipText'] as String?
      ..touchEnabled = json['touchEnabled'] as bool?
      ..visible = json['visible'] as bool?
      ..maximum = (json['maximum'] as num?)?.toInt()
      ..minimum = (json['minimum'] as num?)?.toInt()
      ..selection = (json['selection'] as num?)?.toInt()
      ..state = (json['state'] as num?)?.toInt();

Map<String, dynamic> _$ProgressBarValueToJson(ProgressBarValue instance) {
  final val = <String, dynamic>{
    'swt': instance.swt,
    'id': instance.id,
  };

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('children', instance.children?.map((e) => e.toJson()).toList());
  val['style'] = instance.style;
  writeNotNull('orientation', instance.orientation);
  writeNotNull('textDirection', instance.textDirection);
  writeNotNull('bounds', instance.bounds?.toJson());
  writeNotNull('dragDetect', instance.dragDetect);
  writeNotNull('enabled', instance.enabled);
  writeNotNull('layoutData', instance.layoutData);
  writeNotNull('menu', instance.menu?.toJson());
  writeNotNull('toolTipText', instance.toolTipText);
  writeNotNull('touchEnabled', instance.touchEnabled);
  writeNotNull('visible', instance.visible);
  writeNotNull('maximum', instance.maximum);
  writeNotNull('minimum', instance.minimum);
  writeNotNull('selection', instance.selection);
  writeNotNull('state', instance.state);
  return val;
}
