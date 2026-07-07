// tool/generate_font_metrics.dart
import 'dart:io';
import 'dart:convert';
import 'package:flutter/widgets.dart';

import 'font_metrics_core.dart';

part 'all_fonts_macos.dart';
part 'all_fonts_linux.dart';
part 'all_fonts_windows.dart';

final Map<String, List<(String, FontStyle, FontWeight)>> AllFonts = {};

/// Run this as a Flutter app/script (desktop only — measures with the desktop Skia renderer).
/// Writes two files:
///   - build/font_metrics.json
///   - GenFontMetrics.java
Future<void> main(List<String> args) async {
  _add_fonts_macos(AllFonts);
  _add_fonts_linux(AllFonts);
  _add_fonts_windows(AllFonts);
  final os = args[0]!;
  final fonts = AllFonts[os]!;
  WidgetsFlutterBinding.ensureInitialized();

  final out = measureFonts(fonts);

  final jsonOut = JsonEncoder.withIndent('  ').convert(out);
  final jsonFile = File('./build/font_metrics.json');
  jsonFile.writeAsStringSync(jsonOut);
  print('Wrote ${jsonFile.path}');

  final javaSrc = generateJava(out);
  final javaFile = File(
    '../swt_native/src/$os/java/dev/equo/swt/GenFontMetrics.java',
  );
  javaFile.parent.createSync(recursive: true);
  javaFile.writeAsStringSync(javaSrc);
  print('Wrote ${javaFile.path}');
  exit(0);
}
