import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../impl/tablecolumn_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tablecolumn.g.dart';

class TableColumnSwt<V extends VTableColumn> extends ItemSwt<V> {
  
  const TableColumnSwt({super.key, required super.value});

  
  @override
  State createState() => TableColumnImpl<TableColumnSwt<VTableColumn>, VTableColumn>();

  

  
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


@JsonSerializable() class VTableColumn extends VItem {
  VTableColumn() : this.empty();
  VTableColumn.empty()  { swt = "TableColumn"; }
  
  int? alignment;
  bool? moveable;
  bool? resizable;
  String? toolTipText;
  int? width;
  
  factory VTableColumn.fromJson(Map<String, dynamic> json) => _$VTableColumnFromJson(json);
  Map<String, dynamic> toJson() => _$VTableColumnToJson(this);
  
}