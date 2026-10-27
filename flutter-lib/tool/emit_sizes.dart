import 'dart:convert';
import 'dart:io';

import './measure.dart';
import './measure_canvas.dart' as canvas;
import './measure_theme_scales.dart' as theme_scales;

/// Re-runs the Java emitters alone, against the analysis a measurement run committed to
/// [sizesModelPath].
///
/// The measurement half of `measure_all.dart` needs a real window, real fonts and a GPU, and a
/// machine without one would read different numbers even if it could run it. The emitters need
/// neither — so they run here from committed data, which is what lets CI check that the Java under
/// `dev/equo/swt/size` is still what the emitters produce (see
/// `test/size_java_matches_model_test.dart`).
///
/// `main()` writes over the source tree, the same as a measurement run; the check passes its own
/// [outputDir] instead.
void main(List<String> args) {
  final written = emitSizes(
    outputDir: args.isNotEmpty ? args.first : defaultSizeOutputDir,
  );
  print('Emitted ${written.length} files');
  exit(0);
}

/// Emits every size/theme file the committed model covers into [outputDir], and returns their
/// names.
List<String> emitSizes({
  String outputDir = defaultSizeOutputDir,
  String modelPath = sizesModelPath,
}) {
  final dir = Directory(outputDir);
  dir.createSync(recursive: true);

  // Both of these read a declaration rather than a measurement, so they need no model entry.
  canvas.writeCanvasThemeFile(outputDir: outputDir);
  theme_scales.writeThemeScalesFile(outputDir: outputDir);

  final model =
      jsonDecode(File(modelPath).readAsStringSync()) as Map<String, dynamic>;
  (WidgetMeasurer()..outputDir = outputDir).emitFromSizesModel(model);

  return dir
      .listSync()
      .whereType<File>()
      .map((f) => f.uri.pathSegments.last)
      .toList()
    ..sort();
}
