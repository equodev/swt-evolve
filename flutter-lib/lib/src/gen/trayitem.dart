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
    sendEvent(val, "MenuDetect/MenuDetect", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

class VTrayItem extends VItem {
  VTrayItem() : this.empty();
  VTrayItem.empty() {
    swt = "TrayItem";
  }
}
