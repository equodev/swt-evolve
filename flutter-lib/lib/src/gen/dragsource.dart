import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/control.dart';
import '../gen/widget.dart';
import '../impl/dragsource_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'dragsource.g.dart';

class DragSourceSwt<V extends VDragSource> extends WidgetSwt<V> {
  const DragSourceSwt({super.key, required super.value});

  @override
  State createState() =>
      DragSourceImpl<DragSourceSwt<VDragSource>, VDragSource>();

  void sendDragdragFinished(V val, VEvent? payload) {
    sendEvent(val, "Drag/dragFinished", payload);
  }

  void sendDragdragSetData(V val, VEvent? payload) {
    sendEvent(val, "Drag/dragSetData", payload);
  }

  void sendDragdragStart(V val, VEvent? payload) {
    sendEvent(val, "Drag/dragStart", payload);
  }
}

@JsonSerializable()
class VDragSource extends VWidget {
  VDragSource() : this.empty();
  VDragSource.empty() {
    swt = "DragSource";
  }

  VControl? control;

  factory VDragSource.fromJson(Map<String, dynamic> json) =>
      _$VDragSourceFromJson(json);
  Map<String, dynamic> toJson() => _$VDragSourceToJson(this);
}
