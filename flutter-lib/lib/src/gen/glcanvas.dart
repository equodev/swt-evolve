import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/gldata.dart';
import '../gen/image.dart';
import '../gen/ime.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../impl/glcanvas_evolve.dart';
import 'widgets.dart';

part 'glcanvas.g.dart';

class GLCanvasSwt<V extends VGLCanvas> extends CanvasSwt<V> {
  const GLCanvasSwt({super.key, required super.value});

  @override
  State createState() => GLCanvasImpl<GLCanvasSwt<VGLCanvas>, VGLCanvas>();
}

@JsonSerializable()
class VGLCanvas extends VCanvas {
  VGLCanvas() : this.empty();
  VGLCanvas.empty() {
    swt = "GLCanvas";
  }

  VGLData? GLData;

  factory VGLCanvas.fromJson(Map<String, dynamic> json) =>
      _$VGLCanvasFromJson(json);
  Map<String, dynamic> toJson() => _$VGLCanvasToJson(this);
}
