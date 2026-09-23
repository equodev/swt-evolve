import 'dart:io';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/theme/named_themes.dart';

/// Generates dev/equo/swt/size/ThemeScales.java, the theme-name -> type-scale lookup the Java size
/// model needs.
///
/// Java picks the mirrored sizes by scale, but the application selects a *theme*
/// (`swt.evolve.theme_name`), and which scale a theme draws in is declared in `kNamedThemes` — on
/// this side. Rendering that map into Java is what keeps the two from disagreeing: there is one
/// declaration, and this is it compiled for the other side. A theme absent from the table falls
/// back to the shared scale, which is also what a theme that declares no scale gets.
///
/// Like measure_canvas.dart, and unlike the measured widgets, there is nothing to render and no
/// TextStyle to sample: the mapping is already declared, so this reads it and writes the file.
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  writeThemeScalesFile();
  exit(0);
}

const String _defaultScaleName = 'NonDefault';

void writeThemeScalesFile() {
  final buffer = StringBuffer();
  buffer.writeln('package dev.equo.swt.size;');
  buffer.writeln();
  buffer.writeln('/**');
  buffer.writeln(' * Which type scale each named theme draws in.');
  buffer.writeln(' *');
  buffer.writeln(
    ' * Generated from kNamedThemes by measure_theme_scales.dart. DO NOT EDIT MANUALLY.',
  );
  buffer.writeln(' */');
  buffer.writeln('public final class ThemeScales {');
  buffer.writeln();
  buffer.writeln('    private ThemeScales() {');
  buffer.writeln('    }');
  buffer.writeln();
  buffer.writeln('    public static Themes.Theme forThemeName(String themeName) {');
  buffer.writeln('        if (themeName == null) return Themes.Theme.$_defaultScaleName;');
  buffer.writeln('        switch (themeName.trim()) {');

  // Group the themes by the scale they draw in, so the table reads as "these share this scale"
  // rather than one line per theme.
  final byScale = <String, List<String>>{};
  kNamedThemes.forEach((themeName, theme) {
    final scale = theme.typeScale?.name ?? _defaultScaleName;
    byScale.putIfAbsent(scale, () => []).add(themeName);
  });
  final scales = byScale.keys.toList()..sort();
  for (final scale in scales) {
    if (scale == _defaultScaleName) continue; // the default arm covers these
    final names = byScale[scale]!..sort();
    for (final name in names) {
      buffer.writeln('            case "$name":');
    }
    buffer.writeln('                return Themes.Theme.$scale;');
  }
  buffer.writeln('            default:');
  buffer.writeln('                return Themes.Theme.$_defaultScaleName;');
  buffer.writeln('        }');
  buffer.writeln('    }');
  buffer.writeln('}');

  final file = File(
    '../swt_native/src/main/java/dev/equo/swt/size/ThemeScales.java',
  );
  file.writeAsStringSync(buffer.toString());
  print('Generated: ${file.path}');
}
