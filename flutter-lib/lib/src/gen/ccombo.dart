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
      case 18: // alignment
        alignment = r.int32();
        return true;
      case 19: // itemTooltips
        itemTooltips = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 20: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 21: // listVisible
        listVisible = r.boolean();
        return true;
      case 22: // selection
        selection = r.point();
        return true;
      case 23: // text
        text = r.string();
        return true;
      case 24: // textLimit
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
        w.keyInt32(18, v_);
      }
    }
    {
      final v_ = itemTooltips;
      if (v_ != null) {
        w.key(19);
        w.strings(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(20);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null) {
        w.keyBoolean(21, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(22);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(23);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(24, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(18, alignment);
    visit(19, itemTooltips);
    visit(20, items);
    visit(21, listVisible);
    visit(22, selection);
    visit(23, text);
    visit(24, textLimit);
  }

  @override
  String? get semanticsText => text;
}
