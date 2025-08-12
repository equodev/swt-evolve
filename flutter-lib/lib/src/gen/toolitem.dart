import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/item.dart';
import '../impl/toolitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'toolitem.g.dart';

class ToolItemSwt<V extends VToolItem> extends ItemSwt<V> {
  const ToolItemSwt({super.key, required super.value});

  @override
  State createState() => ToolItemImpl<ToolItemSwt<VToolItem>, VToolItem>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VToolItem extends VItem {
  VToolItem() : this.empty();
  VToolItem.empty() {
    swt = "ToolItem";
  }

  VColor? background;
  VControl? control;
  bool? enabled;
  VColor? foreground;
  bool? selection;
  String? toolTipText;
  int? width;

  factory VToolItem.fromJson(Map<String, dynamic> json) =>
      _$VToolItemFromJson(json);
  Map<String, dynamic> toJson() => _$VToolItemToJson(this);
}
