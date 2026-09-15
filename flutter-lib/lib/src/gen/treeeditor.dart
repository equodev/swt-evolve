import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
import '../impl/treeeditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

part 'treeeditor.g.dart';

class TreeEditorSwt<V extends VTreeEditor> extends WidgetSwt<V> {
  const TreeEditorSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeEditorImpl<TreeEditorSwt<VTreeEditor>, VTreeEditor>();
}

abstract class TreeEditorState<T extends TreeEditorSwt, V extends VTreeEditor>
    extends WidgetSwtState<T, V> {}

@JsonSerializable()
class VTreeEditor extends VWidget {
  VTreeEditor() : this.empty();
  VTreeEditor.empty() {
    swt = "TreeEditor";
  }

  int? column;
  VControl? editor;
  VTreeItem? item;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTreeEditor) {
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
            : VTreeItem.fromJson(json['item'] as Map<String, dynamic>);
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

  factory VTreeEditor.fromJson(Map<String, dynamic> json) =>
      _$VTreeEditorFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTreeEditorToJson(this);
}
