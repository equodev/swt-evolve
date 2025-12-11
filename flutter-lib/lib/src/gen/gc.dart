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
    onOp("copyAreaImageintint",
        (p) => onCopyAreaImageintint(VGCCopyAreaImageintint.fromJson(p)));
    onOp(
        "copyAreaintintintintintint",
        (p) => onCopyAreaintintintintintint(
            VGCCopyAreaintintintintintint.fromJson(p)));
    onOp(
        "copyAreaintintintintintintboolean",
        (p) => onCopyAreaintintintintintintboolean(
            VGCCopyAreaintintintintintintboolean.fromJson(p)));
    onOp(
        "drawArcintintintintintint",
        (p) => onDrawArcintintintintintint(
            VGCDrawArcintintintintintint.fromJson(p)));
    onOp("drawFocusintintintint",
        (p) => onDrawFocusintintintint(VGCDrawFocusintintintint.fromJson(p)));
    onOp("drawImageImageintint",
        (p) => onDrawImageImageintint(VGCDrawImageImageintint.fromJson(p)));
    onOp(
        "drawImageImageintintintintintintintint",
        (p) => onDrawImageImageintintintintintintintint(
            VGCDrawImageImageintintintintintintintint.fromJson(p)));
    onOp("drawLineintintintint",
        (p) => onDrawLineintintintint(VGCDrawLineintintintint.fromJson(p)));
    onOp("drawOvalintintintint",
        (p) => onDrawOvalintintintint(VGCDrawOvalintintintint.fromJson(p)));
    onOp("drawPointintint",
        (p) => onDrawPointintint(VGCDrawPointintint.fromJson(p)));
    onOp("drawPolygonint",
        (p) => onDrawPolygonint(VGCDrawPolygonint.fromJson(p)));
    onOp("drawPolylineint",
        (p) => onDrawPolylineint(VGCDrawPolylineint.fromJson(p)));
    onOp("drawRectangleRectangle",
        (p) => onDrawRectangleRectangle(VGCDrawRectangleRectangle.fromJson(p)));
    onOp(
        "drawRectangleintintintint",
        (p) => onDrawRectangleintintintint(
            VGCDrawRectangleintintintint.fromJson(p)));
    onOp(
        "drawRoundRectangleintintintintintint",
        (p) => onDrawRoundRectangleintintintintintint(
            VGCDrawRoundRectangleintintintintintint.fromJson(p)));
    onOp("drawStringStringintint",
        (p) => onDrawStringStringintint(VGCDrawStringStringintint.fromJson(p)));
    onOp(
        "drawStringStringintintboolean",
        (p) => onDrawStringStringintintboolean(
            VGCDrawStringStringintintboolean.fromJson(p)));
    onOp("drawTextStringintint",
        (p) => onDrawTextStringintint(VGCDrawTextStringintint.fromJson(p)));
    onOp(
        "drawTextStringintintboolean",
        (p) => onDrawTextStringintintboolean(
            VGCDrawTextStringintintboolean.fromJson(p)));
    onOp(
        "drawTextStringintintint",
        (p) =>
            onDrawTextStringintintint(VGCDrawTextStringintintint.fromJson(p)));
    onOp(
        "fillArcintintintintintint",
        (p) => onFillArcintintintintintint(
            VGCFillArcintintintintintint.fromJson(p)));
    onOp(
        "fillGradientRectangleintintintintboolean",
        (p) => onFillGradientRectangleintintintintboolean(
            VGCFillGradientRectangleintintintintboolean.fromJson(p)));
    onOp("fillOvalintintintint",
        (p) => onFillOvalintintintint(VGCFillOvalintintintint.fromJson(p)));
    onOp("fillPolygonint",
        (p) => onFillPolygonint(VGCFillPolygonint.fromJson(p)));
    onOp("fillRectangleRectangle",
        (p) => onFillRectangleRectangle(VGCFillRectangleRectangle.fromJson(p)));
    onOp(
        "fillRectangleintintintint",
        (p) => onFillRectangleintintintint(
            VGCFillRectangleintintintint.fromJson(p)));
    onOp(
        "fillRoundRectangleintintintintintint",
        (p) => onFillRoundRectangleintintintintintint(
            VGCFillRoundRectangleintintintintintint.fromJson(p)));
  }

  void onCopyAreaImageintint(VGCCopyAreaImageintint opArgs);
  void onCopyAreaintintintintintint(VGCCopyAreaintintintintintint opArgs);
  void onCopyAreaintintintintintintboolean(
      VGCCopyAreaintintintintintintboolean opArgs);
  void onDrawArcintintintintintint(VGCDrawArcintintintintintint opArgs);
  void onDrawFocusintintintint(VGCDrawFocusintintintint opArgs);
  void onDrawImageImageintint(VGCDrawImageImageintint opArgs);
  void onDrawImageImageintintintintintintintint(
      VGCDrawImageImageintintintintintintintint opArgs);
  void onDrawLineintintintint(VGCDrawLineintintintint opArgs);
  void onDrawOvalintintintint(VGCDrawOvalintintintint opArgs);
  void onDrawPointintint(VGCDrawPointintint opArgs);
  void onDrawPolygonint(VGCDrawPolygonint opArgs);
  void onDrawPolylineint(VGCDrawPolylineint opArgs);
  void onDrawRectangleRectangle(VGCDrawRectangleRectangle opArgs);
  void onDrawRectangleintintintint(VGCDrawRectangleintintintint opArgs);
  void onDrawRoundRectangleintintintintintint(
      VGCDrawRoundRectangleintintintintintint opArgs);
  void onDrawStringStringintint(VGCDrawStringStringintint opArgs);
  void onDrawStringStringintintboolean(VGCDrawStringStringintintboolean opArgs);
  void onDrawTextStringintint(VGCDrawTextStringintint opArgs);
  void onDrawTextStringintintboolean(VGCDrawTextStringintintboolean opArgs);
  void onDrawTextStringintintint(VGCDrawTextStringintintint opArgs);
  void onFillArcintintintintintint(VGCFillArcintintintintintint opArgs);
  void onFillGradientRectangleintintintintboolean(
      VGCFillGradientRectangleintintintintboolean opArgs);
  void onFillOvalintintintint(VGCFillOvalintintintint opArgs);
  void onFillPolygonint(VGCFillPolygonint opArgs);
  void onFillRectangleRectangle(VGCFillRectangleRectangle opArgs);
  void onFillRectangleintintintint(VGCFillRectangleintintintint opArgs);
  void onFillRoundRectangleintintintintintint(
      VGCFillRoundRectangleintintintintintint opArgs);
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
class VGCCopyAreaImageintint {
  VImage? image;
  int x;
  int y;

