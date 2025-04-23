// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabfolder.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CTabFolderValue _$CTabFolderValueFromJson(Map<String, dynamic> json) =>
    CTabFolderValue()
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
      ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
      ..layout = json['layout'] == null
          ? null
          : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
      ..layoutDeferred = json['layoutDeferred'] as bool?
      ..borderVisible = json['borderVisible'] as bool?
      ..minimized = json['minimized'] as bool?
      ..minimizeVisible = json['minimizeVisible'] as bool?
      ..minimumCharacters = (json['minimumCharacters'] as num?)?.toInt()
      ..maximized = json['maximized'] as bool?
      ..maximizeVisible = json['maximizeVisible'] as bool?
      ..mRUVisible = json['mRUVisible'] as bool?
      ..selectionIndex = (json['selectionIndex'] as num?)?.toInt()
      ..simple = json['simple'] as bool?
      ..single = json['single'] as bool?
      ..tabHeight = (json['tabHeight'] as num?)?.toInt()
      ..tabPosition = (json['tabPosition'] as num?)?.toInt()
      ..unselectedCloseVisible = json['unselectedCloseVisible'] as bool?
      ..unselectedImageVisible = json['unselectedImageVisible'] as bool?
      ..highlightEnabled = json['highlightEnabled'] as bool?;

Map<String, dynamic> _$CTabFolderValueToJson(CTabFolderValue instance) {
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
  writeNotNull('backgroundMode', instance.backgroundMode);
  writeNotNull('layout', instance.layout?.toJson());
  writeNotNull('layoutDeferred', instance.layoutDeferred);
  writeNotNull('borderVisible', instance.borderVisible);
  writeNotNull('minimized', instance.minimized);
  writeNotNull('minimizeVisible', instance.minimizeVisible);
  writeNotNull('minimumCharacters', instance.minimumCharacters);
  writeNotNull('maximized', instance.maximized);
  writeNotNull('maximizeVisible', instance.maximizeVisible);
  writeNotNull('mRUVisible', instance.mRUVisible);
  writeNotNull('selectionIndex', instance.selectionIndex);
  writeNotNull('simple', instance.simple);
  writeNotNull('single', instance.single);
  writeNotNull('tabHeight', instance.tabHeight);
  writeNotNull('tabPosition', instance.tabPosition);
  writeNotNull('unselectedCloseVisible', instance.unselectedCloseVisible);
  writeNotNull('unselectedImageVisible', instance.unselectedImageVisible);
  writeNotNull('highlightEnabled', instance.highlightEnabled);
  return val;
}
