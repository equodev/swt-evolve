// A Control's tooltip was painted with colorScheme.surface and given neither a border nor a
// shadow -- and colorScheme.surface is the very colour Tree and Table paint their own background
// from. Over one of those the panel was the exact colour of what sat behind it, so hovering a
// tree item showed nothing at all, in either theme.
//
// It was also placed by Flutter's Tooltip, which anchors to the centre of its child: on a control
// as tall as a Tree that put the panel hundreds of pixels from the row under the mouse, and even
// anchored at the pointer it would straddle the cursor rather than sit below and right of it the
// way a platform tooltip does.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';

const Size _treeSize = Size(240, 160);
const Offset _hover = Offset(30, 20);
const String _message = '/workspace/project/src/Main.java';

VRectangle _bounds(Size size) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = size.width.toInt()
  ..height = size.height.toInt();

VTree _tooltippedTree() => VTree()
  ..id = 1249
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..toolTipText = _message
  ..bounds = _bounds(_treeSize);

/// Hovers the tree and holds still long enough for the hover timer to open the tooltip.
Future<TestGesture> _hoverAndSettle(WidgetTester tester, ThemeMode mode) async {
  await tester.pumpWidget(EvolveApp(
    theme: mode,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _treeSize.width,
        height: _treeSize.height,
        child: TreeSwt<VTree>(value: _tooltippedTree()),
      ),
    ),
  ));

  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(_hover);

  await tester.pump(const Duration(milliseconds: 700));
  await tester.pump();
  return gesture;
}

Finder get _panel =>
    find.ancestor(of: find.text(_message), matching: find.byType(Container)).first;

void main() {
  for (final mode in [ThemeMode.light, ThemeMode.dark]) {
    testWidgets('a hovered tooltip is told apart from the control behind it (${mode.name})',
        (tester) async {
      await _hoverAndSettle(tester, mode);
      expect(find.text(_message), findsOneWidget);

      final decoration = tester.widget<Container>(_panel).decoration! as BoxDecoration;
      final scheme = Theme.of(tester.element(_panel)).colorScheme;

      expect(decoration.color, isNot(scheme.surface),
          reason: 'a Tree paints its own background from colorScheme.surface');
      expect(decoration.border, isNotNull);
      expect(decoration.boxShadow, isNotEmpty);
    });

    testWidgets('a hovered tooltip sits below and right of the pointer (${mode.name})',
        (tester) async {
      await _hoverAndSettle(tester, mode);

      final rect = tester.getRect(_panel);
      expect(rect.left, greaterThan(_hover.dx),
          reason: 'a platform tooltip does not straddle the cursor');
      expect(rect.top, greaterThan(_hover.dy));
      expect(rect.top, lessThan(_treeSize.height),
          reason: 'anchoring to the control centre is what put the panel far from the row');
    });

    testWidgets('moving the pointer takes the tooltip down again (${mode.name})', (tester) async {
      final gesture = await _hoverAndSettle(tester, mode);
      expect(find.text(_message), findsOneWidget);

      await gesture.moveTo(_hover + const Offset(0, 40));
      await tester.pump();
      expect(find.text(_message), findsNothing);
    });
  }
}
