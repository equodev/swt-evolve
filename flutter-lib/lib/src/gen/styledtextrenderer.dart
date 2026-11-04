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
      case 515: // ascent
        ascent = r.int32();
        return true;
      case 516: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 517: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 518: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 519: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 521: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 522: // descent
        descent = r.int32();
        return true;
      case 523: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 524: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 525: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 526: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 527: // lineCount
        lineCount = r.int32();
        return true;
      case 528: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 529: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 531: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 532: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 533: // maxWidth
        maxWidth = r.int32();
        return true;
      case 534: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 535: // ranges
        ranges = r.ints();
        return true;
      case 536: // redrawLines
        redrawLines = r.ints();
        return true;
      case 537: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 538: // styleCount
        styleCount = r.int32();
        return true;
      case 539: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 540: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 541: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 542: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 543: // tabLength
        tabLength = r.int32();
        return true;
      case 544: // tabWidth
        tabWidth = r.int32();
        return true;
      case 545: // topIndex
        topIndex = r.int32();
        return true;
      case 530: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 520: // bulletTexts
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
        w.keyInt32(515, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(516, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(517);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(518);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(519);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(521);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(522, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(523, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(524, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(525, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(526);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(527, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(528);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(529, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(531);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(532, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(533, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(535);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(536);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(537);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(538, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(539, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(540);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(541);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(545, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(530);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(520);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(515, ascent);
    visit(516, averageCharWidth);
    visit(517, averageLineHeight);
    visit(518, boldFont);
    visit(519, boldItalicFont);
    visit(521, bulletsIndices);
    visit(522, descent);
    visit(523, fixedPitch);
    visit(524, hasLinks);
    visit(525, idleRunning);
    visit(526, italicFont);
    visit(527, lineCount);
    visit(528, lineSizes);
    visit(529, lineSpacingComputing);
    visit(531, lines);
    visit(532, linesInAverageLineHeight);
    visit(533, maxWidth);
    visit(534, maxWidthLineIndex);
    visit(535, ranges);
    visit(536, redrawLines);
    visit(537, regularFont);
    visit(538, styleCount);
    visit(539, styledText);
    visit(540, styles);
    visit(541, stylesSet);
    visit(542, stylesSetCount);
    visit(543, tabLength);
    visit(544, tabWidth);
    visit(545, topIndex);
    visit(530, lineSpacings);
    visit(520, bulletTexts);
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
      case 361: // height
        height = r.int32();
        return true;
      case 362: // width
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
        w.keyInt32(361, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(362, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(361, height);
    visit(362, width);
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
      case 351: // alignment
        alignment = r.int32();
        return true;
      case 352: // background
        background = r.color();
        return true;
      case 353: // flags
        flags = r.int32();
        return true;
      case 354: // indent
        indent = r.int32();
        return true;
      case 355: // justify
        justify = r.boolean();
        return true;
      case 356: // segments
        segments = r.ints();
        return true;
      case 357: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 358: // tabStops
        tabStops = r.ints();
        return true;
      case 359: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 360: // wrapIndent
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
        w.keyInt32(351, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(352);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(353, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(354, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(355, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(356);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(357);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(358);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(360, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(351, alignment);
    visit(352, background);
    visit(353, flags);
    visit(354, indent);
    visit(355, justify);
    visit(356, segments);
    visit(357, segmentsChars);
    visit(358, tabStops);
    visit(359, verticalIndent);
    visit(360, wrapIndent);
  }
}
