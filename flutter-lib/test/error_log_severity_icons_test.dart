// The Error Log view (org.eclipse.ui.views.log) marks each entry with one image per severity, named
// error_st_obj, warning_st_obj, info_st_obj and ok_st_obj. With the icon pack on, each is drawn as
// the bundled SVG of that name, so two severities must not share a glyph.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/assets_manager.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _severities = ['error_st_obj', 'warning_st_obj', 'info_st_obj', 'ok_st_obj'];

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    AssetsManager.resetForTesting();
    applyConfigFlags(ConfigFlags());
  });

  test('each Error Log severity is drawn with a glyph of its own', () async {
    final glyphs = <String, Object?>{
      for (final name in _severities) name: await AssetsManager.loadReplacement(name),
    };
    for (final name in _severities) {
      expect(glyphs[name], isA<String>(), reason: '$name has no bundled SVG');
    }
    for (var i = 0; i < _severities.length; i++) {
      for (var j = i + 1; j < _severities.length; j++) {
        expect(glyphs[_severities[i]], isNot(glyphs[_severities[j]]),
            reason: '${_severities[i]} is drawn as ${_severities[j]}');
      }
    }
  });
}
