import 'dart:convert';
import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/controleditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

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
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 272: // editor
        editor = r.ref<VControl>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = editor;
      if (v_ != null && w.wants(272)) {
        w.key(272);
        w.ref(v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(272, editor);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    editor = VWidget.adoptOne(editor, adopt);
  }
}
