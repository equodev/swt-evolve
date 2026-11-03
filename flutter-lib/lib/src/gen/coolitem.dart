import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/coolitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class CoolItemSwt<V extends VCoolItem> extends ItemSwt<V> {
  const CoolItemSwt({super.key, required super.value});

  @override
  State createState() => CoolItemImpl<CoolItemSwt<VCoolItem>, VCoolItem>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }
}

class VCoolItem extends VItem {
  VCoolItem() : this.empty();
  VCoolItem.empty() {
    swt = "CoolItem";
  }

  VControl? control;
  VPoint? preferredSize;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCoolItem) {
      control = other.control;
      preferredSize = other.preferredSize;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 152: // control
        control = r.ref<VControl>();
        return true;
      case 153: // preferredSize
        preferredSize = r.point();
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
        w.keyRef(152, v_.id);
      }
    }
    {
      final v_ = preferredSize;
      if (v_ != null) {
        w.key(153);
        w.pointValue(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(152, control);
    visit(153, preferredSize);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }
}
