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
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionOpenMenu(V val, VEvent? payload) {
    sendEvent(val, "Selection/OpenMenu", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
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
      case 445: // background
        background = r.color();
        return true;
      case 446: // control
        control = r.ref<VControl>();
        return true;
      case 447: // disabledImage
        disabledImage = r.image();
        return true;
      case 448: // enabled
        enabled = r.boolean();
        return true;
      case 449: // foreground
        foreground = r.color();
        return true;
      case 450: // hotImage
        hotImage = r.image();
        return true;
      case 451: // selection
        selection = r.boolean();
        return true;
      case 452: // toolTipText
        toolTipText = r.string();
        return true;
      case 453: // width
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
      if (v_ != null && w.wants(445)) {
        w.key(445);
        w.colorValue(v_);
      }
    }
    {
      final v_ = control;
      if (v_ != null && w.wants(446)) {
        w.key(446);
        w.ref(v_.id);
      }
    }
    {
      final v_ = disabledImage;
      if (v_ != null && w.wants(447)) {
        w.key(447);
        w.ref(v_.id);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null && w.wants(448)) {
        w.key(448);
        w.boolean(v_);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(449)) {
        w.key(449);
        w.colorValue(v_);
      }
    }
    {
      final v_ = hotImage;
      if (v_ != null && w.wants(450)) {
        w.key(450);
        w.ref(v_.id);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(451)) {
        w.key(451);
        w.boolean(v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null && w.wants(452)) {
        w.key(452);
        w.string(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null && w.wants(453)) {
        w.key(453);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(445, background);
    visit(446, control);
    visit(447, disabledImage);
    visit(448, enabled);
    visit(449, foreground);
    visit(450, hotImage);
    visit(451, selection);
    visit(452, toolTipText);
    visit(453, width);
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
