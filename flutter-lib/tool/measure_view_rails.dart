import 'dart:io';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/theme/theme_extensions/view_rails_theme_extension.dart';
import './measure.dart' show defaultSizeOutputDir;
import './measure_all.dart' show getThemes;

/// Generates dev/equo/swt/size/ViewRailsSizes.java from the view rails' theme.
///
/// A rail is as wide as the theme says for its size, whatever it holds, so this reads the theme
/// instead of rendering a case, and Java reserves the width the rail is drawn at.
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  writeViewRailsSizesFile();
  exit(0);
}

void writeViewRailsSizesFile({String outputDir = defaultSizeOutputDir}) {
  final theme = getThemes().first.themeFactory().extension<ViewRailsThemeExtension>()!;
  final widths = [for (var size = 0; size < theme.buttonSizes.length; size++) theme.railWidth(size).ceil()].join(', ');
  final buffer = StringBuffer()
    ..writeln('package dev.equo.swt.size;')
    ..writeln()
    ..writeln('/**')
    ..writeln(" * The view rails' widths, small, medium and large, from the view rails' theme.")
    ..writeln(' *')
    ..writeln(' * DO NOT EDIT MANUALLY - regenerate with flutter-lib/tool/measure_view_rails.dart')
    ..writeln(' */')
    ..writeln('public final class ViewRailsSizes {')
    ..writeln()
    ..writeln('    private static final int[] WIDTHS = {$widths};')
    ..writeln()
    ..writeln('    /** The height a rail asks for when it holds a view. */')
    ..writeln('    public static final int MIN_HEIGHT = ${theme.minHeight.ceil()};')
    ..writeln()
    ..writeln('    private ViewRailsSizes() {')
    ..writeln('    }')
    ..writeln()
    ..writeln('    /** The width of a rail of {@code size}: 0 small, 1 medium, 2 large. */')
    ..writeln('    public static int width(int size) {')
    ..writeln('        return WIDTHS[size];')
    ..writeln('    }')
    ..writeln('}');
  final file = File('$outputDir/ViewRailsSizes.java');
  file.writeAsStringSync(buffer.toString());
  print('Generated: ${file.path}');
}
