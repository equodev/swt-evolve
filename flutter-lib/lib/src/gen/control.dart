import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import 'event.dart';
import 'widgets.dart';

abstract class ControlSwt<V extends VControl> extends WidgetSwt<V> {
  const ControlSwt({super.key, required super.value});

  void sendControlMove(V val, VEvent? payload) {
    sendEvent(val, 20, payload); // Control/Move
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, 21, payload); // Control/Resize
  }

  void sendDragDetectDragDetect(V val, VEvent? payload) {
    sendEvent(val, 26, payload); // DragDetect/DragDetect
  }

  void sendFocusFocusIn(V val, VEvent? payload) {
    sendEvent(val, 38, payload); // Focus/FocusIn
  }

  void sendFocusFocusOut(V val, VEvent? payload) {
    sendEvent(val, 39, payload); // Focus/FocusOut
  }

  void sendGestureGesture(V val, VEvent? payload) {
    sendEvent(val, 42, payload); // Gesture/Gesture
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, 49, payload); // Help/Help
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, 54, payload); // Key/KeyDown
  }

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, 55, payload); // Key/KeyUp
  }

  void sendMenuDetectMenuDetect(V val, VEvent? payload) {
    sendEvent(val, 63, payload); // MenuDetect/MenuDetect
  }

  void sendMouseMouseDoubleClick(V val, VEvent? payload) {
    sendEvent(val, 66, payload); // Mouse/MouseDoubleClick
  }

  void sendMouseMouseDown(V val, VEvent? payload) {
    sendEvent(val, 67, payload); // Mouse/MouseDown
  }

  void sendMouseMouseUp(V val, VEvent? payload) {
    sendEvent(val, 68, payload); // Mouse/MouseUp
  }

  void sendMouseMoveMouseMove(V val, VEvent? payload) {
    sendEvent(val, 69, payload); // MouseMove/MouseMove
  }

  void sendMouseTrackMouseEnter(V val, VEvent? payload) {
    sendEvent(val, 70, payload); // MouseTrack/MouseEnter
  }

  void sendMouseTrackMouseExit(V val, VEvent? payload) {
    sendEvent(val, 71, payload); // MouseTrack/MouseExit
  }

  void sendMouseTrackMouseHover(V val, VEvent? payload) {
    sendEvent(val, 72, payload); // MouseTrack/MouseHover
  }

  void sendMouseWheelMouseWheel(V val, VEvent? payload) {
    sendEvent(val, 73, payload); // MouseWheel/MouseWheel
  }

  void sendPaintPaint(V val, VEvent? payload) {
    sendEvent(val, 76, payload); // Paint/Paint
  }

  void sendTouchTouch(V val, VEvent? payload) {
    sendEvent(val, 100, payload); // Touch/Touch
  }

  void sendTraverseTraverse(V val, VEvent? payload) {
    sendEvent(val, 104, payload); // Traverse/Traverse
  }
}

class VControl extends VWidget {
  VControl() : this.empty();
  VControl.empty() {
    swt = "Control";
  }

  VColor? background;
  VImage? backgroundImage;
  VRectangle? bounds;
  VCursor? cursor;
  bool? dragDetect;
  bool? dragSource;
  int? dragSourceId;
  int? dropTargetId;
  bool? enabled;
  VFont? font;
  VColor? foreground;
  bool? hasOwnBackground;
  bool? inheritsBackground;
  VMenu? menu;
  VRegion? region;
  bool? tabStop;
  String? toolTipText;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VControl) {
      background = other.background;
      backgroundImage = other.backgroundImage;
      bounds = other.bounds;
      cursor = other.cursor;
      dragDetect = other.dragDetect;
      dragSource = other.dragSource;
      dragSourceId = other.dragSourceId;
      dropTargetId = other.dropTargetId;
      enabled = other.enabled;
      font = other.font;
      foreground = other.foreground;
      hasOwnBackground = other.hasOwnBackground;
      inheritsBackground = other.inheritsBackground;
      menu = other.menu;
      region = other.region;
      tabStop = other.tabStop;
      toolTipText = other.toolTipText;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 1152: // background
        background = r.color();
        return true;
      case 1153: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 1154: // bounds
        bounds = r.rect();
        return true;
      case 1155: // cursor
        cursor = r.object(VCursor.new);
        return true;
      case 1156: // dragDetect
        dragDetect = r.boolean();
        return true;
      case 1157: // dragSource
        dragSource = r.boolean();
        return true;
      case 1158: // dragSourceId
        dragSourceId = r.longBoxed();
        return true;
      case 1159: // dropTargetId
        dropTargetId = r.longBoxed();
        return true;
      case 1160: // enabled
        enabled = r.boolBoxed();
        return true;
      case 1161: // font
        font = r.object(VFont.new);
        return true;
      case 1162: // foreground
        foreground = r.color();
        return true;
      case 1163: // hasOwnBackground
        hasOwnBackground = r.boolean();
        return true;
      case 1164: // inheritsBackground
        inheritsBackground = r.boolean();
        return true;
      case 1165: // menu
        menu = r.ref<VMenu>();
        return true;
      case 1166: // region
        region = r.object(VRegion.new);
        return true;
      case 1167: // tabStop
        tabStop = r.boolean();
        return true;
      case 1168: // toolTipText
        toolTipText = r.string();
        return true;
      case 1169: // visible
        visible = r.boolBoxed();
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
        w.key(1152);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null) {
        w.keyRef(1153, v_.id);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null) {
        w.key(1154);
        w.rectValue(v_);
      }
    }
    {
      final v_ = cursor;
      if (v_ != null) {
        w.key(1155);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = dragDetect;
      if (v_ != null) {
        w.keyBoolean(1156, v_);
      }
    }
    {
      final v_ = dragSource;
      if (v_ != null) {
        w.keyBoolean(1157, v_);
      }
    }
    {
      final v_ = dragSourceId;
      if (v_ != null) {
        w.key(1158);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = dropTargetId;
      if (v_ != null) {
        w.key(1159);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null) {
        w.key(1160);
        w.boolBoxed(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(1161);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(1162);
        w.colorValue(v_);
      }
    }
    {
      final v_ = hasOwnBackground;
      if (v_ != null) {
        w.keyBoolean(1163, v_);
      }
    }
    {
      final v_ = inheritsBackground;
      if (v_ != null) {
        w.keyBoolean(1164, v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null) {
        w.keyRef(1165, v_.id);
      }
    }
    {
      final v_ = region;
      if (v_ != null) {
        w.key(1166);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = tabStop;
      if (v_ != null) {
        w.keyBoolean(1167, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(1168);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.key(1169);
        w.boolBoxed(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1152, background);
    visit(1153, backgroundImage);
    visit(1154, bounds);
    visit(1155, cursor);
    visit(1156, dragDetect);
    visit(1157, dragSource);
    visit(1158, dragSourceId);
    visit(1159, dropTargetId);
    visit(1160, enabled);
    visit(1161, font);
    visit(1162, foreground);
    visit(1163, hasOwnBackground);
    visit(1164, inheritsBackground);
    visit(1165, menu);
    visit(1166, region);
    visit(1167, tabStop);
    visit(1168, toolTipText);
    visit(1169, visible);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menu = VWidget.adoptOne(menu, adopt);
  }
}
