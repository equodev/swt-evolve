import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/menu.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/menuitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class MenuItemSwt<V extends VMenuItem> extends ItemSwt<V> {
  const MenuItemSwt({super.key, required super.value});

  @override
  State createState() => MenuItemImpl<MenuItemSwt<VMenuItem>, VMenuItem>();

  void sendArmArm(V val, VEvent? payload) {
    sendEvent(val, "Arm/Arm", payload);
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

class VMenuItem extends VItem {
  VMenuItem() : this.empty();
  VMenuItem.empty() {
    swt = "MenuItem";
  }

  int? accelerator;
  bool? enabled;
  VMenu? menu;
  bool? selection;
  String? toolTipText;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VMenuItem) {
      accelerator = other.accelerator;
      enabled = other.enabled;
      menu = other.menu;
      selection = other.selection;
      toolTipText = other.toolTipText;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 56: // accelerator
        accelerator = r.int32();
        return true;
      case 57: // enabled
        enabled = r.boolean();
        return true;
      case 58: // menu
        menu = r.ref<VMenu>();
        return true;
      case 59: // selection
        selection = r.boolean();
        return true;
      case 60: // toolTipText
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
      final v_ = accelerator;
      if (v_ != null && w.wants(56)) {
        w.key(56);
        w.int32(v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null && w.wants(57)) {
        w.key(57);
        w.boolean(v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null && w.wants(58)) {
        w.key(58);
        w.ref(v_.id);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(59)) {
        w.key(59);
        w.boolean(v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null && w.wants(60)) {
        w.key(60);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(56, accelerator);
    visit(57, enabled);
    visit(58, menu);
    visit(59, selection);
    visit(60, toolTipText);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menu = VWidget.adoptOne(menu, adopt);
  }

  @override
  String? get semanticsTooltip => toolTipText;

  @override
  bool? get semanticsEnabled => enabled;
}
