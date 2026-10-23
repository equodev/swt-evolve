// A CHECK or RADIO menu item draws no box or ring: native menus show only a mark in the state
// column, and only while the item is selected. Labels keep one column whatever the state.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/theme/theme_extensions/menuitem_theme_extension.dart';

import 'support/menu_shown_ack.dart';

const _checkOff = 'Check off';
const _checkOn = 'Check on';
const _radioOff = 'Radio off';
const _radioOn = 'Radio on';
const _labels = [_checkOff, _checkOn, _radioOff, _radioOn];

VMenuItem _menuItem(int id, int style, String text, bool selected) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..selection = selected
  ..text = text;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Node 0',
  ]
  ..menu = (VMenu()
    ..id = 100
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      _menuItem(101, SWT.CHECK, _checkOff, false),
      _menuItem(102, SWT.CHECK, _checkOn, true),
      _menuItem(103, SWT.RADIO, _radioOff, false),
      _menuItem(104, SWT.RADIO, _radioOn, true),
    ])
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Future<void> _openMenu(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 800,
      height: 400,
      child: TreeSwt<VTree>(value: _tree()),
    ),
  ));
  await tester.pump();
  await tester.tap(find.text('Node 0'), buttons: kSecondaryButton);
  await tester.pump();
  await tester.pump();
  await ackMenuShown(tester, 100);
}

/// The menu row a rect left of [label] belongs to: vertically inside the label's line.
bool _inLeadingOf(WidgetTester tester, Rect rect, String label) {
  final labelRect = tester.getRect(find.text(label));
  return rect.right <= labelRect.left &&
      rect.center.dy >= labelRect.top &&
      rect.center.dy <= labelRect.bottom;
}

Iterable<(Rect, BoxDecoration)> _boxDecorations(WidgetTester tester) sync* {
  for (final element in find.byType(DecoratedBox).evaluate()) {
    final decoration = (element.widget as DecoratedBox).decoration;
    if (decoration is! BoxDecoration) continue;
    final box = element.renderObject as RenderBox;
    yield (box.localToGlobal(Offset.zero) & box.size, decoration);
  }
}

void main() {
  testWidgets('CHECK and RADIO items draw a mark only when selected, no box or ring',
      (tester) async {
    await _openMenu(tester);
    for (final label in _labels) {
      expect(find.text(label), findsOneWidget, reason: 'the menu must show "$label"');
    }

    final context = tester.element(find.text(_checkOn));
    final textColor = Theme.of(context).extension<MenuItemThemeExtension>()!.textColor;

    for (final (rect, decoration) in _boxDecorations(tester)) {
      for (final label in _labels) {
        if (!_inLeadingOf(tester, rect, label)) continue;
        expect(decoration.border, isNull,
            reason: '"$label" must not draw an outlined box or ring before its label');
      }
    }

    final checks = find.byIcon(Icons.check).evaluate().toList();
    expect(checks, hasLength(1), reason: 'only the selected CHECK item shows a check mark');
    final checkRect = tester.getRect(find.byIcon(Icons.check));
    expect(_inLeadingOf(tester, checkRect, _checkOn), isTrue,
        reason: 'the check mark belongs to the selected CHECK item');
    expect((checks.single.widget as Icon).color, textColor,
        reason: 'the check mark is drawn in the item text colour');

    final dots = _boxDecorations(tester)
        .where((d) => d.$2.shape == BoxShape.circle && d.$2.color != null)
        .where((d) => _labels.any((label) => _inLeadingOf(tester, d.$1, label)))
        .toList();
    expect(dots, hasLength(1), reason: 'only the selected RADIO item shows a dot');
    expect(_inLeadingOf(tester, dots.single.$1, _radioOn), isTrue,
        reason: 'the dot belongs to the selected RADIO item');
    expect(dots.single.$2.color, textColor,
        reason: 'the dot is drawn in the item text colour');

    final labelX = _labels.map((l) => tester.getTopLeft(find.text(l)).dx).toSet();
    expect(labelX, hasLength(1), reason: 'all labels start at the same x whatever the state');
  });
}