  VGCCopyAreaImageintint({this.x = 0, this.y = 0});

  factory VGCCopyAreaImageintint.fromJson(Map<String, dynamic> json) =>
      _$VGCCopyAreaImageintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCCopyAreaImageintintToJson(this);
}

@JsonSerializable()
class VGCCopyAreaintintintintintint {
  int srcX;
  int srcY;
  int width;
  int height;
  int destX;
  int destY;

  VGCCopyAreaintintintintintint(
      {this.srcX = 0,
      this.srcY = 0,
      this.width = 0,
      this.height = 0,
      this.destX = 0,
      this.destY = 0});

  factory VGCCopyAreaintintintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCCopyAreaintintintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCCopyAreaintintintintintintToJson(this);
}

@JsonSerializable()
class VGCCopyAreaintintintintintintboolean {
  int srcX;
  int srcY;
  int width;
  int height;
  int destX;
  int destY;
  bool paint;

  VGCCopyAreaintintintintintintboolean(
      {this.srcX = 0,
      this.srcY = 0,
      this.width = 0,
      this.height = 0,
      this.destX = 0,
      this.destY = 0,
      this.paint = false});

  factory VGCCopyAreaintintintintintintboolean.fromJson(
          Map<String, dynamic> json) =>
      _$VGCCopyAreaintintintintintintbooleanFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCCopyAreaintintintintintintbooleanToJson(this);
}

