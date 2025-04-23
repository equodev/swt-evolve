// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'combo.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ComboValue _$ComboValueFromJson(Map<String, dynamic> json) => ComboValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
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
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..layout = json['layout'] == null
      ? null
      : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..items = (json['items'] as List<dynamic>?)?.map((e) => e as String).toList()
  ..listVisible = json['listVisible'] as bool?
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..selectionIndex = (json['selectionIndex'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..textLimit = (json['textLimit'] as num?)?.toInt()
  ..visibleItemCount = (json['visibleItemCount'] as num?)?.toInt();

Map<String, dynamic> _$ComboValueToJson(ComboValue instance) {
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
  writeNotNull('textDirection', instance.textDirection);
  writeNotNull('bounds', instance.bounds?.toJson());
  writeNotNull('dragDetect', instance.dragDetect);
  writeNotNull('enabled', instance.enabled);
  writeNotNull('layoutData', instance.layoutData);
  writeNotNull('menu', instance.menu?.toJson());
  writeNotNull('toolTipText', instance.toolTipText);
  writeNotNull('touchEnabled', instance.touchEnabled);
  writeNotNull('visible', instance.visible);
  writeNotNull('backgroundMode', instance.backgroundMode);
  writeNotNull('layout', instance.layout?.toJson());
  writeNotNull('layoutDeferred', instance.layoutDeferred);
  writeNotNull('items', instance.items);
  writeNotNull('listVisible', instance.listVisible);
  writeNotNull('orientation', instance.orientation);
  writeNotNull('selectionIndex', instance.selectionIndex);
  writeNotNull('text', instance.text);
  writeNotNull('textLimit', instance.textLimit);
  writeNotNull('visibleItemCount', instance.visibleItemCount);
  return val;
}
