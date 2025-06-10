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

Map<String, dynamic> _$ProgressBarValueToJson(ProgressBarValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'orientation': instance.orientation,
      'textDirection': instance.textDirection,
      'bounds': instance.bounds,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'layoutData': instance.layoutData,
      'menu': instance.menu,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'maximum': instance.maximum,
      'minimum': instance.minimum,
      'selection': instance.selection,
      'state': instance.state,
    };
