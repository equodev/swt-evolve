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
      case 581: // caretPosition
        caretPosition = r.int32();
        return true;
      case 582: // echoCharacter
        echoCharacter = r.int32();
        return true;
      case 584: // editable
        editable = r.boolean();
        return true;
      case 586: // message
        message = r.string();
        return true;
      case 587: // selection
        selection = r.point();
        return true;
      case 588: // text
        text = r.string();
        return true;
      case 589: // textLimit
        textLimit = r.int32();
        return true;
      case 585: // edits
        edits = r.ints();
        return true;
      case 583: // editTexts
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
      if (v_ != null) {
        w.keyInt32(581, v_);
      }
    }
    {
      final v_ = echoCharacter;
      if (v_ != null) {
        w.keyInt32(582, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(584, v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null) {
        w.key(586);
        w.string(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(587);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(588);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(589, v_);
      }
    }
    {
      final v_ = edits;
      if (v_ != null) {
        w.key(585);
        w.ints(v_);
      }
    }
    {
      final v_ = editTexts;
      if (v_ != null) {
        w.key(583);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(581, caretPosition);
    visit(582, echoCharacter);
    visit(584, editable);
    visit(586, message);
    visit(587, selection);
    visit(588, text);
    visit(589, textLimit);
    visit(585, edits);
    visit(583, editTexts);
  }

  @override
  String? get semanticsText => text;
}
