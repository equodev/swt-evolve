import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/toolitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ToolItemSwt<V extends VToolItem> extends ItemSwt<V> {
  const ToolItemSwt({super.key, required super.value});

  @override
  State createState() => ToolItemImpl<ToolItemSwt<VToolItem>, VToolItem>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionOpenMenu(V val, VEvent? payload) {
    sendEvent(val, 94, payload); // Selection/OpenMenu
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VToolItem extends VItem {
  VToolItem() : this.empty();
  VToolItem.empty() {
    swt = "ToolItem";
  }

  VColor? background;
  VControl? control;
  VImage? disabledImage;
  bool? enabled;
  VColor? foreground;
  VImage? hotImage;
  bool? selection;
  String? toolTipText;
  int? width;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolItem) {
      background = other.background;
      control = other.control;
      disabledImage = other.disabledImage;
      enabled = other.enabled;
      foreground = other.foreground;
      hotImage = other.hotImage;
      selection = other.selection;
      toolTipText = other.toolTipText;
      width = other.width;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 602: // background
        background = r.color();
        return true;
      case 603: // control
        control = r.ref<VControl>();
        return true;
      case 604: // disabledImage
        disabledImage = r.image();
        return true;
      case 605: // enabled
        enabled = r.boolean();
        return true;
      case 606: // foreground
        foreground = r.color();
        return true;
      case 607: // hotImage
        hotImage = r.image();
        return true;
      case 608: // selection
        selection = r.boolean();
        return true;
      case 609: // toolTipText
        toolTipText = r.string();
        return true;
      case 610: // width
        width = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = background;
      if (v_ != null) {
        w.key(602);
        w.colorValue(v_);
      }
    }
    {
      final v_ = control;
      if (v_ != null) {
        w.keyRef(603, v_.id);
      }
    }
    {
      final v_ = disabledImage;
      if (v_ != null) {
        w.keyRef(604, v_.id);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null) {
        w.keyBoolean(605, v_);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(606);
        w.colorValue(v_);
      }
    }
    {
      final v_ = hotImage;
      if (v_ != null) {
        w.keyRef(607, v_.id);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyBoolean(608, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(609);
        w.string(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(610, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(602, background);
    visit(603, control);
    visit(604, disabledImage);
    visit(605, enabled);
    visit(606, foreground);
    visit(607, hotImage);
    visit(608, selection);
    visit(609, toolTipText);
    visit(610, width);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    control = VWidget.adoptOne(control, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;

  @override
  bool? get semanticsEnabled => enabled;
}
