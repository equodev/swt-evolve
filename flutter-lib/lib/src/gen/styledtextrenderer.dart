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
      case 525: // ascent
        ascent = r.int32();
        return true;
      case 526: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 527: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 528: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 529: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 531: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 532: // descent
        descent = r.int32();
        return true;
      case 533: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 534: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 535: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 536: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 537: // lineCount
        lineCount = r.int32();
        return true;
      case 538: // lineSizes
        lineSizes = r.objects(VLineSizeInfo.new);
        return true;
      case 539: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 541: // lines
        lines = r.objects(VLineInfo.new);
        return true;
      case 542: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 543: // maxWidth
        maxWidth = r.int32();
        return true;
      case 544: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 545: // ranges
        ranges = r.ints();
        return true;
      case 546: // redrawLines
        redrawLines = r.ints();
        return true;
      case 547: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 548: // styleCount
        styleCount = r.int32();
        return true;
      case 549: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 550: // styles
        styles = r.objects(VStyleRange.new)?.whereType<VStyleRange>().toList();
        return true;
      case 551: // stylesSet
        stylesSet = r
            .objects(VStyleRange.new)
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 552: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 553: // tabLength
        tabLength = r.int32();
        return true;
      case 554: // tabWidth
        tabWidth = r.int32();
        return true;
      case 555: // topIndex
        topIndex = r.int32();
        return true;
      case 540: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 530: // bulletTexts
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
        w.keyInt32(525, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(526, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(527);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(528);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(529);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(531);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(532, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(533, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(534, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(535, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(536);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(537, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(538);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(539, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(541);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(545);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(546);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(547);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(548, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(549, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(550);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(551);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(552, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(553, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(554, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(555, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(540);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(530);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(525, ascent);
    visit(526, averageCharWidth);
    visit(527, averageLineHeight);
    visit(528, boldFont);
    visit(529, boldItalicFont);
    visit(531, bulletsIndices);
    visit(532, descent);
    visit(533, fixedPitch);
    visit(534, hasLinks);
    visit(535, idleRunning);
    visit(536, italicFont);
    visit(537, lineCount);
    visit(538, lineSizes);
    visit(539, lineSpacingComputing);
    visit(541, lines);
    visit(542, linesInAverageLineHeight);
    visit(543, maxWidth);
    visit(544, maxWidthLineIndex);
    visit(545, ranges);
    visit(546, redrawLines);
    visit(547, regularFont);
    visit(548, styleCount);
    visit(549, styledText);
    visit(550, styles);
    visit(551, stylesSet);
    visit(552, stylesSetCount);
    visit(553, tabLength);
    visit(554, tabWidth);
    visit(555, topIndex);
    visit(540, lineSpacings);
    visit(530, bulletTexts);
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
      case 370: // height
        height = r.int32();
        return true;
      case 371: // width
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
        w.keyInt32(370, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(371, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(370, height);
    visit(371, width);
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
      case 360: // alignment
        alignment = r.int32();
        return true;
      case 361: // background
        background = r.color();
        return true;
      case 362: // flags
        flags = r.int32();
        return true;
      case 363: // indent
        indent = r.int32();
        return true;
      case 364: // justify
        justify = r.boolean();
        return true;
      case 365: // segments
        segments = r.ints();
        return true;
      case 366: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 367: // tabStops
        tabStops = r.ints();
        return true;
      case 368: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 369: // wrapIndent
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
        w.keyInt32(360, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(361);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(362, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(363, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(364, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(365);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(366);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(367);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(368, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(369, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(360, alignment);
    visit(361, background);
    visit(362, flags);
    visit(363, indent);
    visit(364, justify);
    visit(365, segments);
    visit(366, segmentsChars);
    visit(367, tabStops);
    visit(368, verticalIndent);
    visit(369, wrapIndent);
  }
}
