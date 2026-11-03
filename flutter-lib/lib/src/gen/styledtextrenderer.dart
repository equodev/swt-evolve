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
      case 514: // ascent
        ascent = r.int32();
        return true;
      case 515: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 516: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 517: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 518: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 520: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 521: // descent
        descent = r.int32();
        return true;
      case 522: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 523: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 524: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 525: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 526: // lineCount
        lineCount = r.int32();
        return true;
      case 527: // lineSizes
        lineSizes = r.list((r) => r.object(VLineSizeInfo.new));
        return true;
      case 528: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 530: // lines
        lines = r.list((r) => r.object(VLineInfo.new));
        return true;
      case 531: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 532: // maxWidth
        maxWidth = r.int32();
        return true;
      case 533: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 534: // ranges
        ranges = r.ints();
        return true;
      case 535: // redrawLines
        redrawLines = r.ints();
        return true;
      case 536: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 537: // styleCount
        styleCount = r.int32();
        return true;
      case 538: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 539: // styles
        styles = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 540: // stylesSet
        stylesSet = r
            .list((r) => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 541: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 542: // tabLength
        tabLength = r.int32();
        return true;
      case 543: // tabWidth
        tabWidth = r.int32();
        return true;
      case 544: // topIndex
        topIndex = r.int32();
        return true;
      case 529: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 519: // bulletTexts
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
        w.keyInt32(514, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(515, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(516);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(517);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(518);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(520);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(521, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(522, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(523, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(524, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(525);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(526, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(527);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(528, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(530);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(531, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(532, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(533, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(534);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(535);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(536);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(537, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(538, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(539);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(540);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(541, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(542, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(543, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(544, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(529);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(519);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(514, ascent);
    visit(515, averageCharWidth);
    visit(516, averageLineHeight);
    visit(517, boldFont);
    visit(518, boldItalicFont);
    visit(520, bulletsIndices);
    visit(521, descent);
    visit(522, fixedPitch);
    visit(523, hasLinks);
    visit(524, idleRunning);
    visit(525, italicFont);
    visit(526, lineCount);
    visit(527, lineSizes);
    visit(528, lineSpacingComputing);
    visit(530, lines);
    visit(531, linesInAverageLineHeight);
    visit(532, maxWidth);
    visit(533, maxWidthLineIndex);
    visit(534, ranges);
    visit(535, redrawLines);
    visit(536, regularFont);
    visit(537, styleCount);
    visit(538, styledText);
    visit(539, styles);
    visit(540, stylesSet);
    visit(541, stylesSetCount);
    visit(542, tabLength);
    visit(543, tabWidth);
    visit(544, topIndex);
    visit(529, lineSpacings);
    visit(519, bulletTexts);
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
      case 360: // height
        height = r.int32();
        return true;
      case 361: // width
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
        w.keyInt32(360, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(361, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(360, height);
    visit(361, width);
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
      case 350: // alignment
        alignment = r.int32();
        return true;
      case 351: // background
        background = r.color();
        return true;
      case 352: // flags
        flags = r.int32();
        return true;
      case 353: // indent
        indent = r.int32();
        return true;
      case 354: // justify
        justify = r.boolean();
        return true;
      case 355: // segments
        segments = r.ints();
        return true;
      case 356: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 357: // tabStops
        tabStops = r.ints();
        return true;
      case 358: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 359: // wrapIndent
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
        w.keyInt32(350, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(351);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(352, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(353, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(354, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(355);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(356);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(357);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(358, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(359, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(350, alignment);
    visit(351, background);
    visit(352, flags);
    visit(353, indent);
    visit(354, justify);
    visit(355, segments);
    visit(356, segmentsChars);
    visit(357, tabStops);
    visit(358, verticalIndent);
    visit(359, wrapIndent);
  }
}
