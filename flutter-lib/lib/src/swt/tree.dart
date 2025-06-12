import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/composite.dart';
import '../impl/tree_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'tree.g.dart';

class TreeSwt<V extends TreeValue> extends CompositeSwt<V> {
  const TreeSwt({super.key, required super.value});

  @override
  State createState() => TreeImpl<TreeSwt<TreeValue>, TreeValue>();

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendTreeExpand(V val, Object? payload) {
    sendEvent(val, "Tree/Expand", payload);
  }

  void sendTreeCollapse(V val, Object? payload) {
    sendEvent(val, "Tree/Collapse", payload);
  }
}

@JsonSerializable()
class TreeValue extends CompositeValue {
  TreeValue() : this.empty();
  TreeValue.empty() {
    swt = "Tree";
  }

  bool? headerVisible;
  int? itemCount;
  bool? linesVisible;
  int? sortDirection;

  factory TreeValue.fromJson(Map<String, dynamic> json) =>
      _$TreeValueFromJson(json);
  Map<String, dynamic> toJson() => _$TreeValueToJson(this);
}
