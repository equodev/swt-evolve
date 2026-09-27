import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
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
import '../gen/stylerange.dart';
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

  int? alignment;
  bool? alwaysShowScrollBars;
  bool? blockSelection;
  VRectangle? blockSelectionBounds;
  int? bottomMargin;
  int? caretOffset;
  bool? doubleClickEnabled;
  bool? editable;
  int? horizontalPixel;
  int? indent;
  bool? justify;
  int? leftMargin;
  int? lineSpacing;
  VColor? marginColor;
  int? rightMargin;
  VPoint? selection;
  VColor? selectionBackground;
  VColor? selectionForeground;
  List<int>? selectionRanges;
  List<int>? tabStops;
  int? tabs;
  String? text;
  int? topMargin;
  int? topPixel;
  bool? wordWrap;
  int? wrapIndent;

  VStyledTextRenderer? renderer;
  List<int>? caretOffsets;
  List<VStyleRange?>? styles;
  List<int>? styleIndex;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VStyledText) {
      alignment = other.alignment;
      alwaysShowScrollBars = other.alwaysShowScrollBars;
      blockSelection = other.blockSelection;
      blockSelectionBounds = other.blockSelectionBounds;
      bottomMargin = other.bottomMargin;
      caretOffset = other.caretOffset;
      doubleClickEnabled = other.doubleClickEnabled;
      editable = other.editable;
      horizontalPixel = other.horizontalPixel;
      indent = other.indent;
      justify = other.justify;
      leftMargin = other.leftMargin;
      lineSpacing = other.lineSpacing;
      marginColor = other.marginColor;
      rightMargin = other.rightMargin;
      selection = other.selection;
      selectionBackground = other.selectionBackground;
      selectionForeground = other.selectionForeground;
      selectionRanges = other.selectionRanges;
      tabStops = other.tabStops;
      tabs = other.tabs;
      text = other.text;
      topMargin = other.topMargin;
      topPixel = other.topPixel;
      wordWrap = other.wordWrap;
      wrapIndent = other.wrapIndent;
      renderer = other.renderer;
      caretOffsets = other.caretOffsets;
      styles = other.styles;
      styleIndex = other.styleIndex;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'alignment':
        alignment = (json['alignment'] as num?)?.toInt();
      case 'alwaysShowScrollBars':
        alwaysShowScrollBars = json['alwaysShowScrollBars'] as bool?;
      case 'blockSelection':
        blockSelection = json['blockSelection'] as bool?;
      case 'blockSelectionBounds':
        blockSelectionBounds = json['blockSelectionBounds'] == null
            ? null
            : VRectangle.fromJson(
                json['blockSelectionBounds'] as Map<String, dynamic>,
              );
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
      case 'indent':
        indent = (json['indent'] as num?)?.toInt();
      case 'justify':
        justify = json['justify'] as bool?;
      case 'leftMargin':
        leftMargin = (json['leftMargin'] as num?)?.toInt();
      case 'lineSpacing':
        lineSpacing = (json['lineSpacing'] as num?)?.toInt();
      case 'marginColor':
        marginColor = json['marginColor'] == null
            ? null
            : VColor.fromJson(json['marginColor'] as Map<String, dynamic>);
      case 'rightMargin':
        rightMargin = (json['rightMargin'] as num?)?.toInt();
      case 'selection':
        selection = json['selection'] == null
            ? null
            : VPoint.fromJson(json['selection'] as Map<String, dynamic>);
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
      case 'selectionRanges':
        selectionRanges = (json['selectionRanges'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'tabStops':
        tabStops = (json['tabStops'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
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
      case 'wrapIndent':
        wrapIndent = (json['wrapIndent'] as num?)?.toInt();
      case 'renderer':
        renderer = json['renderer'] == null
            ? null
            : VStyledTextRenderer.fromJson(
                json['renderer'] as Map<String, dynamic>,
              );
      case 'caretOffsets':
        caretOffsets = (json['caretOffsets'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      case 'styles':
        styles = (json['styles'] as List<dynamic>?)
            ?.map(
              (e) => e == null
                  ? null
                  : VStyleRange.fromJson(e as Map<String, dynamic>),
            )
            .toList();
      case 'styleIndex':
        styleIndex = (json['styleIndex'] as List<dynamic>?)
            ?.map((e) => (e as num).toInt())
            .toList();
      default:
        super.readProperty(key, json);
    }
  }

  factory VStyledText.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VStyledTextToJson(this);
}
