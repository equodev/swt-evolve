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
    sendEvent(val, 65, payload); // Modify/Modify
  }

  void sendSegmentSegments(V val, VEvent? payload) {
    sendEvent(val, 85, payload); // Segment/Segments
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
      case 575: // caretPosition
        caretPosition = r.int32();
        return true;
      case 576: // echoCharacter
        echoCharacter = r.int32();
        return true;
      case 578: // editable
        editable = r.boolean();
        return true;
      case 580: // message
        message = r.string();
        return true;
      case 581: // selection
        selection = r.point();
        return true;
      case 582: // text
        text = r.string();
        return true;
      case 583: // textLimit
        textLimit = r.int32();
        return true;
      case 579: // edits
        edits = r.ints();
        return true;
      case 577: // editTexts
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
        w.keyInt32(575, v_);
      }
    }
    {
      final v_ = echoCharacter;
      if (v_ != null) {
        w.keyInt32(576, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(578, v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null) {
        w.key(580);
        w.string(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(581);
        w.pointValue(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(582);
        w.string(v_);
      }
    }
    {
      final v_ = textLimit;
      if (v_ != null) {
        w.keyInt32(583, v_);
      }
    }
    {
      final v_ = edits;
      if (v_ != null) {
        w.key(579);
        w.ints(v_);
      }
    }
    {
      final v_ = editTexts;
      if (v_ != null) {
        w.key(577);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(575, caretPosition);
    visit(576, echoCharacter);
    visit(578, editable);
    visit(580, message);
    visit(581, selection);
    visit(582, text);
    visit(583, textLimit);
    visit(579, edits);
    visit(577, editTexts);
  }

  @override
  String? get semanticsText => text;
}
