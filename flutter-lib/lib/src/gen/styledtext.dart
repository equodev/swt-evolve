import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../impl/styledtext_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'styledtext.g.dart';

class StyledTextSwt<V extends VStyledText> extends CanvasSwt<V> {
  const StyledTextSwt({super.key, required super.value});

  @override
  State createState() =>
      StyledTextImpl<StyledTextSwt<VStyledText>, VStyledText>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }
}

@JsonSerializable()
class VStyledText extends VCanvas {
  VStyledText() : this.empty();
  VStyledText.empty() {
    swt = "StyledText";
  }

  int? alignment;
  bool? alwaysShowScrollBars;
  bool? bidiColoring;
  bool? blockSelection;
  VRectangle? blockSelectionBounds;
  int? bottomMargin;
  int? caretOffset;
  int? columnX;
  bool? doubleClickEnabled;
  bool? editable;
  int? horizontalIndex;
  int? horizontalPixel;
  int? indent;
  bool? justify;
  int? leftMargin;
  int? lineSpacing;
  VColor? marginColor;
  bool? mouseNavigatorEnabled;
  List<int>? ranges;
  int? rightMargin;
  VPoint? selection;
  VColor? selectionBackground;
  VColor? selectionForeground;
  VPoint? selectionRange;
  List<int>? selectionRanges;
  int? styleRange;
  List<int>? tabStops;
  int? tabs;
  String? text;
  int? textLimit;
  int? topIndex;
  int? topMargin;
  int? topPixel;
  int? verticalScrollOffset;
  bool? wordWrap;
  int? wrapIndent;

  factory VStyledText.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextFromJson(json);
  Map<String, dynamic> toJson() => _$VStyledTextToJson(this);
}
