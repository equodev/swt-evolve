import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/canvas.dart';
import '../impl/decorations_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'decorations.g.dart';

class DecorationsSwt<V extends DecorationsValue> extends CanvasSwt<V> {
  const DecorationsSwt({super.key, required super.value});

  @override
  State createState() =>
      DecorationsImpl<DecorationsSwt<DecorationsValue>, DecorationsValue>();
}

@JsonSerializable()
class DecorationsValue extends CanvasValue {
  DecorationsValue() : this.empty();
  DecorationsValue.empty() {
    swt = "Decorations";
  }

  bool? maximized;
  Object? menuBar;
  bool? minimized;
  String? text;

  factory DecorationsValue.fromJson(Map<String, dynamic> json) =>
      _$DecorationsValueFromJson(json);
  Map<String, dynamic> toJson() => _$DecorationsValueToJson(this);
}
