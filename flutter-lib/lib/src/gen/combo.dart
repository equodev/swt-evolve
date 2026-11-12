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
    sendEvent(val, 72, payload); // Modify/Modify
  }

  void sendSegmentSegments(V val, VEvent? payload) {
    sendEvent(val, 92, payload); // Segment/Segments
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 114, payload); // Verify/Verify
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
  int? textLimit;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCombo) {
      caretPosition = other.caretPosition;
      items = other.items;
      listVisible = other.listVisible;
      text = other.text;
      textLimit = other.textLimit;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 85: // caretPosition
        caretPosition = r.int32();
        return true;
      case 86: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 87: // listVisible
        listVisible = r.boolean();
        return true;
      case 88: // text
        text = r.string();
        return true;
      case 89: // textLimit
        textLimit = r.int32();
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
      if (v_ != null) {
        w.keyInt32(85, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(86);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null) {
        w.keyBoolean(87, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(88);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(89, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(85, caretPosition);
    visit(86, items);
    visit(87, listVisible);
    visit(88, text);
    visit(89, textLimit);
  }

  @override
  String? get semanticsText => text;
}
