// A Composite lays its children out from the PARENT's copy of them: NoLayout's
// _AbsoluteLayoutDelegate reads `child.bounds` off `state.children` and constrains each child
// tightly to it. The child's own state channel is a second, independent source of truth for the
// very same bounds — and when only that one arrives (the parent's copy having been sent before the
// layout ran, and its refresh lost), the child stays pinned at the parent's stale size.
//
// At 0x0 that is invisible: nothing paints, and a zero-size render object emits no semantics node
// at all, so on Flutter Web the widget has no DOM element either. That is exactly how an opened
// editor came up permanently blank while its own live state held healthy bounds (960x816,
// visible: true) — the parent's copy of it still said 0x0.
//
// The child's own bounds must win over a stale parent copy.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCanvas _canvas(int id, VRectangle bounds) => VCanvas()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = bounds;

VComposite _part(List<VControl> children) => VComposite()
  ..id = 500
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 970, 958)
  ..children = children;

void main() {
  testWidgets('a child update on its own channel resizes it despite a stale 0x0 parent copy',
      (WidgetTester tester) async {
    // The part is mounted from the parent payload that was serialized BEFORE the example laid its
    // children out — the child is in the tree, at 0x0.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 970,
        height: 958,
        child: CompositeSwt<VComposite>(value: _part([_canvas(933297292, _rect(0, 0, 0, 0))])),
      ),
    ));
    await tester.pumpAndSettle();

    expect(tester.getSize(find.byType(CanvasSwt<VCanvas>)), const Size(0, 0),
        reason: 'precondition: mounted from the stale parent copy, so the child starts at 0x0');

    // Java lays the example out and the child's own state channel delivers its real bounds. No
    // parent payload follows (that is the failure being reproduced), so this update is all Flutter
    // gets.
    final childState = tester.state(find.byType(CanvasSwt<VCanvas>)) as WidgetSwtState;
    // ignore: invalid_use_of_protected_member
    childState.setValue(_canvas(933297292, _rect(5, 5, 960, 816)));
    await tester.pumpAndSettle();

    expect(tester.getSize(find.byType(CanvasSwt<VCanvas>)), const Size(960, 816),
        reason: 'the child knows its own bounds; a stale parent copy must not pin it at 0x0');
  });
}
