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
import '../gen/tablecolumn.dart';
import '../gen/tableeditor.dart';
import '../gen/tableitem.dart';
import '../gen/widget.dart';
import '../impl/table_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'table.g.dart';

class TableSwt<V extends VTable> extends CompositeSwt<V> {
  const TableSwt({super.key, required super.value});

  @override
  State createState() => TableImpl<TableSwt<VTable>, VTable>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

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

  int? itemCount;
  List<VTableColumn>? columns;
  List<VTableEditor>? editors;
  VColor? headerBackground;
  VColor? headerForeground;
  bool? headerVisible;
  List<VTableItem>? items;
  bool? linesVisible;
  List<int>? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTable) {
      columns = other.columns;
      editors = other.editors;
      headerBackground = other.headerBackground;
      headerForeground = other.headerForeground;
      headerVisible = other.headerVisible;
      items = other.items;
      linesVisible = other.linesVisible;
      selection = other.selection;
      itemCount = other.itemCount;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'columns':
        columns = (json['columns'] as List<dynamic>?)
            ?.map((e) => VTableColumn.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'editors':
        editors = (json['editors'] as List<dynamic>?)
            ?.map((e) => VTableEditor.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'headerBackground':
        headerBackground = json['headerBackground'] == null
            ? null
            : VColor.fromJson(json['headerBackground'] as Map<String, dynamic>);
      case 'headerForeground':
        headerForeground = json['headerForeground'] == null
            ? null
            : VColor.fromJson(json['headerForeground'] as Map<String, dynamic>);
      case 'headerVisible':
        headerVisible = json['headerVisible'] as bool?;
      case 'items':
        items = (json['items'] as List<dynamic>?)
            ?.map((e) => VTableItem.fromJson(e as Map<String, dynamic>))
            .toList();
      case 'linesVisible':
        linesVisible = json['linesVisible'] as bool?;
      case 'selection':
        selection = (json['selection'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'itemCount':
        itemCount = (json['itemCount'] as num?)?.toInt();
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
  }

  factory VTable.fromJson(Map<String, dynamic> json) =>
      _$VTableFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTableToJson(this);
}
