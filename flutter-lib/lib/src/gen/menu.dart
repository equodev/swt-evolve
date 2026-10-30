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
    sendEvent(val, "Help/Help", payload);
  }

  void sendMenuHide(V val, VEvent? payload) {
    sendEvent(val, "Menu/Hide", payload);
  }

  void sendMenuShow(V val, VEvent? payload) {
    sendEvent(val, "Menu/Show", payload);
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
      case 219: // enabled
        enabled = r.boolean();
        return true;
      case 220: // items
        items = r.refs<VMenuItem>();
        return true;
      case 221: // location
        location = r.point();
        return true;
      case 222: // orientation
        orientation = r.int32();
        return true;
      case 223: // visible
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
      if (v_ != null && w.wants(219)) {
        w.key(219);
        w.boolean(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(220)) {
        w.key(220);
        w.refs(v_);
      }
    }
    {
      final v_ = location;
      if (v_ != null && w.wants(221)) {
        w.key(221);
        w.pointValue(v_);
      }
    }
    {
      final v_ = orientation;
      if (v_ != null && w.wants(222)) {
        w.key(222);
        w.int32(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null && w.wants(223)) {
        w.key(223);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(219, enabled);
    visit(220, items);
    visit(221, location);
    visit(222, orientation);
    visit(223, visible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
