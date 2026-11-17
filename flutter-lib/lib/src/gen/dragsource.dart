import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/dragsource_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class DragSourceSwt<V extends VDragSource> extends WidgetSwt<V> {
  const DragSourceSwt({super.key, required super.value});

  @override
  State createState() =>
      DragSourceImpl<DragSourceSwt<VDragSource>, VDragSource>();

  void sendDragdragFinished(V val, VEvent? payload) {
    sendEvent(val, 23, payload); // Drag/dragFinished
  }

  void sendDragdragSetData(V val, VEvent? payload) {
    sendEvent(val, 24, payload); // Drag/dragSetData
  }

  void sendDragdragStart(V val, VEvent? payload) {
    sendEvent(val, 25, payload); // Drag/dragStart
  }
}

class VDragSource extends VWidget {
  VDragSource() : this.empty();
  VDragSource.empty() {
    swt = "DragSource";
  }
}
