import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../impl/spinner_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'spinner.g.dart';

class SpinnerSwt<V extends VSpinner> extends CompositeSwt<V> {
  const SpinnerSwt({super.key, required super.value});

  @override
  State createState() => SpinnerImpl<SpinnerSwt<VSpinner>, VSpinner>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

@JsonSerializable()
class VSpinner extends VComposite {
  VSpinner() : this.empty();
  VSpinner.empty() {
    swt = "Spinner";
  }

  int? digits;
  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? textLimit;

  factory VSpinner.fromJson(Map<String, dynamic> json) =>
      _$VSpinnerFromJson(json);
  Map<String, dynamic> toJson() => _$VSpinnerToJson(this);
}
