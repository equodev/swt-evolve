// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shell.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShellValue _$ShellValueFromJson(Map<String, dynamic> json) => ShellValue()
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
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..layout = json['layout'] == null
      ? null
      : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..menuBar = json['menuBar']
  ..minimized = json['minimized'] as bool?
  ..text = json['text'] as String?
  ..alpha = (json['alpha'] as num?)?.toInt()
  ..fullScreen = json['fullScreen'] as bool?
  ..maximized = json['maximized'] as bool?
  ..modified = json['modified'] as bool?
  ..visible = json['visible'] as bool?
  ..imeInputMode = (json['imeInputMode'] as num?)?.toInt();

Map<String, dynamic> _$ShellValueToJson(ShellValue instance) {
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
  writeNotNull('backgroundMode', instance.backgroundMode);
  writeNotNull('layout', instance.layout?.toJson());
  writeNotNull('layoutDeferred', instance.layoutDeferred);
  writeNotNull('menuBar', instance.menuBar);
  writeNotNull('minimized', instance.minimized);
  writeNotNull('text', instance.text);
  writeNotNull('alpha', instance.alpha);
  writeNotNull('fullScreen', instance.fullScreen);
  writeNotNull('maximized', instance.maximized);
  writeNotNull('modified', instance.modified);
  writeNotNull('visible', instance.visible);
  writeNotNull('imeInputMode', instance.imeInputMode);
  return val;
}
