// swt.autoScale picks the zoom SWT measures its UI in, and this side draws at the monitor's own
// zoom. Whenever the two differ the whole tree has to be scaled by the ratio, or half a mixed tree
// comes out one size and the native half another -- and anything SWT positions in screen
// coordinates, a context menu or a tooltip, lands off by that same factor.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  test('an unscaled run leaves the tree alone', () {
    expect(swtUiScaleFor(100, 100), 1.0);
  });

  test('the two zooms agreeing leaves the tree alone whatever they are', () {
    expect(swtUiScaleFor(150, 150), 1.0);
    expect(swtUiScaleFor(200, 200), 1.0);
  });

  test('autoScale=integer on a 150% monitor draws the tree smaller', () {
    // max((150 + 25) / 100 * 100, 100) == 100, so SWT measures at 100 while the monitor is at 150.
    expect(swtUiScaleFor(100, 150), closeTo(0.6667, 0.0001));
  });

  test('a UI zoom above the monitor draws the tree larger', () {
    expect(swtUiScaleFor(200, 100), 2.0);
  });

  test('a zoom that is not known yet leaves the tree alone', () {
    // Both directions: before the client has reported its monitor, and before Java has answered.
    expect(swtUiScaleFor(0, 150), 1.0);
    expect(swtUiScaleFor(150, 0), 1.0);
    expect(swtUiScaleFor(-1, 150), 1.0);
  });
}
