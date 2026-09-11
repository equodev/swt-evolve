// The hover zoom must stay visible on a toolbar whose icon already fills its box.
//
// A Composite lays its children out absolutely and wraps each one in a ClipRect the size of that
// child's SWT bounds (nolayout.dart) -- which is correct SWT semantics: a control is clipped to its
// own bounds. The zoom is a paint-time transform that deliberately does not change layout, so on a
// control sized to exactly its content the grown icon has nowhere to go and is cut at the boundary.
//
// The shape that hits it: a single-item ToolBar in a GridLayout cell sized to the preferred size of
// a 16pt image, so the icon has no slack at all and the grown one is cut on every side.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

// A one-item ToolBar sized to its content, the way the stepper's GridLayout cell sizes each arrow.
const _itemBox = 24;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _stepperLike() => VComposite()
  ..id = 10
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 120, 40)
  ..children = [
    VToolBar()
      ..id = 100
      ..style = SWT.FLAT | SWT.RIGHT
      ..enabled = true
      ..visible = true
      ..bounds = _rect(8, 8, _itemBox, _itemBox)
      ..items = [
        // CHECK rather than the stepper's PUSH only so the probe renders a plain Icon instead of
        // an async image: both styles go through the same _applyHoverZoom path.
        VToolItem()
          ..id = 101
          ..style = SWT.CHECK
          ..enabled = true
          ..selection = false
          ..text = 'F'
          ..toolTipText = 'Step Forward'
      ],
  ];

final _icon = find.byIcon(Icons.check_box_outline_blank);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a zoomed icon reaches past its control bounds but is still drawn whole',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 120,
        height: 40,
        child: CompositeSwt<VComposite>(value: _stepperLike()),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    // The region the absolute layout actually clips this ToolBar to. The ClipRect's own box is the
    // child's bounds; with the zoom on it carries a clipper that inflates that box, so the region
    // has to be read from the clipper rather than from the widget's rect.
    final clipFinder = find.ancestor(
      of: find.byType(ToolBarSwt),
      matching: find.byType(ClipRect),
    ).first;
    final clipWidget = tester.widget<ClipRect>(clipFinder);
    final clipBox = tester.getRect(clipFinder);
    final clip = (clipWidget.clipper?.getClip(clipBox.size) ?? (Offset.zero & clipBox.size))
        .shift(clipBox.topLeft);

    final atRest = tester.getRect(_icon);
    expect(clipBox.height, lessThanOrEqualTo(_itemBox.toDouble()),
        reason: 'precondition: the control is sized to its content, not given slack');
    expect(clip, isNot(clipBox),
        reason: 'the zoom is on by default, so the clip must be inflated past the bare bounds');

    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: Offset.zero);
    addTearDown(gesture.removePointer);
    await gesture.moveTo(tester.getCenter(_icon));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    final hovered = tester.getRect(_icon);
    expect(hovered.height, greaterThan(atRest.height), reason: 'the zoom did not apply');

    // The grown icon must be drawn whole. It reaches past the control's own bounds -- that is the
    // point, the zoom has nowhere else to go on a control sized to its content -- so what it must
    // stay inside is the inflated clip.
    final cut = <String, double>{
      'left': clip.left - hovered.left,
      'right': hovered.right - clip.right,
      'top': clip.top - hovered.top,
      'bottom': hovered.bottom - clip.bottom,
    }..removeWhere((_, px) => px <= 0.01);
    expect(cut, isEmpty,
        reason: 'the zoomed icon is cut off (clip=$clip hovered=$hovered)');
  });
}
