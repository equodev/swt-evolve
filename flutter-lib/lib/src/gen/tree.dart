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
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/treecolumn.dart';
import '../gen/treeeditor.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
import '../impl/tree_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tree.g.dart';

class TreeSwt<V extends VTree> extends CompositeSwt<V> {
  const TreeSwt({super.key, required super.value});

  @override
  State createState() => TreeImpl<TreeSwt<VTree>, VTree>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

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

  List<VTreeColumn>? columns;
  List<VTreeEditor>? editors;
  VColor? headerBackground;
  bool? headerVisible;
  List<VTreeItem>? items;
  bool? linesVisible;
  List<VTreeItem>? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTree) {
      columns = other.columns;
      editors = other.editors;
      headerBackground = other.headerBackground;
      headerVisible = other.headerVisible;
      items = other.items;
      linesVisible = other.linesVisible;
      selection = other.selection;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'columns':
        columns = (json['columns'] as List<dynamic>?)
            ?.map((e) => VTreeColumn.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'editors':
        editors = (json['editors'] as List<dynamic>?)
            ?.map((e) => VTreeEditor.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'headerBackground':
        headerBackground = json['headerBackground'] == null
            ? null
            : VColor.fromJson(json['headerBackground'] as Map<String, dynamic>);
      case 'headerVisible':
        headerVisible = json['headerVisible'] as bool?;
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'linesVisible':
        linesVisible = json['linesVisible'] as bool?;
      case 'selection':
        selection = (json['selection'] as List<dynamic>?)
            ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(columns, adopt);
    VWidget.adoptEach(editors, adopt);
    VWidget.adoptEach(items, adopt);
    VWidget.adoptEach(selection, adopt);
  }

  factory VTree.fromJson(Map<String, dynamic> json) =>
      _$VTreeFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTreeToJson(this);
}
