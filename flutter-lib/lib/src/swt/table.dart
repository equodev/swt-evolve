import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/table_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'table.g.dart';

class TableSwt<V extends TableValue> extends CompositeSwt<V> {
  const TableSwt({super.key, required super.value});

  @override
  State createState() => TableImpl<TableSwt<TableValue>, TableValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendDeselectionDeselection(V val, Object? payload) {
    sendEvent(val, "Deselection/Deselection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }
}

@JsonSerializable()
class TableValue extends CompositeValue {
  TableValue() : this.empty();
  TableValue.empty() {
    swt = "Table";
  }

  bool? headerVisible;
  int? itemCount;
  bool? linesVisible;
  int? selectionIndex;
  int? sortDirection;
  int? topIndex;
  List<int>? selection;

  factory TableValue.fromJson(Map<String, dynamic> json) =>
      _$TableValueFromJson(json);
  Map<String, dynamic> toJson() => _$TableValueToJson(this);
}
