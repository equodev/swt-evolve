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
    sendEvent(val, "Modify/Modify", payload);
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
      case 501: // alignment
        alignment = r.int32();
        return true;
      case 502: // itemTooltips
        itemTooltips = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 503: // items
        items = r.strings()?.map((e) => e ?? '').toList();
        return true;
      case 504: // listVisible
        listVisible = r.boolean();
        return true;
      case 505: // selection
        selection = r.point();
        return true;
      case 506: // text
        text = r.string();
        return true;
      case 507: // textLimit
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
      if (v_ != null && w.wants(501)) {
        w.key(501);
        w.int32(v_);
      }
    }
    {
      final v_ = itemTooltips;
      if (v_ != null && w.wants(502)) {
        w.key(502);
        w.strings(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(503)) {
        w.key(503);
        w.strings(v_);
      }
    }
    {
      final v_ = listVisible;
      if (v_ != null && w.wants(504)) {
        w.key(504);
        w.boolean(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(505)) {
        w.key(505);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(506)) {
        w.key(506);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null && w.wants(507)) {
        w.key(507);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(501, alignment);
    visit(502, itemTooltips);
    visit(503, items);
    visit(504, listVisible);
    visit(505, selection);
    visit(506, text);
    visit(507, textLimit);
  }

  @override
  String? get semanticsText => text;
}
