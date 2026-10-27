// SWT does not display the mnemonic indicator in a tool tip: `&Restore` shows as `Restore`, and a
// doubled `&&` is a single literal `&`. Applications routinely reuse a menu label, mnemonic
// included, as a tooltip.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/rich_tooltip.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/gen/tree.dart';

const _cases = {
  '&Restore': 'Restore',
  'Save && Close': 'Save & Close',
};

VRectangle _bounds(double width, double height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width.toInt()
  ..height = height.toInt();

VToolBar _toolBar(String toolTipText) => VToolBar()
  ..id = 1
  ..style = SWT.VERTICAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'R'
      ..toolTipText = toolTipText,
  ]
  ..bounds = _bounds(40, 200);

VTree _tree(String toolTipText) => VTree()
  ..id = 3
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..toolTipText = toolTipText
  ..bounds = _bounds(240, 160);

void main() {
  for (final MapEntry(key: raw, value: shown) in _cases.entries) {
    testWidgets('a ToolItem tooltip of "$raw" reads "$shown"', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 40, height: 200, child: ToolBarSwt(value: _toolBar(raw))),
        ),
      ));
      await tester.pumpAndSettle();

      final tooltip = tester.widget<Tooltip>(
        find.descendant(of: find.byType(ToolItemSwt), matching: find.byType(Tooltip)).first,
      );
      expect(tooltip.message, shown);
    });

    testWidgets('a Control tooltip of "$raw" reads "$shown"', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 240, height: 160, child: TreeSwt<VTree>(value: _tree(raw))),
        ),
      ));

      final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
      await gesture.addPointer(location: Offset.zero);
      addTearDown(gesture.removePointer);
      await gesture.moveTo(const Offset(30, 20));
      await tester.pump(const Duration(milliseconds: 700));
      await tester.pump();

      expect(find.text(shown), findsOneWidget);
      expect(find.text(raw), findsNothing);
    });

    test('a tooltip card built from "$raw" reads "$shown"', () {
      final data = RichToolTipData.of(toolTipText: raw)!;
      expect(data.title, shown);
    });
  }
}
