// A point is worth fewer logical pixels while the app is zoomed: `swt.autoScale` grows boxes and
// leaves glyphs alone, and the transform every painted scene sits under would otherwise grow them
// with everything else. Java divides its own text measurements by the same factor, so a GC's text
// lands where Java said it would.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/font_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  tearDown(() {
    appScaleNotifier.value = 1.0;
    resetConfigFlags();
  });

  test('a point keeps the host DPI while the app is unzoomed', () {
    setConfigFlags(ConfigFlags()..font_point_scale = 96 / 72);
    expect(FontUtils.pointScale, closeTo(96 / 72, 1e-9));
  });

  test('a zoomed app shrinks a point by the factor it is magnified by', () {
    setConfigFlags(ConfigFlags()..font_point_scale = 96 / 72);
    appScaleNotifier.value = 1.5;
    expect(FontUtils.pointScale, closeTo(96 / 72 / 1.5, 1e-9));
  });

  test('painted text with no font of its own comes down by the zoom too', () {
    // A GC that never had a font set still paints, at a fallback size in logical pixels -- left
    // alone it is the one piece of text the zoom would still grow.
    appScaleNotifier.value = 2.0;
    final painted =
        FontUtils.textStyleFromVFont(null, null, applyDpiScaling: true);
    final laidOut = FontUtils.textStyleFromVFont(null, null);

    expect(painted.fontSize, 6.0);
    expect(laidOut.fontSize, 12.0);
  });
}
