import 'package:json_annotation/json_annotation.dart';
import 'package:swtflutter/src/swt/menu.dart';
import '../swt/widget.dart';
import '../swt/rectangle.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'control.g.dart';

abstract class ControlSwt<V extends ControlValue> extends WidgetSwt<V> {
  const ControlSwt({super.key, required super.value});

  void sendControlResize(V val, Object? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendControlMove(V val, Object? payload) {
    sendEvent(val, "Control/Move", payload);
  }

  void sendDragDetectDragDetect(V val, Object? payload) {
    sendEvent(val, "DragDetect/DragDetect", payload);
  }

  void sendFocusFocusIn(V val, Object? payload) {
    sendEvent(val, "Focus/FocusIn", payload);
  }

  void sendFocusFocusOut(V val, Object? payload) {
    sendEvent(val, "Focus/FocusOut", payload);
  }

  void sendGestureGesture(V val, Object? payload) {
    sendEvent(val, "Gesture/Gesture", payload);
  }

  void sendHelpHelp(V val, Object? payload) {
    sendEvent(val, "Help/Help", payload);
  }

  void sendKeyKeyUp(V val, Object? payload) {
    sendEvent(val, "Key/KeyUp", payload);
  }

  void sendKeyKeyDown(V val, Object? payload) {
    sendEvent(val, "Key/KeyDown", payload);
  }

  void sendMenuDetectMenuDetect(V val, Object? payload) {
    sendEvent(val, "MenuDetect/MenuDetect", payload);
  }

  void sendMouseMouseDown(V val, Object? payload) {
    sendEvent(val, "Mouse/MouseDown", payload);
  }

  void sendMouseMouseUp(V val, Object? payload) {
    sendEvent(val, "Mouse/MouseUp", payload);
  }

  void sendMouseMouseDoubleClick(V val, Object? payload) {
    sendEvent(val, "Mouse/MouseDoubleClick", payload);
  }

  void sendMouseMoveMouseMove(V val, Object? payload) {
    sendEvent(val, "MouseMove/MouseMove", payload);
  }

  void sendMouseTrackMouseEnter(V val, Object? payload) {
    sendEvent(val, "MouseTrack/MouseEnter", payload);
  }

  void sendMouseTrackMouseExit(V val, Object? payload) {
    sendEvent(val, "MouseTrack/MouseExit", payload);
  }

  void sendMouseTrackMouseHover(V val, Object? payload) {
    sendEvent(val, "MouseTrack/MouseHover", payload);
  }

  void sendMouseWheelMouseWheel(V val, Object? payload) {
    sendEvent(val, "MouseWheel/MouseWheel", payload);
  }

  void sendPaintPaint(V val, Object? payload) {
    sendEvent(val, "Paint/Paint", payload);
  }

  void sendTouchTouch(V val, Object? payload) {
    sendEvent(val, "Touch/Touch", payload);
  }

  void sendTraverseTraverse(V val, Object? payload) {
    sendEvent(val, "Traverse/Traverse", payload);
  }
}

@JsonSerializable()
class ControlValue extends WidgetValue {
  ControlValue() : this.empty();
  ControlValue.empty() {
    swt = "Control";
  }

  int? orientation;
  int? textDirection;
  RectangleValue? bounds;
  bool? dragDetect;
  bool? enabled;
  Object? layoutData;
  MenuValue? menu;
  String? toolTipText;
  bool? touchEnabled;
  bool? visible;

  factory ControlValue.fromJson(Map<String, dynamic> json) =>
      _$ControlValueFromJson(json);
  Map<String, dynamic> toJson() => _$ControlValueToJson(this);
}
