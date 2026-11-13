import 'package:flutter/widgets.dart';
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
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/toolbar_evolve.dart';
import 'widgets.dart';

class ToolBarSwt<V extends VToolBar> extends CompositeSwt<V> {
  const ToolBarSwt({super.key, required super.value});

  @override
  State createState() => ToolBarImpl<ToolBarSwt<VToolBar>, VToolBar>();
}

class VToolBar extends VComposite {
  VToolBar() : this.empty();
  VToolBar.empty() {
    swt = "ToolBar";
  }

  List<VToolItem>? items;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolBar) {
      items = other.items;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 613: // items
        items = r.refs<VToolItem>(items);
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
        w.key(613);
        w.refs(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(613, items);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
