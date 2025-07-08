import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/rectangle.dart';
import '../gen/widget.dart';
import 'event.dart';
import 'widgets.dart';

part 'control.g.dart';

abstract class ControlSwt<V extends VControl> extends WidgetSwt<V> {
  const ControlSwt({super.key, required super.value});

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendControlMove(V val, VEvent? payload) {
    sendEvent(val, "Control/Move", payload);
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

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyUp", payload);
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyDown", payload);
  }

  void sendMenuDetectMenuDetect(V val, VEvent? payload) {
    sendEvent(val, "MenuDetect/MenuDetect", payload);
  }

  void sendMouseMouseDown(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseDown", payload);
  }

  void sendMouseMouseUp(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseUp", payload);
  }

  void sendMouseMouseDoubleClick(V val, VEvent? payload) {
    sendEvent(val, "Mouse/MouseDoubleClick", payload);
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

  void sendMouseMoveMouseMove(V val, VEvent? payload) {
    sendEvent(val, "MouseMove/MouseMove", payload);
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
  VRectangle? bounds;
  bool? dragDetect;
  bool? enabled;
  VColor? foreground;
  int? orientation;
  int? textDirection;
  String? toolTipText;
  bool? touchEnabled;
  bool? visible;
  bool? capture;
  bool? redraw;

  factory VControl.fromJson(Map<String, dynamic> json) =>
      _$VControlFromJson(json);
  Map<String, dynamic> toJson() => _$VControlToJson(this);
}
