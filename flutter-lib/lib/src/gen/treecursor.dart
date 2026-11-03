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
import '../impl/treecursor_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TreeCursorSwt<V extends VTreeCursor> extends CanvasSwt<V> {
  const TreeCursorSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeCursorImpl<TreeCursorSwt<VTreeCursor>, VTreeCursor>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VTreeCursor extends VCanvas {
  VTreeCursor() : this.empty();
  VTreeCursor.empty() {
    swt = "TreeCursor";
  }
}
