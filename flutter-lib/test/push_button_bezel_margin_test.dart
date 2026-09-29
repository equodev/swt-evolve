// push_button_margin is a transparent margin on each side of a push or toggle button. An
// application that expects native frames wider than what the buttons draw overlaps adjacent frames;
// painting the surface inside the margin keeps those buttons apart instead of touching.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VRectangle _rect(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

Future<Rect> _surface(WidgetTester tester, int style) async {
  final button = VButton()
    ..id = 51
    ..style = style
    ..enabled = true
    ..text = 'Cherry-Pick'
    ..bounds = _rect(120, 26);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ButtonSwt<VButton>(value: button),
    ),
  ));
  await tester.pumpAndSettle();
  final frame = tester.getTopLeft(find.byType(ButtonSwt<VButton>));
  return tester
      .getRect(find.descendant(
        of: find.byType(ButtonSwt<VButton>),
        matching: find.byType(Material),
      ).last)
      .shift(-frame);
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  void margin(int px) => setConfigFlags(ConfigFlags()..push_button_margin = px);

  testWidgets('a push button paints its surface inside the margin',
      (WidgetTester tester) async {
    margin(6);

    expect(await _surface(tester, SWT.PUSH), const Rect.fromLTWH(6, 0, 108, 26));
  });

  testWidgets('a toggle button paints its surface inside the margin',
      (WidgetTester tester) async {
    margin(6);

    expect(await _surface(tester, SWT.TOGGLE), const Rect.fromLTWH(6, 0, 108, 26));
  });

  testWidgets('without a margin the surface fills the bounds',
      (WidgetTester tester) async {
    margin(0);

    expect(await _surface(tester, SWT.PUSH), const Rect.fromLTWH(0, 0, 120, 26));
  });

  testWidgets('a flat push button has no bezel to inset',
      (WidgetTester tester) async {
    margin(6);

    expect(await _surface(tester, SWT.PUSH | SWT.FLAT),
        const Rect.fromLTWH(0, 0, 120, 26));
  });
}
