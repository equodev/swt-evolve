// Issue #823 — SWT glues the shortcut to the label with a tab ("&New\tAlt+Shift+N").
// Only PUSH items split it; CASCADE/CHECK/RADIO rendered the raw text, so the
// shortcut showed up inside the label, in the label's style instead of the
// themed accelerator style.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/menuitem_theme_extension.dart';

VMenu _subMenu() => VMenu()
  ..id = 99
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = [VMenuItem()..id = 100..style = SWT.PUSH..text = 'Sub'];

void main() {
  Future<void> expectSplit(WidgetTester tester, int style, String label,
      String shortcut) async {
    final item = VMenuItem()
      ..id = 1
      ..style = style
      ..enabled = true
      ..text = '$label\t$shortcut'
      ..menu = style == SWT.CASCADE ? _subMenu() : null;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 80,
        child: MenuItemSwt<VMenuItem>(value: item),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text(label), findsOneWidget, reason: 'label without shortcut');
    expect(find.text(shortcut), findsOneWidget, reason: 'shortcut on its own');

    final theme = Theme.of(tester.element(find.text(label)))
        .extension<MenuItemThemeExtension>()!;
    expect(tester.widget<Text>(find.text(shortcut)).style?.color,
        theme.acceleratorTextColor,
        reason: 'shortcut must use the themed accelerator color');
  }

  testWidgets('CASCADE item splits label and shortcut',
      (t) => expectSplit(t, SWT.CASCADE, 'More Options', 'Alt+Shift+N'));
  testWidgets('CHECK item splits label and shortcut',
      (t) => expectSplit(t, SWT.CHECK, 'Word Wrap', 'Alt+Shift+Y'));
  testWidgets('RADIO item splits label and shortcut',
      (t) => expectSplit(t, SWT.RADIO, 'Option A', 'Ctrl+1'));
  testWidgets('PUSH item splits label and shortcut',
      (t) => expectSplit(t, SWT.PUSH, 'Save', 'Ctrl+S'));
}
