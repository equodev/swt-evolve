import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
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
import '../impl/combo_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ComboSwt<V extends VCombo> extends CompositeSwt<V> {
  const ComboSwt({super.key, required super.value});

  @override
  State createState() => ComboImpl<ComboSwt<VCombo>, VCombo>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSegmentSegments(V val, VEvent? payload) {
    sendEvent(val, "Segment/Segments", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

class VCombo extends VComposite {
  VCombo() : this.empty();
  VCombo.empty() {
    swt = "Combo";
  }

  int? caretPosition;
  List<String>? items;
  bool? listVisible;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCombo) {
      caretPosition = other.caretPosition;
      items = other.items;
      listVisible = other.listVisible;
      text = other.text;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 1410: // caretPosition
        caretPosition = r.int32();
        return true;
      case 287: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 288: // listVisible
        listVisible = r.boolean();
        return true;
      case 289: // text
        text = r.string();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = caretPosition;
      if (v_ != null && w.wants(1410)) {
        w.key(1410);
        w.int32(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(287)) {
        w.key(287);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null && w.wants(288)) {
        w.key(288);
        w.boolean(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(289)) {
        w.key(289);
        w.string(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1410, caretPosition);
    visit(287, items);
    visit(288, listVisible);
    visit(289, text);
  }

  @override
  String? get semanticsText => text;
}
