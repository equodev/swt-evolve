import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolbar.dart' show ToolBarSwt;
import 'package:swtflutter/src/gen/toolitem.dart';

/// Distinct from every color the theme itself would pick, so a match can only come from the
/// value this test actually passed in -- never from a default that happens to look similar.
const Color _tabAccent = Color(0xFF123456);

VComposite _topRightComposite({required int itemStyle}) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..children = [
    VToolBar()
      ..id = 2
      ..style = SWT.HORIZONTAL | SWT.FLAT
      ..enabled = true
      ..visible = true
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 60
        ..height = 24)
      ..items = [
        VToolItem()
          ..id = 3
          ..style = itemStyle
          ..enabled = true
          // getToolItems() drops an item with neither -- an icon-only real item still carries an
          // image, this text is standing in for it so the item survives that filter.
          ..text = 'x',
      ],
  ];

void main() {
  testWidgets(
      'a bar nested in a topRight-style composite paints the same background the composite chose',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 60,
        child: ToolbarComposite(
          value: _topRightComposite(itemStyle: SWT.PUSH),
          backgroundColor: _tabAccent,
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final barBackground = find.descendant(
      of: find.byType(ToolBarSwt),
      matching: find.byWidgetPredicate((w) =>
          w is Container &&
          w.decoration is BoxDecoration &&
          (w.decoration as BoxDecoration).color == _tabAccent),
    );

    expect(barBackground, findsWidgets,
        reason: 'the nested ToolBar must inherit the accent color its composite painted '
            'with, not fall back to the toolbar theme\'s own default background');
  });

  testWidgets(
      'a CHECK item in that same bar paints its own backdrop with the composite\'s color too',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 60,
        child: ToolbarComposite(
          value: _topRightComposite(itemStyle: SWT.CHECK),
          backgroundColor: _tabAccent,
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // A CHECK item paints its own opaque backdrop (so the box under the checkbox reads even
    // when unchecked and unhovered) via a bare Container(color: ...) rather than a decoration,
    // so match on the widget's own color field instead of decoration.color.
    final itemBackdrop = find.descendant(
      of: find.byType(ToolItemSwt),
      matching:
          find.byWidgetPredicate((w) => w is Container && w.color == _tabAccent),
    );

    expect(itemBackdrop, findsWidgets,
        reason: 'the CHECK item\'s own backdrop must match the bar\'s accent color, not the '
            'toolbar theme\'s generic composite background');
  });
}
