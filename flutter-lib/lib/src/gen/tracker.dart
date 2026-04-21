import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/cursor.dart';
import '../gen/rectangle.dart';
import '../gen/widget.dart';
import '../impl/tracker_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tracker.g.dart';

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

@JsonSerializable()
class VTracker extends VWidget {
  VTracker() : this.empty();
  VTracker.empty() {
    swt = "Tracker";
  }

  VCursor? cursor;
  List<VRectangle>? rectangles;
  bool? stippled;

  factory VTracker.fromJson(Map<String, dynamic> json) =>
      _$VTrackerFromJson(json);
  Map<String, dynamic> toJson() => _$VTrackerToJson(this);
}
