import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/droptarget_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class DropTargetSwt<V extends VDropTarget> extends WidgetSwt<V> {
  const DropTargetSwt({super.key, required super.value});

  @override
  State createState() =>
      DropTargetImpl<DropTargetSwt<VDropTarget>, VDropTarget>();

  void sendDropdragEnter(V val, VEvent? payload) {
    sendEvent(val, "Drop/dragEnter", payload);
  }

  void sendDropdragLeave(V val, VEvent? payload) {
    sendEvent(val, "Drop/dragLeave", payload);
  }

  void sendDropdragOperationChanged(V val, VEvent? payload) {
    sendEvent(val, "Drop/dragOperationChanged", payload);
  }

  void sendDropdragOver(V val, VEvent? payload) {
    sendEvent(val, "Drop/dragOver", payload);
  }

  void sendDropdrop(V val, VEvent? payload) {
    sendEvent(val, "Drop/drop", payload);
  }

  void sendDropdropAccept(V val, VEvent? payload) {
    sendEvent(val, "Drop/dropAccept", payload);
  }
}

class VDropTarget extends VWidget {
  VDropTarget() : this.empty();
  VDropTarget.empty() {
    swt = "DropTarget";
  }
}
