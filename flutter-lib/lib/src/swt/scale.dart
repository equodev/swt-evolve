import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/scale_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'scale.g.dart';

class ScaleSwt<V extends ScaleValue> extends ControlSwt<V> {
  const ScaleSwt({super.key, required super.value});

  @override
  State createState() => ScaleImpl<ScaleSwt<ScaleValue>, ScaleValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class ScaleValue extends ControlValue {
  ScaleValue() : this.empty();
  ScaleValue.empty() {
    swt = "Scale";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;

  factory ScaleValue.fromJson(Map<String, dynamic> json) =>
      _$ScaleValueFromJson(json);
  Map<String, dynamic> toJson() => _$ScaleValueToJson(this);
}
