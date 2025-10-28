import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../gen/treecolumn.dart';
import '../gen/treeitem.dart';
import '../impl/tree_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tree.g.dart';

class TreeSwt<V extends VTree> extends CompositeSwt<V> {
  const TreeSwt({super.key, required super.value});

  @override
  State createState() => TreeImpl<TreeSwt<VTree>, VTree>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendTreeCollapse(V val, VEvent? payload) {
    sendEvent(val, "Tree/Collapse", payload);
  }

  void sendTreeExpand(V val, VEvent? payload) {
    sendEvent(val, "Tree/Expand", payload);
  }
}

@JsonSerializable()
class VTree extends VComposite {
  VTree() : this.empty();
  VTree.empty() {
    swt = "Tree";
  }

  List<int>? columnOrder;
  List<VTreeColumn>? columns;
  VColor? headerBackground;
  VColor? headerForeground;
  bool? headerVisible;
  List<VTreeItem>? items;
  bool? linesVisible;
  List<VTreeItem>? selection;
  VTreeColumn? sortColumn;
  int? sortDirection;
  VTreeItem? topItem;

  factory VTree.fromJson(Map<String, dynamic> json) => _$VTreeFromJson(json);
  Map<String, dynamic> toJson() => _$VTreeToJson(this);
}
