import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
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
import '../impl/text_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'text.g.dart';

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

@JsonSerializable()
class VText extends VScrollable {
  VText() : this.empty();
  VText.empty() {
    swt = "Text";
  }

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
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'caretPosition':
        caretPosition = (json['caretPosition'] as num?)?.toInt();
      case 'echoCharacter':
        echoCharacter = (json['echoCharacter'] as num?)?.toInt();
      case 'editable':
        editable = json['editable'] as bool?;
      case 'message':
        message = json['message'] as String?;
      case 'selection':
        selection = json['selection'] == null
            ? null
            : VPoint.fromJson(json['selection'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      case 'textLimit':
        textLimit = (json['textLimit'] as num?)?.toInt();
      default:
        super.readProperty(key, json);
    }
  }

  factory VText.fromJson(Map<String, dynamic> json) =>
      _$VTextFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VTextToJson(this);
}
