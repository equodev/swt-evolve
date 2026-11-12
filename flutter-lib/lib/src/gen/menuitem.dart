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
    sendEvent(val, 2, payload); // Arm/Arm
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, 56, payload); // Help/Help
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
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
      case 378: // accelerator
        accelerator = r.int32();
        return true;
      case 379: // enabled
        enabled = r.boolean();
        return true;
      case 380: // menu
        menu = r.ref<VMenu>();
        return true;
      case 381: // selection
        selection = r.boolean();
        return true;
      case 382: // toolTipText
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
      if (v_ != null) {
        w.keyInt32(378, v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null) {
        w.keyBoolean(379, v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null) {
        w.keyRef(380, v_.id);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyBoolean(381, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(382);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(378, accelerator);
    visit(379, enabled);
    visit(380, menu);
    visit(381, selection);
    visit(382, toolTipText);
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
