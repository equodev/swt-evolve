import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/expanditem_evolve.dart';
import 'widgets.dart';

class ExpandItemSwt<V extends VExpandItem> extends ItemSwt<V> {
  const ExpandItemSwt({super.key, required super.value});

  @override
  State createState() =>
      ExpandItemImpl<ExpandItemSwt<VExpandItem>, VExpandItem>();
}

class VExpandItem extends VItem {
  VExpandItem() : this.empty();
  VExpandItem.empty() {
    swt = "ExpandItem";
  }

  VControl? control;
  bool? expanded;
  int? height;

  int? headerHeight;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VExpandItem) {
      control = other.control;
      expanded = other.expanded;
      height = other.height;
      headerHeight = other.headerHeight;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 142: // control
        control = r.ref<VControl>();
        return true;
      case 143: // expanded
        expanded = r.boolean();
        return true;
      case 144: // height
        height = r.int32();
        return true;
      case 1416: // headerHeight
        headerHeight = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = control;
      if (v_ != null && w.wants(142)) {
        w.key(142);
        w.ref(v_.id);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null && w.wants(143)) {
        w.key(143);
        w.boolean(v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null && w.wants(144)) {
        w.key(144);
        w.int32(v_);
      }
    }
    {
      final v_ = headerHeight;
      if (v_ != null && w.wants(1416)) {
        w.key(1416);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(142, control);
    visit(143, expanded);
    visit(144, height);
    visit(1416, headerHeight);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }
}
