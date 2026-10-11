// A Canvas draws its Caret: a bar at the caret's bounds in the canvas's foreground colour, while the
// caret is visible and its display's focus caret, blinking at its blink rate.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/caret.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/canvas_theme_extension.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCaret _caret({bool visible = true, bool focused = true, int blinkRate = 500}) => VCaret()
  ..swt = 'Caret'
  ..id = 21
  ..style = SWT.NONE
  ..bounds = _rect(30, 12, 2, 18)
  ..visible = visible
  ..focused = focused
  ..blinkRate = blinkRate;

VCanvas _canvas(VCaret? caret) => VCanvas()
  ..swt = 'Canvas'
  ..id = 20
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 100)
  ..caret = caret;

Future<void> _pump(WidgetTester tester, VCaret? caret) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 100,
      child: CanvasSwt<VCanvas>(value: _canvas(caret)),
    ),
  ));
  await tester.pump();
}

Finder _bar() => find.descendant(
      of: find.byWidgetPredicate((w) => w is CaretSwt),
      matching: find.byType(ColoredBox),
    );

/// Disposes the tree, and with it the blink timer.
Future<void> _unmount(WidgetTester tester) => tester.pumpWidget(const SizedBox());

void main() {
  testWidgets('a visible focus caret is drawn at its bounds in the foreground colour', (tester) async {
    await _pump(tester, _caret());

    expect(_bar(), findsOneWidget);
    final canvasOrigin = tester.getTopLeft(find.byWidgetPredicate((w) => w is CanvasSwt));
    expect(tester.getRect(_bar()), Rect.fromLTWH(canvasOrigin.dx + 30, canvasOrigin.dy + 12, 2, 18));
    final context = tester.element(_bar());
    final foreground = Theme.of(context).extension<CanvasThemeExtension>()!.foregroundColor;
    expect(tester.widget<ColoredBox>(_bar()).color, foreground);
    await _unmount(tester);
  });

  testWidgets('a caret that is not the focus caret is not drawn', (tester) async {
    await _pump(tester, _caret(focused: false));
    expect(find.byWidgetPredicate((w) => w is CaretSwt), findsOneWidget);
    expect(_bar(), findsNothing);
    await _unmount(tester);
  });

  testWidgets('a hidden caret is not drawn', (tester) async {
    await _pump(tester, _caret(visible: false));
    expect(_bar(), findsNothing);
    await _unmount(tester);
  });

  testWidgets('the caret blinks at its blink rate', (tester) async {
    await _pump(tester, _caret(blinkRate: 500));
    expect(_bar(), findsOneWidget);
    await tester.pump(const Duration(milliseconds: 500));
    expect(_bar(), findsNothing);
    await tester.pump(const Duration(milliseconds: 500));
    expect(_bar(), findsOneWidget);
    await _unmount(tester);
  });

  testWidgets('a blink rate of zero keeps the caret drawn', (tester) async {
    await _pump(tester, _caret(blinkRate: 0));
    await tester.pump(const Duration(seconds: 2));
    expect(_bar(), findsOneWidget);
    await _unmount(tester);
  });

  testWidgets('a canvas without a caret draws none', (tester) async {
    await _pump(tester, null);
    expect(find.byWidgetPredicate((w) => w is CaretSwt), findsNothing);
    await _unmount(tester);
  });
}
