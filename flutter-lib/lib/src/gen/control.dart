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
    sendEvent(val, 28, payload); // DragDetect/DragDetect
  }

  void sendFocusFocusIn(V val, VEvent? payload) {
    sendEvent(val, 45, payload); // Focus/FocusIn
  }

  void sendFocusFocusOut(V val, VEvent? payload) {
    sendEvent(val, 46, payload); // Focus/FocusOut
  }

  void sendGestureGesture(V val, VEvent? payload) {
    sendEvent(val, 49, payload); // Gesture/Gesture
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, 56, payload); // Help/Help
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, 61, payload); // Key/KeyDown
  }

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, 62, payload); // Key/KeyUp
  }

  void sendMenuDetectMenuDetect(V val, VEvent? payload) {
    sendEvent(val, 70, payload); // MenuDetect/MenuDetect
  }

  void sendMouseMouseDoubleClick(V val, VEvent? payload) {
    sendEvent(val, 73, payload); // Mouse/MouseDoubleClick
  }

  void sendMouseMouseDown(V val, VEvent? payload) {
    sendEvent(val, 74, payload); // Mouse/MouseDown
  }

  void sendMouseMouseUp(V val, VEvent? payload) {
    sendEvent(val, 75, payload); // Mouse/MouseUp
  }

  void sendMouseMoveMouseMove(V val, VEvent? payload) {
    sendEvent(val, 76, payload); // MouseMove/MouseMove
  }

  void sendMouseTrackMouseEnter(V val, VEvent? payload) {
    sendEvent(val, 77, payload); // MouseTrack/MouseEnter
  }

  void sendMouseTrackMouseExit(V val, VEvent? payload) {
    sendEvent(val, 78, payload); // MouseTrack/MouseExit
  }

  void sendMouseTrackMouseHover(V val, VEvent? payload) {
    sendEvent(val, 79, payload); // MouseTrack/MouseHover
  }

  void sendMouseWheelMouseWheel(V val, VEvent? payload) {
    sendEvent(val, 80, payload); // MouseWheel/MouseWheel
  }

  void sendPaintPaint(V val, VEvent? payload) {
    sendEvent(val, 83, payload); // Paint/Paint
  }

  void sendTouchTouch(V val, VEvent? payload) {
    sendEvent(val, 107, payload); // Touch/Touch
  }

  void sendTraverseTraverse(V val, VEvent? payload) {
    sendEvent(val, 111, payload); // Traverse/Traverse
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
      case 139: // background
        background = r.color();
        return true;
      case 140: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 141: // bounds
        bounds = r.rect();
        return true;
      case 142: // cursor
        cursor = r.object(VCursor.new);
        return true;
      case 143: // dragDetect
        dragDetect = r.boolean();
        return true;
      case 144: // dragSource
        dragSource = r.boolean();
        return true;
      case 145: // dragSourceId
        dragSourceId = r.longBoxed();
        return true;
      case 146: // dropTargetId
        dropTargetId = r.longBoxed();
        return true;
      case 147: // enabled
        enabled = r.boolBoxed();
        return true;
      case 148: // font
        font = r.object(VFont.new);
        return true;
      case 149: // foreground
        foreground = r.color();
        return true;
      case 150: // hasOwnBackground
        hasOwnBackground = r.boolean();
        return true;
      case 151: // inheritsBackground
        inheritsBackground = r.boolean();
        return true;
      case 152: // menu
        menu = r.ref<VMenu>();
        return true;
      case 153: // region
        region = r.object(VRegion.new);
        return true;
      case 154: // tabStop
        tabStop = r.boolean();
        return true;
      case 155: // toolTipText
        toolTipText = r.string();
        return true;
      case 156: // visible
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
        w.key(139);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null) {
        w.keyRef(140, v_.id);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null) {
        w.key(141);
        w.rectValue(v_);
      }
    }
    {
      final v_ = cursor;
      if (v_ != null) {
        w.key(142);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = dragDetect;
      if (v_ != null) {
        w.keyBoolean(143, v_);
      }
    }
    {
      final v_ = dragSource;
      if (v_ != null) {
        w.keyBoolean(144, v_);
      }
    }
    {
      final v_ = dragSourceId;
      if (v_ != null) {
        w.key(145);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = dropTargetId;
      if (v_ != null) {
        w.key(146);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null) {
        w.key(147);
        w.boolBoxed(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null) {
        w.key(148);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null) {
        w.key(149);
        w.colorValue(v_);
      }
    }
    {
      final v_ = hasOwnBackground;
      if (v_ != null) {
        w.keyBoolean(150, v_);
      }
    }
    {
      final v_ = inheritsBackground;
      if (v_ != null) {
        w.keyBoolean(151, v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null) {
        w.keyRef(152, v_.id);
      }
    }
    {
      final v_ = region;
      if (v_ != null) {
        w.key(153);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = tabStop;
      if (v_ != null) {
        w.keyBoolean(154, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(155);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.key(156);
        w.boolBoxed(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(139, background);
    visit(140, backgroundImage);
    visit(141, bounds);
    visit(142, cursor);
    visit(143, dragDetect);
    visit(144, dragSource);
    visit(145, dragSourceId);
    visit(146, dropTargetId);
    visit(147, enabled);
    visit(148, font);
    visit(149, foreground);
    visit(150, hasOwnBackground);
    visit(151, inheritsBackground);
    visit(152, menu);
    visit(153, region);
    visit(154, tabStop);
    visit(155, toolTipText);
    visit(156, visible);
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
