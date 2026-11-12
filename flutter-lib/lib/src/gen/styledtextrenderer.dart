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
      case 522: // ascent
        ascent = r.int32();
        return true;
      case 523: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 524: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 525: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 526: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 528: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 529: // descent
        descent = r.int32();
        return true;
      case 530: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 531: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 532: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 533: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 534: // lineCount
        lineCount = r.int32();
        return true;
      case 535: // lineSizes
        lineSizes = r.objects(VLineSizeInfo.new);
        return true;
      case 536: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 538: // lines
        lines = r.objects(VLineInfo.new);
        return true;
      case 539: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 540: // maxWidth
        maxWidth = r.int32();
        return true;
      case 541: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 542: // ranges
        ranges = r.ints();
        return true;
      case 543: // redrawLines
        redrawLines = r.ints();
        return true;
      case 544: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 545: // styleCount
        styleCount = r.int32();
        return true;
      case 546: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 547: // styles
        styles = r.objects(VStyleRange.new)?.whereType<VStyleRange>().toList();
        return true;
      case 548: // stylesSet
        stylesSet = r
            .objects(VStyleRange.new)
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 549: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 550: // tabLength
        tabLength = r.int32();
        return true;
      case 551: // tabWidth
        tabWidth = r.int32();
        return true;
      case 552: // topIndex
        topIndex = r.int32();
        return true;
      case 537: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 527: // bulletTexts
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
        w.keyInt32(522, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(523, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(524);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(525);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(526);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(528);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(529, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(530, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(531, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(532, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(533);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(535);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(536, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(538);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(539, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(542);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(543);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(544);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(545, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(546, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(547);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(548);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(549, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(550, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(551, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(552, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(537);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(527);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(522, ascent);
    visit(523, averageCharWidth);
    visit(524, averageLineHeight);
    visit(525, boldFont);
    visit(526, boldItalicFont);
    visit(528, bulletsIndices);
    visit(529, descent);
    visit(530, fixedPitch);
    visit(531, hasLinks);
    visit(532, idleRunning);
    visit(533, italicFont);
    visit(534, lineCount);
    visit(535, lineSizes);
    visit(536, lineSpacingComputing);
    visit(538, lines);
    visit(539, linesInAverageLineHeight);
    visit(540, maxWidth);
    visit(541, maxWidthLineIndex);
    visit(542, ranges);
    visit(543, redrawLines);
    visit(544, regularFont);
    visit(545, styleCount);
    visit(546, styledText);
    visit(547, styles);
    visit(548, stylesSet);
    visit(549, stylesSetCount);
    visit(550, tabLength);
    visit(551, tabWidth);
    visit(552, topIndex);
    visit(537, lineSpacings);
    visit(527, bulletTexts);
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
      case 367: // height
        height = r.int32();
        return true;
      case 368: // width
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
        w.keyInt32(367, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(368, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(367, height);
    visit(368, width);
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
      case 357: // alignment
        alignment = r.int32();
        return true;
      case 358: // background
        background = r.color();
        return true;
      case 359: // flags
        flags = r.int32();
        return true;
      case 360: // indent
        indent = r.int32();
        return true;
      case 361: // justify
        justify = r.boolean();
        return true;
      case 362: // segments
        segments = r.ints();
        return true;
      case 363: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 364: // tabStops
        tabStops = r.ints();
        return true;
      case 365: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 366: // wrapIndent
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
        w.keyInt32(357, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(358);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(360, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(361, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(362);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(363);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(364);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(365, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(366, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(357, alignment);
    visit(358, background);
    visit(359, flags);
    visit(360, indent);
    visit(361, justify);
    visit(362, segments);
    visit(363, segmentsChars);
    visit(364, tabStops);
    visit(365, verticalIndent);
    visit(366, wrapIndent);
  }
}
