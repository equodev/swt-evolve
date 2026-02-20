import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';
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

  factory VControlEditor.fromJson(Map<String, dynamic> json) =>
      _$VControlEditorFromJson(json);
  Map<String, dynamic> toJson() => _$VControlEditorToJson(this);
}
