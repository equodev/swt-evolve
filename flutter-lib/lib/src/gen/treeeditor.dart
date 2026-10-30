import 'dart:convert';
import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/controleditor.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/treeeditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

class TreeEditorSwt<V extends VTreeEditor> extends WidgetSwt<V> {
  const TreeEditorSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeEditorImpl<TreeEditorSwt<VTreeEditor>, VTreeEditor>();
}

abstract class TreeEditorState<T extends TreeEditorSwt, V extends VTreeEditor>
    extends WidgetSwtState<T, V> {}

class VTreeEditor extends VControlEditor {
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
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 638: // column
        column = r.int32();
        return true;
      case 639: // editor
        editor = r.ref<VControl>();
        return true;
      case 640: // item
        item = r.ref<VTreeItem>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = column;
      if (v_ != null) {
        w.keyInt32(638, v_);
      }
    }
    {
      final v_ = editor;
      if (v_ != null) {
        w.keyRef(639, v_.id);
      }
    }
    {
      final v_ = item;
      if (v_ != null) {
        w.keyRef(640, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(638, column);
    visit(639, editor);
    visit(640, item);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    editor = VWidget.adoptOne(editor, adopt);
    item = VWidget.adoptOne(item, adopt);
  }
}
