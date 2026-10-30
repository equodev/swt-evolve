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
    sendEvent(val, 49, payload); // Help/Help
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
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
      case 369: // accelerator
        accelerator = r.int32();
        return true;
      case 370: // enabled
        enabled = r.boolean();
        return true;
      case 371: // menu
        menu = r.ref<VMenu>();
        return true;
      case 372: // selection
        selection = r.boolean();
        return true;
      case 373: // toolTipText
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
        w.keyInt32(369, v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null) {
        w.keyBoolean(370, v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null) {
        w.keyRef(371, v_.id);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyBoolean(372, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(373);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(369, accelerator);
    visit(370, enabled);
    visit(371, menu);
    visit(372, selection);
    visit(373, toolTipText);
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
