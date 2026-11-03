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
    sendEvent(val, 37, payload); // ExtendedModify/modifyText
  }

  void sendLineBackgroundlineGetBackground(V val, VEvent? payload) {
    sendEvent(val, 56, payload); // LineBackground/lineGetBackground
  }

  void sendLineStylelineGetStyle(V val, VEvent? payload) {
    sendEvent(val, 57, payload); // LineStyle/lineGetStyle
  }

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, 65, payload); // Modify/Modify
  }

  void sendPaintObjectpaintObject(V val, VEvent? payload) {
    sendEvent(val, 78, payload); // PaintObject/paintObject
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }

  void sendVerifyVerify(V val, VEvent? payload) {
    sendEvent(val, 107, payload); // Verify/Verify
  }

  void sendVerifyKeyverifyKey(V val, VEvent? payload) {
    sendEvent(val, 108, payload); // VerifyKey/verifyKey
  }

  void sendWordMovementgetNextOffset(V val, VEvent? payload) {
    sendEvent(val, 120, payload); // WordMovement/getNextOffset
  }

  void sendWordMovementgetPreviousOffset(V val, VEvent? payload) {
    sendEvent(val, 121, payload); // WordMovement/getPreviousOffset
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
      case 474: // alignment
        alignment = r.int32();
        return true;
      case 475: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 476: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 477: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 478: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 479: // caretOffset
        caretOffset = r.int32();
        return true;
      case 481: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 482: // editable
        editable = r.boolean();
        return true;
      case 484: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 485: // indent
        indent = r.int32();
        return true;
      case 486: // justify
        justify = r.boolean();
        return true;
      case 487: // leftMargin
        leftMargin = r.int32();
        return true;
      case 488: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 489: // marginColor
        marginColor = r.color();
        return true;
      case 491: // rightMargin
        rightMargin = r.int32();
        return true;
      case 492: // selection
        selection = r.point();
        return true;
      case 493: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 494: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 495: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 498: // tabStops
        tabStops = r.ints();
        return true;
      case 499: // tabs
        tabs = r.int32();
        return true;
      case 500: // text
        text = r.string();
        return true;
      case 501: // topMargin
        topMargin = r.int32();
        return true;
      case 502: // topPixel
        topPixel = r.int32();
        return true;
      case 503: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 504: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 490: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 480: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 497: // styles
        styles = r.list((r) => r.object(VStyleRange.new));
        return true;
      case 496: // styleIndex
        styleIndex = r.ints();
        return true;
      case 483: // fixedLineHeight
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
        w.keyInt32(474, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(475, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(476, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(477);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(478, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(479, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(481, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(482, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(484, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(485, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(486, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(487, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(488, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(489);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(491, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(492);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(493);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(494);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(495);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(498);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(499, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(500);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(501, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(502, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(503, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(504, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(490);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(480);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(497);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(496);
        w.ints(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(483, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(474, alignment);
    visit(475, alwaysShowScrollBars);
    visit(476, blockSelection);
    visit(477, blockSelectionBounds);
    visit(478, bottomMargin);
    visit(479, caretOffset);
    visit(481, doubleClickEnabled);
    visit(482, editable);
    visit(484, horizontalPixel);
    visit(485, indent);
    visit(486, justify);
    visit(487, leftMargin);
    visit(488, lineSpacing);
    visit(489, marginColor);
    visit(491, rightMargin);
    visit(492, selection);
    visit(493, selectionBackground);
    visit(494, selectionForeground);
    visit(495, selectionRanges);
    visit(498, tabStops);
    visit(499, tabs);
    visit(500, text);
    visit(501, topMargin);
    visit(502, topPixel);
    visit(503, wordWrap);
    visit(504, wrapIndent);
    visit(490, renderer);
    visit(480, caretOffsets);
    visit(497, styles);
    visit(496, styleIndex);
    visit(483, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
