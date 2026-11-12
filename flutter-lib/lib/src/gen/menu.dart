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
      case 373: // enabled
        enabled = r.boolean();
        return true;
      case 374: // items
        items = r.refs<VMenuItem>(items);
        return true;
      case 375: // location
        location = r.point();
        return true;
      case 376: // orientation
        orientation = r.int32();
        return true;
      case 377: // visible
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
        w.keyBoolean(373, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(374);
        w.refs(v_);
      }
    }
    {
      final v_ = location;
      if (v_ != null) {
        w.key(375);
        w.pointValue(v_);
      }
    }
    {
      final v_ = orientation;
      if (v_ != null) {
        w.keyInt32(376, v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(377, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(373, enabled);
    visit(374, items);
    visit(375, location);
    visit(376, orientation);
    visit(377, visible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(items, adopt);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
