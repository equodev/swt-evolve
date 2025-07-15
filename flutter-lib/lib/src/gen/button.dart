import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../impl/button_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'button.g.dart';

class ButtonSwt<V extends VButton> extends ControlSwt<V> {
  const ButtonSwt({super.key, required super.value});

  @override
  State createState() => ButtonImpl<ButtonSwt<VButton>, VButton>();

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class VButton extends VControl {
  VButton() : this.empty();
  VButton.empty() {
    swt = "Button";
  }

  int? alignment;
  bool? grayed;
  bool? selection;
  String? text;

  factory VButton.fromJson(Map<String, dynamic> json) =>
      _$VButtonFromJson(json);
  Map<String, dynamic> toJson() => _$VButtonToJson(this);
}
