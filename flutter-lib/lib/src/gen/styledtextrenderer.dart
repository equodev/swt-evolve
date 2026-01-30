import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/styledtext.dart';
import '../gen/stylerange.dart';

part 'styledtextrenderer.g.dart';

@JsonSerializable()
class VStyledTextRenderer {
  VStyledTextRenderer();

  int? ascent;
  int? averageCharWidth;
  double? averageLineHeight;
  VFont? boldFont;
  VFont? boldItalicFont;
  List<int>? bulletsIndices;
  int? descent;
  bool? fixedPitch;
  bool? hasLinks;
  bool? idleRunning;
  VFont? italicFont;
  int? lineCount;
  List<VLineSizeInfo?>? lineSizes;
  bool? lineSpacingComputing;
  List<VLineInfo?>? lines;
  int? linesInAverageLineHeight;
  int? maxWidth;
  int? maxWidthLineIndex;
  List<int>? ranges;
  List<int>? redrawLines;
  VFont? regularFont;
  int? styleCount;
  VStyledText? styledText;
  List<VStyleRange>? styles;
  List<VStyleRange>? stylesSet;
  int? stylesSetCount;
  int? tabLength;
  int? tabWidth;
  int? topIndex;

  factory VStyledTextRenderer.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextRendererFromJson(json);
  Map<String, dynamic> toJson() => _$VStyledTextRendererToJson(this);
}

@JsonSerializable()
class VLineSizeInfo {
  VLineSizeInfo() : this.empty();
  VLineSizeInfo.empty();

  int? height;
  int? width;

  factory VLineSizeInfo.fromJson(Map<String, dynamic> json) =>
      _$VLineSizeInfoFromJson(json);
  Map<String, dynamic> toJson() => _$VLineSizeInfoToJson(this);
}

@JsonSerializable()
class VLineInfo {
  VLineInfo() : this.empty();
  VLineInfo.empty();

  int? alignment;
  VColor? background;
  int? flags;
  int? indent;
  bool? justify;
  List<int>? segments;
  List<int>? segmentsChars;
  List<int>? tabStops;
  int? verticalIndent;
  int? wrapIndent;

  factory VLineInfo.fromJson(Map<String, dynamic> json) =>
      _$VLineInfoFromJson(json);
  Map<String, dynamic> toJson() => _$VLineInfoToJson(this);
}
