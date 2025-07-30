import 'package:json_annotation/json_annotation.dart';
import 'package:flutter/widgets.dart';
import '../swt/scrollable.dart';
import '../impl/text_impl.dart';
import '../comm/comm.dart';
import '../widgets.dart';
import 'layout.dart';
import 'menu.dart';
import 'widget.dart';
import 'rectangle.dart';

part 'text.g.dart';

class TextSwt<V extends TextValue> extends ScrollableSwt<V> {
  const TextSwt({super.key, required super.value});

  @override
  State createState() => TextImpl<TextSwt<TextValue>, TextValue>();

  void sendModifyModify(V val, Object? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSegmentSegments(V val, Object? payload) {
    sendEvent(val, "Segment/Segments", payload);
  }

  void sendSelectionSelection(V val, Object? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSelectionDefaultSelection(V val, Object? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendVerifyVerify(V val, Object? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

@JsonSerializable()
class TextValue extends ScrollableValue {
  TextValue() : this.empty();
  TextValue.empty() {
    swt = "Text";
  }

  bool? doubleClickEnabled;
  int? echoChar;
  bool? editable;
  String? message;
  int? orientation;
  int? tabs;
  String? text;
  int? textLimit;
  int? topIndex;

  factory TextValue.fromJson(Map<String, dynamic> json) =>
      _$TextValueFromJson(json);
  Map<String, dynamic> toJson() => _$TextValueToJson(this);
}
