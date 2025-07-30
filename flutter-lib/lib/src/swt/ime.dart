import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/widget.dart';
import '../impl/ime_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'ime.g.dart';

class IMESwt<V extends IMEValue> extends WidgetSwt<V> {
  const IMESwt({super.key, required super.value});

  @override
  State createState() => IMEImpl<IMESwt<IMEValue>, IMEValue>();
}

@JsonSerializable()
class IMEValue extends WidgetValue {
  IMEValue() : this.empty();
  IMEValue.empty() {
    swt = "IME";
  }

  int? compositionOffset;

  factory IMEValue.fromJson(Map<String, dynamic> json) =>
      _$IMEValueFromJson(json);
  Map<String, dynamic> toJson() => _$IMEValueToJson(this);
}
