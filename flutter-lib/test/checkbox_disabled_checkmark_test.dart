// A disabled CHECK button must still show whether it is checked. The tick was
// always painted, but in checkboxCheckmarkColor — a colour picked to contrast
// with the *selected* (dark) fill — on top of the light disabled fill, which
// made a disabled-and-checked checkbox indistinguishable from an unchecked one.
//
// The assertions are contrast-based rather than colour-equality based so they
// keep holding for any theme: what matters is that the mark stays readable.

import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// WCAG 2.1 minimum contrast for a non-text UI component (1.4.11).
const double _minMarkContrast = 3.0;

VButton _button(int style, {required bool enabled, required bool selection}) =>
    VButton()
      ..id = 42
      ..style = style
      ..enabled = enabled
      ..selection = selection
      ..text = 'Enable advanced options';

double _relativeLuminance(Color c) {
  double channel(double v) =>
      v <= 0.03928 ? v / 12.92 : math.pow((v + 0.055) / 1.055, 2.4) as double;

  return 0.2126 * channel(c.r) + 0.7152 * channel(c.g) + 0.0722 * channel(c.b);
}

double _contrastRatio(Color a, Color b) {
  final la = _relativeLuminance(a);
  final lb = _relativeLuminance(b);
  return (math.max(la, lb) + 0.05) / (math.min(la, lb) + 0.05);
}

Future<void> _pump(WidgetTester tester, VButton value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 40,
      child: ButtonSwt<VButton>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The colour of the box the checkbox/radio mark is painted on.
Color _boxColor(WidgetTester tester) {
  final box = tester.widgetList<AnimatedContainer>(
    find.descendant(
      of: find.byType(ButtonSwt<VButton>),
      matching: find.byType(AnimatedContainer),
    ),
  ).first;
  return (box.decoration as BoxDecoration).color!;
}

void main() {
  testWidgets('a disabled checked checkbox keeps a visible tick',
      (WidgetTester tester) async {
    await _pump(tester, _button(SWT.CHECK, enabled: false, selection: true));

    final tick = find.byIcon(Icons.check);
    expect(tick, findsOneWidget, reason: 'the tick must still be painted');

    final markColor = tester.widget<Icon>(tick).color!;
    expect(
      _contrastRatio(markColor, _boxColor(tester)),
      greaterThanOrEqualTo(_minMarkContrast),
      reason: 'a greyed-out checkbox must read as ticked, not as empty',
    );
  });

  testWidgets('an enabled checked checkbox keeps a visible tick',
      (WidgetTester tester) async {
    await _pump(tester, _button(SWT.CHECK, enabled: true, selection: true));

    final markColor = tester.widget<Icon>(find.byIcon(Icons.check)).color!;
    expect(
      _contrastRatio(markColor, _boxColor(tester)),
      greaterThanOrEqualTo(_minMarkContrast),
    );
  });

  testWidgets('a disabled selected radio keeps a visible dot',
      (WidgetTester tester) async {
    await _pump(tester, _button(SWT.RADIO, enabled: false, selection: true));

    // The radio's inner dot is the only circle inside the box that has no border
    // — the box itself is a bordered circle.
    final dot = tester
        .widgetList<Container>(find.descendant(
          of: find.byType(AnimatedContainer),
          matching: find.byType(Container),
        ))
        .map((c) => c.decoration)
        .whereType<BoxDecoration>()
        .firstWhere((d) => d.shape == BoxShape.circle && d.border == null);
    final dotColor = dot.color!;
    expect(
      _contrastRatio(dotColor, _boxColor(tester)),
      greaterThanOrEqualTo(_minMarkContrast),
    );
  });
}
