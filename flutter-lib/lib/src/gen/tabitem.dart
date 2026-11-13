import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tabitem_evolve.dart';
import 'widgets.dart';

class TabItemSwt<V extends VTabItem> extends ItemSwt<V> {
  const TabItemSwt({super.key, required super.value});

  @override
  State createState() => TabItemImpl<TabItemSwt<VTabItem>, VTabItem>();
}

class VTabItem extends VItem {
  VTabItem() : this.empty();
  VTabItem.empty() {
    swt = "TabItem";
  }

  VControl? control;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTabItem) {
      control = other.control;
      toolTipText = other.toolTipText;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 557: // control
        control = r.ref<VControl>();
        return true;
      case 558: // toolTipText
        toolTipText = r.string();
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
        w.keyRef(557, v_.id);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(558);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(557, control);
    visit(558, toolTipText);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
