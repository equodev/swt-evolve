// A horizontal SWT.WRAP ToolBar narrower than its items draws the rows Java's layout wraps it into,
// so a control hosted in it sits where Java's toDisplay says it does.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _viewportWidth = 400;
const double _viewportHeight = 200;

/// Two rows of 30, 2px apart, as Java measures a wrapped bar.
const int _rowHeight = 30;
const int _barWidth = 300;
const int _barHeight = 2 * _rowHeight + 2;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VComposite _hosted(int id, int width) => VComposite()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, width, _rowHeight)
  ..children = [
    VLabel()
      ..id = id + 1
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, width, 20)
      ..text = 'hosted $id',
  ];

VToolItem _separatorHosting(int id, VComposite control) => VToolItem()
  ..id = id
  ..style = SWT.SEPARATOR
  ..enabled = true
  ..width = control.bounds!.width
  ..control = control;

VToolBar _bar(int style, int height) => VToolBar()
  ..id = 100
  ..style = style
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _barWidth, height)
  ..items = [
    _separatorHosting(200, _hosted(500, 200)),
    _separatorHosting(201, _hosted(600, 150)),
  ];

Future<void> _pump(WidgetTester tester, VToolBar bar) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _viewportWidth,
        height: _viewportHeight,
        child: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: _barWidth.toDouble(),
            height: bar.bounds!.height.toDouble(),
            child: ToolBarSwt<VToolBar>(value: bar),
          ),
        ),
      ),
    ),
  ));
  await tester.pump();
}

Rect _hostedRect(WidgetTester tester, int id) {
  final bar = tester.getTopLeft(find.byType(ToolBarSwt<VToolBar>).first);
  return tester.getRect(find.byKey(ValueKey(id)).first).shift(-bar);
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the item Java wraps starts the second row, at its left edge', (tester) async {
    await _pump(tester, _bar(SWT.FLAT | SWT.WRAP, _barHeight));

    final first = _hostedRect(tester, 500);
    final wrapped = _hostedRect(tester, 600);
    expect(first.left, closeTo(0, 1));
    expect(first.top, closeTo(0, 1));
    expect(wrapped.left, closeTo(0, 1), reason: 'Java puts the wrapped item at x = 0');
    expect(wrapped.top, closeTo(_rowHeight + 2, 1), reason: 'on the second row');
    expect(wrapped.width, closeTo(150, 1), reason: 'at the width Java gave it, not squeezed');
  });

  testWidgets('a bar without WRAP keeps its items on one row', (tester) async {
    await _pump(tester, _bar(SWT.FLAT, _rowHeight));

    final first = _hostedRect(tester, 500);
    final second = _hostedRect(tester, 600);
    expect(second.top, closeTo(first.top, 1));
    expect(second.left, greaterThan(first.left));
  });
}
