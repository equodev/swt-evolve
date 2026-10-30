import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
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
import '../gen/tabitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tabfolder_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TabFolderSwt<V extends VTabFolder> extends CompositeSwt<V> {
  const TabFolderSwt({super.key, required super.value});

  @override
  State createState() => TabFolderImpl<TabFolderSwt<VTabFolder>, VTabFolder>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }
}

class VTabFolder extends VComposite {
  VTabFolder() : this.empty();
  VTabFolder.empty() {
    swt = "TabFolder";
  }

  List<VTabItem>? items;
  List<VTabItem>? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTabFolder) {
      items = other.items;
      selection = other.selection;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 542: // items
        items = r.refs<VTabItem>();
        return true;
      case 543: // selection
        selection = r.refs<VTabItem>();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = items;
      if (v_ != null) {
        w.key(542);
        w.refs(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(543);
        w.refs(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(542, items);
    visit(543, selection);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
    VWidget.adoptEach(selection, adopt);
  }
}
