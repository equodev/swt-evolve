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
    sendEvent(val, 28, payload); // Drop/dragEnter
  }

  void sendDropdragLeave(V val, VEvent? payload) {
    sendEvent(val, 29, payload); // Drop/dragLeave
  }

  void sendDropdragOperationChanged(V val, VEvent? payload) {
    sendEvent(val, 30, payload); // Drop/dragOperationChanged
  }

  void sendDropdragOver(V val, VEvent? payload) {
    sendEvent(val, 31, payload); // Drop/dragOver
  }

  void sendDropdrop(V val, VEvent? payload) {
    sendEvent(val, 32, payload); // Drop/drop
  }

  void sendDropdropAccept(V val, VEvent? payload) {
    sendEvent(val, 33, payload); // Drop/dropAccept
  }
}

class VDropTarget extends VWidget {
  VDropTarget() : this.empty();
  VDropTarget.empty() {
    swt = "DropTarget";
  }
}
