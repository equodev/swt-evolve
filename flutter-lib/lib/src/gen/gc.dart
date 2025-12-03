import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../impl/gc_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

part 'gc.g.dart';

class GCSwt<V extends VGC> extends WidgetSwt<V> {
  const GCSwt({super.key, required super.value});

  @override
  State createState() => GCImpl<GCSwt<VGC>, VGC>();
}

abstract class GCState<T extends GCSwt, V extends VGC>
    extends WidgetSwtState<T, V> {
  @override
  void initState() {
    super.initState();
    _registerListeners();
  }

  void _registerListeners() {
    onOp("copyArea", (p) => onCopyArea(VGCCopyArea.fromJson(p)));
    onOp("copyAreaImage", (p) => onCopyAreaImage(VGCCopyAreaImage.fromJson(p)));
    onOp("drawArc", (p) => onDrawArc(VGCDrawArc.fromJson(p)));
    onOp("drawFocus", (p) => onDrawFocus(VGCDrawFocus.fromJson(p)));
    onOp("drawImage", (p) => onDrawImage(VGCDrawImage.fromJson(p)));
    onOp("drawLine", (p) => onDrawLine(VGCDrawLine.fromJson(p)));
    onOp("drawOval", (p) => onDrawOval(VGCDrawOval.fromJson(p)));
    onOp("drawPoint", (p) => onDrawPoint(VGCDrawPoint.fromJson(p)));
    onOp("drawPolygon", (p) => onDrawPolygon(VGCDrawPolygon.fromJson(p)));
    onOp("drawPolyline", (p) => onDrawPolyline(VGCDrawPolyline.fromJson(p)));
    onOp("drawRectangle", (p) => onDrawRectangle(VGCDrawRectangle.fromJson(p)));
    onOp("drawRoundRectangle",
        (p) => onDrawRoundRectangle(VGCDrawRoundRectangle.fromJson(p)));
    onOp("drawText", (p) => onDrawText(VGCDrawText.fromJson(p)));
    onOp("fillArc", (p) => onFillArc(VGCFillArc.fromJson(p)));
    onOp("fillGradientRectangle",
        (p) => onFillGradientRectangle(VGCFillGradientRectangle.fromJson(p)));
    onOp("fillOval", (p) => onFillOval(VGCFillOval.fromJson(p)));
    onOp("fillPolygon", (p) => onFillPolygon(VGCFillPolygon.fromJson(p)));
    onOp("fillRectangle", (p) => onFillRectangle(VGCFillRectangle.fromJson(p)));
    onOp("fillRoundRectangle",
        (p) => onFillRoundRectangle(VGCFillRoundRectangle.fromJson(p)));
  }

  void onCopyArea(VGCCopyArea opArgs);
  void onCopyAreaImage(VGCCopyAreaImage opArgs);
  void onDrawArc(VGCDrawArc opArgs);
  void onDrawFocus(VGCDrawFocus opArgs);
  void onDrawImage(VGCDrawImage opArgs);
  void onDrawLine(VGCDrawLine opArgs);
  void onDrawOval(VGCDrawOval opArgs);
  void onDrawPoint(VGCDrawPoint opArgs);
  void onDrawPolygon(VGCDrawPolygon opArgs);
  void onDrawPolyline(VGCDrawPolyline opArgs);
  void onDrawRectangle(VGCDrawRectangle opArgs);
  void onDrawRoundRectangle(VGCDrawRoundRectangle opArgs);
  void onDrawText(VGCDrawText opArgs);
  void onFillArc(VGCFillArc opArgs);
  void onFillGradientRectangle(VGCFillGradientRectangle opArgs);
  void onFillOval(VGCFillOval opArgs);
  void onFillPolygon(VGCFillPolygon opArgs);
  void onFillRectangle(VGCFillRectangle opArgs);
  void onFillRoundRectangle(VGCFillRoundRectangle opArgs);
}

@JsonSerializable()
class VGC extends VWidget {
  VGC() : this.empty();
  VGC.empty() {
    swt = "GC";
  }

  bool? XORMode;
  bool? advanced;
  int? alpha;
  int? antialias;
  VColor? background;
  VRectangle? clipping;
  int? fillRule;
  VFont? font;
  VColor? foreground;
  int? interpolation;
  int? lineCap;
  List<int>? lineDash;
  int? lineJoin;
  int? lineStyle;
  int? lineWidth;
  int? textAntialias;

  factory VGC.fromJson(Map<String, dynamic> json) => _$VGCFromJson(json);
  Map<String, dynamic> toJson() => _$VGCToJson(this);
}

@JsonSerializable()
class VGCCopyArea {
  int srcX;
  int srcY;
  int width;
  int height;
  int destX;
  int destY;
  bool paint;

  VGCCopyArea(
      {this.srcX = 0,
      this.srcY = 0,
      this.width = 0,
      this.height = 0,
      this.destX = 0,
      this.destY = 0,
      this.paint = false});

