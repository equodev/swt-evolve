import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../impl/combo_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'combo.g.dart';

class ComboSwt<V extends VCombo> extends CompositeSwt<V> {
  const ComboSwt({super.key, required super.value});

  @override
  State createState() => ComboImpl<ComboSwt<VCombo>, VCombo>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSegmentSegments(V val, VEvent? payload) {
    sendEvent(val, "Segment/Segments", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

@JsonSerializable()
class VCombo extends VComposite {
  VCombo() : this.empty();
  VCombo.empty() {
    swt = "Combo";
  }

  bool? ignoreSelection;
  List<String>? items;
  bool? listVisible;
  VPoint? selection;
  String? text;
  int? textLimit;
  int? visibleItemCount;

  factory VCombo.fromJson(Map<String, dynamic> json) => _$VComboFromJson(json);
  Map<String, dynamic> toJson() => _$VComboToJson(this);
}
