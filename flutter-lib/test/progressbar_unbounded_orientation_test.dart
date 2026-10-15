// A progress bar the layout has not sized yet takes its natural size from the theme: wide and short when
// horizontal, tall and narrow when vertical. Java's computeSize is generated from this measurement.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/progressbar.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// Each bar gets its own id: the widget registry keeps the state of an id it has seen.
int _nextId = 1;

/// The size a bar is drawn at: its natural one, or [bounds] once the layout has given it some.
Future<Size> _sizeOf(WidgetTester tester, int style, {Size? bounds}) async {
  final bar = VProgressBar()
    ..id = _nextId++
    ..style = style
    ..enabled = true;
  if (bounds != null) {
    bar.bounds = VRectangle()
      ..x = 0
      ..y = 0
      ..width = bounds.width.toInt()
      ..height = bounds.height.toInt();
  }
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ProgressBarSwt<VProgressBar>(value: bar),
    ),
  ));
  await tester.pump();
  return tester.getSize(find.byType(ProgressBarSwt<VProgressBar>));
}

void main() {
  testWidgets('a horizontal bar is wider than tall', (WidgetTester tester) async {
    final size = await _sizeOf(tester, SWT.HORIZONTAL);

    expect(size.width, greaterThan(size.height));
  });

  testWidgets('a vertical bar is the horizontal one turned', (WidgetTester tester) async {
    final horizontal = await _sizeOf(tester, SWT.HORIZONTAL);
    final vertical = await _sizeOf(tester, SWT.VERTICAL);

    expect(vertical, Size(horizontal.height, horizontal.width));
  });

  testWidgets('indeterminate and smooth bars keep the size of their orientation',
      (WidgetTester tester) async {
    final vertical = await _sizeOf(tester, SWT.VERTICAL);

    expect(await _sizeOf(tester, SWT.VERTICAL | SWT.SMOOTH), vertical);
    expect(await _sizeOf(tester, SWT.VERTICAL | SWT.INDETERMINATE), vertical);
  });

  testWidgets('a bar with bounds fills them in either orientation', (WidgetTester tester) async {
    expect(await _sizeOf(tester, SWT.HORIZONTAL, bounds: const Size(150, 18)), const Size(150, 18));
    expect(await _sizeOf(tester, SWT.VERTICAL, bounds: const Size(18, 150)), const Size(18, 150));
  });
}
