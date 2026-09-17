// SWT cuts a child at its parent's edge. NoLayout inflated every child's clip by the room a
// ToolItem's hover zoom needs (iconSize * (scale - 1) / 2 = 7.5px with the shipped theme), so any
// control could bleed 7.5px past its parent.
//
// Invisible for a control that fits, and wrong for one that does not: an Eclipse Forms Section used
// as a captioned rule is pinned to `GridData.heightHint = 2` with no client, and its own layout puts
// a 20px caption Label at y=3. SWT clips that caption away entirely and the Section reads as a plain
// horizontal rule; the inflated clip leaked the top ~7px of the glyphs instead -- the smear that
// reads as a field painting over the caption.
//
// The ToolBar keeps the inflation: a bar sized to exactly its icons needs the room to grow into.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// The Section: a Canvas sized to the 2px rule, carrying the caption its own layout placed below it.
VCanvas _section() => VCanvas()
  ..id = 22051779
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(5, 2, 974, 2)
  ..children = <VControl>[
    VLabel()
      ..id = 1089993541
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(6, 3, 121, 20)
      ..text = 'Category Details'
  ];

VToolBar _toolBar() => VToolBar()
  ..id = 777
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(5, 2, 30, 30);

VComposite _body(List<VControl> children) => VComposite()
  ..id = 1007392323
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 984, 754)
  ..children = children;

/// The clip NoLayout puts around [childType], and the size of that child.
Future<({Rect clip, Size size})> _clipAround(
    WidgetTester tester, VControl child, Type childType) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 984,
      height: 754,
      child: CompositeSwt<VComposite>(value: _body([child])),
    ),
  ));
  await tester.pumpAndSettle();

  final target = find.byType(childType);
  final clip = tester.widget<ClipRect>(
    find.ancestor(of: target, matching: find.byType(ClipRect)).first,
  );
  final size = tester.getSize(target);
  return (clip: clip.clipper?.getClip(size) ?? (Offset.zero & size), size: size);
}

void main() {
  testWidgets('a control is cut at its own edge', (WidgetTester tester) async {
    final r = await _clipAround(tester, _section(), CanvasSwt<VCanvas>);

    expect(r.clip, Offset.zero & r.size,
        reason: 'nothing of the 20px caption may survive a 2px Section -- SWT clips it away');
  });

  testWidgets('a ToolBar keeps the room its hover zoom needs', (WidgetTester tester) async {
    final r = await _clipAround(tester, _toolBar(), ToolBarSwt<VToolBar>);

    expect(r.clip.top, lessThan(0.0),
        reason: 'a bar sized to its icons must let the zoomed icon grow past its bounds');
  });
}
