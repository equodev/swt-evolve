import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tracker_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TrackerSwt<V extends VTracker> extends WidgetSwt<V> {
  const TrackerSwt({super.key, required super.value});

  @override
  State createState() => TrackerImpl<TrackerSwt<VTracker>, VTracker>();

  void sendControlMove(V val, VEvent? payload) {
    sendEvent(val, "Control/Move", payload);
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyDown", payload);
  }

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, "Key/KeyUp", payload);
  }
}

class VTracker extends VWidget {
  VTracker() : this.empty();
  VTracker.empty() {
    swt = "Tracker";
  }
}
