import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/tableitem.dart';
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

  factory VTableEditor.fromJson(Map<String, dynamic> json) =>
      _$VTableEditorFromJson(json);
  Map<String, dynamic> toJson() => _$VTableEditorToJson(this);
}
