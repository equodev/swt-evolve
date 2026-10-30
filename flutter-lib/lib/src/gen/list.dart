import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollable.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/list_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ListSwt<V extends VList> extends ScrollableSwt<V> {
  const ListSwt({super.key, required super.value});

  @override
  State createState() => ListImpl<ListSwt<VList>, VList>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

class VList extends VScrollable {
  VList() : this.empty();
  VList.empty() {
    swt = "List";
  }

  List<String>? items;
  List<int>? selection;
  int? topIndex;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VList) {
      items = other.items;
      selection = other.selection;
      topIndex = other.topIndex;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 461: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 462: // selection
        selection = r.ints();
        return true;
      case 1422: // topIndex
        topIndex = r.int32();
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
      if (v_ != null && w.wants(461)) {
        w.key(461);
        w.strings(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(462)) {
        w.key(462);
        w.ints(v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null && w.wants(1422)) {
        w.key(1422);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(461, items);
    visit(462, selection);
    visit(1422, topIndex);
  }
}