@JsonSerializable()
class VGCDrawArcintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCDrawArcintintintintintint(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.startAngle = 0,
      this.arcAngle = 0});

  factory VGCDrawArcintintintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawArcintintintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawArcintintintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawFocusintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawFocusintintintint(
      {this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawFocusintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawFocusintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawFocusintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawImageImageintint {
  VImage? image;
  int x;
  int y;

  VGCDrawImageImageintint({this.x = 0, this.y = 0});

  factory VGCDrawImageImageintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawImageImageintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawImageImageintintToJson(this);
}

@JsonSerializable()
class VGCDrawImageImageintintintintintintintint {
  VImage? image;
  int srcX;
  int srcY;
  int srcWidth;
  int srcHeight;
  int destX;
  int destY;
  int destWidth;
  int destHeight;

  VGCDrawImageImageintintintintintintintint(
      {this.srcX = 0,
      this.srcY = 0,
      this.srcWidth = 0,
      this.srcHeight = 0,
      this.destX = 0,
      this.destY = 0,
      this.destWidth = 0,
      this.destHeight = 0});

  factory VGCDrawImageImageintintintintintintintint.fromJson(
          Map<String, dynamic> json) =>
      _$VGCDrawImageImageintintintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCDrawImageImageintintintintintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawLineintintintint {
  int x1;
  int y1;
  int x2;
  int y2;

  VGCDrawLineintintintint({this.x1 = 0, this.y1 = 0, this.x2 = 0, this.y2 = 0});

  factory VGCDrawLineintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawLineintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawLineintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawOvalintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawOvalintintintint(
      {this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawOvalintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawOvalintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawOvalintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawPointintint {
  int x;
  int y;

  VGCDrawPointintint({this.x = 0, this.y = 0});

  factory VGCDrawPointintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPointintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPointintintToJson(this);
}

@JsonSerializable()
class VGCDrawPolygonint {
  List<int>? pointArray;

  VGCDrawPolygonint();

  factory VGCDrawPolygonint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolygonintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolygonintToJson(this);
}

@JsonSerializable()
class VGCDrawPolylineint {
  List<int>? pointArray;

  VGCDrawPolylineint();

  factory VGCDrawPolylineint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawPolylineintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawPolylineintToJson(this);
}

@JsonSerializable()
class VGCDrawRectangleRectangle {
  VRectangle? rect;

  VGCDrawRectangleRectangle();

  factory VGCDrawRectangleRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawRectangleRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawRectangleRectangleToJson(this);
}

@JsonSerializable()
class VGCDrawRectangleintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCDrawRectangleintintintint(
      {this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCDrawRectangleintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawRectangleintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawRectangleintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawRoundRectangleintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCDrawRoundRectangleintintintintintint(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.arcWidth = 0,
      this.arcHeight = 0});

  factory VGCDrawRoundRectangleintintintintintint.fromJson(
          Map<String, dynamic> json) =>
      _$VGCDrawRoundRectangleintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCDrawRoundRectangleintintintintintintToJson(this);
}

@JsonSerializable()
class VGCDrawStringStringintint {
  String string;
  int x;
  int y;

  VGCDrawStringStringintint({this.string = '', this.x = 0, this.y = 0});

  factory VGCDrawStringStringintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawStringStringintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawStringStringintintToJson(this);
}

@JsonSerializable()
class VGCDrawStringStringintintboolean {
  String string;
  int x;
  int y;
  bool isTransparent;

  VGCDrawStringStringintintboolean(
      {this.string = '', this.x = 0, this.y = 0, this.isTransparent = false});

  factory VGCDrawStringStringintintboolean.fromJson(
          Map<String, dynamic> json) =>
      _$VGCDrawStringStringintintbooleanFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCDrawStringStringintintbooleanToJson(this);
}

@JsonSerializable()
class VGCDrawTextStringintint {
  String string;
  int x;
  int y;

  VGCDrawTextStringintint({this.string = '', this.x = 0, this.y = 0});

  factory VGCDrawTextStringintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawTextStringintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawTextStringintintToJson(this);
}

@JsonSerializable()
class VGCDrawTextStringintintboolean {
  String string;
  int x;
  int y;
  bool isTransparent;

  VGCDrawTextStringintintboolean(
      {this.string = '', this.x = 0, this.y = 0, this.isTransparent = false});

  factory VGCDrawTextStringintintboolean.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawTextStringintintbooleanFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawTextStringintintbooleanToJson(this);
}

@JsonSerializable()
class VGCDrawTextStringintintint {
  String string;
  int x;
  int y;
  int flags;

  VGCDrawTextStringintintint(
      {this.string = '', this.x = 0, this.y = 0, this.flags = 0});

  factory VGCDrawTextStringintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCDrawTextStringintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCDrawTextStringintintintToJson(this);
}

@JsonSerializable()
class VGCFillArcintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int startAngle;
  int arcAngle;

  VGCFillArcintintintintintint(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.startAngle = 0,
      this.arcAngle = 0});

  factory VGCFillArcintintintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillArcintintintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillArcintintintintintintToJson(this);
}

@JsonSerializable()
class VGCFillGradientRectangleintintintintboolean {
  int x;
  int y;
  int width;
  int height;
  bool vertical;

  VGCFillGradientRectangleintintintintboolean(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.vertical = false});

  factory VGCFillGradientRectangleintintintintboolean.fromJson(
          Map<String, dynamic> json) =>
      _$VGCFillGradientRectangleintintintintbooleanFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCFillGradientRectangleintintintintbooleanToJson(this);
}

@JsonSerializable()
class VGCFillOvalintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCFillOvalintintintint(
      {this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCFillOvalintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillOvalintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillOvalintintintintToJson(this);
}

@JsonSerializable()
class VGCFillPolygonint {
  List<int>? pointArray;

  VGCFillPolygonint();

  factory VGCFillPolygonint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillPolygonintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillPolygonintToJson(this);
}

@JsonSerializable()
class VGCFillRectangleRectangle {
  VRectangle? rect;

  VGCFillRectangleRectangle();

  factory VGCFillRectangleRectangle.fromJson(Map<String, dynamic> json) =>
      _$VGCFillRectangleRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillRectangleRectangleToJson(this);
}

@JsonSerializable()
class VGCFillRectangleintintintint {
  int x;
  int y;
  int width;
  int height;

  VGCFillRectangleintintintint(
      {this.x = 0, this.y = 0, this.width = 0, this.height = 0});

  factory VGCFillRectangleintintintint.fromJson(Map<String, dynamic> json) =>
      _$VGCFillRectangleintintintintFromJson(json);
  Map<String, dynamic> toJson() => _$VGCFillRectangleintintintintToJson(this);
}

@JsonSerializable()
class VGCFillRoundRectangleintintintintintint {
  int x;
  int y;
  int width;
  int height;
  int arcWidth;
  int arcHeight;

  VGCFillRoundRectangleintintintintintint(
      {this.x = 0,
      this.y = 0,
      this.width = 0,
      this.height = 0,
      this.arcWidth = 0,
      this.arcHeight = 0});

  factory VGCFillRoundRectangleintintintintintint.fromJson(
          Map<String, dynamic> json) =>
      _$VGCFillRoundRectangleintintintintintintFromJson(json);
  Map<String, dynamic> toJson() =>
      _$VGCFillRoundRectangleintintintintintintToJson(this);
}
