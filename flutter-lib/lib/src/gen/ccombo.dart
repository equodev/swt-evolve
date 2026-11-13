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
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/ccombo_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class CComboSwt<V extends VCCombo> extends CompositeSwt<V> {
  const CComboSwt({super.key, required super.value});

  @override
  State createState() => CComboImpl<CComboSwt<VCCombo>, VCCombo>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, 72, payload); // Modify/Modify
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

class VCCombo extends VComposite {
  VCCombo() : this.empty();
  VCCombo.empty() {
    swt = "CCombo";
  }

  int? alignment;
  bool? editable;
  List<String>? itemTooltips;
  List<String>? items;
  bool? listVisible;
  VPoint? selection;
  String? text;
  int? textLimit;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCCombo) {
      alignment = other.alignment;
      editable = other.editable;
      itemTooltips = other.itemTooltips;
      items = other.items;
      listVisible = other.listVisible;
      selection = other.selection;
      text = other.text;
      textLimit = other.textLimit;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 20: // alignment
        alignment = r.int32();
        return true;
      case 21: // editable
        editable = r.boolean();
        return true;
      case 22: // itemTooltips
        itemTooltips = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 23: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 24: // listVisible
        listVisible = r.boolean();
        return true;
      case 25: // selection
        selection = r.point();
        return true;
      case 26: // text
        text = r.string();
        return true;
      case 27: // textLimit
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
      final v_ = alignment;
      if (v_ != null) {
        w.keyInt32(20, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(21, v_);
      }
    }
    {
      final v_ = itemTooltips;
      if (v_ != null) {
        w.key(22);
        w.strings(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(23);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null) {
        w.keyBoolean(24, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(25);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(26);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(27, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(20, alignment);
    visit(21, editable);
    visit(22, itemTooltips);
    visit(23, items);
    visit(24, listVisible);
    visit(25, selection);
    visit(26, text);
    visit(27, textLimit);
  }

  @override
  String? get semanticsText => text;
}
