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
      case 179: // alignment
        alignment = r.int32();
        return true;
      case 180: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 181: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 182: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 183: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 184: // caretOffset
        caretOffset = r.int32();
        return true;
      case 186: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 187: // editable
        editable = r.boolean();
        return true;
      case 188: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 189: // indent
        indent = r.int32();
        return true;
      case 190: // justify
        justify = r.boolean();
        return true;
      case 191: // leftMargin
        leftMargin = r.int32();
        return true;
      case 192: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 193: // marginColor
        marginColor = r.color();
        return true;
      case 195: // rightMargin
        rightMargin = r.int32();
        return true;
      case 196: // selection
        selection = r.point();
        return true;
      case 197: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 198: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 199: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 202: // tabStops
        tabStops = r.ints();
        return true;
      case 203: // tabs
        tabs = r.int32();
        return true;
      case 204: // text
        text = r.string();
        return true;
      case 205: // topMargin
        topMargin = r.int32();
        return true;
      case 206: // topPixel
        topPixel = r.int32();
        return true;
      case 207: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 208: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 194: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 185: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 201: // styles
        styles = r.list(() => r.object(VStyleRange.new));
        return true;
      case 200: // styleIndex
        styleIndex = r.ints();
        return true;
      case 1364: // fixedLineHeight
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
      if (v_ != null && w.wants(179)) {
        w.key(179);
        w.int32(v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null && w.wants(180)) {
        w.key(180);
        w.boolean(v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null && w.wants(181)) {
        w.key(181);
        w.boolean(v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null && w.wants(182)) {
        w.key(182);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null && w.wants(183)) {
        w.key(183);
        w.int32(v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null && w.wants(184)) {
        w.key(184);
        w.int32(v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null && w.wants(186)) {
        w.key(186);
        w.boolean(v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null && w.wants(187)) {
        w.key(187);
        w.boolean(v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null && w.wants(188)) {
        w.key(188);
        w.int32(v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null && w.wants(189)) {
        w.key(189);
        w.int32(v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null && w.wants(190)) {
        w.key(190);
        w.boolean(v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null && w.wants(191)) {
        w.key(191);
        w.int32(v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null && w.wants(192)) {
        w.key(192);
        w.int32(v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null && w.wants(193)) {
        w.key(193);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null && w.wants(195)) {
        w.key(195);
        w.int32(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(196)) {
        w.key(196);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null && w.wants(197)) {
        w.key(197);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null && w.wants(198)) {
        w.key(198);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null && w.wants(199)) {
        w.key(199);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null && w.wants(202)) {
        w.key(202);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null && w.wants(203)) {
        w.key(203);
        w.int32(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(204)) {
        w.key(204);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null && w.wants(205)) {
        w.key(205);
        w.int32(v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null && w.wants(206)) {
        w.key(206);
        w.int32(v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null && w.wants(207)) {
        w.key(207);
        w.boolean(v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null && w.wants(208)) {
        w.key(208);
        w.int32(v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null && w.wants(194)) {
        w.key(194);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null && w.wants(185)) {
        w.key(185);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null && w.wants(201)) {
        w.key(201);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null && w.wants(200)) {
        w.key(200);
        w.ints(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null && w.wants(1364)) {
        w.key(1364);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(179, alignment);
    visit(180, alwaysShowScrollBars);
    visit(181, blockSelection);
    visit(182, blockSelectionBounds);
    visit(183, bottomMargin);
    visit(184, caretOffset);
    visit(186, doubleClickEnabled);
    visit(187, editable);
    visit(188, horizontalPixel);
    visit(189, indent);
    visit(190, justify);
    visit(191, leftMargin);
    visit(192, lineSpacing);
    visit(193, marginColor);
    visit(195, rightMargin);
    visit(196, selection);
    visit(197, selectionBackground);
    visit(198, selectionForeground);
    visit(199, selectionRanges);
    visit(202, tabStops);
    visit(203, tabs);
    visit(204, text);
    visit(205, topMargin);
    visit(206, topPixel);
    visit(207, wordWrap);
    visit(208, wrapIndent);
    visit(194, renderer);
    visit(185, caretOffsets);
    visit(201, styles);
    visit(200, styleIndex);
    visit(1364, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
