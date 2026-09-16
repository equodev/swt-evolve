import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _hbDarkFocus = Color(0xFFFF52A8);
const _equoDarkFocus = Color(0xFF6BA3FF);

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Node 0',
    VTreeItem()
      ..id = 11
      ..text = 'Node 1',
  ]
  ..bounds = _bounds(800, 400);

VButton _button() => VButton()
  ..id = 2
  ..style = SWT.PUSH
  ..enabled = true
  ..visible = true
  ..text = 'OK'
  ..bounds = _bounds(120, 32);

Widget _app(Widget child) => EvolveApp(
      theme: ThemeMode.dark,
      contentWidget: SizedBox(width: 800, height: 400, child: child),
    );

/// Foreground borders in [color]: the rings are painted over the widget, never around it.
Finder _ringsIn(Color color) => find.byWidgetPredicate((widget) {
      if (widget is! DecoratedBox || widget.position != DecorationPosition.foreground) return false;
      final decoration = widget.decoration;
      if (decoration is! BoxDecoration) return false;
      final border = decoration.border;
      return border is Border && border.top.color == color;
    });

void _useTheme(String name, {bool? focusIndicators}) {
  resetConfigFlags();
  setConfigFlags(ConfigFlags()
    ..theme_name = name
    ..force_theme = 'dark'
    ..focus_indicators = focusIndicators);
}

void main() {
  tearDown(resetConfigFlags);

  group('hb dark', () {
    setUp(() => _useTheme('hb'));

    testWidgets('a tree rings its selected row once it holds focus', (tester) async {
      await tester.pumpWidget(_app(TreeSwt<VTree>(value: _tree())));
      await tester.pump();
      expect(_ringsIn(_hbDarkFocus), findsNothing);

      await tester.tap(find.text('Node 0'));
      await tester.pump();
      await tester.pump();

      expect(_ringsIn(_hbDarkFocus), findsOneWidget);
    });

    testWidgets('a push button shows the ring while it holds focus', (tester) async {
      await tester.pumpWidget(_app(ButtonSwt<VButton>(value: _button())));
      await tester.pump();
      expect(_ringsIn(_hbDarkFocus), findsNothing);

      Focus.of(tester.element(find.text('OK'))).requestFocus();
      await tester.pump();
      await tester.pump();

      expect(_ringsIn(_hbDarkFocus), findsOneWidget);
    });
  });

  Future<void> focusTreeRowAndButton(WidgetTester tester) async {
    await tester.pumpWidget(_app(Column(children: [
      SizedBox(height: 300, child: TreeSwt<VTree>(value: _tree())),
      ButtonSwt<VButton>(value: _button()),
    ])));
    await tester.pump();
    await tester.tap(find.text('Node 0'));
    await tester.pump();
    await tester.pump();
    Focus.of(tester.element(find.text('OK'))).requestFocus();
    await tester.pump();
    await tester.pump();
  }

  testWidgets('a theme without focus indicators by default draws no ring', (tester) async {
    _useTheme('equo');
    await focusTreeRowAndButton(tester);
    expect(_ringsIn(_equoDarkFocus), findsNothing);
    expect(_ringsIn(_hbDarkFocus), findsNothing);
  });

  testWidgets('the flag turns rings on for any theme, in its own focus colour', (tester) async {
    _useTheme('equo', focusIndicators: true);
    await focusTreeRowAndButton(tester);
    expect(_ringsIn(_equoDarkFocus), findsWidgets);
  });

  testWidgets('the flag turns rings off for hb', (tester) async {
    _useTheme('hb', focusIndicators: false);
    await focusTreeRowAndButton(tester);
    expect(_ringsIn(_hbDarkFocus), findsNothing);
  });
}
