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
      case 524: // ascent
        ascent = r.int32();
        return true;
      case 525: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 526: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 527: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 528: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 530: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 531: // descent
        descent = r.int32();
        return true;
      case 532: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 533: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 534: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 535: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 536: // lineCount
        lineCount = r.int32();
        return true;
      case 537: // lineSizes
        lineSizes = r.objects(VLineSizeInfo.new);
        return true;
      case 538: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 540: // lines
        lines = r.objects(VLineInfo.new);
        return true;
      case 541: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 542: // maxWidth
        maxWidth = r.int32();
        return true;
      case 543: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 544: // ranges
        ranges = r.ints();
        return true;
      case 545: // redrawLines
        redrawLines = r.ints();
        return true;
      case 546: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 547: // styleCount
        styleCount = r.int32();
        return true;
      case 548: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 549: // styles
        styles = r.objects(VStyleRange.new)?.whereType<VStyleRange>().toList();
        return true;
      case 550: // stylesSet
        stylesSet = r
            .objects(VStyleRange.new)
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 551: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 552: // tabLength
        tabLength = r.int32();
        return true;
      case 553: // tabWidth
        tabWidth = r.int32();
        return true;
      case 554: // topIndex
        topIndex = r.int32();
        return true;
      case 539: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 529: // bulletTexts
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
        w.keyInt32(524, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(525, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(526);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(527);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(528);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(530);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(531, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(532, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(533, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(534, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(535);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(536, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(537);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(538, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(540);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(544);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(545);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(546);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(547, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(548, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(549);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(550);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(551, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(552, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(553, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(554, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(539);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(529);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(524, ascent);
    visit(525, averageCharWidth);
    visit(526, averageLineHeight);
    visit(527, boldFont);
    visit(528, boldItalicFont);
    visit(530, bulletsIndices);
    visit(531, descent);
    visit(532, fixedPitch);
    visit(533, hasLinks);
    visit(534, idleRunning);
    visit(535, italicFont);
    visit(536, lineCount);
    visit(537, lineSizes);
    visit(538, lineSpacingComputing);
    visit(540, lines);
    visit(541, linesInAverageLineHeight);
    visit(542, maxWidth);
    visit(543, maxWidthLineIndex);
    visit(544, ranges);
    visit(545, redrawLines);
    visit(546, regularFont);
    visit(547, styleCount);
    visit(548, styledText);
    visit(549, styles);
    visit(550, stylesSet);
    visit(551, stylesSetCount);
    visit(552, tabLength);
    visit(553, tabWidth);
    visit(554, topIndex);
    visit(539, lineSpacings);
    visit(529, bulletTexts);
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
      case 369: // height
        height = r.int32();
        return true;
      case 370: // width
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
        w.keyInt32(369, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(370, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(369, height);
    visit(370, width);
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
      case 359: // alignment
        alignment = r.int32();
        return true;
      case 360: // background
        background = r.color();
        return true;
      case 361: // flags
        flags = r.int32();
        return true;
      case 362: // indent
        indent = r.int32();
        return true;
      case 363: // justify
        justify = r.boolean();
        return true;
      case 364: // segments
        segments = r.ints();
        return true;
      case 365: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 366: // tabStops
        tabStops = r.ints();
        return true;
      case 367: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 368: // wrapIndent
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
        w.keyInt32(359, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(360);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(361, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(362, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(363, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(364);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(365);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(366);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(367, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(368, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(359, alignment);
    visit(360, background);
    visit(361, flags);
    visit(362, indent);
    visit(363, justify);
    visit(364, segments);
    visit(365, segmentsChars);
    visit(366, tabStops);
    visit(367, verticalIndent);
    visit(368, wrapIndent);
  }
}
