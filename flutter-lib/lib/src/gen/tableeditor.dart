import 'dart:convert';
import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/controleditor.dart';
import '../gen/tableitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tableeditor_evolve.dart';
import 'widget.dart';
import 'widgets.dart';

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

class VTableEditor extends VControlEditor {
  VTableEditor() : this.empty();
  VTableEditor.empty() {
    swt = "TableEditor";
  }

  int? column;
  VTableItem? item;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTableEditor) {
      column = other.column;
      item = other.item;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 173: // column
        column = r.int32();
        return true;
      case 174: // item
        item = r.ref<VTableItem>();
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
      if (v_ != null && w.wants(173)) {
        w.key(173);
        w.int32(v_);
      }
    }
    {
      final v_ = item;
      if (v_ != null && w.wants(174)) {
        w.key(174);
        w.ref(v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(173, column);
    visit(174, item);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    item = VWidget.adoptOne(item, adopt);
  }
}
