import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/treeitem.dart';
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

  factory VTreeEditor.fromJson(Map<String, dynamic> json) =>
      _$VTreeEditorFromJson(json);
  Map<String, dynamic> toJson() => _$VTreeEditorToJson(this);
}
