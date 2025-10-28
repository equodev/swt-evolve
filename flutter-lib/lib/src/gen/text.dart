import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/scrollable.dart';
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

  bool? doubleClickEnabled;
  int? echoChar;
  bool? editable;
  String? message;
  VPoint? selection;
  int? tabs;
  String? text;
  List<int>? textChars;
  int? textLimit;
  int? topIndex;

  factory VText.fromJson(Map<String, dynamic> json) => _$VTextFromJson(json);
  Map<String, dynamic> toJson() => _$VTextToJson(this);
}
