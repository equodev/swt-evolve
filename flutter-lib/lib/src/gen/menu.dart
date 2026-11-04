import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/menuitem.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/menu_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class MenuSwt<V extends VMenu> extends WidgetSwt<V> {
  const MenuSwt({super.key, required super.value});

  @override
  State createState() => MenuImpl<MenuSwt<VMenu>, VMenu>();

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, 56, payload); // Help/Help
  }

  void sendMenuHide(V val, VEvent? payload) {
    sendEvent(val, 68, payload); // Menu/Hide
  }

  void sendMenuShow(V val, VEvent? payload) {
    sendEvent(val, 69, payload); // Menu/Show
  }
}

class VMenu extends VWidget {
  VMenu() : this.empty();
  VMenu.empty() {
    swt = "Menu";
  }

  bool? enabled;
  List<VMenuItem>? items;
  VPoint? location;
  int? orientation;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VMenu) {
      enabled = other.enabled;
      items = other.items;
      location = other.location;
      orientation = other.orientation;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 369: // enabled
        enabled = r.boolean();
        return true;
      case 370: // items
        items = r.refs<VMenuItem>();
        return true;
      case 371: // location
        location = r.point();
        return true;
      case 372: // orientation
        orientation = r.int32();
        return true;
      case 373: // visible
        visible = r.boolean();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = enabled;
      if (v_ != null) {
        w.keyBoolean(369, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(370);
        w.refs(v_);
      }
    }
    {
      final v_ = location;
      if (v_ != null) {
        w.key(371);
        w.pointValue(v_);
      }
    }
    {
      final v_ = orientation;
      if (v_ != null) {
        w.keyInt32(372, v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(373, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(369, enabled);
    visit(370, items);
    visit(371, location);
    visit(372, orientation);
    visit(373, visible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
