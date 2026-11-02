import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/sash_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class SashSwt<V extends VSash> extends ControlSwt<V> {
  const SashSwt({super.key, required super.value});

  @override
  State createState() => SashImpl<SashSwt<VSash>, VSash>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }
}

class VSash extends VControl {
  VSash() : this.empty();
  VSash.empty() {
    swt = "Sash";
  }
}