  factory VGCCopyArea.fromJson(Map<String, dynamic> json) =>
      _$VGCCopyAreaFromJson(json);
  Map<String, dynamic> toJson() => _$VGCCopyAreaToJson(this);
}

@JsonSerializable()
class VGCCopyAreaImage {
  VImage? image;
  int x;
  int y;

  VGCCopyAreaImage({this.x = 0, this.y = 0});

  factory VGCCopyAreaImage.fromJson(Map<String, dynamic> json) =>
      _$VGCCopyAreaImageFromJson(json);
  Map<String, dynamic> toJson() => _$VGCCopyAreaImageToJson(this);
}

@JsonSerializable()
class VGCDrawArc {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCDrawArc(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.startAngle = 0,
      this.arcAngle = 0});

  factory VGCDrawArc.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawArcFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawArcToJson(this);
}

@JsonSerializable()
class VGCDrawFocus {
  int x;
  int y;
  int width;
  int height;

  VGCDrawFocus({this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawFocus.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawFocusFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawFocusToJson(this);
}

@JsonSerializable()
class VGCDrawImage {
  VImage? srcImage;
  int srcX;
  int srcY;
  int srcWidth;
  int srcHeight;
  int destX;
  int destY;
  int destWidth;
  int destHeight;
  bool simple;

  VGCDrawImage(
      {this.srcX = 0,
      this.srcY = 0,
      this.srcWidth = 0,
      this.srcHeight = 0,
      this.destX = 0,
      this.destY = 0,
      this.destWidth = 0,
      this.destHeight = 0,
      this.simple = false});

  factory VGCDrawImage.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawImageFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawImageToJson(this);
}

@JsonSerializable()
class VGCDrawLine {
  int x1;
  int y1;
  int x2;
  int y2;

  VGCDrawLine({this.x1 = 0, this.y1 = 0, this.x2 = 0, this.y2 = 0});

  factory VGCDrawLine.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawLineFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawLineToJson(this);
}

@JsonSerializable()
class VGCDrawOval {
  int x;
  int y;
  int width;
  int height;

  VGCDrawOval({this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawOval.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawOvalFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawOvalToJson(this);
}

@JsonSerializable()
class VGCDrawPoint {
  int x;
  int y;

  VGCDrawPoint({this.x = 0, this.y = 0});

  factory VGCDrawPoint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPointFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPointToJson(this);
}

@JsonSerializable()
class VGCDrawPolygon {
  List<int>? pointArray;

  VGCDrawPolygon();

  factory VGCDrawPolygon.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolygonFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolygonToJson(this);
}

@JsonSerializable()
class VGCDrawPolyline {
  List<int>? pointArray;

  VGCDrawPolyline();

  factory VGCDrawPolyline.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolylineFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolylineToJson(this);
}

@JsonSerializable()
class VGCDrawRectangle {
  int x;
  int y;
  int width;
  int height;

  VGCDrawRectangle({this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawRectangleToJson(this);
}

@JsonSerializable()
class VGCDrawRoundRectangle {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCDrawRoundRectangle(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.arcWidth = 0,
      this.arcHeight = 0});

  factory VGCDrawRoundRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawRoundRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawRoundRectangleToJson(this);
}

@JsonSerializable()
class VGCDrawText {
  String string;
  int x;
  int y;
  int flags;

  VGCDrawText({this.string = '', this.x = 0, this.y = 0, this.flags = 0});

  factory VGCDrawText.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawTextFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawTextToJson(this);
}

@JsonSerializable()
class VGCFillArc {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCFillArc(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.startAngle = 0,
      this.arcAngle = 0});

  factory VGCFillArc.fromJson(Map<String, dynamic> json) =>
      _$VGCFillArcFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillArcToJson(this);
}

@JsonSerializable()
class VGCFillGradientRectangle {
  int x;
  int y;
  int width;
  int height;
  bool vertical;

  VGCFillGradientRectangle(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.vertical = false});

  factory VGCFillGradientRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCFillGradientRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillGradientRectangleToJson(this);
}

@JsonSerializable()
class VGCFillOval {
  int x;
  int y;
  int width;
  int height;

  VGCFillOval({this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCFillOval.fromJson(Map<String, dynamic> json) =>
      _$VGCFillOvalFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillOvalToJson(this);
}

@JsonSerializable()
class VGCFillPolygon {
  List<int>? pointArray;

  VGCFillPolygon();

  factory VGCFillPolygon.fromJson(Map<String, dynamic> json) =>
      _$VGCFillPolygonFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillPolygonToJson(this);
}

@JsonSerializable()
class VGCFillRectangle {
  int x;
  int y;
  int width;
  int height;

  VGCFillRectangle({this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCFillRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCFillRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillRectangleToJson(this);
}

@JsonSerializable()
class VGCFillRoundRectangle {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCFillRoundRectangle(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.arcWidth = 0,
      this.arcHeight = 0});

  factory VGCFillRoundRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCFillRoundRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillRoundRectangleToJson(this);
}
