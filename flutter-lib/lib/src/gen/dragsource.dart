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
    sendEvent(val, "Drag/dragFinished", payload);
  }

  void sendDragdragSetData(V val, VEvent? payload) {
    sendEvent(val, "Drag/dragSetData", payload);
  }

  void sendDragdragStart(V val, VEvent? payload) {
    sendEvent(val, "Drag/dragStart", payload);
  }
}

class VDragSource extends VWidget {
  VDragSource() : this.empty();
  VDragSource.empty() {
    swt = "DragSource";
  }
}
