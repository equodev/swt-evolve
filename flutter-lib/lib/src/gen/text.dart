import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollable.dart';
import '../gen/scrollbar.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/text_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TextSwt<V extends VText> extends ScrollableSwt<V> {
  const TextSwt({super.key, required super.value});

  @override
  State createState() => TextImpl<TextSwt<VText>, VText>();

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

class VText extends VScrollable {
  VText() : this.empty();
  VText.empty() {
    swt = "Text";
  }

  List<int>? edits;
  List<String>? editTexts;
  int? caretPosition;
  int? echoCharacter;
  bool? editable;
  String? message;
  VPoint? selection;
  String? text;
  int? textLimit;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VText) {
      caretPosition = other.caretPosition;
      echoCharacter = other.echoCharacter;
      editable = other.editable;
      message = other.message;
      selection = other.selection;
      text = other.text;
      textLimit = other.textLimit;
      edits = other.edits;
      editTexts = other.editTexts;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 515: // caretPosition
        caretPosition = r.int32();
        return true;
      case 516: // echoCharacter
        echoCharacter = r.int32();
        return true;
      case 517: // editable
        editable = r.boolean();
        return true;
      case 518: // message
        message = r.string();
        return true;
      case 519: // selection
        selection = r.point();
        return true;
      case 520: // text
        text = r.string();
        return true;
      case 521: // textLimit
        textLimit = r.int32();
        return true;
      case 1354: // edits
        edits = r.ints();
        return true;
      case 1430: // editTexts
        editTexts = r.strings()?.map((e) => e ?? '').toList();
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
      if (v_ != null && w.wants(515)) {
        w.key(515);
        w.int32(v_);
      }
    }
    {
      final v_ = echoCharacter;
      if (v_ != null && w.wants(516)) {
        w.key(516);
        w.int32(v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null && w.wants(517)) {
        w.key(517);
        w.boolean(v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null && w.wants(518)) {
        w.key(518);
        w.string(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(519)) {
        w.key(519);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(520)) {
        w.key(520);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null && w.wants(521)) {
        w.key(521);
        w.int32(v_);
      }
    }
    {
      final v_ = edits;
      if (v_ != null && w.wants(1354)) {
        w.key(1354);
        w.ints(v_);
      }
    }
    {
      final v_ = editTexts;
      if (v_ != null && w.wants(1430)) {
        w.key(1430);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(515, caretPosition);
    visit(516, echoCharacter);
    visit(517, editable);
    visit(518, message);
    visit(519, selection);
    visit(520, text);
    visit(521, textLimit);
    visit(1354, edits);
    visit(1430, editTexts);
  }

  @override
  String? get semanticsText => text;
}
