import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../gen/tablecolumn.dart';
import '../gen/tableitem.dart';
import '../impl/table_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'table.g.dart';

class TableSwt<V extends VTable> extends CompositeSwt<V> {
  const TableSwt({super.key, required super.value});

  @override
  State createState() => TableImpl<TableSwt<VTable>, VTable>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VTable extends VComposite {
  VTable() : this.empty();
  VTable.empty() {
    swt = "Table";
  }

  List<int>? columnOrder;
  List<VTableColumn>? columns;
  VColor? headerBackground;
  VColor? headerForeground;
  bool? headerVisible;
  List<VTableItem>? items;
  bool? linesVisible;
  List<int>? selection;
  VTableColumn? sortColumn;
  int? sortDirection;
  int? topIndex;

  factory VTable.fromJson(Map<String, dynamic> json) => _$VTableFromJson(json);
  Map<String, dynamic> toJson() => _$VTableToJson(this);
}
