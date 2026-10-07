// A string drawn without SWT.DRAW_TRANSPARENT fills its extent with the GC's background, and a
// paint event's GC starts with the control's background. Natively that fill is invisible: it is the
// same colour the control was erased to. A Composite paints the theme's ground unless the
// application's colours are asked for, so a fill in the application's colour showed up as a box
// hugging every label an application draws onto its own Composite.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'delivery/support/deliver.dart';

const int _panelId = 1682973478;
const _appGround = Color(0xFF292929);
const _highlight = Color(0xFF3060A0);

VColor _v(Color c) => VColor()
  ..alpha = (c.a * 255).round()
  ..red = (c.r * 255).round()
  ..green = (c.g * 255).round()
  ..blue = (c.b * 255).round();

Map<String, dynamic> _rgba(Color c) => _v(c).toJson();

VRectangle _bounds(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _panel() => VComposite()
  ..id = _panelId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..background = _v(_appGround)
  ..bounds = _bounds(0, 0, 246, 877)
  ..children = [
    VComposite()
      ..id = 779809646
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..background = _v(_appGround)
      ..bounds = _bounds(0, 24, 246, 781),
  ];

Future<void> _paint(Color gcBackground, {bool transparent = false}) async {
  await deliverFrame('GC/$_panelId', {
    'id': _panelId,
    'swt': 'GC',
    '_s': 1,
    'background': _rgba(gcBackground),
    'foreground': _rgba(const Color(0xFFFFFFFF)),
  });
  // drawString reaches the client as drawText with flags: none, or DRAW_TRANSPARENT.
  await deliverFrame('GC/$_panelId/drawTextStringintintint',
      {'string': ' Header ', 'x': 0, 'y': 6, 'flags': transparent ? SWT.DRAW_TRANSPARENT : 0});
  await deliverFrame('GC/$_panelId/gcDispose', {'fullRepaint': true});
}

Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 20; i++) {
    await tester.pump(Duration.zero);
  }
}

List<Shape> _painted(WidgetTester tester) => [
      for (final p in tester
          .widgetList<CustomPaint>(find.byType(CustomPaint))
          .map((w) => w.painter)
          .whereType<ScenePainter>())
        ...p.shapes,
    ];

List<Color> _textFills(WidgetTester tester) => _painted(tester)
    .whereType<RectShape>()
    .where((s) => s.isFilled)
    .map((s) => s.color)
    .toList();

/// The colour the Composite itself paints behind its content.
Color _renderedGround(WidgetTester tester) => tester
    .widgetList<ColoredBox>(find.descendant(
        of: find.byType(CompositeSwt<VComposite>).first, matching: find.byType(ColoredBox)))
    .first
    .color;

Future<void> _pump(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: SizedBox(
      width: 246,
      height: 877,
      child: CompositeSwt<VComposite>(value: _panel()),
    ),
  ));
  await _settle(tester);
}

void main() {
  testWidgets('text drawn in the control\'s own background fills with the ground it paints',
      (tester) async {
    await _pump(tester);
    final ground = _renderedGround(tester);
    expect(ground, isNot(_appGround),
        reason: 'precondition: the Composite paints the theme ground, not the application colour');

    await _paint(_appGround);
    await _settle(tester);

    expect(_painted(tester).whereType<TextShape>().map((s) => s.text), contains(' Header '));
    expect(_textFills(tester), [ground],
        reason: 'the fill behind the text must be the colour the Composite renders, or it reads as '
            'a box around the label');
  });

  testWidgets('a background the application chose for the text keeps its colour', (tester) async {
    await _pump(tester);

    await _paint(_highlight);
    await _settle(tester);

    expect(_textFills(tester), [_highlight]);
  });

  testWidgets('transparent text fills nothing', (tester) async {
    await _pump(tester);

    await _paint(_appGround, transparent: true);
    await _settle(tester);

    expect(_textFills(tester), isEmpty);
  });
}
