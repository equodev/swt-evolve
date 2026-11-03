import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/styledtext.dart';
import '../gen/stylerange.dart';

class VStyledTextRenderer implements WireReadable, WireWritable, WireVisitable {
  VStyledTextRenderer();

  int? ascent;
  int? averageCharWidth;
  double? averageLineHeight;
  VFont? boldFont;
  VFont? boldItalicFont;
  List<int>? bulletsIndices;
  int? descent;
  bool? fixedPitch;
  bool? hasLinks;
  bool? idleRunning;
  VFont? italicFont;
  int? lineCount;
  List<VLineSizeInfo?>? lineSizes;
  bool? lineSpacingComputing;
  List<VLineInfo?>? lines;
  int? linesInAverageLineHeight;
  int? maxWidth;
  int? maxWidthLineIndex;
  List<int>? ranges;
  List<int>? redrawLines;
  VFont? regularFont;
  int? styleCount;
  VStyledText? styledText;
  List<VStyleRange>? styles;
  List<VStyleRange>? stylesSet;
  int? stylesSetCount;
  int? tabLength;
  int? tabWidth;
  int? topIndex;
  List<int>? lineSpacings;
  List<String?>? bulletTexts;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 513: // ascent
        ascent = r.int32();
        return true;
      case 514: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 515: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 516: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 517: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 519: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 520: // descent
        descent = r.int32();
        return true;
      case 521: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 522: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 523: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 524: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 525: // lineCount
        lineCount = r.int32();
        return true;
      case 526: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 527: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 529: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 530: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 531: // maxWidth
        maxWidth = r.int32();
        return true;
      case 532: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 533: // ranges
        ranges = r.ints();
        return true;
      case 534: // redrawLines
        redrawLines = r.ints();
        return true;
      case 535: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 536: // styleCount
        styleCount = r.int32();
        return true;
      case 537: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 538: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 539: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 540: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 541: // tabLength
        tabLength = r.int32();
        return true;
      case 542: // tabWidth
        tabWidth = r.int32();
        return true;
      case 543: // topIndex
        topIndex = r.int32();
        return true;
      case 528: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 518: // bulletTexts
        bulletTexts = r.strings();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = ascent;
      if (v_ != null) {
        w.keyInt32(513, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(514, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(515);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(516);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(517);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(519);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(520, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(521, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(522, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(523, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(524);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(525, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(526);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(527, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(529);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(530, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(531, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(532, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(533);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(534);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(535);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(536, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(537, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(538);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(539);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(528);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(518);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(513, ascent);
    visit(514, averageCharWidth);
    visit(515, averageLineHeight);
    visit(516, boldFont);
    visit(517, boldItalicFont);
    visit(519, bulletsIndices);
    visit(520, descent);
    visit(521, fixedPitch);
    visit(522, hasLinks);
    visit(523, idleRunning);
    visit(524, italicFont);
    visit(525, lineCount);
    visit(526, lineSizes);
    visit(527, lineSpacingComputing);
    visit(529, lines);
    visit(530, linesInAverageLineHeight);
    visit(531, maxWidth);
    visit(532, maxWidthLineIndex);
    visit(533, ranges);
    visit(534, redrawLines);
    visit(535, regularFont);
    visit(536, styleCount);
    visit(537, styledText);
    visit(538, styles);
    visit(539, stylesSet);
    visit(540, stylesSetCount);
    visit(541, tabLength);
    visit(542, tabWidth);
    visit(543, topIndex);
    visit(528, lineSpacings);
    visit(518, bulletTexts);
  }
}

class VLineSizeInfo implements WireReadable, WireWritable, WireVisitable {
  VLineSizeInfo() : this.empty();
  VLineSizeInfo.empty();

  int? height;
  int? width;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 359: // height
        height = r.int32();
        return true;
      case 360: // width
        width = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(360, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(359, height);
    visit(360, width);
  }
}

class VLineInfo implements WireReadable, WireWritable, WireVisitable {
  VLineInfo() : this.empty();
  VLineInfo.empty();

  int? alignment;
  VColor? background;
  int? flags;
  int? indent;
  bool? justify;
  List<int>? segments;
  List<int>? segmentsChars;
  List<int>? tabStops;
  int? verticalIndent;
  int? wrapIndent;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 349: // alignment
        alignment = r.int32();
        return true;
      case 350: // background
        background = r.color();
        return true;
      case 351: // flags
        flags = r.int32();
        return true;
      case 352: // indent
        indent = r.int32();
        return true;
      case 353: // justify
        justify = r.boolean();
        return true;
      case 354: // segments
        segments = r.ints();
        return true;
      case 355: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 356: // tabStops
        tabStops = r.ints();
        return true;
      case 357: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 358: // wrapIndent
        wrapIndent = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = alignment;
      if (v_ != null) {
        w.keyInt32(349, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(350);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(351, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(352, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(353, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(354);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(355);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(356);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(357, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(358, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(349, alignment);
    visit(350, background);
    visit(351, flags);
    visit(352, indent);
    visit(353, justify);
    visit(354, segments);
    visit(355, segmentsChars);
    visit(356, tabStops);
    visit(357, verticalIndent);
    visit(358, wrapIndent);
  }
}
