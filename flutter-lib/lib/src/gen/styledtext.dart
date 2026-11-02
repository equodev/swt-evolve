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
      case 472: // alignment
        alignment = r.int32();
        return true;
      case 473: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 474: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 475: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 476: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 477: // caretOffset
        caretOffset = r.int32();
        return true;
      case 479: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 480: // editable
        editable = r.boolean();
        return true;
      case 482: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 483: // indent
        indent = r.int32();
        return true;
      case 484: // justify
        justify = r.boolean();
        return true;
      case 485: // leftMargin
        leftMargin = r.int32();
        return true;
      case 486: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 487: // marginColor
        marginColor = r.color();
        return true;
      case 489: // rightMargin
        rightMargin = r.int32();
        return true;
      case 490: // selection
        selection = r.point();
        return true;
      case 491: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 492: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 493: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 496: // tabStops
        tabStops = r.ints();
        return true;
      case 497: // tabs
        tabs = r.int32();
        return true;
      case 498: // text
        text = r.string();
        return true;
      case 499: // topMargin
        topMargin = r.int32();
        return true;
      case 500: // topPixel
        topPixel = r.int32();
        return true;
      case 501: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 502: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 488: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 478: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 495: // styles
        styles = r.list((r) => r.object(VStyleRange.new));
        return true;
      case 494: // styleIndex
        styleIndex = r.ints();
        return true;
      case 481: // fixedLineHeight
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
        w.keyInt32(472, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(473, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(474, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(475);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(476, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(477, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(479, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(480, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(482, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(483, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(484, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(485, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(486, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(487);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(489, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(490);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(491);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(492);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(493);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(496);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(497, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(498);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(499, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(500, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(501, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(502, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(488);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(478);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(495);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(494);
        w.ints(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(481, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(472, alignment);
    visit(473, alwaysShowScrollBars);
    visit(474, blockSelection);
    visit(475, blockSelectionBounds);
    visit(476, bottomMargin);
    visit(477, caretOffset);
    visit(479, doubleClickEnabled);
    visit(480, editable);
    visit(482, horizontalPixel);
    visit(483, indent);
    visit(484, justify);
    visit(485, leftMargin);
    visit(486, lineSpacing);
    visit(487, marginColor);
    visit(489, rightMargin);
    visit(490, selection);
    visit(491, selectionBackground);
    visit(492, selectionForeground);
    visit(493, selectionRanges);
    visit(496, tabStops);
    visit(497, tabs);
    visit(498, text);
    visit(499, topMargin);
    visit(500, topPixel);
    visit(501, wordWrap);
    visit(502, wrapIndent);
    visit(488, renderer);
    visit(478, caretOffsets);
    visit(495, styles);
    visit(494, styleIndex);
    visit(481, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
