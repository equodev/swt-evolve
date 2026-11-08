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
      case 517: // ascent
        ascent = r.int32();
        return true;
      case 518: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 519: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 520: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 521: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 523: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 524: // descent
        descent = r.int32();
        return true;
      case 525: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 526: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 527: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 528: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 529: // lineCount
        lineCount = r.int32();
        return true;
      case 530: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 531: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 533: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 534: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 535: // maxWidth
        maxWidth = r.int32();
        return true;
      case 536: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 537: // ranges
        ranges = r.ints();
        return true;
      case 538: // redrawLines
        redrawLines = r.ints();
        return true;
      case 539: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 540: // styleCount
        styleCount = r.int32();
        return true;
      case 541: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 542: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 543: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 544: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 545: // tabLength
        tabLength = r.int32();
        return true;
      case 546: // tabWidth
        tabWidth = r.int32();
        return true;
      case 547: // topIndex
        topIndex = r.int32();
        return true;
      case 532: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 522: // bulletTexts
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
        w.keyInt32(517, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(518, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(519);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(520);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(521);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(523);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(524, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(525, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(526, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(527, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(528);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(529, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(530);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(531, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(533);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(535, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(536, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(537);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(538);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(539);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(541, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(542);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(543);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(545, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(546, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(547, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(532);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(522);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(517, ascent);
    visit(518, averageCharWidth);
    visit(519, averageLineHeight);
    visit(520, boldFont);
    visit(521, boldItalicFont);
    visit(523, bulletsIndices);
    visit(524, descent);
    visit(525, fixedPitch);
    visit(526, hasLinks);
    visit(527, idleRunning);
    visit(528, italicFont);
    visit(529, lineCount);
    visit(530, lineSizes);
    visit(531, lineSpacingComputing);
    visit(533, lines);
    visit(534, linesInAverageLineHeight);
    visit(535, maxWidth);
    visit(536, maxWidthLineIndex);
    visit(537, ranges);
    visit(538, redrawLines);
    visit(539, regularFont);
    visit(540, styleCount);
    visit(541, styledText);
    visit(542, styles);
    visit(543, stylesSet);
    visit(544, stylesSetCount);
    visit(545, tabLength);
    visit(546, tabWidth);
    visit(547, topIndex);
    visit(532, lineSpacings);
    visit(522, bulletTexts);
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
