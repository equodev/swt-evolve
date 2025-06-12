import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/widget.dart';
import '../impl/caret_impl.dart';
import '../swt/rectangle.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'caret.g.dart';

class CaretSwt<V extends CaretValue> extends WidgetSwt<V> {
  const CaretSwt({super.key, required super.value});

  @override
  State createState() => CaretImpl<CaretSwt<CaretValue>, CaretValue>();
}

@JsonSerializable()
class CaretValue extends WidgetValue {
  CaretValue() : this.empty();
  CaretValue.empty() {
    swt = "Caret";
  }

  RectangleValue? bounds;
  bool? visible;

  factory CaretValue.fromJson(Map<String, dynamic> json) =>
      _$CaretValueFromJson(json);
  Map<String, dynamic> toJson() => _$CaretValueToJson(this);
}
