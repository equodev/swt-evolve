// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'text.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VText _$VTextFromJson(Map<String, dynamic> json) => VText()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..backgroundImage = json['backgroundImage'] == null
      ? null
      : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>)
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
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..redraw = json['redraw'] as bool?
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..doubleClickEnabled = json['doubleClickEnabled'] as bool?
  ..echoChar = (json['echoChar'] as num?)?.toInt()
  ..editable = json['editable'] as bool?
  ..message = json['message'] as String?
  ..selection = json['selection'] == null
      ? null
      : VPoint.fromJson(json['selection'] as Map<String, dynamic>)
  ..tabs = (json['tabs'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..textChars = (json['textChars'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..textLimit = (json['textLimit'] as num?)?.toInt()
  ..topIndex = (json['topIndex'] as num?)?.toInt();

Map<String, dynamic> _$VTextToJson(VText instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'backgroundImage': instance.backgroundImage,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'font': instance.font,
      'foreground': instance.foreground,
      'menu': instance.menu,
      'orientation': instance.orientation,
      'redraw': instance.redraw,
      'textDirection': instance.textDirection,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'scrollbarsMode': instance.scrollbarsMode,
      'doubleClickEnabled': instance.doubleClickEnabled,
      'echoChar': instance.echoChar,
      'editable': instance.editable,
      'message': instance.message,
      'selection': instance.selection,
      'tabs': instance.tabs,
      'text': instance.text,
      'textChars': instance.textChars,
      'textLimit': instance.textLimit,
      'topIndex': instance.topIndex,
    };
