// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'styledtextrenderer.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VStyledTextRenderer _$VStyledTextRendererFromJson(Map<String, dynamic> json) =>
    VStyledTextRenderer()
      ..ascent = (json['ascent'] as num?)?.toInt()
      ..averageCharWidth = (json['averageCharWidth'] as num?)?.toInt()
      ..averageLineHeight = (json['averageLineHeight'] as num?)?.toDouble()
      ..boldFont = json['boldFont'] == null
          ? null
          : VFont.fromJson(json['boldFont'] as Map<String, dynamic>)
      ..boldItalicFont = json['boldItalicFont'] == null
          ? null
          : VFont.fromJson(json['boldItalicFont'] as Map<String, dynamic>)
      ..bulletsIndices = (json['bulletsIndices'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList()
      ..descent = (json['descent'] as num?)?.toInt()
      ..fixedPitch = json['fixedPitch'] as bool?
      ..hasLinks = json['hasLinks'] as bool?
      ..idleRunning = json['idleRunning'] as bool?
      ..italicFont = json['italicFont'] == null
          ? null
          : VFont.fromJson(json['italicFont'] as Map<String, dynamic>)
      ..lineCount = (json['lineCount'] as num?)?.toInt()
      ..lineSizes = (json['lineSizes'] as List<dynamic>?)
          ?.map(
            (e) => e == null
                ? null
                : VLineSizeInfo.fromJson(e as Map<String, dynamic>),
          )
          .toList()
      ..lineSpacingComputing = json['lineSpacingComputing'] as bool?
      ..lines = (json['lines'] as List<dynamic>?)
          ?.map(
            (e) => e == null
                ? null
                : VLineInfo.fromJson(e as Map<String, dynamic>),
          )
          .toList()
      ..linesInAverageLineHeight = (json['linesInAverageLineHeight'] as num?)
          ?.toInt()
      ..maxWidth = (json['maxWidth'] as num?)?.toInt()
      ..maxWidthLineIndex = (json['maxWidthLineIndex'] as num?)?.toInt()
      ..ranges = (json['ranges'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList()
      ..redrawLines = (json['redrawLines'] as List<dynamic>?)
          ?.map((e) => (e as num).toInt())
          .toList()
      ..regularFont = json['regularFont'] == null
          ? null
          : VFont.fromJson(json['regularFont'] as Map<String, dynamic>)
      ..styleCount = (json['styleCount'] as num?)?.toInt()
      ..styledText = json['styledText'] == null
          ? null
          : VStyledText.fromJson(json['styledText'] as Map<String, dynamic>)
      ..styles = (json['styles'] as List<dynamic>?)
          ?.map((e) => VStyleRange.fromJson(e as Map<String, dynamic>))
          .toList()
      ..stylesSet = (json['stylesSet'] as List<dynamic>?)
          ?.map((e) => VStyleRange.fromJson(e as Map<String, dynamic>))
          .toList()
      ..stylesSetCount = (json['stylesSetCount'] as num?)?.toInt()
      ..tabLength = (json['tabLength'] as num?)?.toInt()
      ..tabWidth = (json['tabWidth'] as num?)?.toInt()
      ..topIndex = (json['topIndex'] as num?)?.toInt();

Map<String, dynamic> _$VStyledTextRendererToJson(
  VStyledTextRenderer instance,
) => <String, dynamic>{
  'ascent': instance.ascent,
  'averageCharWidth': instance.averageCharWidth,
  'averageLineHeight': instance.averageLineHeight,
  'boldFont': instance.boldFont,
  'boldItalicFont': instance.boldItalicFont,
  'bulletsIndices': instance.bulletsIndices,
  'descent': instance.descent,
  'fixedPitch': instance.fixedPitch,
  'hasLinks': instance.hasLinks,
  'idleRunning': instance.idleRunning,
  'italicFont': instance.italicFont,
  'lineCount': instance.lineCount,
  'lineSizes': instance.lineSizes,
  'lineSpacingComputing': instance.lineSpacingComputing,
  'lines': instance.lines,
  'linesInAverageLineHeight': instance.linesInAverageLineHeight,
  'maxWidth': instance.maxWidth,
  'maxWidthLineIndex': instance.maxWidthLineIndex,
  'ranges': instance.ranges,
  'redrawLines': instance.redrawLines,
  'regularFont': instance.regularFont,
  'styleCount': instance.styleCount,
  'styledText': instance.styledText,
  'styles': instance.styles,
  'stylesSet': instance.stylesSet,
  'stylesSetCount': instance.stylesSetCount,
  'tabLength': instance.tabLength,
  'tabWidth': instance.tabWidth,
  'topIndex': instance.topIndex,
};

VLineSizeInfo _$VLineSizeInfoFromJson(Map<String, dynamic> json) =>
    VLineSizeInfo()
      ..height = (json['height'] as num?)?.toInt()
      ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VLineSizeInfoToJson(VLineSizeInfo instance) =>
    <String, dynamic>{'height': instance.height, 'width': instance.width};

VLineInfo _$VLineInfoFromJson(Map<String, dynamic> json) => VLineInfo()
  ..alignment = (json['alignment'] as num?)?.toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..flags = (json['flags'] as num?)?.toInt()
  ..indent = (json['indent'] as num?)?.toInt()
  ..justify = json['justify'] as bool?
  ..segments = (json['segments'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..segmentsChars = (json['segmentsChars'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..tabStops = (json['tabStops'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..verticalIndent = (json['verticalIndent'] as num?)?.toInt()
  ..wrapIndent = (json['wrapIndent'] as num?)?.toInt();

Map<String, dynamic> _$VLineInfoToJson(VLineInfo instance) => <String, dynamic>{
  'alignment': instance.alignment,
  'background': instance.background,
  'flags': instance.flags,
  'indent': instance.indent,
  'justify': instance.justify,
  'segments': instance.segments,
  'segmentsChars': instance.segmentsChars,
  'tabStops': instance.tabStops,
  'verticalIndent': instance.verticalIndent,
  'wrapIndent': instance.wrapIndent,
};
