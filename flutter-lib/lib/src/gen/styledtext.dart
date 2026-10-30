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
      case 471: // alignment
        alignment = r.int32();
        return true;
      case 472: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 473: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 474: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 475: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 476: // caretOffset
        caretOffset = r.int32();
        return true;
      case 478: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 479: // editable
        editable = r.boolean();
        return true;
      case 481: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 482: // indent
        indent = r.int32();
        return true;
      case 483: // justify
        justify = r.boolean();
        return true;
      case 484: // leftMargin
        leftMargin = r.int32();
        return true;
      case 485: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 486: // marginColor
        marginColor = r.color();
        return true;
      case 488: // rightMargin
        rightMargin = r.int32();
        return true;
      case 489: // selection
        selection = r.point();
        return true;
      case 490: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 491: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 492: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 495: // tabStops
        tabStops = r.ints();
        return true;
      case 496: // tabs
        tabs = r.int32();
        return true;
      case 497: // text
        text = r.string();
        return true;
      case 498: // topMargin
        topMargin = r.int32();
        return true;
      case 499: // topPixel
        topPixel = r.int32();
        return true;
      case 500: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 501: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 487: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 477: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 494: // styles
        styles = r.list((r) => r.object(VStyleRange.new));
        return true;
      case 493: // styleIndex
        styleIndex = r.ints();
        return true;
      case 480: // fixedLineHeight
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
        w.keyInt32(471, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(472, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(473, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(474);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(475, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(476, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(478, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(479, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(481, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(482, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(483, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(484, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(485, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(486);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(488, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(489);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(490);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(491);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(492);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(495);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(496, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(497);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(498, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(499, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(500, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(501, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(487);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(477);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(494);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(493);
        w.ints(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(480, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(471, alignment);
    visit(472, alwaysShowScrollBars);
    visit(473, blockSelection);
    visit(474, blockSelectionBounds);
    visit(475, bottomMargin);
    visit(476, caretOffset);
    visit(478, doubleClickEnabled);
    visit(479, editable);
    visit(481, horizontalPixel);
    visit(482, indent);
    visit(483, justify);
    visit(484, leftMargin);
    visit(485, lineSpacing);
    visit(486, marginColor);
    visit(488, rightMargin);
    visit(489, selection);
    visit(490, selectionBackground);
    visit(491, selectionForeground);
    visit(492, selectionRanges);
    visit(495, tabStops);
    visit(496, tabs);
    visit(497, text);
    visit(498, topMargin);
    visit(499, topPixel);
    visit(500, wordWrap);
    visit(501, wrapIndent);
    visit(487, renderer);
    visit(477, caretOffsets);
    visit(494, styles);
    visit(493, styleIndex);
    visit(480, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
