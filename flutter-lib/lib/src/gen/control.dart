import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import 'event.dart';
import 'widgets.dart';

part 'control.g.dart';

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

@JsonSerializable()
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
      menu = other.menu;
      region = other.region;
      toolTipText = other.toolTipText;
      visible = other.visible;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'background':
        background = json['background'] == null
            ? null
            : VColor.fromJson(json['background'] as Map<String, dynamic>);
      case 'backgroundImage':
        backgroundImage = json['backgroundImage'] == null
            ? null
            : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>);
      case 'bounds':
        bounds = json['bounds'] == null
            ? null
            : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>);
      case 'cursor':
        cursor = json['cursor'] == null
            ? null
            : VCursor.fromJson(json['cursor'] as Map<String, dynamic>);
      case 'dragDetect':
        dragDetect = json['dragDetect'] as bool?;
      case 'dragSource':
        dragSource = json['dragSource'] as bool?;
      case 'dropTargetId':
        dropTargetId = (json['dropTargetId'] as num?)?.toInt();
      case 'enabled':
        enabled = json['enabled'] as bool?;
      case 'font':
        font = json['font'] == null
            ? null
            : VFont.fromJson(json['font'] as Map<String, dynamic>);
      case 'foreground':
        foreground = json['foreground'] == null
            ? null
            : VColor.fromJson(json['foreground'] as Map<String, dynamic>);
      case 'hasOwnBackground':
        hasOwnBackground = json['hasOwnBackground'] as bool?;
      case 'menu':
        menu = json['menu'] == null
            ? null
            : VMenu.fromJson(json['menu'] as Map<String, dynamic>);
      case 'region':
        region = json['region'] == null
            ? null
            : VRegion.fromJson(json['region'] as Map<String, dynamic>);
      case 'toolTipText':
        toolTipText = json['toolTipText'] as String?;
      case 'visible':
        visible = json['visible'] as bool?;
      default:
        super.readProperty(key, json);
    }
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    menu = VWidget.adoptOne(menu, adopt);
  }

  factory VControl.fromJson(Map<String, dynamic> json) =>
      mapWidgetValue(json) as VControl;
  Map<String, dynamic> toJson() =>
      throw UnsupportedError("Unsupported toJson in Control");
}
