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
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
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
      case 362: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 363: // selection
        selection = r.ints();
        return true;
      case 364: // topIndex
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
      if (v_ != null) {
        w.key(362);
        w.strings(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(363);
        w.ints(v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(364, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(362, items);
    visit(363, selection);
    visit(364, topIndex);
  }
}
