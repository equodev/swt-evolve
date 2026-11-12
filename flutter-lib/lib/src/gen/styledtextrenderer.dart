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
      case 521: // ascent
        ascent = r.int32();
        return true;
      case 522: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 523: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 524: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 525: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 527: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 528: // descent
        descent = r.int32();
        return true;
      case 529: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 530: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 531: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 532: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 533: // lineCount
        lineCount = r.int32();
        return true;
      case 534: // lineSizes
        lineSizes = r.objects(VLineSizeInfo.new);
        return true;
      case 535: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 537: // lines
        lines = r.objects(VLineInfo.new);
        return true;
      case 538: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 539: // maxWidth
        maxWidth = r.int32();
        return true;
      case 540: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 541: // ranges
        ranges = r.ints();
        return true;
      case 542: // redrawLines
        redrawLines = r.ints();
        return true;
      case 543: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 544: // styleCount
        styleCount = r.int32();
        return true;
      case 545: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 546: // styles
        styles = r.objects(VStyleRange.new)?.whereType<VStyleRange>().toList();
        return true;
      case 547: // stylesSet
        stylesSet = r
            .objects(VStyleRange.new)
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 548: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 549: // tabLength
        tabLength = r.int32();
        return true;
      case 550: // tabWidth
        tabWidth = r.int32();
        return true;
      case 551: // topIndex
        topIndex = r.int32();
        return true;
      case 536: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 526: // bulletTexts
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
        w.keyInt32(521, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(522, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(523);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(524);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(525);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(527);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(528, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(529, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(530, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(531, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(532);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(533, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(534);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(535, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(537);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(538, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(539, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(541);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(542);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(543);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(545, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(546);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(547);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(548, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(549, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(550, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(551, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(536);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(526);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(521, ascent);
    visit(522, averageCharWidth);
    visit(523, averageLineHeight);
    visit(524, boldFont);
    visit(525, boldItalicFont);
    visit(527, bulletsIndices);
    visit(528, descent);
    visit(529, fixedPitch);
    visit(530, hasLinks);
    visit(531, idleRunning);
    visit(532, italicFont);
    visit(533, lineCount);
    visit(534, lineSizes);
    visit(535, lineSpacingComputing);
    visit(537, lines);
    visit(538, linesInAverageLineHeight);
    visit(539, maxWidth);
    visit(540, maxWidthLineIndex);
    visit(541, ranges);
    visit(542, redrawLines);
    visit(543, regularFont);
    visit(544, styleCount);
    visit(545, styledText);
    visit(546, styles);
    visit(547, stylesSet);
    visit(548, stylesSetCount);
    visit(549, tabLength);
    visit(550, tabWidth);
    visit(551, topIndex);
    visit(536, lineSpacings);
    visit(526, bulletTexts);
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
      case 366: // height
        height = r.int32();
        return true;
      case 367: // width
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
        w.keyInt32(366, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(367, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(366, height);
    visit(367, width);
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
      case 356: // alignment
        alignment = r.int32();
        return true;
      case 357: // background
        background = r.color();
        return true;
      case 358: // flags
        flags = r.int32();
        return true;
      case 359: // indent
        indent = r.int32();
        return true;
      case 360: // justify
        justify = r.boolean();
        return true;
      case 361: // segments
        segments = r.ints();
        return true;
      case 362: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 363: // tabStops
        tabStops = r.ints();
        return true;
      case 364: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 365: // wrapIndent
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
        w.keyInt32(356, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(357);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(358, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(360, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(361);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(362);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(363);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(364, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(365, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(356, alignment);
    visit(357, background);
    visit(358, flags);
    visit(359, indent);
    visit(360, justify);
    visit(361, segments);
    visit(362, segmentsChars);
    visit(363, tabStops);
    visit(364, verticalIndent);
    visit(365, wrapIndent);
  }
}
