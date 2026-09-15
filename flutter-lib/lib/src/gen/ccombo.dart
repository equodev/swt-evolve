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
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../impl/ccombo_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'ccombo.g.dart';

class CComboSwt<V extends VCCombo> extends CompositeSwt<V> {
  const CComboSwt({super.key, required super.value});

  @override
  State createState() => CComboImpl<CComboSwt<VCCombo>, VCCombo>();

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
class VCCombo extends VComposite {
  VCCombo() : this.empty();
  VCCombo.empty() {
    swt = "CCombo";
  }

  int? alignment;
  List<String>? itemTooltips;
  List<String>? items;
  bool? listVisible;
  VPoint? selection;
  String? text;
  int? textLimit;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCCombo) {
      alignment = other.alignment;
      itemTooltips = other.itemTooltips;
      items = other.items;
      listVisible = other.listVisible;
      selection = other.selection;
      text = other.text;
      textLimit = other.textLimit;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alignment':
        alignment = (json['alignment'] as num?)?.toInt();
      case 'itemTooltips':
        itemTooltips = (json['itemTooltips'] as List<dynamic>?)
            ?.map((e) => e as String)
            .toList();
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => e as String)
            .toList();
      case 'listVisible':
        listVisible = json['listVisible'] as bool?;
      case 'selection':
        selection = json['selection'] == null
            ? null
            : VPoint.fromJson(json['selection'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      case 'textLimit':
        textLimit = (json['textLimit'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VCCombo.fromJson(Map<String, dynamic> json) =>
      _$VCComboFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VCComboToJson(this);
}
