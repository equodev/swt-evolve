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
      case 4864: // alignment
        alignment = r.int32();
        return true;
      case 4865: // alwaysShowScrollBars
        alwaysShowScrollBars = r.boolean();
        return true;
      case 4866: // blockSelection
        blockSelection = r.boolean();
        return true;
      case 4867: // blockSelectionBounds
        blockSelectionBounds = r.rect();
        return true;
      case 4868: // bottomMargin
        bottomMargin = r.int32();
        return true;
      case 4869: // caretOffset
        caretOffset = r.int32();
        return true;
      case 4871: // doubleClickEnabled
        doubleClickEnabled = r.boolean();
        return true;
      case 4872: // editable
        editable = r.boolean();
        return true;
      case 4874: // horizontalPixel
        horizontalPixel = r.int32();
        return true;
      case 4875: // indent
        indent = r.int32();
        return true;
      case 4876: // justify
        justify = r.boolean();
        return true;
      case 4877: // leftMargin
        leftMargin = r.int32();
        return true;
      case 4878: // lineSpacing
        lineSpacing = r.int32();
        return true;
      case 4879: // marginColor
        marginColor = r.color();
        return true;
      case 4881: // rightMargin
        rightMargin = r.int32();
        return true;
      case 4882: // selection
        selection = r.point();
        return true;
      case 4883: // selectionBackground
        selectionBackground = r.color();
        return true;
      case 4884: // selectionForeground
        selectionForeground = r.color();
        return true;
      case 4885: // selectionRanges
        selectionRanges = r.ints();
        return true;
      case 4888: // tabStops
        tabStops = r.ints();
        return true;
      case 4889: // tabs
        tabs = r.int32();
        return true;
      case 4890: // text
        text = r.string();
        return true;
      case 4891: // topMargin
        topMargin = r.int32();
        return true;
      case 4892: // topPixel
        topPixel = r.int32();
        return true;
      case 4893: // wordWrap
        wordWrap = r.boolean();
        return true;
      case 4894: // wrapIndent
        wrapIndent = r.int32();
        return true;
      case 4880: // renderer
        renderer = r.object(VStyledTextRenderer.new);
        return true;
      case 4870: // caretOffsets
        caretOffsets = r.ints();
        return true;
      case 4887: // styles
        styles = r.objects(VStyleRange.new);
        return true;
      case 4886: // styleIndex
        styleIndex = r.runs();
        return true;
      case 4873: // fixedLineHeight
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
        w.keyInt32(4864, v_);
      }
    }
    {
      final v_ = alwaysShowScrollBars;
      if (v_ != null) {
        w.keyBoolean(4865, v_);
      }
    }
    {
      final v_ = blockSelection;
      if (v_ != null) {
        w.keyBoolean(4866, v_);
      }
    }
    {
      final v_ = blockSelectionBounds;
      if (v_ != null) {
        w.key(4867);
        w.rectValue(v_);
      }
    }
    {
      final v_ = bottomMargin;
      if (v_ != null) {
        w.keyInt32(4868, v_);
      }
    }
    {
      final v_ = caretOffset;
      if (v_ != null) {
        w.keyInt32(4869, v_);
      }
    }
    {
      final v_ = doubleClickEnabled;
      if (v_ != null) {
        w.keyBoolean(4871, v_);
      }
    }
    {
      final v_ = editable;
      if (v_ != null) {
        w.keyBoolean(4872, v_);
      }
    }
    {
      final v_ = horizontalPixel;
      if (v_ != null) {
        w.keyInt32(4874, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(4875, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(4876, v_);
      }
    }
    {
      final v_ = leftMargin;
      if (v_ != null) {
        w.keyInt32(4877, v_);
      }
    }
    {
      final v_ = lineSpacing;
      if (v_ != null) {
        w.keyInt32(4878, v_);
      }
    }
    {
      final v_ = marginColor;
      if (v_ != null) {
        w.key(4879);
        w.colorValue(v_);
      }
    }
    {
      final v_ = rightMargin;
      if (v_ != null) {
        w.keyInt32(4881, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(4882);
        w.pointValue(v_);
      }
    }
    {
      final v_ = selectionBackground;
      if (v_ != null) {
        w.key(4883);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionForeground;
      if (v_ != null) {
        w.key(4884);
        w.colorValue(v_);
      }
    }
    {
      final v_ = selectionRanges;
      if (v_ != null) {
        w.key(4885);
        w.ints(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(4888);
        w.ints(v_);
      }
    }
    {
      final v_ = tabs;
      if (v_ != null) {
        w.keyInt32(4889, v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(4890);
        w.string(v_);
      }
    }
    {
      final v_ = topMargin;
      if (v_ != null) {
        w.keyInt32(4891, v_);
      }
    }
    {
      final v_ = topPixel;
      if (v_ != null) {
        w.keyInt32(4892, v_);
      }
    }
    {
      final v_ = wordWrap;
      if (v_ != null) {
        w.keyBoolean(4893, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(4894, v_);
      }
    }
    {
      final v_ = renderer;
      if (v_ != null) {
        w.key(4880);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = caretOffsets;
      if (v_ != null) {
        w.key(4870);
        w.ints(v_);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(4887);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = styleIndex;
      if (v_ != null) {
        w.key(4886);
        w.runs(v_);
      }
    }
    {
      final v_ = fixedLineHeight;
      if (v_ != null) {
        w.keyBoolean(4873, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(4864, alignment);
    visit(4865, alwaysShowScrollBars);
    visit(4866, blockSelection);
    visit(4867, blockSelectionBounds);
    visit(4868, bottomMargin);
    visit(4869, caretOffset);
    visit(4871, doubleClickEnabled);
    visit(4872, editable);
    visit(4874, horizontalPixel);
    visit(4875, indent);
    visit(4876, justify);
    visit(4877, leftMargin);
    visit(4878, lineSpacing);
    visit(4879, marginColor);
    visit(4881, rightMargin);
    visit(4882, selection);
    visit(4883, selectionBackground);
    visit(4884, selectionForeground);
    visit(4885, selectionRanges);
    visit(4888, tabStops);
    visit(4889, tabs);
    visit(4890, text);
    visit(4891, topMargin);
    visit(4892, topPixel);
    visit(4893, wordWrap);
    visit(4894, wrapIndent);
    visit(4880, renderer);
    visit(4870, caretOffsets);
    visit(4887, styles);
    visit(4886, styleIndex);
    visit(4873, fixedLineHeight);
  }

  @override
  String? get semanticsText => text;
}
