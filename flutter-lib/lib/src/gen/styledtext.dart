import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/point.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/styledtextrenderer.dart';
import '../gen/widget.dart';
import '../impl/styledtext_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'styledtext.g.dart';

class StyledTextSwt<V extends VStyledText> extends CanvasSwt<V> {
  const StyledTextSwt({super.key, required super.value});

  @override
  State createState() =>
      StyledTextImpl<StyledTextSwt<VStyledText>, VStyledText>();

  void sendBidiSegmentlineGetSegments(V val, VEvent? payload) {
    sendEvent(val, "BidiSegment/lineGetSegments", payload);
  }

  void sendCaretcaretMoved(V val, VEvent? payload) {
    sendEvent(val, "Caret/caretMoved", payload);
  }

  void sendExtendedModifymodifyText(V val, VEvent? payload) {
    sendEvent(val, "ExtendedModify/modifyText", payload);
  }

  void sendLineBackgroundlineGetBackground(V val, VEvent? payload) {
    sendEvent(val, "LineBackground/lineGetBackground", payload);
  }

  void sendLineStylelineGetStyle(V val, VEvent? payload) {
    sendEvent(val, "LineStyle/lineGetStyle", payload);
  }

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendPaintObjectpaintObject(V val, VEvent? payload) {
    sendEvent(val, "PaintObject/paintObject", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, "Verify/Verify", payload);
  }

  void sendVerifyKeyverifyKey(V val, VEvent? payload) {
    sendEvent(val, "VerifyKey/verifyKey", payload);
  }

  void sendWordMovementgetNextOffset(V val, VEvent? payload) {
    sendEvent(val, "WordMovement/getNextOffset", payload);
  }

  void sendWordMovementgetPreviousOffset(V val, VEvent? payload) {
    sendEvent(val, "WordMovement/getPreviousOffset", payload);
  }
}

@JsonSerializable()
class VStyledText extends VCanvas {
  VStyledText() : this.empty();
  VStyledText.empty() {
    swt = "StyledText";
  }

  bool? alwaysShowScrollBars;
  int? bottomMargin;
  int? caretOffset;
  bool? doubleClickEnabled;
  bool? editable;
  int? horizontalPixel;
  int? leftMargin;
  VColor? marginColor;
  int? rightMargin;
  VColor? selectionBackground;
  VColor? selectionForeground;
  VPoint? selectionRange;
  int? tabs;
  String? text;
  int? topMargin;
  int? topPixel;
  bool? wordWrap;

  VStyledTextRenderer? renderer;
  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VStyledText) {
      alwaysShowScrollBars = other.alwaysShowScrollBars;
      bottomMargin = other.bottomMargin;
      caretOffset = other.caretOffset;
      doubleClickEnabled = other.doubleClickEnabled;
      editable = other.editable;
      horizontalPixel = other.horizontalPixel;
      leftMargin = other.leftMargin;
      marginColor = other.marginColor;
      rightMargin = other.rightMargin;
      selectionBackground = other.selectionBackground;
      selectionForeground = other.selectionForeground;
      selectionRange = other.selectionRange;
      tabs = other.tabs;
      text = other.text;
      topMargin = other.topMargin;
      topPixel = other.topPixel;
      wordWrap = other.wordWrap;
      renderer = other.renderer;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alwaysShowScrollBars':
        alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?;
      case 'bottomMargin':
        bottomMargin = (json['bottomMargin'] as num?)?.toInt();
      case 'caretOffset':
        caretOffset = (json['caretOffset'] as num?)?.toInt();
      case 'doubleClickEnabled':
        doubleClickEnabled = json['doubleClickEnabled'] as bool?;
      case 'editable':
        editable = json['editable'] as bool?;
      case 'horizontalPixel':
        horizontalPixel = (json['horizontalPixel'] as num?)?.toInt();
      case 'leftMargin':
        leftMargin = (json['leftMargin'] as num?)?.toInt();
      case 'marginColor':
        marginColor = json['marginColor'] == null
            ? null
            : VColor.fromJson(json['marginColor'] as Map<String, dynamic>);
      case 'rightMargin':
        rightMargin = (json['rightMargin'] as num?)?.toInt();
      case 'selectionBackground':
        selectionBackground = json['selectionBackground'] == null
            ? null
            : VColor.fromJson(
                json['selectionBackground'] as Map<String, dynamic>,
              );
      case 'selectionForeground':
        selectionForeground = json['selectionForeground'] == null
            ? null
            : VColor.fromJson(
                json['selectionForeground'] as Map<String, dynamic>,
              );
      case 'selectionRange':
        selectionRange = json['selectionRange'] == null
            ? null
            : VPoint.fromJson(json['selectionRange'] as Map<String, dynamic>);
      case 'tabs':
        tabs = (json['tabs'] as num?)?.toInt();
      case 'text':
        text = json['text'] as String?;
      case 'topMargin':
        topMargin = (json['topMargin'] as num?)?.toInt();
      case 'topPixel':
        topPixel = (json['topPixel'] as num?)?.toInt();
      case 'wordWrap':
        wordWrap = json['wordWrap'] as bool?;
      case 'renderer':
        renderer = json['renderer'] == null
            ? null
            : VStyledTextRenderer.fromJson(
                json['renderer'] as Map<String, dynamic>,
              );
      default:
        super.readProperty(key, json);
    }
  }

  factory VStyledText.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VStyledTextToJson(this);
}
