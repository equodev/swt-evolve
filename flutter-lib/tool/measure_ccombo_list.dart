import 'dart:io';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/theme/theme_extensions/ccombo_theme_extension.dart';
import './measure.dart' show defaultSizeOutputDir;
import './measure_all.dart' show getThemes;

/// Generates dev/equo/swt/size/CComboListSizes.java from the CCombo theme.
///
/// A CCombo's list lays its items out at `ccomboItemExtent` (ccombo_theme_settings.dart): the
/// theme's item height, or one line of the item text plus the item's vertical padding when that is
/// taller. Every number in that rule is a theme declaration, so this reads the theme instead of
/// rendering a case, and Java answers `getItemHeight()` with the same numbers and the same text
/// style the items are drawn in.
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  writeCComboListSizesFile();
  exit(0);
}

void writeCComboListSizesFile({String outputDir = defaultSizeOutputDir}) {
  final theme = getThemes().first.themeFactory().extension<CComboThemeExtension>()!;
  final buffer = StringBuffer()
    ..writeln('package dev.equo.swt.size;')
    ..writeln()
    ..writeln('import dev.equo.swt.Config;')
    ..writeln('import dev.equo.swt.FontMetricsUtil;')
    ..writeln('import org.eclipse.swt.custom.DartCCombo;')
    ..writeln()
    ..writeln('/**')
    ..writeln(" * The height of a row of a CCombo's list, from the CCombo theme.")
    ..writeln(' *')
    ..writeln(' * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_ccombo_list.dart')
    ..writeln(' */')
    ..writeln('public class CComboListSizes {')
    ..writeln()
    ..writeln('    static final double ITEM_HEIGHT = ${theme.itemHeight};')
    ..writeln('    static final double ITEM_PADDING_VERTICAL = ${theme.textFieldPadding.vertical};')
    ..writeln()
    ..writeln("    /** The theme's item height, or one line of the item text plus its vertical padding when that is taller. */")
    ..writeln('    public static int getItemHeight(DartCCombo combo) {')
    ..writeln('        double line = FontMetricsUtil.getFontSize("Ag", textStyle(combo)).y() + ITEM_PADDING_VERTICAL;')
    ..writeln('        return (int) Math.ceil(Math.max(line, ITEM_HEIGHT));')
    ..writeln('    }')
    ..writeln()
    ..writeln('    private static TextStyle textStyle(DartCCombo combo) {')
    ..writeln('        if (!Config.getConfigFlags().use_swt_fonts || combo.getExplicitFont() == null)')
    ..writeln('            return CComboTheme.get().textStyle().withStyleFrom(combo.getFont());')
    ..writeln('        return TextStyle.from(combo.getFont());')
    ..writeln('    }')
    ..writeln('}');
  final file = File('$outputDir/CComboListSizes.java');
  file.writeAsStringSync(buffer.toString());
  print('Generated: ${file.path}');
}
