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
      case 4992: // ascent
        ascent = r.int32();
        return true;
      case 4993: // averageCharWidth
        averageCharWidth = r.int32();
        return true;
      case 4994: // averageLineHeight
        averageLineHeight = r.float32();
        return true;
      case 4995: // boldFont
        boldFont = r.object(VFont.new);
        return true;
      case 4996: // boldItalicFont
        boldItalicFont = r.object(VFont.new);
        return true;
      case 4998: // bulletsIndices
        bulletsIndices = r.ints();
        return true;
      case 4999: // descent
        descent = r.int32();
        return true;
      case 5000: // fixedPitch
        fixedPitch = r.boolean();
        return true;
      case 5001: // hasLinks
        hasLinks = r.boolean();
        return true;
      case 5002: // idleRunning
        idleRunning = r.boolean();
        return true;
      case 5003: // italicFont
        italicFont = r.object(VFont.new);
        return true;
      case 5004: // lineCount
        lineCount = r.int32();
        return true;
      case 5005: // lineSizes
        lineSizes = r.objects(VLineSizeInfo.new);
        return true;
      case 5006: // lineSpacingComputing
        lineSpacingComputing = r.boolean();
        return true;
      case 5008: // lines
        lines = r.objects(VLineInfo.new);
        return true;
      case 5009: // linesInAverageLineHeight
        linesInAverageLineHeight = r.int32();
        return true;
      case 5010: // maxWidth
        maxWidth = r.int32();
        return true;
      case 5011: // maxWidthLineIndex
        maxWidthLineIndex = r.int32();
        return true;
      case 5012: // ranges
        ranges = r.ints();
        return true;
      case 5013: // redrawLines
        redrawLines = r.ints();
        return true;
      case 5014: // regularFont
        regularFont = r.object(VFont.new);
        return true;
      case 5015: // styleCount
        styleCount = r.int32();
        return true;
      case 5016: // styledText
        styledText = r.ref<VStyledText>();
        return true;
      case 5017: // styles
        styles = r.objects(VStyleRange.new)?.whereType<VStyleRange>().toList();
        return true;
      case 5018: // stylesSet
        stylesSet = r
            .objects(VStyleRange.new)
            ?.whereType<VStyleRange>()
            .toList();
        return true;
      case 5019: // stylesSetCount
        stylesSetCount = r.int32();
        return true;
      case 5020: // tabLength
        tabLength = r.int32();
        return true;
      case 5021: // tabWidth
        tabWidth = r.int32();
        return true;
      case 5022: // topIndex
        topIndex = r.int32();
        return true;
      case 5007: // lineSpacings
        lineSpacings = r.ints();
        return true;
      case 4997: // bulletTexts
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
        w.keyInt32(4992, v_);
      }
    }
    {
      final v_ = averageCharWidth;
      if (v_ != null) {
        w.keyInt32(4993, v_);
      }
    }
    {
      final v_ = averageLineHeight;
      if (v_ != null) {
        w.key(4994);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = boldFont;
      if (v_ != null) {
        w.key(4995);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = boldItalicFont;
      if (v_ != null) {
        w.key(4996);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = bulletsIndices;
      if (v_ != null) {
        w.key(4998);
        w.ints(v_);
      }
    }
    {
      final v_ = descent;
      if (v_ != null) {
        w.keyInt32(4999, v_);
      }
    }
    {
      final v_ = fixedPitch;
      if (v_ != null) {
        w.keyBoolean(5000, v_);
      }
    }
    {
      final v_ = hasLinks;
      if (v_ != null) {
        w.keyBoolean(5001, v_);
      }
    }
    {
      final v_ = idleRunning;
      if (v_ != null) {
        w.keyBoolean(5002, v_);
      }
    }
    {
      final v_ = italicFont;
      if (v_ != null) {
        w.key(5003);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = lineCount;
      if (v_ != null) {
        w.keyInt32(5004, v_);
      }
    }
    {
      final v_ = lineSizes;
      if (v_ != null) {
        w.key(5005);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = lineSpacingComputing;
      if (v_ != null) {
        w.keyBoolean(5006, v_);
      }
    }
    {
      final v_ = lines;
      if (v_ != null) {
        w.key(5008);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = linesInAverageLineHeight;
      if (v_ != null) {
        w.keyInt32(5009, v_);
      }
    }
    {
      final v_ = maxWidth;
      if (v_ != null) {
        w.keyInt32(5010, v_);
      }
    }
    {
      final v_ = maxWidthLineIndex;
      if (v_ != null) {
        w.keyInt32(5011, v_);
      }
    }
    {
      final v_ = ranges;
      if (v_ != null) {
        w.key(5012);
        w.ints(v_);
      }
    }
    {
      final v_ = redrawLines;
      if (v_ != null) {
        w.key(5013);
        w.ints(v_);
      }
    }
    {
      final v_ = regularFont;
      if (v_ != null) {
        w.key(5014);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = styleCount;
      if (v_ != null) {
        w.keyInt32(5015, v_);
      }
    }
    {
      final v_ = styledText;
      if (v_ != null) {
        w.keyRef(5016, v_.id);
      }
    }
    {
      final v_ = styles;
      if (v_ != null) {
        w.key(5017);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSet;
      if (v_ != null) {
        w.key(5018);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
    {
      final v_ = stylesSetCount;
      if (v_ != null) {
        w.keyInt32(5019, v_);
      }
    }
    {
      final v_ = tabLength;
      if (v_ != null) {
        w.keyInt32(5020, v_);
      }
    }
    {
      final v_ = tabWidth;
      if (v_ != null) {
        w.keyInt32(5021, v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(5022, v_);
      }
    }
    {
      final v_ = lineSpacings;
      if (v_ != null) {
        w.key(5007);
        w.ints(v_);
      }
    }
    {
      final v_ = bulletTexts;
      if (v_ != null) {
        w.key(4997);
        w.strings(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(4992, ascent);
    visit(4993, averageCharWidth);
    visit(4994, averageLineHeight);
    visit(4995, boldFont);
    visit(4996, boldItalicFont);
    visit(4998, bulletsIndices);
    visit(4999, descent);
    visit(5000, fixedPitch);
    visit(5001, hasLinks);
    visit(5002, idleRunning);
    visit(5003, italicFont);
    visit(5004, lineCount);
    visit(5005, lineSizes);
    visit(5006, lineSpacingComputing);
    visit(5008, lines);
    visit(5009, linesInAverageLineHeight);
    visit(5010, maxWidth);
    visit(5011, maxWidthLineIndex);
    visit(5012, ranges);
    visit(5013, redrawLines);
    visit(5014, regularFont);
    visit(5015, styleCount);
    visit(5016, styledText);
    visit(5017, styles);
    visit(5018, stylesSet);
    visit(5019, stylesSetCount);
    visit(5020, tabLength);
    visit(5021, tabWidth);
    visit(5022, topIndex);
    visit(5007, lineSpacings);
    visit(4997, bulletTexts);
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
      case 3072: // height
        height = r.int32();
        return true;
      case 3073: // width
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
        w.keyInt32(3072, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(3073, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(3072, height);
    visit(3073, width);
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
      case 3008: // alignment
        alignment = r.int32();
        return true;
      case 3009: // background
        background = r.color();
        return true;
      case 3010: // flags
        flags = r.int32();
        return true;
      case 3011: // indent
        indent = r.int32();
        return true;
      case 3012: // justify
        justify = r.boolean();
        return true;
      case 3013: // segments
        segments = r.ints();
        return true;
      case 3014: // segmentsChars
        segmentsChars = r.chars();
        return true;
      case 3015: // tabStops
        tabStops = r.ints();
        return true;
      case 3016: // verticalIndent
        verticalIndent = r.int32();
        return true;
      case 3017: // wrapIndent
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
        w.keyInt32(3008, v_);
      }
    }
    {
      final v_ = background;
      if (v_ != null) {
        w.key(3009);
        w.colorValue(v_);
      }
    }
    {
      final v_ = flags;
      if (v_ != null) {
        w.keyInt32(3010, v_);
      }
    }
    {
      final v_ = indent;
      if (v_ != null) {
        w.keyInt32(3011, v_);
      }
    }
    {
      final v_ = justify;
      if (v_ != null) {
        w.keyBoolean(3012, v_);
      }
    }
    {
      final v_ = segments;
      if (v_ != null) {
        w.key(3013);
        w.ints(v_);
      }
    }
    {
      final v_ = segmentsChars;
      if (v_ != null) {
        w.key(3014);
        w.chars(v_);
      }
    }
    {
      final v_ = tabStops;
      if (v_ != null) {
        w.key(3015);
        w.ints(v_);
      }
    }
    {
      final v_ = verticalIndent;
      if (v_ != null) {
        w.keyInt32(3016, v_);
      }
    }
    {
      final v_ = wrapIndent;
      if (v_ != null) {
        w.keyInt32(3017, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(3008, alignment);
    visit(3009, background);
    visit(3010, flags);
    visit(3011, indent);
    visit(3012, justify);
    visit(3013, segments);
    visit(3014, segmentsChars);
    visit(3015, tabStops);
    visit(3016, verticalIndent);
    visit(3017, wrapIndent);
  }
}
