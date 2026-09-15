import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/tableitem.dart';
import '../gen/widget.dart';
import '../impl/tableeditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

part 'tableeditor.g.dart';

class TableEditorSwt<V extends VTableEditor> extends WidgetSwt<V> {
  const TableEditorSwt({super.key, required super.value});

  @override
  State createState() =>
      TableEditorImpl<TableEditorSwt<VTableEditor>, VTableEditor>();
}

abstract class TableEditorState<
  T extends TableEditorSwt,
  V extends VTableEditor
>
    extends WidgetSwtState<T, V> {}

@JsonSerializable()
class VTableEditor extends VWidget {
  VTableEditor() : this.empty();
  VTableEditor.empty() {
    swt = "TableEditor";
  }

  int? column;
  VControl? editor;
  VTableItem? item;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTableEditor) {
      column = other.column;
      editor = other.editor;
      item = other.item;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'column':
        column = (json['column'] as num?)?.toInt();
      case 'editor':
        editor = json['editor'] == null
            ? null
            : VControl.fromJson(json['editor'] as Map<String, dynamic>);
      case 'item':
        item = json['item'] == null
            ? null
            : VTableItem.fromJson(json['item'] as Map<String, dynamic>);
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    editor = VWidget.adoptOne(editor, adopt);
    item = VWidget.adoptOne(item, adopt);
  }

  factory VTableEditor.fromJson(Map<String, dynamic> json) =>
      _$VTableEditorFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTableEditorToJson(this);
}
