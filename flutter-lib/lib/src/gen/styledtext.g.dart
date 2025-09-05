// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'styledtext.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VStyledText _$VStyledTextFromJson(Map<String, dynamic> json) => VStyledText()
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
  ..alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?
  ..bidiColoring = json['bidiColoring'] as bool?
  ..blockSelection = json['blockSelection'] as bool?
  ..blockSelectionBounds = json['blockSelectionBounds'] == null
      ? null
      : VRectangle.fromJson(
          json['blockSelectionBounds'] as Map<String, dynamic>)
  ..bottomMargin = (json['bottomMargin'] as num?)?.toInt()
  ..caretOffset = (json['caretOffset'] as num?)?.toInt()
  ..columnX = (json['columnX'] as num?)?.toInt()
  ..doubleClickEnabled = json['doubleClickEnabled'] as bool?
  ..editable = json['editable'] as bool?
  ..horizontalIndex = (json['horizontalIndex'] as num?)?.toInt()
  ..horizontalPixel = (json['horizontalPixel'] as num?)?.toInt()
  ..indent = (json['indent'] as num?)?.toInt()
  ..justify = json['justify'] as bool?
  ..leftMargin = (json['leftMargin'] as num?)?.toInt()
  ..lineSpacing = (json['lineSpacing'] as num?)?.toInt()
  ..marginColor = json['marginColor'] == null
      ? null
      : VColor.fromJson(json['marginColor'] as Map<String, dynamic>)
  ..mouseNavigatorEnabled = json['mouseNavigatorEnabled'] as bool?
  ..ranges = (json['ranges'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..rightMargin = (json['rightMargin'] as num?)?.toInt()
  ..selection = json['selection'] == null
      ? null
      : VPoint.fromJson(json['selection'] as Map<String, dynamic>)
  ..selectionBackground = json['selectionBackground'] == null
      ? null
      : VColor.fromJson(json['selectionBackground'] as Map<String, dynamic>)
  ..selectionForeground = json['selectionForeground'] == null
      ? null
      : VColor.fromJson(json['selectionForeground'] as Map<String, dynamic>)
  ..selectionRange = json['selectionRange'] == null
      ? null
      : VPoint.fromJson(json['selectionRange'] as Map<String, dynamic>)
  ..selectionRanges = (json['selectionRanges'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..styleRange = (json['styleRange'] as num?)?.toInt()
  ..tabStops = (json['tabStops'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..tabs = (json['tabs'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..textLimit = (json['textLimit'] as num?)?.toInt()
  ..topIndex = (json['topIndex'] as num?)?.toInt()
  ..topMargin = (json['topMargin'] as num?)?.toInt()
  ..topPixel = (json['topPixel'] as num?)?.toInt()
  ..verticalScrollOffset = (json['verticalScrollOffset'] as num?)?.toInt()
  ..wordWrap = json['wordWrap'] as bool?
  ..wrapIndent = (json['wrapIndent'] as num?)?.toInt();

Map<String, dynamic> _$VStyledTextToJson(VStyledText instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'backgroundImage': instance.backgroundImage,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
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
      'alwaysShowScrollBars': instance.alwaysShowScrollBars,
      'bidiColoring': instance.bidiColoring,
      'blockSelection': instance.blockSelection,
      'blockSelectionBounds': instance.blockSelectionBounds,
      'bottomMargin': instance.bottomMargin,
      'caretOffset': instance.caretOffset,
      'columnX': instance.columnX,
      'doubleClickEnabled': instance.doubleClickEnabled,
      'editable': instance.editable,
      'horizontalIndex': instance.horizontalIndex,
      'horizontalPixel': instance.horizontalPixel,
      'indent': instance.indent,
      'justify': instance.justify,
      'leftMargin': instance.leftMargin,
      'lineSpacing': instance.lineSpacing,
      'marginColor': instance.marginColor,
      'mouseNavigatorEnabled': instance.mouseNavigatorEnabled,
      'ranges': instance.ranges,
      'rightMargin': instance.rightMargin,
      'selection': instance.selection,
      'selectionBackground': instance.selectionBackground,
      'selectionForeground': instance.selectionForeground,
      'selectionRange': instance.selectionRange,
      'selectionRanges': instance.selectionRanges,
      'styleRange': instance.styleRange,
      'tabStops': instance.tabStops,
      'tabs': instance.tabs,
      'text': instance.text,
      'textLimit': instance.textLimit,
      'topIndex': instance.topIndex,
      'topMargin': instance.topMargin,
      'topPixel': instance.topPixel,
      'verticalScrollOffset': instance.verticalScrollOffset,
      'wordWrap': instance.wordWrap,
      'wrapIndent': instance.wrapIndent,
    };
