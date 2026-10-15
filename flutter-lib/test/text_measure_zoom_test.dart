// Every Text under SwtZoomScale paints through a textScaler of 1 / uiScale. A TextPainter that
// measures the same text to size a row or a column has to use that scaler too, or on a monitor
// whose zoom autoscale rounds down the box comes out shorter and narrower than the text in it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/table_evolve.dart';
import 'package:swtflutter/src/impl/utils/swt_zoom_scale.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _textKey = Key('text');
const _style = TextStyle(fontSize: 14);
const _sample = '0x50060000';

Future<Size> _paintedSizeAt(WidgetTester tester, double scale) async {
  appScaleNotifier.value = scale;
  await tester.pumpWidget(MaterialApp(
    builder: (context, child) => SwtZoomScale(child: child!),
    home: const Align(
      alignment: Alignment.topLeft,
      child: Text(_sample, key: _textKey, style: _style),
    ),
  ));
  return tester.getSize(find.byKey(_textKey));
}

void main() {
  tearDown(() => appScaleNotifier.value = 1.0);

  testWidgets('a packed column fits the text painted on a monitor autoscale rounds down',
      (tester) async {
    // A 125% monitor under the default autoScale: SWT lays out at 100, the tree is drawn at 0.8.
    final painted = await _paintedSizeAt(tester, 0.8);
    expect(measureTableText(_sample, _style), closeTo(painted.width, 0.01));
  });

  testWidgets('measuring is unchanged while the app is unzoomed', (tester) async {
    final painted = await _paintedSizeAt(tester, 1.0);
    expect(measureTableText(_sample, _style), closeTo(painted.width, 0.01));
  });
}
