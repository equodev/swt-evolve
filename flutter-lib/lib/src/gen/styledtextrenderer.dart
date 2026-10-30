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
      case 511: // ascent
        ascent = r.int32();
        return true;
      case 512: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 513: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 514: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 515: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 517: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 518: // descent
        descent = r.int32();
        return true;
      case 519: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 520: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 521: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 522: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 523: // lineCount
        lineCount = r.int32();
        return true;
      case 524: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 525: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 527: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 528: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 529: // maxWidth
        maxWidth = r.int32();
        return true;
      case 530: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 531: // ranges
        ranges = r.ints();
        return true;
      case 532: // redrawLines
        redrawLines = r.ints();
        return true;
      case 533: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 534: // styleCount
        styleCount = r.int32();
        return true;
      case 535: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 536: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 537: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 538: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 539: // tabLength
        tabLength = r.int32();
        return true;
      case 540: // tabWidth
        tabWidth = r.int32();
        return true;
      case 541: // topIndex
        topIndex = r.int32();
        return true;
      case 526: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 516: // bulletTexts
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
        w.keyInt32(511, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(512, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(513);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(514);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(515);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(517);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(518, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(519, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(520, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(521, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(522);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(523, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(524);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(525, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(527);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(528, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(529, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(530, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(531);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(532);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(533);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(534, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(535, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(536);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(537);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(538, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(539, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(526);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(516);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(511, ascent);
    visit(512, averageCharWidth);
    visit(513, averageLineHeight);
    visit(514, boldFont);
    visit(515, boldItalicFont);
    visit(517, bulletsIndices);
    visit(518, descent);
    visit(519, fixedPitch);
    visit(520, hasLinks);
    visit(521, idleRunning);
    visit(522, italicFont);
    visit(523, lineCount);
    visit(524, lineSizes);
    visit(525, lineSpacingComputing);
    visit(527, lines);
    visit(528, linesInAverageLineHeight);
    visit(529, maxWidth);
    visit(530, maxWidthLineIndex);
    visit(531, ranges);
    visit(532, redrawLines);
    visit(533, regularFont);
    visit(534, styleCount);
    visit(535, styledText);
    visit(536, styles);
    visit(537, stylesSet);
    visit(538, stylesSetCount);
    visit(539, tabLength);
    visit(540, tabWidth);
    visit(541, topIndex);
    visit(526, lineSpacings);
    visit(516, bulletTexts);
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
      case 357: // height
        height = r.int32();
        return true;
      case 358: // width
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
        w.keyInt32(357, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(358, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(357, height);
    visit(358, width);
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
      case 347: // alignment
        alignment = r.int32();
        return true;
      case 348: // background
        background = r.color();
        return true;
      case 349: // flags
        flags = r.int32();
        return true;
      case 350: // indent
        indent = r.int32();
        return true;
      case 351: // justify
        justify = r.boolean();
        return true;
      case 352: // segments
        segments = r.ints();
        return true;
      case 353: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 354: // tabStops
        tabStops = r.ints();
        return true;
      case 355: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 356: // wrapIndent
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
        w.keyInt32(347, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(348);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(349, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(350, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(351, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(352);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(353);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(354);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(355, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(356, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(347, alignment);
    visit(348, background);
    visit(349, flags);
    visit(350, indent);
    visit(351, justify);
    visit(352, segments);
    visit(353, segmentsChars);
    visit(354, tabStops);
    visit(355, verticalIndent);
    visit(356, wrapIndent);
  }
}
