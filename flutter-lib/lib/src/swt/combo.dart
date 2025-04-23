import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/combo_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'combo.g.dart';

class ComboSwt<V extends ComboValue> extends CompositeSwt<V> {
  const ComboSwt({super.key, required super.value});

  @override
  State createState() => ComboImpl<ComboSwt<ComboValue>, ComboValue>();

  void sendModifyModify(V val, Object? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSegmentSegments(V val, Object? payload) {
    sendEvent(val, "Segment/Segments", payload);
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
class ComboValue extends CompositeValue {
  ComboValue() : this.empty();
  ComboValue.empty() {
    swt = "Combo";
  }

  List<String>? items;
  bool? listVisible;
  int? orientation;
  int? selectionIndex;
  String? text;
  int? textLimit;
  int? visibleItemCount;

  factory ComboValue.fromJson(Map<String, dynamic> json) =>
      _$ComboValueFromJson(json);
  Map<String, dynamic> toJson() => _$ComboValueToJson(this);
}
