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
    sendEvent(val, 20, payload); // Control/Move
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, 21, payload); // Control/Resize
  }

  void sendKeyKeyDown(V val, VEvent? payload) {
    sendEvent(val, 61, payload); // Key/KeyDown
  }

  void sendKeyKeyUp(V val, VEvent? payload) {
    sendEvent(val, 62, payload); // Key/KeyUp
  }
}

class VTracker extends VWidget {
  VTracker() : this.empty();
  VTracker.empty() {
    swt = "Tracker";
  }
}
