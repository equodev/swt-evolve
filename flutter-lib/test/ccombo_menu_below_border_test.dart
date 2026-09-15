// An open CCombo dropdown must sit outside the control's border, as native CCombo places its popup
// against the control's bounds. The border is painted around the DropdownMenu, so a menu anchored
// to the DropdownMenu itself covers the border on the side it opens on.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _QuietCComboSwt extends CComboSwt<VCCombo> {
  const _QuietCComboSwt({super.key, required super.value});

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) {}
}

VCCombo _borderedReadOnlyCombo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 3
  ..style = SWT.BORDER | SWT.READ_ONLY
  ..enabled = true
  ..items = const ['TWT', 'TVD', 'TVDSS', 'FREQ']
  ..text = 'TVDSS'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 180
    ..height = 30);

Rect _menuSurface(WidgetTester tester) => tester.getRect(find
    .descendant(of: find.byType(CustomSingleChildLayout), matching: find.byType(Material))
    .first);

Future<Rect> _openMenu(WidgetTester tester, Alignment placement) async {
  final key = GlobalKey();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.dark,
    contentWidget: Align(
      alignment: placement,
      child: SizedBox(
        width: 180,
        height: 30,
        child: _QuietCComboSwt(key: key, value: _borderedReadOnlyCombo()),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
  await tester.pumpAndSettle();
  expect(find.text('FREQ'), findsWidgets, reason: 'sanity: the popup is open');
  return tester.getRect(find.byKey(key));
}

void main() {
  testWidgets('a menu opening below starts under the bottom border', (tester) async {
    final combo = await _openMenu(tester, Alignment.topCenter);
    final menu = _menuSurface(tester);

    expect(menu.top, greaterThanOrEqualTo(combo.bottom),
        reason: 'the menu covers ${combo.bottom - menu.top}px of the field\'s bottom border');
  });

  testWidgets('a menu opening above ends over the top border', (tester) async {
    final combo = await _openMenu(tester, Alignment.bottomCenter);
    final menu = _menuSurface(tester);

    expect(menu.bottom, lessThanOrEqualTo(combo.top),
        reason: 'the menu covers ${menu.bottom - combo.top}px of the field\'s top border');
  });
}
