import 'dart:io';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/theme/theme_extensions/list_theme_extension.dart';
import './java_value_class.dart';
import './measure.dart' show defaultSizeOutputDir, fontWeightFromStyle, javaFontFamily;
import './measure_all.dart' show getThemes;

/// Generates dev/equo/swt/size/ListSizes.java and ListTheme.java from the List theme.
///
/// A List lays its items out at `listItemExtent` (list_theme_settings.dart): the theme's item
/// height, or one line of the item text plus the item's vertical padding when that is taller. Every
/// number in that rule is a theme declaration, so this reads the theme instead of rendering a case,
/// and Java computes the item height and the preferred width with the same numbers and the same
/// text style the items are drawn in.
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  writeListSizesFiles();
  exit(0);
}

void writeListSizesFiles({String outputDir = defaultSizeOutputDir}) {
  final themes = getThemes();
  final sizing = themes.first.themeFactory().extension<ListThemeExtension>()!;
  _writeSizes(sizing, outputDir);
  _writeTheme(
    {
      for (final t in themes)
        t.name: t.themeFactory().extension<ListThemeExtension>()!.textStyle,
    },
    outputDir,
  );
}

String _d(double v) => v.toString();

void _writeSizes(ListThemeExtension theme, String outputDir) {
  final buffer = StringBuffer()
    ..writeln('package dev.equo.swt.size;')
    ..writeln()
    ..writeln('import dev.equo.swt.Config;')
    ..writeln('import dev.equo.swt.FontMetricsUtil;')
    ..writeln('import org.eclipse.swt.SWT;')
    ..writeln('import org.eclipse.swt.graphics.Point;')
    ..writeln('import org.eclipse.swt.graphics.Rectangle;')
    ..writeln('import org.eclipse.swt.widgets.DartList;')
    ..writeln()
    ..writeln('/**')
    ..writeln(' * Item height and preferred size of a List, from the List theme.')
    ..writeln(' *')
    ..writeln(' * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_list.dart')
    ..writeln(' */')
    ..writeln('public class ListSizes {')
    ..writeln()
    ..writeln('    private static final double ROW_HEIGHT = ${_d(theme.itemHeight)};')
    ..writeln(
      '    private static final double ROW_PADDING_VERTICAL = ${_d(theme.itemPadding.vertical)};',
    )
    ..writeln(
      '    private static final double ITEM_PADDING_HORIZONTAL = ${_d(theme.itemPadding.horizontal)};',
    )
    ..writeln('    private static final int EMPTY_WIDTH = 100;')
    ..writeln('    private static final int MIN_EXTENT = 64;')
    ..writeln()
    ..writeln('    private ListSizes() {')
    ..writeln('    }')
    ..writeln()
    ..writeln(
      '    public static Point computeSize(DartList list, int wHint, int hHint, boolean changed) {',
    )
    ..writeln('        int width = wHint == SWT.DEFAULT ? getContentWidth(list) : wHint;')
    ..writeln('        if (width <= 0)')
    ..writeln('            width = MIN_EXTENT;')
    ..writeln(
      '        int height = hHint == SWT.DEFAULT ? list.getItemCount() * getItemHeight(list) : hHint;',
    )
    ..writeln('        if (height <= 0)')
    ..writeln('            height = MIN_EXTENT;')
    ..writeln('        Rectangle trim = list.computeTrim(0, 0, width, height);')
    ..writeln('        return new Point(trim.width, trim.height);')
    ..writeln('    }')
    ..writeln()
    ..writeln(
      '    /** One line of the item text and its padding, never less than the theme\'s item height. */',
    )
    ..writeln('    public static int getItemHeight(DartList list) {')
    ..writeln(
      '        double textHeight = FontMetricsUtil.getFontSize("Ag", textStyle(list)).y() + ROW_PADDING_VERTICAL;',
    )
    ..writeln('        return (int) Math.ceil(Math.max(textHeight, ROW_HEIGHT));')
    ..writeln('    }')
    ..writeln()
    ..writeln(
      '    /** The widest item and its padding; {@code EMPTY_WIDTH} when no item has text. */',
    )
    ..writeln('    private static int getContentWidth(DartList list) {')
    ..writeln('        TextStyle ts = textStyle(list);')
    ..writeln('        double widest = 0;')
    ..writeln('        for (String item : list.getItems())')
    ..writeln(
      '            widest = Math.max(widest, FontMetricsUtil.getFontSize(item, ts).x());',
    )
    ..writeln(
      '        return widest > 0 ? (int) Math.ceil(widest + ITEM_PADDING_HORIZONTAL) : EMPTY_WIDTH;',
    )
    ..writeln('    }')
    ..writeln()
    ..writeln('    private static TextStyle textStyle(DartList list) {')
    ..writeln(
      '        if (!Config.getConfigFlags().use_swt_fonts || list.getExplicitFont() == null)',
    )
    ..writeln('            return ListTheme.get().textStyle().withStyleFrom(list.getFont());')
    ..writeln('        return TextStyle.from(list.getFont());')
    ..writeln('    }')
    ..writeln('}');
  _write(outputDir, 'ListSizes.java', buffer);
}

String _javaTextStyle(TextStyle? style) {
  if (style == null) return 'TextStyle.def()';
  final family = javaFontFamily(style.fontFamily);
  final size = (style.fontSize ?? 14).round();
  final italic = style.fontStyle == FontStyle.italic;
  final weight = fontWeightFromStyle(style);
  final height = style.height;
  return height == null
      ? 'new TextStyle("$family", $size, $italic, $weight)'
      : 'new TextStyle("$family", $size, $italic, $weight, $height)';
}

void _writeTheme(Map<String, TextStyle?> byTheme, String outputDir) {
  final buffer = StringBuffer()
    ..writeln('package dev.equo.swt.size;')
    ..writeln()
    ..writeln('/** The text style List items are drawn in. Generated by measure_list.dart. */')
    ..write(javaValueClass('ListTheme', const [('TextStyle', 'textStyle')]))
    ..writeln('    public static ListTheme get() {')
    ..writeln('        return Themes.getTheme().list;')
    ..writeln('    }')
    ..writeln();
  byTheme.forEach((name, style) {
    buffer
      ..writeln('    public static ListTheme get${name}Theme() {')
      ..writeln('        return new ListTheme(${_javaTextStyle(style)});')
      ..writeln('    }')
      ..writeln();
  });
  buffer.writeln('}');
  _write(outputDir, 'ListTheme.java', buffer);
}

void _write(String outputDir, String name, StringBuffer buffer) {
  final file = File('$outputDir/$name');
  file.writeAsStringSync(buffer.toString());
  print('Generated: ${file.path}');
}
