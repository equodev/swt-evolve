import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../impl/treecolumn_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'treecolumn.g.dart';

class TreeColumnSwt<V extends VTreeColumn> extends ItemSwt<V> {
  const TreeColumnSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeColumnImpl<TreeColumnSwt<VTreeColumn>, VTreeColumn>();

  void sendControlMove(V val, VEvent? payload) {
    sendEvent(val, "Control/Move", payload);
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VTreeColumn extends VItem {
  VTreeColumn() : this.empty();
  VTreeColumn.empty() {
    swt = "TreeColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  String? toolTipText;
  int? width;

  factory VTreeColumn.fromJson(Map<String, dynamic> json) =>
      _$VTreeColumnFromJson(json);
  Map<String, dynamic> toJson() => _$VTreeColumnToJson(this);
}
