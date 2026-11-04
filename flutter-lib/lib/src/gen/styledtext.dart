import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
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
import '../gen/widgets.dart';
import '../impl/styledtext_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class StyledTextSwt<V extends VStyledText> extends CanvasSwt<V> {
  const StyledTextSwt({super.key, required super.value});

  @override
  State createState() =>
      StyledTextImpl<StyledTextSwt<VStyledText>, VStyledText>();

  void sendBidiSegmentlineGetSegments(V val, VEvent? payload) {
    sendEvent(val, 4, payload); // BidiSegment/lineGetSegments
  }

  void sendCaretcaretMoved(V val, VEvent? payload) {
    sendEvent(val, 13, payload); // Caret/caretMoved
  }

  void sendExtendedModifymodifyText(V val, VEvent? payload) {
    sendEvent(val, 44, payload); // ExtendedModify/modifyText
  }

  void sendLineBackgroundlineGetBackground(V val, VEvent? payload) {
    sendEvent(val, 63, payload); // LineBackground/lineGetBackground
  }

  void sendLineStylelineGetStyle(V val, VEvent? payload) {
    sendEvent(val, 64, payload); // LineStyle/lineGetStyle
  }

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, 72, payload); // Modify/Modify
  }

  void sendPaintObjectpaintObject(V val, VEvent? payload) {
    sendEvent(val, 85, payload); // PaintObject/paintObject
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 114, payload); // Verify/Verify
  }

  void sendVerifyKeyverifyKey(V val, VEvent? payload) {
    sendEvent(val, 115, payload); // VerifyKey/verifyKey
  }

  void sendWordMovementgetNextOffset(V val, VEvent? payload) {
    sendEvent(val, 132, payload); // WordMovement/getNextOffset
  }

  void sendWordMovementgetPreviousOffset(V val, VEvent? payload) {
    sendEvent(val, 133, payload); // WordMovement/getPreviousOffset
  }
}

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
  bool? fixedLineHeight;

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
      fixedLineHeight = other.fixedLineHeight;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 475: // alignment
        alignment = r.int32();
        return true;
      case 476: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 477: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 478: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 479: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 480: // caretOffset
        caretOffset = r.int32();
        return true;
      case 482: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 483: // editable
        editable = r.boolean();
        return true;
      case 485: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 486: // indent
        indent = r.int32();
        return true;
      case 487: // justify
        justify = r.boolean();
        return true;
      case 488: // leftMargin
        leftMargin = r.int32();
        return true;
      case 489: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 490: // marginColor
        marginColor = r.color();
        return true;
      case 492: // rightMargin
        rightMargin = r.int32();
        return true;
      case 493: // selection
        selection = r.point();
        return true;
      case 494: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 495: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 496: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 499: // tabStops
        tabStops = r.ints();
        return true;
      case 500: // tabs
        tabs = r.int32();
        return true;
      case 501: // text
        text = r.string();
        return true;
      case 502: // topMargin
        topMargin = r.int32();
        return true;
      case 503: // topPixel
        topPixel = r.int32();
        return true;
      case 504: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 505: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 491: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 481: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 498: // styles
        styles = r.list((r) => r.object(VStyleRange.new));
        return true;
      case 497: // styleIndex
        styleIndex = r.ints();
        return true;
      case 484: // fixedLineHeight
        fixedLineHeight = r.boolean();
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
      if (v_ != null) {
        w.keyInt32(475, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(476, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(477, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(478);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(479, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(480, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(482, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(483, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(485, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(486, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(487, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(488, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(489, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(490);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(492, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(493);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(494);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(495);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(496);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(499);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(500, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(501);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(502, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(503, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(504, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(505, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(491);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(481);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(498);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(497);
        w.ints(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(484, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(475, alignment);
    visit(476, alwaysShowScrollBars);
    visit(477, blockSelection);
    visit(478, blockSelectionBounds);
    visit(479, bottomMargin);
    visit(480, caretOffset);
    visit(482, doubleClickEnabled);
    visit(483, editable);
    visit(485, horizontalPixel);
    visit(486, indent);
    visit(487, justify);
    visit(488, leftMargin);
    visit(489, lineSpacing);
    visit(490, marginColor);
    visit(492, rightMargin);
    visit(493, selection);
    visit(494, selectionBackground);
    visit(495, selectionForeground);
    visit(496, selectionRanges);
    visit(499, tabStops);
    visit(500, tabs);
    visit(501, text);
    visit(502, topMargin);
    visit(503, topPixel);
    visit(504, wordWrap);
    visit(505, wrapIndent);
    visit(491, renderer);
    visit(481, caretOffsets);
    visit(498, styles);
    visit(497, styleIndex);
    visit(484, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
