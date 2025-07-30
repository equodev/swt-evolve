import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/tablecolumn_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'tablecolumn.g.dart';

class TableColumnSwt<V extends TableColumnValue> extends ItemSwt<V> {
  const TableColumnSwt({super.key, required super.value});

  @override
  State createState() =>
      TableColumnImpl<TableColumnSwt<TableColumnValue>, TableColumnValue>();

  void sendControlResize(V val, Object? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendControlMove(V val, Object? payload) {
    sendEvent(val, "Control/Move", payload);
  }

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class TableColumnValue extends ItemValue {
  TableColumnValue() : this.empty();
  TableColumnValue.empty() {
    swt = "TableColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  String? toolTipText;
  int? width;

  factory TableColumnValue.fromJson(Map<String, dynamic> json) =>
      _$TableColumnValueFromJson(json);
  Map<String, dynamic> toJson() => _$TableColumnValueToJson(this);
}
