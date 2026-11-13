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
      case 484: // alignment
        alignment = r.int32();
        return true;
      case 485: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 486: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 487: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 488: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 489: // caretOffset
        caretOffset = r.int32();
        return true;
      case 491: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 492: // editable
        editable = r.boolean();
        return true;
      case 494: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 495: // indent
        indent = r.int32();
        return true;
      case 496: // justify
        justify = r.boolean();
        return true;
      case 497: // leftMargin
        leftMargin = r.int32();
        return true;
      case 498: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 499: // marginColor
        marginColor = r.color();
        return true;
      case 501: // rightMargin
        rightMargin = r.int32();
        return true;
      case 502: // selection
        selection = r.point();
        return true;
      case 503: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 504: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 505: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 508: // tabStops
        tabStops = r.ints();
        return true;
      case 509: // tabs
        tabs = r.int32();
        return true;
      case 510: // text
        text = r.string();
        return true;
      case 511: // topMargin
        topMargin = r.int32();
        return true;
      case 512: // topPixel
        topPixel = r.int32();
        return true;
      case 513: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 514: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 500: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 490: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 507: // styles
        styles = r.objects(VStyleRange.new);
        return true;
      case 506: // styleIndex
        styleIndex = r.runs();
        return true;
      case 493: // fixedLineHeight
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
        w.keyInt32(484, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(485, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(486, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(487);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(488, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(489, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(491, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(492, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(494, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(495, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(496, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(497, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(498, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(499);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(501, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(502);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(503);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(504);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(505);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(508);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(509, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(510);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(511, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(512, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(513, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(514, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(500);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(490);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(507);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(506);
        w.runs(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(493, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(484, alignment);
    visit(485, alwaysShowScrollBars);
    visit(486, blockSelection);
    visit(487, blockSelectionBounds);
    visit(488, bottomMargin);
    visit(489, caretOffset);
    visit(491, doubleClickEnabled);
    visit(492, editable);
    visit(494, horizontalPixel);
    visit(495, indent);
    visit(496, justify);
    visit(497, leftMargin);
    visit(498, lineSpacing);
    visit(499, marginColor);
    visit(501, rightMargin);
    visit(502, selection);
    visit(503, selectionBackground);
    visit(504, selectionForeground);
    visit(505, selectionRanges);
    visit(508, tabStops);
    visit(509, tabs);
    visit(510, text);
    visit(511, topMargin);
    visit(512, topPixel);
    visit(513, wordWrap);
    visit(514, wrapIndent);
    visit(500, renderer);
    visit(490, caretOffsets);
    visit(507, styles);
    visit(506, styleIndex);
    visit(493, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
