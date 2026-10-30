import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/expanditem.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/expandbar_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ExpandBarSwt<V extends VExpandBar> extends CompositeSwt<V> {
  const ExpandBarSwt({super.key, required super.value});

  @override
  State createState() => ExpandBarImpl<ExpandBarSwt<VExpandBar>, VExpandBar>();

  void sendExpandCollapse(V val, VEvent? payload) {
    sendEvent(val, "Expand/Collapse", payload);
  }

  void sendExpandExpand(V val, VEvent? payload) {
    sendEvent(val, "Expand/Expand", payload);
  }
}

class VExpandBar extends VComposite {
  VExpandBar() : this.empty();
  VExpandBar.empty() {
    swt = "ExpandBar";
  }

  List<VExpandItem>? items;
  int? spacing;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VExpandBar) {
      items = other.items;
      spacing = other.spacing;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 76: // items
        items = r.refs<VExpandItem>();
        return true;
      case 77: // spacing
        spacing = r.int32();
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
      if (v_ != null && w.wants(76)) {
        w.key(76);
        w.refs(v_);
      }
    }
    {
      final v_ = spacing;
      if (v_ != null && w.wants(77)) {
        w.key(77);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(76, items);
    visit(77, spacing);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
