import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/item.dart';
import '../impl/treecolumn_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'treecolumn.g.dart';

class TreeColumnSwt<V extends TreeColumnValue> extends ItemSwt<V> {
  const TreeColumnSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeColumnImpl<TreeColumnSwt<TreeColumnValue>, TreeColumnValue>();

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
class TreeColumnValue extends ItemValue {
  TreeColumnValue() : this.empty();
  TreeColumnValue.empty() {
    swt = "TreeColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  String? toolTipText;
  int? width;

  factory TreeColumnValue.fromJson(Map<String, dynamic> json) =>
      _$TreeColumnValueFromJson(json);
  Map<String, dynamic> toJson() => _$TreeColumnValueToJson(this);
}
