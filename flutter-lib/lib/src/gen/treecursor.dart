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
import '../impl/treecursor_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'treecursor.g.dart';

class TreeCursorSwt<V extends VTreeCursor> extends CanvasSwt<V> {
  const TreeCursorSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeCursorImpl<TreeCursorSwt<VTreeCursor>, VTreeCursor>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VTreeCursor extends VCanvas {
  VTreeCursor() : this.empty();
  VTreeCursor.empty() {
    swt = "TreeCursor";
  }

  factory VTreeCursor.fromJson(Map<String, dynamic> json) =>
      _$VTreeCursorFromJson(json);
  Map<String, dynamic> toJson() => _$VTreeCursorToJson(this);
}
