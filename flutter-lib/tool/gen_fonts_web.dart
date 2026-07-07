// tool/gen_fonts_web.dart
//
// Web counterpart of tool/gen_fonts.dart. Measures the bundled Liberation/Inter fonts with *real*
// CanvasKit by running under `flutter run -d web-server` in a browser, instead of the desktop Skia
// renderer `gen_fonts.dart` uses. Two things `gen_fonts.dart` relies on aren't available for a web
// compile target, so this entrypoint avoids them entirely:
//   - dart:io (File writes, exit()) — not part of the web SDK.
//   - `flutter run`'s `--dart-entrypoint-args` — not reliably delivered under the `-d chrome` /
//     `-d web-server` devices, so this entrypoint takes no args and always measures the fixed
//     `_add_fonts_webMain` list (there is nothing else it would ever need to measure on web).
//
// Output: the generated Java source is printed to stdout, wrapped in BEGIN/END markers. The Kotlin
// task driving this (see swtgenerator/build.gradle.kts's `generateFontsWeb`) launches this via
// `flutter run -d web-server`, attaches a headless Chrome instance (which `flutter run` bridges via
// DWDS), reads this process's stdout for the markers, merges the enclosed source into the
// already-checked-in `GenFontMetrics.java`, then kills both processes — since there's no `exit()`
// on web to let this script terminate itself.
import 'package:flutter/widgets.dart';

import 'font_metrics_core.dart';
import 'all_fonts_web.dart';

const String beginMarker = '===EQUO_FONT_METRICS_BEGIN===';
const String endMarker = '===EQUO_FONT_METRICS_END===';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  final fonts = <String, List<(String, FontStyle, FontWeight)>>{};
  addFontsWebMain(fonts);

  final out = measureFonts(fonts['webMain']!);
  // CanvasKit's fallback for the unregistered 'System' family is measured under that name (to
  // match production exactly), but must be stored under its own key — "System-*" already belongs
  // to the desktop-measured entries in GenFontMetrics.DATA (a different renderer's fallback
  // typeface), and merging would silently overwrite them.
  final renamed = <String, dynamic>{
    for (final entry in out.entries)
      (entry.key.startsWith('System-')
              ? 'System Web-${entry.key.substring('System-'.length)}'
              : entry.key):
          entry.value,
  };
  final javaSrc = generateJava(renamed);

  print(beginMarker);
  print(javaSrc);
  print(endMarker);
}
