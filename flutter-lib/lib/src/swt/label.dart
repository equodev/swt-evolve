import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/label_impl.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'label.g.dart';

class LabelSwt<V extends LabelValue> extends ControlSwt<V> {
  const LabelSwt({super.key, required super.value});

  @override
  State createState() => LabelImpl<LabelSwt<LabelValue>, LabelValue>();
}

@JsonSerializable()
class LabelValue extends ControlValue {
  LabelValue() : this.empty();
  LabelValue.empty() {
    swt = "Label";
  }

  int? alignment;
  String? text;

  factory LabelValue.fromJson(Map<String, dynamic> json) =>
      _$LabelValueFromJson(json);
  Map<String, dynamic> toJson() => _$LabelValueToJson(this);
}
