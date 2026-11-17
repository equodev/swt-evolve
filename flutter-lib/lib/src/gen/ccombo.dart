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
    sendEvent(val, 65, payload); // Modify/Modify
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 107, payload); // Verify/Verify
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
      case 384: // alignment
        alignment = r.int32();
        return true;
      case 391: // editable
        editable = r.boolean();
        return true;
      case 385: // itemTooltips
        itemTooltips = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 386: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 387: // listVisible
        listVisible = r.boolean();
        return true;
      case 388: // selection
        selection = r.point();
        return true;
      case 389: // text
        text = r.string();
        return true;
      case 390: // textLimit
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
        w.keyInt32(384, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(391, v_);
      }
    }
    {
      final v_ = itemTooltips;
      if (v_ != null) {
        w.key(385);
        w.strings(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(386);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null) {
        w.keyBoolean(387, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(388);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(389);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(390, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(384, alignment);
    visit(391, editable);
    visit(385, itemTooltips);
    visit(386, items);
    visit(387, listVisible);
    visit(388, selection);
    visit(389, text);
    visit(390, textLimit);
  }

  @override
  String? get semanticsText => text;
}
