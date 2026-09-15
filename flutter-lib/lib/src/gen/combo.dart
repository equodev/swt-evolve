import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
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
class VCombo extends VComposite {
  VCombo() : this.empty();
  VCombo.empty() {
    swt = "Combo";
  }

  List<String>? items;
  bool? listVisible;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCombo) {
      items = other.items;
      listVisible = other.listVisible;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => e as String)
            .toList();
      case 'listVisible':
        listVisible = json['listVisible'] as bool?;
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VCombo.fromJson(Map<String, dynamic> json) =>
      _$VComboFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VComboToJson(this);
}
