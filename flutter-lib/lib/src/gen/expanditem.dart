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
      case 219: // control
        control = r.ref<VControl>();
        return true;
      case 220: // expanded
        expanded = r.boolean();
        return true;
      case 222: // height
        height = r.int32();
        return true;
      case 221: // headerHeight
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
      if (v_ != null) {
        w.keyRef(219, v_.id);
      }
    }
    {
      final v_ = expanded;
      if (v_ != null) {
        w.keyBoolean(220, v_);
      }
    }
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(222, v_);
      }
    }
    {
      final v_ = headerHeight;
      if (v_ != null) {
        w.keyInt32(221, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(219, control);
    visit(220, expanded);
    visit(222, height);
    visit(221, headerHeight);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }
}
