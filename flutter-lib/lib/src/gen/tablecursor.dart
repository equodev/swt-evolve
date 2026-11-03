import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tablecursor_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TableCursorSwt<V extends VTableCursor> extends CanvasSwt<V> {
  const TableCursorSwt({super.key, required super.value});

  @override
  State createState() =>
      TableCursorImpl<TableCursorSwt<VTableCursor>, VTableCursor>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VTableCursor extends VCanvas {
  VTableCursor() : this.empty();
  VTableCursor.empty() {
    swt = "TableCursor";
  }
}
