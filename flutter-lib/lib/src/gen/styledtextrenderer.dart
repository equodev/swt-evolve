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
      case 512: // ascent
        ascent = r.int32();
        return true;
      case 513: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 514: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 515: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 516: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 518: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 519: // descent
        descent = r.int32();
        return true;
      case 520: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 521: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 522: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 523: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 524: // lineCount
        lineCount = r.int32();
        return true;
      case 525: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 526: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 528: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 529: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 530: // maxWidth
        maxWidth = r.int32();
        return true;
      case 531: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 532: // ranges
        ranges = r.ints();
        return true;
      case 533: // redrawLines
        redrawLines = r.ints();
        return true;
      case 534: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 535: // styleCount
        styleCount = r.int32();
        return true;
      case 536: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 537: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 538: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 539: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 540: // tabLength
        tabLength = r.int32();
        return true;
      case 541: // tabWidth
        tabWidth = r.int32();
        return true;
      case 542: // topIndex
        topIndex = r.int32();
        return true;
      case 527: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 517: // bulletTexts
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
        w.keyInt32(512, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(513, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(514);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(515);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(516);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(518);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(519, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(520, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(521, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(522, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(523);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(524, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(525);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(526, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(528);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(529, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(530, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(531, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(532);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(533);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(534);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(535, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(536, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(537);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(538);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(539, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(540, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(527);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(517);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(512, ascent);
    visit(513, averageCharWidth);
    visit(514, averageLineHeight);
    visit(515, boldFont);
    visit(516, boldItalicFont);
    visit(518, bulletsIndices);
    visit(519, descent);
    visit(520, fixedPitch);
    visit(521, hasLinks);
    visit(522, idleRunning);
    visit(523, italicFont);
    visit(524, lineCount);
    visit(525, lineSizes);
    visit(526, lineSpacingComputing);
    visit(528, lines);
    visit(529, linesInAverageLineHeight);
    visit(530, maxWidth);
    visit(531, maxWidthLineIndex);
    visit(532, ranges);
    visit(533, redrawLines);
    visit(534, regularFont);
    visit(535, styleCount);
    visit(536, styledText);
    visit(537, styles);
    visit(538, stylesSet);
    visit(539, stylesSetCount);
    visit(540, tabLength);
    visit(541, tabWidth);
    visit(542, topIndex);
    visit(527, lineSpacings);
    visit(517, bulletTexts);
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
      case 358: // height
        height = r.int32();
        return true;
      case 359: // width
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
        w.keyInt32(358, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(358, height);
    visit(359, width);
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
      case 348: // alignment
        alignment = r.int32();
        return true;
      case 349: // background
        background = r.color();
        return true;
      case 350: // flags
        flags = r.int32();
        return true;
      case 351: // indent
        indent = r.int32();
        return true;
      case 352: // justify
        justify = r.boolean();
        return true;
      case 353: // segments
        segments = r.ints();
        return true;
      case 354: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 355: // tabStops
        tabStops = r.ints();
        return true;
      case 356: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 357: // wrapIndent
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
        w.keyInt32(348, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(349);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(350, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(351, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(352, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(353);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(354);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(355);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(356, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(357, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(348, alignment);
    visit(349, background);
    visit(350, flags);
    visit(351, indent);
    visit(352, justify);
    visit(353, segments);
    visit(354, segmentsChars);
    visit(355, tabStops);
    visit(356, verticalIndent);
    visit(357, wrapIndent);
  }
}
