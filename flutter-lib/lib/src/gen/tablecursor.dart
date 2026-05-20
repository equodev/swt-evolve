import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/ime.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../impl/tablecursor_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'tablecursor.g.dart';

class TableCursorSwt<V extends VTableCursor> extends CanvasSwt<V> {
  const TableCursorSwt({super.key, required super.value});

  @override
  State createState() =>
      TableCursorImpl<TableCursorSwt<VTableCursor>, VTableCursor>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VTableCursor extends VCanvas {
  VTableCursor() : this.empty();
  VTableCursor.empty() {
    swt = "TableCursor";
  }

  factory VTableCursor.fromJson(Map<String, dynamic> json) =>
      _$VTableCursorFromJson(json);
  Map<String, dynamic> toJson() => _$VTableCursorToJson(this);
}
