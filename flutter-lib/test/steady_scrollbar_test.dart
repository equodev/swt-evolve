// A scroll bar shown at all times is there in full from the first frames, as a native one is, and
// looks as Material's does once Material's has faded in.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/steady_scrollbar.dart';

Widget _host(Brightness brightness, Widget Function(ScrollController controller, Widget child) bar) {
  final controller = ScrollController();
  return MaterialApp(
    theme: ThemeData(useMaterial3: true, brightness: brightness, platform: TargetPlatform.macOS),
    home: Center(
      child: SizedBox(
        width: 300,
        height: 200,
        child: bar(
          controller,
          SingleChildScrollView(controller: controller, child: const SizedBox(height: 2000)),
        ),
      ),
    ),
  );
}

ScrollbarPainter _painter(WidgetTester tester) => tester
    .widgetList<CustomPaint>(find.byType(CustomPaint))
    .map((p) => p.foregroundPainter)
    .whereType<ScrollbarPainter>()
    .first;

void main() {
  for (final brightness in Brightness.values) {
    testWidgets('an always-shown thumb is opaque from the first frames ($brightness)', (tester) async {
      await tester.pumpWidget(_host(
          brightness, (c, child) => SteadyScrollbar(controller: c, thumbVisibility: true, child: child)));
      await tester.pump();

      expect(_painter(tester).fadeoutOpacityAnimation.value, 1.0);
    });

    testWidgets('it is drawn in the color Material settles on ($brightness)', (tester) async {
      await tester.pumpWidget(
          _host(brightness, (c, child) => Scrollbar(controller: c, thumbVisibility: true, child: child)));
      await tester.pump();
      expect(_painter(tester).fadeoutOpacityAnimation.value, lessThan(1.0),
          reason: "Material's own fades in: without that this test proves nothing");
      await tester.pumpAndSettle();
      final material = _painter(tester);
      final settledColor = material.color;
      final settledThickness = material.thickness;

      await tester.pumpWidget(_host(
          brightness, (c, child) => SteadyScrollbar(controller: c, thumbVisibility: true, child: child)));
      await tester.pump();

      expect(_painter(tester).color, settledColor);
      expect(_painter(tester).thickness, settledThickness);
    });
  }

  testWidgets('a scroll bar shown only while scrolling still fades', (tester) async {
    await tester.pumpWidget(_host(Brightness.light, (c, child) => SteadyScrollbar(controller: c, child: child)));
    await tester.pump();

    expect(_painter(tester).fadeoutOpacityAnimation.value, 0.0);
  });
}
