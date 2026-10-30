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
    sendEvent(val, 49, payload); // Help/Help
  }

  void sendMenuHide(V val, VEvent? payload) {
    sendEvent(val, 61, payload); // Menu/Hide
  }

  void sendMenuShow(V val, VEvent? payload) {
    sendEvent(val, 62, payload); // Menu/Show
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
      case 364: // enabled
        enabled = r.boolean();
        return true;
      case 365: // items
        items = r.refs<VMenuItem>();
        return true;
      case 366: // location
        location = r.point();
        return true;
      case 367: // orientation
        orientation = r.int32();
        return true;
      case 368: // visible
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
        w.keyBoolean(364, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(365);
        w.refs(v_);
      }
    }
    {
      final v_ = location;
      if (v_ != null) {
        w.key(366);
        w.pointValue(v_);
      }
    }
    {
      final v_ = orientation;
      if (v_ != null) {
        w.keyInt32(367, v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(368, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(364, enabled);
    visit(365, items);
    visit(366, location);
    visit(367, orientation);
    visit(368, visible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
