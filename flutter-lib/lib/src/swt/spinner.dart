import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/spinner_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'spinner.g.dart';

class SpinnerSwt<V extends SpinnerValue> extends CompositeSwt<V> {
  const SpinnerSwt({super.key, required super.value});

  @override
  State createState() => SpinnerImpl<SpinnerSwt<SpinnerValue>, SpinnerValue>();

  void sendModifyModify(V val, Object? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendVerifyVerify(V val, Object? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

@JsonSerializable()
class SpinnerValue extends CompositeValue {
  SpinnerValue() : this.empty();
  SpinnerValue.empty() {
    swt = "Spinner";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? textLimit;
  int? digits;

  factory SpinnerValue.fromJson(Map<String, dynamic> json) =>
      _$SpinnerValueFromJson(json);
  Map<String, dynamic> toJson() => _$SpinnerValueToJson(this);
}
