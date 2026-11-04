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
      case 516: // ascent
        ascent = r.int32();
        return true;
      case 517: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 518: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 519: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 520: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 522: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 523: // descent
        descent = r.int32();
        return true;
      case 524: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 525: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 526: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 527: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 528: // lineCount
        lineCount = r.int32();
        return true;
      case 529: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 530: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 532: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 533: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 534: // maxWidth
        maxWidth = r.int32();
        return true;
      case 535: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 536: // ranges
        ranges = r.ints();
        return true;
      case 537: // redrawLines
        redrawLines = r.ints();
        return true;
      case 538: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 539: // styleCount
        styleCount = r.int32();
        return true;
      case 540: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 541: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 542: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 543: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 544: // tabLength
        tabLength = r.int32();
        return true;
      case 545: // tabWidth
        tabWidth = r.int32();
        return true;
      case 546: // topIndex
        topIndex = r.int32();
        return true;
      case 531: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 521: // bulletTexts
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
        w.keyInt32(516, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(517, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(518);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(519);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(520);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(522);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(523, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(524, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(525, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(526, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(527);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(528, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(529);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(530, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(532);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(533, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(535, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(536);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(537);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(538);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(539, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(540, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(541);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(542);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(545, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(546, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(531);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(521);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(516, ascent);
    visit(517, averageCharWidth);
    visit(518, averageLineHeight);
    visit(519, boldFont);
    visit(520, boldItalicFont);
    visit(522, bulletsIndices);
    visit(523, descent);
    visit(524, fixedPitch);
    visit(525, hasLinks);
    visit(526, idleRunning);
    visit(527, italicFont);
    visit(528, lineCount);
    visit(529, lineSizes);
    visit(530, lineSpacingComputing);
    visit(532, lines);
    visit(533, linesInAverageLineHeight);
    visit(534, maxWidth);
    visit(535, maxWidthLineIndex);
    visit(536, ranges);
    visit(537, redrawLines);
    visit(538, regularFont);
    visit(539, styleCount);
    visit(540, styledText);
    visit(541, styles);
    visit(542, stylesSet);
    visit(543, stylesSetCount);
    visit(544, tabLength);
    visit(545, tabWidth);
    visit(546, topIndex);
    visit(531, lineSpacings);
    visit(521, bulletTexts);
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
      case 362: // height
        height = r.int32();
        return true;
      case 363: // width
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
        w.keyInt32(362, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(363, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(362, height);
    visit(363, width);
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
      case 352: // alignment
        alignment = r.int32();
        return true;
      case 353: // background
        background = r.color();
        return true;
      case 354: // flags
        flags = r.int32();
        return true;
      case 355: // indent
        indent = r.int32();
        return true;
      case 356: // justify
        justify = r.boolean();
        return true;
      case 357: // segments
        segments = r.ints();
        return true;
      case 358: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 359: // tabStops
        tabStops = r.ints();
        return true;
      case 360: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 361: // wrapIndent
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
        w.keyInt32(352, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(353);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(354, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(355, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(356, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(357);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(358);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(359);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(360, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(361, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(352, alignment);
    visit(353, background);
    visit(354, flags);
    visit(355, indent);
    visit(356, justify);
    visit(357, segments);
    visit(358, segmentsChars);
    visit(359, tabStops);
    visit(360, verticalIndent);
    visit(361, wrapIndent);
  }
}
