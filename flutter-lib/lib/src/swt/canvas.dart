import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/canvas_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'canvas.g.dart';

class CanvasSwt<V extends CanvasValue> extends CompositeSwt<V> {
  const CanvasSwt({super.key, required super.value});

  @override
  State createState() => CanvasImpl<CanvasSwt<CanvasValue>, CanvasValue>();
}

@JsonSerializable()
class CanvasValue extends CompositeValue {
  CanvasValue() : this.empty();
  CanvasValue.empty() {
    swt = "Canvas";
  }

  factory CanvasValue.fromJson(Map<String, dynamic> json) =>
      _$CanvasValueFromJson(json);
  Map<String, dynamic> toJson() => _$CanvasValueToJson(this);
}
