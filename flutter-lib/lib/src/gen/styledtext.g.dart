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
  ..cursor = json['cursor'] == null
      ? null
      : VCursor.fromJson(json['cursor'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..dragSource = json['dragSource'] as bool?
  ..dropTargetId = (json['dropTargetId'] as num?)?.toInt()
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
  ..region = json['region'] == null
      ? null
      : VRegion.fromJson(json['region'] as Map<String, dynamic>)
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..horizontalBar = json['horizontalBar'] == null
      ? null
      : VScrollBar.fromJson(json['horizontalBar'] as Map<String, dynamic>)
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..verticalBar = json['verticalBar'] == null
      ? null
      : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>)
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..tabList = (json['tabList'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..IME = json['IME'] == null
      ? null
      : VIME.fromJson(json['IME'] as Map<String, dynamic>)
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
          json['blockSelectionBounds'] as Map<String, dynamic>,
        )
  ..bottomMargin = (json['bottomMargin'] as num?)?.toInt()
  ..caretOffset = (json['caretOffset'] as num?)?.toInt()
  ..columnX = (json['columnX'] as num?)?.toInt()
  ..doubleClickEnabled = json['doubleClickEnabled'] as bool?
  ..editable = json['editable'] as bool?
  ..fixedLineMetrics = json['fixedLineMetrics'] == null
      ? null
      : VFontMetrics.fromJson(json['fixedLineMetrics'] as Map<String, dynamic>)
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
  ..styleRanges = (json['styleRanges'] as List<dynamic>?)
      ?.map((e) => VStyleRange.fromJson(e as Map<String, dynamic>))
      .toList()
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
  ..wrapIndent = (json['wrapIndent'] as num?)?.toInt()
  ..renderer = json['renderer'] == null
      ? null
      : VStyledTextRenderer.fromJson(json['renderer'] as Map<String, dynamic>);

Map<String, dynamic> _$VStyledTextToJson(VStyledText instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': ?instance.background,
      'backgroundImage': ?instance.backgroundImage,
      'bounds': ?instance.bounds,
      'capture': ?instance.capture,
      'cursor': ?instance.cursor,
      'dragDetect': ?instance.dragDetect,
      'dragSource': ?instance.dragSource,
      'dropTargetId': ?instance.dropTargetId,
      'enabled': ?instance.enabled,
      'font': ?instance.font,
      'foreground': ?instance.foreground,
      'menu': ?instance.menu,
      'orientation': ?instance.orientation,
      'redraw': ?instance.redraw,
      'region': ?instance.region,
      'textDirection': ?instance.textDirection,
      'toolTipText': ?instance.toolTipText,
      'touchEnabled': ?instance.touchEnabled,
      'visible': ?instance.visible,
      'horizontalBar': ?instance.horizontalBar,
      'scrollbarsMode': ?instance.scrollbarsMode,
      'verticalBar': ?instance.verticalBar,
      'backgroundMode': ?instance.backgroundMode,
      'children': ?instance.children,
      'layoutDeferred': ?instance.layoutDeferred,
      'tabList': ?instance.tabList,
      'IME': ?instance.IME,
      'caret': ?instance.caret,
      'alignment': ?instance.alignment,
      'alwaysShowScrollBars': ?instance.alwaysShowScrollBars,
      'bidiColoring': ?instance.bidiColoring,
      'blockSelection': ?instance.blockSelection,
      'blockSelectionBounds': ?instance.blockSelectionBounds,
      'bottomMargin': ?instance.bottomMargin,
      'caretOffset': ?instance.caretOffset,
      'columnX': ?instance.columnX,
      'doubleClickEnabled': ?instance.doubleClickEnabled,
      'editable': ?instance.editable,
      'fixedLineMetrics': ?instance.fixedLineMetrics,
      'horizontalIndex': ?instance.horizontalIndex,
      'horizontalPixel': ?instance.horizontalPixel,
      'indent': ?instance.indent,
      'justify': ?instance.justify,
      'leftMargin': ?instance.leftMargin,
      'lineSpacing': ?instance.lineSpacing,
      'marginColor': ?instance.marginColor,
      'mouseNavigatorEnabled': ?instance.mouseNavigatorEnabled,
      'ranges': ?instance.ranges,
      'rightMargin': ?instance.rightMargin,
      'selection': ?instance.selection,
      'selectionBackground': ?instance.selectionBackground,
      'selectionForeground': ?instance.selectionForeground,
      'selectionRange': ?instance.selectionRange,
      'selectionRanges': ?instance.selectionRanges,
      'styleRange': ?instance.styleRange,
      'styleRanges': ?instance.styleRanges,
      'tabStops': ?instance.tabStops,
      'tabs': ?instance.tabs,
      'text': ?instance.text,
      'textLimit': ?instance.textLimit,
      'topIndex': ?instance.topIndex,
      'topMargin': ?instance.topMargin,
      'topPixel': ?instance.topPixel,
      'verticalScrollOffset': ?instance.verticalScrollOffset,
      'wordWrap': ?instance.wordWrap,
      'wrapIndent': ?instance.wrapIndent,
      'renderer': ?instance.renderer,
    };
