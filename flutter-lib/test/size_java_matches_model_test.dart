import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

import '../tool/emit_sizes.dart';
import '../tool/measure.dart' show defaultSizeOutputDir, sizesModelPath;

void main() {
  test('the committed size/*.java is what the emitters produce from the model', () {
    expect(
      File(sizesModelPath).existsSync(),
      isTrue,
      reason:
          '$sizesModelPath not found. This test runs from the flutter-lib package root, '
          'which is where `flutter test` and the measurement tool both start.',
    );

    final committedDir = Directory(defaultSizeOutputDir);
    final tmp = Directory.systemTemp.createTempSync('swt-size-emit-');
    try {
      final emitted = emitSizes(outputDir: tmp.path);
      expect(
        emitted,
        isNotEmpty,
        reason: 'the emitters wrote nothing — is the model empty?',
      );

      final mismatched = <String>[];
      final missing = <String>[];

      for (final name in emitted) {
        final committed = File('${committedDir.path}/$name');
        if (!committed.existsSync()) {
          missing.add(name);
          continue;
        }
        if (_text(File('${tmp.path}/$name')) != _text(committed)) {
          mismatched.add(name);
        }
      }

      expect(
        missing,
        isEmpty,
        reason:
            'the emitters produced files that are not committed: ${missing.join(', ')}. '
            'Run the measurement tool and commit what it writes.',
      );
      expect(
        mismatched,
        isEmpty,
        reason:
            'these committed files are not what the emitters produce from '
            '$sizesModelPath: ${mismatched.join(', ')}.\n'
            'Either the .java was hand-edited — put the change in the emitter in '
            'flutter-lib/tool/measure.dart instead — or an emitter changed without the '
            'measurement being re-run. Re-run `measure_all.dart` (plus measure_coolbar.dart and '
            'measure_ctabfolder.dart) and commit the Java and the model together.',
      );
    } finally {
      tmp.deleteSync(recursive: true);
    }
  });
}

/// A file's text with the line ending normalized away, so a checkout that rewrote them is not
/// read as a generator change.
String _text(File f) => f.readAsStringSync().replaceAll('\r\n', '\n');
