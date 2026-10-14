// A grayed check must read differently from a full check — that difference is how a check tree
// says "some but not all of my children are checked". Grayed only shows on a checked box: every
// native platform draws grayed-but-unchecked as an empty box.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VButton _checkBox({required bool selection, required bool grayed, int id = 42}) => VButton()
  ..id = id
  ..style = SWT.CHECK
  ..enabled = true
  ..selection = selection
  ..grayed = grayed
  ..text = 'Include sub-items';

/// A check tree whose first item is partially checked and whose second is fully checked — the
/// exact pair the report says renders identically.
VTree _checkTree() => VTree()
  ..id = 1
  ..style = SWT.CHECK
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Datasets'
      ..checked = true
      ..grayed = true,
    VTreeItem()
      ..id = 11
      ..text = 'Archives'
      ..checked = true
      ..grayed = false,
  ];

/// A check tree whose only item is left grayed but unchecked.
VTree _uncheckedGrayedTree() => VTree()
  ..id = 1
  ..style = SWT.CHECK
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Datasets'
      ..checked = false
      ..grayed = true,
  ];

Future<void> _pump(WidgetTester tester, Widget child) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 320, height: 240, child: child),
  ));
  await tester.pumpAndSettle();
}

/// The partial-state mark: a solid rectangle inside the checkbox box, as opposed to the box
/// itself (which carries a border) and the radio's dot (which is a circle).
bool _hasPartialMark(WidgetTester tester) => tester
    .widgetList<Container>(find.descendant(
      of: find.byType(AnimatedContainer),
      matching: find.byType(Container),
    ))
    .map((c) => c.decoration)
    .whereType<BoxDecoration>()
    .any((d) =>
        d.color != null && d.border == null && d.shape == BoxShape.rectangle);

/// The checkbox box itself, as opposed to the marks drawn inside it.
BoxDecoration _boxDecoration(WidgetTester tester) => tester
    .widget<AnimatedContainer>(find.byType(AnimatedContainer).first)
    .decoration as BoxDecoration;

void main() {
  testWidgets('a grayed checkbox renders the partial mark, not a full checkmark',
      (WidgetTester tester) async {
    await _pump(
      tester,
      ButtonSwt<VButton>(value: _checkBox(selection: true, grayed: true)),
    );

    expect(
      find.byIcon(Icons.check),
      findsNothing,
      reason: 'a partially-checked box must not claim to be fully checked',
    );
    expect(
      _hasPartialMark(tester),
      isTrue,
      reason: 'the grayed state needs a mark of its own, not an empty box',
    );
  });

  testWidgets('a fully checked checkbox still renders the checkmark',
      (WidgetTester tester) async {
    await _pump(
      tester,
      ButtonSwt<VButton>(value: _checkBox(selection: true, grayed: false)),
    );

    expect(find.byIcon(Icons.check), findsOneWidget);
  });

  testWidgets('a check tree renders a partial item differently from a fully checked one',
      (WidgetTester tester) async {
    await _pump(tester, TreeSwt<VTree>(value: _checkTree()));

    expect(
      find.byIcon(Icons.check),
      findsOneWidget,
      reason: 'only the fully-checked item may show a checkmark; the grayed one '
          'must be distinguishable from it',
    );
  });

  testWidgets('a grayed but unchecked checkbox renders as an empty box',
      (WidgetTester tester) async {
    await _pump(
      tester,
      ButtonSwt<VButton>(value: _checkBox(selection: false, grayed: true)),
    );

    expect(find.byIcon(Icons.check), findsNothing);
    expect(
      _hasPartialMark(tester),
      isFalse,
      reason: 'grayed only marks a checked box; unchecked it draws empty',
    );
  });

  testWidgets('a grayed but unchecked checkbox has the unchecked fill and border',
      (WidgetTester tester) async {
    await _pump(
      tester,
      ButtonSwt<VButton>(value: _checkBox(selection: false, grayed: false)),
    );
    final unchecked = _boxDecoration(tester);

    await _pump(
      tester,
      ButtonSwt<VButton>(
          value: _checkBox(selection: false, grayed: true, id: 43)),
    );

    expect(_boxDecoration(tester).color, unchecked.color);
    expect(_boxDecoration(tester).border, unchecked.border);
  });

  testWidgets('a check tree item left grayed but unchecked renders an empty box',
      (WidgetTester tester) async {
    await _pump(tester, TreeSwt<VTree>(value: _uncheckedGrayedTree()));

    expect(find.byIcon(Icons.check), findsNothing);
    expect(_hasPartialMark(tester), isFalse);
  });
}
