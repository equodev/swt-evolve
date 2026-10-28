// A CoolBar's row layout runs in Java (DartCoolBar.layoutItems): when the bar is narrower than its
// items, they wrap onto several rows and each item's control arrives with the bounds of its row.
// The render must place the controls there, inside the bar's frame.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/coolbar.dart';
import 'package:swtflutter/src/gen/coolitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/coolbar_theme_extension.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, String text, VRectangle bounds) => VLabel()
  ..id = id
  ..style = SWT.NONE
  ..visible = true
  ..text = text
  ..bounds = bounds;

/// Two items wrapped onto two rows, as Java lays them out in a 798 px wide bar.
VCoolBar _twoRowBar({int style = SWT.NONE}) {
  final first = _label(11, 'first', _rect(10, 0, 788, 24));
  final second = _label(12, 'second', _rect(10, 24, 772, 31));
  return VCoolBar()
    ..id = 1
    ..style = style
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 798, 61)
    ..children = [first, second]
    ..items = [
      VCoolItem()
        ..id = 21
        ..control = first,
      VCoolItem()
        ..id = 22
        ..control = second,
    ];
}

Future<void> _pump(WidgetTester tester, VCoolBar bar, Size size) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: size.width,
        height: size.height,
        child: CoolBarSwt<VCoolBar>(value: bar),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

Offset _frameOrigin(WidgetTester tester) {
  final theme = Theme.of(tester.element(find.byType(CoolBarSwt<VCoolBar>)))
      .extension<CoolBarThemeExtension>()!;
  return Offset(theme.padding.left + theme.borderWidth,
      theme.padding.top + theme.borderWidth);
}

Rect _rectOf(WidgetTester tester, String text) =>
    tester.getRect(find.ancestor(of: find.text(text), matching: find.byType(LabelSwt<VLabel>)).first);

void main() {
  testWidgets('a wrapped CoolBar draws each item control at its Java bounds', (tester) async {
    await _pump(tester, _twoRowBar(), const Size(798, 61));

    final bar = tester.getTopLeft(find.byType(CoolBarSwt<VCoolBar>));
    final origin = bar + _frameOrigin(tester);
    final first = _rectOf(tester, 'first');
    final second = _rectOf(tester, 'second');

    expect(first, Rect.fromLTWH(origin.dx + 10, origin.dy, 788, 24));
    expect(second, Rect.fromLTWH(origin.dx + 10, origin.dy + 24, 772, 31),
        reason: 'the second item belongs on its own row, at the width Java gave it');
  });

  testWidgets('a vertical CoolBar draws each item control at its Java bounds', (tester) async {
    final first = _label(11, 'first', _rect(0, 10, 24, 200));
    final second = _label(12, 'second', _rect(24, 10, 31, 180));
    final bar = VCoolBar()
      ..id = 1
      ..style = SWT.VERTICAL
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 61, 216)
      ..children = [first, second]
      ..items = [
        VCoolItem()
          ..id = 21
          ..control = first,
        VCoolItem()
          ..id = 22
          ..control = second,
      ];
    await _pump(tester, bar, const Size(61, 216));

    final origin = tester.getTopLeft(find.byType(CoolBarSwt<VCoolBar>)) + _frameOrigin(tester);
    expect(_rectOf(tester, 'first'), Rect.fromLTWH(origin.dx, origin.dy + 10, 24, 200));
    expect(_rectOf(tester, 'second'), Rect.fromLTWH(origin.dx + 24, origin.dy + 10, 31, 180));
  });
}
