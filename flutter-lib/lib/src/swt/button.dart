import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/control.dart';
import '../impl/button_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'button.g.dart';

class ButtonSwt<V extends ButtonValue> extends ControlSwt<V> {
  const ButtonSwt({super.key, required super.value});

  @override
  State createState() => ButtonImpl<ButtonSwt<ButtonValue>, ButtonValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class ButtonValue extends ControlValue {
  ButtonValue() : this.empty();
  ButtonValue.empty() {
    swt = "Button";
  }

  int? alignment;
  bool? grayed;
  bool? selection;
  String? text;

  factory ButtonValue.fromJson(Map<String, dynamic> json) =>
      _$ButtonValueFromJson(json);
  Map<String, dynamic> toJson() => _$ButtonValueToJson(this);
}
