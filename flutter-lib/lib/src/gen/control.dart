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
    sendEvent(val, "Control/Move", payload);
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendDragDetectDragDetect(V val, VEvent? payload) {
    sendEvent(val, "DragDetect/DragDetect", payload);
  }

  void sendFocusFocusIn(V val, VEvent? payload) {
    sendEvent(val, "Focus/FocusIn", payload);
  }

  void sendFocusFocusOut(V val, VEvent? payload) {
    sendEvent(val, "Focus/FocusOut", payload);
  }

  void sendGestureGesture(V val, VEvent? payload) {
    sendEvent(val, "Gesture/Gesture", payload);
  }

  void sendHelpHelp(V val, VEvent? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyDown", payload);
  }

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyUp", payload);
  }

  void sendMenuDetectMenuDetect(V val, VEvent? payload) {
    sendEvent(val, "MenuDetect/MenuDetect", payload);
  }

  void sendMouseMouseDoubleClick(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseDoubleClick", payload);
  }

  void sendMouseMouseDown(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseDown", payload);
  }

  void sendMouseMouseUp(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseUp", payload);
  }

  void sendMouseMoveMouseMove(V val, VEvent? payload) {
    sendEvent(val, "MouseMove/MouseMove", payload);
  }

  void sendMouseTrackMouseEnter(V val, VEvent? payload) {
    sendEvent(val, "MouseTrack/MouseEnter", payload);
  }

  void sendMouseTrackMouseExit(V val, VEvent? payload) {
    sendEvent(val, "MouseTrack/MouseExit", payload);
  }

  void sendMouseTrackMouseHover(V val, VEvent? payload) {
    sendEvent(val, "MouseTrack/MouseHover", payload);
  }

  void sendMouseWheelMouseWheel(V val, VEvent? payload) {
    sendEvent(val, "MouseWheel/MouseWheel", payload);
  }

  void sendPaintPaint(V val, VEvent? payload) {
    sendEvent(val, "Paint/Paint", payload);
  }

  void sendTouchTouch(V val, VEvent? payload) {
    sendEvent(val, "Touch/Touch", payload);
  }

  void sendTraverseTraverse(V val, VEvent? payload) {
    sendEvent(val, "Traverse/Traverse", payload);
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
  int? dropTargetId;
  bool? enabled;
  VFont? font;
  VColor? foreground;
  bool? hasOwnBackground;
  bool? inheritsBackground;
  VMenu? menu;
  VRegion? region;
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
      dropTargetId = other.dropTargetId;
      enabled = other.enabled;
      font = other.font;
      foreground = other.foreground;
      hasOwnBackground = other.hasOwnBackground;
      inheritsBackground = other.inheritsBackground;
      menu = other.menu;
      region = other.region;
      toolTipText = other.toolTipText;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 290: // background
        background = r.color();
        return true;
      case 291: // backgroundImage
        backgroundImage = r.image();
        return true;
      case 292: // bounds
        bounds = r.rect();
        return true;
      case 293: // cursor
        cursor = r.object(VCursor.new);
        return true;
      case 294: // dragDetect
        dragDetect = r.boolean();
        return true;
      case 295: // dragSource
        dragSource = r.boolean();
        return true;
      case 296: // dropTargetId
        dropTargetId = r.longBoxed();
        return true;
      case 297: // enabled
        enabled = r.boolBoxed();
        return true;
      case 298: // font
        font = r.object(VFont.new);
        return true;
      case 299: // foreground
        foreground = r.color();
        return true;
      case 300: // hasOwnBackground
        hasOwnBackground = r.boolean();
        return true;
      case 301: // inheritsBackground
        inheritsBackground = r.boolean();
        return true;
      case 302: // menu
        menu = r.ref<VMenu>();
        return true;
      case 569: // region
        region = r.object(VRegion.new);
        return true;
      case 303: // toolTipText
        toolTipText = r.string();
        return true;
      case 304: // visible
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
      if (v_ != null && w.wants(290)) {
        w.key(290);
        w.colorValue(v_);
      }
    }
    {
      final v_ = backgroundImage;
      if (v_ != null && w.wants(291)) {
        w.key(291);
        w.ref(v_.id);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null && w.wants(292)) {
        w.key(292);
        w.rectValue(v_);
      }
    }
    {
      final v_ = cursor;
      if (v_ != null && w.wants(293)) {
        w.key(293);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = dragDetect;
      if (v_ != null && w.wants(294)) {
        w.key(294);
        w.boolean(v_);
      }
    }
    {
      final v_ = dragSource;
      if (v_ != null && w.wants(295)) {
        w.key(295);
        w.boolean(v_);
      }
    }
    {
      final v_ = dropTargetId;
      if (v_ != null && w.wants(296)) {
        w.key(296);
        w.longBoxed(v_);
      }
    }
    {
      final v_ = enabled;
      if (v_ != null && w.wants(297)) {
        w.key(297);
        w.boolBoxed(v_);
      }
    }
    {
      final v_ = font;
      if (v_ != null && w.wants(298)) {
        w.key(298);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = foreground;
      if (v_ != null && w.wants(299)) {
        w.key(299);
        w.colorValue(v_);
      }
    }
    {
      final v_ = hasOwnBackground;
      if (v_ != null && w.wants(300)) {
        w.key(300);
        w.boolean(v_);
      }
    }
    {
      final v_ = inheritsBackground;
      if (v_ != null && w.wants(301)) {
        w.key(301);
        w.boolean(v_);
      }
    }
    {
      final v_ = menu;
      if (v_ != null && w.wants(302)) {
        w.key(302);
        w.ref(v_.id);
      }
    }
    {
      final v_ = region;
      if (v_ != null && w.wants(569)) {
        w.key(569);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null && w.wants(303)) {
        w.key(303);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null && w.wants(304)) {
        w.key(304);
        w.boolBoxed(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(290, background);
    visit(291, backgroundImage);
    visit(292, bounds);
    visit(293, cursor);
    visit(294, dragDetect);
    visit(295, dragSource);
    visit(296, dropTargetId);
    visit(297, enabled);
    visit(298, font);
    visit(299, foreground);
    visit(300, hasOwnBackground);
    visit(301, inheritsBackground);
    visit(302, menu);
    visit(569, region);
    visit(303, toolTipText);
    visit(304, visible);
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
