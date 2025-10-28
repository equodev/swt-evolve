// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'clabel.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCLabel _$VCLabelFromJson(Map<String, dynamic> json) => VCLabel()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..capture = json['capture'] as bool?
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..redraw = json['redraw'] as bool?
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..tabList = (json['tabList'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..caret = json['caret'] == null
      ? null
      : VCaret.fromJson(json['caret'] as Map<String, dynamic>)
  ..alignment = (json['alignment'] as num?)?.toInt()
  ..backgroundImage = json['backgroundImage'] == null
      ? null
      : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>)
  ..bottomMargin = (json['bottomMargin'] as num?)?.toInt()
  ..gradientColors = (json['gradientColors'] as List<dynamic>?)
      ?.map((e) => VColor.fromJson(e as Map<String, dynamic>))
      .toList()
  ..gradientPercents = (json['gradientPercents'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..gradientVertical = json['gradientVertical'] as bool?
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..leftMargin = (json['leftMargin'] as num?)?.toInt()
  ..rightMargin = (json['rightMargin'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..topMargin = (json['topMargin'] as num?)?.toInt();

Map<String, dynamic> _$VCLabelToJson(VCLabel instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'font': instance.font,
      'foreground': instance.foreground,
      'orientation': instance.orientation,
      'redraw': instance.redraw,
      'textDirection': instance.textDirection,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'scrollbarsMode': instance.scrollbarsMode,
      'backgroundMode': instance.backgroundMode,
      'children': instance.children,
      'layoutDeferred': instance.layoutDeferred,
      'tabList': instance.tabList,
      'caret': instance.caret,
      'alignment': instance.alignment,
      'backgroundImage': instance.backgroundImage,
      'bottomMargin': instance.bottomMargin,
      'gradientColors': instance.gradientColors,
      'gradientPercents': instance.gradientPercents,
      'gradientVertical': instance.gradientVertical,
      'image': instance.image,
      'leftMargin': instance.leftMargin,
      'rightMargin': instance.rightMargin,
      'text': instance.text,
      'topMargin': instance.topMargin,
    };
