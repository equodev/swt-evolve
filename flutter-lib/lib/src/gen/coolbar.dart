import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/coolitem.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/coolbar_evolve.dart';
import 'widgets.dart';

class CoolBarSwt<V extends VCoolBar> extends CompositeSwt<V> {
  const CoolBarSwt({super.key, required super.value});

  @override
  State createState() => CoolBarImpl<CoolBarSwt<VCoolBar>, VCoolBar>();
}

class VCoolBar extends VComposite {
  VCoolBar() : this.empty();
  VCoolBar.empty() {
    swt = "CoolBar";
  }

  List<int>? itemOrder;
  List<VPoint>? itemSizes;
  List<VCoolItem>? items;
  List<int>? wrapIndices;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCoolBar) {
      itemOrder = other.itemOrder;
      itemSizes = other.itemSizes;
      items = other.items;
      wrapIndices = other.wrapIndices;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 147: // itemOrder
        itemOrder = r.ints();
        return true;
      case 148: // itemSizes
        itemSizes = r.list((r) => r.point())?.whereType<VPoint>().toList();
        return true;
      case 149: // items
        items = r.refs<VCoolItem>();
        return true;
      case 150: // wrapIndices
        wrapIndices = r.ints();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = itemOrder;
      if (v_ != null) {
        w.key(147);
        w.ints(v_);
      }
    }
    {
      final v_ = itemSizes;
      if (v_ != null) {
        w.key(148);
        w.list(v_, (e) => w.pointValue(e));
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(149);
        w.refs(v_);
      }
    }
    {
      final v_ = wrapIndices;
      if (v_ != null) {
        w.key(150);
        w.ints(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(147, itemOrder);
    visit(148, itemSizes);
    visit(149, items);
    visit(150, wrapIndices);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }
}
