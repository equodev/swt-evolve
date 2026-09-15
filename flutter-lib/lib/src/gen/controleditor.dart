import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
import '../gen/widget.dart';
import '../impl/controleditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

part 'controleditor.g.dart';

class ControlEditorSwt<V extends VControlEditor> extends WidgetSwt<V> {
  const ControlEditorSwt({super.key, required super.value});

  @override
  State createState() =>
      ControlEditorImpl<ControlEditorSwt<VControlEditor>, VControlEditor>();
}

abstract class ControlEditorState<
  T extends ControlEditorSwt,
  V extends VControlEditor
>
    extends WidgetSwtState<T, V> {}

@JsonSerializable()
class VControlEditor extends VWidget {
  VControlEditor() : this.empty();
  VControlEditor.empty() {
    swt = "ControlEditor";
  }

  VControl? editor;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VControlEditor) {
      editor = other.editor;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'editor':
        editor = json['editor'] == null
            ? null
            : VControl.fromJson(json['editor'] as Map<String, dynamic>);
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    editor = VWidget.adoptOne(editor, adopt);
  }

  factory VControlEditor.fromJson(Map<String, dynamic> json) =>
      _$VControlEditorFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VControlEditorToJson(this);
}
