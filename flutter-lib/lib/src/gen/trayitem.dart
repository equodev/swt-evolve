import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/trayitem_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TrayItemSwt<V extends VTrayItem> extends ItemSwt<V> {
  const TrayItemSwt({super.key, required super.value});

  @override
  State createState() => TrayItemImpl<TrayItemSwt<VTrayItem>, VTrayItem>();

  void sendMenuDetectMenuDetect(V val, VEvent? payload) {
    sendEvent(val, 70, payload); // MenuDetect/MenuDetect
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VTrayItem extends VItem {
  VTrayItem() : this.empty();
  VTrayItem.empty() {
    swt = "TrayItem";
  }
}
