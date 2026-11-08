// A Canvas's scroll bar is not its client area: pressing it takes no focus, as on every native,
// so the Canvas is neither focused nor activated by it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/swt.dart';

VCanvas _canvas() => VCanvas()
  ..swt = 'Canvas'
  ..id = 7
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 200)
  ..verticalBar = (VScrollBar()
    ..swt = 'ScrollBar'
    ..id = 11
    ..style = SWT.VERTICAL
    ..visible = true
    ..enabled = true
    ..minimum = 0
    ..maximum = 100
    ..thumb = 10
    ..selection = 40);

Finder get _bar => find.byWidgetPredicate(
    (w) => w is CustomPaint && w.painter.runtimeType.toString() == '_ScrollBarPainter');

Finder get _canvasFinder => find.byType(CanvasSwt<VCanvas>);

Future<void> _pump(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 200, height: 200, child: CanvasSwt<VCanvas>(value: _canvas())),
    ),
  ));
  await tester.pump();
}

/// Whether the primary focus is the Canvas's or one of its descendants'.
bool _canvasFocused(WidgetTester tester) {
  final focused = FocusManager.instance.primaryFocus?.context;
  if (focused == null) return false;
  final canvas = tester.element(_canvasFinder);
  var inside = false;
  focused.visitAncestorElements((e) {
    if (e == canvas) inside = true;
    return !inside;
  });
  return inside;
}

void main() {
  testWidgets('a press on the client area focuses the Canvas', (tester) async {
    await _pump(tester);
    await tester.tapAt(const Offset(50, 100), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 400));

    expect(_canvasFocused(tester), isTrue);
  });

  testWidgets('a press on its scroll bar does not', (tester) async {
    await _pump(tester);
    final bar = tester.getRect(_bar);
    await tester.tapAt(Offset(bar.center.dx, bar.top + bar.height * 0.9), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 400));

    expect(_canvasFocused(tester), isFalse);
  });
}
