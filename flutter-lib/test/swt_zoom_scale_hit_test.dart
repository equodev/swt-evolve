// SwtZoomScale lays the app out in SWT's units and paints it at `swtUiScale()`. The box that
// widens the constraints sits below the Transform, so the pointer reaches it already mapped into
// the laid-out space while its own size is still the view's -- and RenderBox.hitTest drops
// everything past that. Painted and unclickable, from `view * scale` rightwards.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/swt_zoom_scale.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  // A HiDPI monitor (zoom 160) against SWT's zoom 100: what a 1.6 dpr screen gives.
  const double scale = 100 / 160;
  const Size view = Size(1405, 1203);
  final Size laidOut = view / scale; // 2248 x 1925

  tearDown(() => appScaleNotifier.value = 1.0);

  Future<void> pumpAt(WidgetTester tester, Offset buttonTopLeft,
      {required List<String> taps}) async {
    appScaleNotifier.value = scale;
    await tester.binding.setSurfaceSize(view);
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(MaterialApp(
      home: SwtZoomScale(
        child: Stack(
          children: [
            Positioned(
              left: buttonTopLeft.dx,
              top: buttonTopLeft.dy,
              child: GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: () => taps.add('tap'),
                child: const SizedBox(width: 100, height: 26),
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();
  }

  testWidgets('a control near the origin takes its tap', (tester) async {
    final taps = <String>[];
    await pumpAt(tester, const Offset(50, 50), taps: taps);

    await tester.tapAt(const Offset(50, 50) * scale + const Offset(10, 10));
    await tester.pump();

    expect(taps, isNotEmpty, reason: 'sanity: the near side is clickable');
  });

  testWidgets('a control past the view width also takes its tap', (tester) async {
    final taps = <String>[];
    // Past `view.width` in the laid-out space, and so well inside the window once painted at
    // `scale` -- the range a control stops taking its tap in.
    final at = Offset(laidOut.width - 200, 100);
    await pumpAt(tester, at, taps: taps);

    final onScreen = at * scale + const Offset(10, 10);
    expect(onScreen.dx, lessThan(view.width),
        reason: 'sanity: it is painted inside the window');

    await tester.tapAt(onScreen);
    await tester.pump();

    expect(taps, isNotEmpty,
        reason: 'painted inside the window, so it must be clickable');
  });
}
