import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../impl/canvas_evolve.dart';
import 'widgets.dart';

part 'canvas.g.dart';

class CanvasSwt<V extends VCanvas> extends CompositeSwt<V> {
  const CanvasSwt({super.key, required super.value});

  @override
  State createState() => CanvasImpl<CanvasSwt<VCanvas>, VCanvas>();
}

@JsonSerializable()
class VCanvas extends VComposite {
  VCanvas() : this.empty();
  VCanvas.empty() {
    swt = "Canvas";
  }

  VCaret? caret;

  factory VCanvas.fromJson(Map<String, dynamic> json) =>
      _$VCanvasFromJson(json);
  Map<String, dynamic> toJson() => _$VCanvasToJson(this);
}
