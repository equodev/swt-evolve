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
      case 481: // alignment
        alignment = r.int32();
        return true;
      case 482: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 483: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 484: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 485: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 486: // caretOffset
        caretOffset = r.int32();
        return true;
      case 488: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 489: // editable
        editable = r.boolean();
        return true;
      case 491: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 492: // indent
        indent = r.int32();
        return true;
      case 493: // justify
        justify = r.boolean();
        return true;
      case 494: // leftMargin
        leftMargin = r.int32();
        return true;
      case 495: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 496: // marginColor
        marginColor = r.color();
        return true;
      case 498: // rightMargin
        rightMargin = r.int32();
        return true;
      case 499: // selection
        selection = r.point();
        return true;
      case 500: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 501: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 502: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 505: // tabStops
        tabStops = r.ints();
        return true;
      case 506: // tabs
        tabs = r.int32();
        return true;
      case 507: // text
        text = r.string();
        return true;
      case 508: // topMargin
        topMargin = r.int32();
        return true;
      case 509: // topPixel
        topPixel = r.int32();
        return true;
      case 510: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 511: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 497: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 487: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 504: // styles
        styles = r.objects(VStyleRange.new);
        return true;
      case 503: // styleIndex
        styleIndex = r.runs();
        return true;
      case 490: // fixedLineHeight
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
        w.keyInt32(481, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(482, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(483, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(484);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(485, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(486, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(488, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(489, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(491, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(492, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(493, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(494, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(495, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(496);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(498, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(499);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(500);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(501);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(502);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(505);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(506, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(507);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(508, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(509, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(510, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(511, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(497);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(487);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(504);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(503);
        w.runs(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(490, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(481, alignment);
    visit(482, alwaysShowScrollBars);
    visit(483, blockSelection);
    visit(484, blockSelectionBounds);
    visit(485, bottomMargin);
    visit(486, caretOffset);
    visit(488, doubleClickEnabled);
    visit(489, editable);
    visit(491, horizontalPixel);
    visit(492, indent);
    visit(493, justify);
    visit(494, leftMargin);
    visit(495, lineSpacing);
    visit(496, marginColor);
    visit(498, rightMargin);
    visit(499, selection);
    visit(500, selectionBackground);
    visit(501, selectionForeground);
    visit(502, selectionRanges);
    visit(505, tabStops);
    visit(506, tabs);
    visit(507, text);
    visit(508, topMargin);
    visit(509, topPixel);
    visit(510, wordWrap);
    visit(511, wrapIndent);
    visit(497, renderer);
    visit(487, caretOffsets);
    visit(504, styles);
    visit(503, styleIndex);
    visit(490, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
