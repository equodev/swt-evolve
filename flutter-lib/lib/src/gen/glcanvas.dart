import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/canvas.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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

  factory VGLCanvas.fromJson(Map<String, dynamic> json) =>
      _$VGLCanvasFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VGLCanvasToJson(this);
}
