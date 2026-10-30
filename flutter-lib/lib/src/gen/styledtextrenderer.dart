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
      case 488: // ascent
        ascent = r.int32();
        return true;
      case 583: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 584: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 585: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 586: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 587: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 490: // descent
        descent = r.int32();
        return true;
      case 588: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 589: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 590: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 591: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 592: // lineCount
        lineCount = r.int32();
        return true;
      case 593: // lineSizes
        lineSizes = r.list(() => r.object(VLineSizeInfo.new));
        return true;
      case 594: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 492: // lines
        lines = r.list(() => r.object(VLineInfo.new));
        return true;
      case 595: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 596: // maxWidth
        maxWidth = r.int32();
        return true;
      case 597: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 493: // ranges
        ranges = r.ints();
        return true;
      case 598: // redrawLines
        redrawLines = r.ints();
        return true;
      case 494: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 495: // styleCount
        styleCount = r.int32();
        return true;
      case 599: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 600: // styles
        styles = r
            .list(() => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 601: // stylesSet
        stylesSet = r
            .list(() => r.object(VStyleRange.new))
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 602: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 603: // tabLength
        tabLength = r.int32();
        return true;
      case 604: // tabWidth
        tabWidth = r.int32();
        return true;
      case 605: // topIndex
        topIndex = r.int32();
        return true;
      case 491: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 489: // bulletTexts
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
      if (v_ != null && w.wants(488)) {
        w.key(488);
        w.int32(v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null && w.wants(583)) {
        w.key(583);
        w.int32(v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null && w.wants(584)) {
        w.key(584);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null && w.wants(585)) {
        w.key(585);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null && w.wants(586)) {
        w.key(586);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null && w.wants(587)) {
        w.key(587);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null && w.wants(490)) {
        w.key(490);
        w.int32(v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null && w.wants(588)) {
        w.key(588);
        w.boolean(v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null && w.wants(589)) {
        w.key(589);
        w.boolean(v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null && w.wants(590)) {
        w.key(590);
        w.boolean(v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null && w.wants(591)) {
        w.key(591);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null && w.wants(592)) {
        w.key(592);
        w.int32(v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null && w.wants(593)) {
        w.key(593);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null && w.wants(594)) {
        w.key(594);
        w.boolean(v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null && w.wants(492)) {
        w.key(492);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null && w.wants(595)) {
        w.key(595);
        w.int32(v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null && w.wants(596)) {
        w.key(596);
        w.int32(v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null && w.wants(597)) {
        w.key(597);
        w.int32(v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null && w.wants(493)) {
        w.key(493);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null && w.wants(598)) {
        w.key(598);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null && w.wants(494)) {
        w.key(494);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null && w.wants(495)) {
        w.key(495);
        w.int32(v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null && w.wants(599)) {
        w.key(599);
        w.ref(v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null && w.wants(600)) {
        w.key(600);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null && w.wants(601)) {
        w.key(601);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null && w.wants(602)) {
        w.key(602);
        w.int32(v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null && w.wants(603)) {
        w.key(603);
        w.int32(v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null && w.wants(604)) {
        w.key(604);
        w.int32(v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null && w.wants(605)) {
        w.key(605);
        w.int32(v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null && w.wants(491)) {
        w.key(491);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null && w.wants(489)) {
        w.key(489);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(488, ascent);
    visit(583, averageCharWidth);
    visit(584, averageLineHeight);
    visit(585, boldFont);
    visit(586, boldItalicFont);
    visit(587, bulletsIndices);
    visit(490, descent);
    visit(588, fixedPitch);
    visit(589, hasLinks);
    visit(590, idleRunning);
    visit(591, italicFont);
    visit(592, lineCount);
    visit(593, lineSizes);
    visit(594, lineSpacingComputing);
    visit(492, lines);
    visit(595, linesInAverageLineHeight);
    visit(596, maxWidth);
    visit(597, maxWidthLineIndex);
    visit(493, ranges);
    visit(598, redrawLines);
    visit(494, regularFont);
    visit(495, styleCount);
    visit(599, styledText);
    visit(600, styles);
    visit(601, stylesSet);
    visit(602, stylesSetCount);
    visit(603, tabLength);
    visit(604, tabWidth);
    visit(605, topIndex);
    visit(491, lineSpacings);
    visit(489, bulletTexts);
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
      case 474: // height
        height = r.int32();
        return true;
      case 475: // width
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
      if (v_ != null && w.wants(474)) {
        w.key(474);
        w.int32(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null && w.wants(475)) {
        w.key(475);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(474, height);
    visit(475, width);
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
      case 477: // alignment
        alignment = r.int32();
        return true;
      case 478: // background
        background = r.color();
        return true;
      case 479: // flags
        flags = r.int32();
        return true;
      case 480: // indent
        indent = r.int32();
        return true;
      case 481: // justify
        justify = r.boolean();
        return true;
      case 482: // segments
        segments = r.ints();
        return true;
      case 483: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 484: // tabStops
        tabStops = r.ints();
        return true;
      case 485: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 486: // wrapIndent
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
      if (v_ != null && w.wants(477)) {
        w.key(477);
        w.int32(v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null && w.wants(478)) {
        w.key(478);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null && w.wants(479)) {
        w.key(479);
        w.int32(v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null && w.wants(480)) {
        w.key(480);
        w.int32(v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null && w.wants(481)) {
        w.key(481);
        w.boolean(v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null && w.wants(482)) {
        w.key(482);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null && w.wants(483)) {
        w.key(483);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null && w.wants(484)) {
        w.key(484);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null && w.wants(485)) {
        w.key(485);
        w.int32(v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null && w.wants(486)) {
        w.key(486);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(477, alignment);
    visit(478, background);
    visit(479, flags);
    visit(480, indent);
    visit(481, justify);
    visit(482, segments);
    visit(483, segmentsChars);
    visit(484, tabStops);
    visit(485, verticalIndent);
    visit(486, wrapIndent);
  }
}
