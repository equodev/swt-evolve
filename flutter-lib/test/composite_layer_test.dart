// Every Composite draws in a layer of its own: painting restarts from the nearest layer above
// what changed, so without one a change to any control repainted the whole shell, every control
// in it included.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _section(int id, String name, int y) {
  final controls = <VControl>[];
  for (var i = 0; i < 4; i++) {
    controls.add(VLabel()
      ..id = id * 10 + i * 2
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..text = '$name label $i'
      ..bounds = _rect(10, 10 + i * 34, 150, 24));
    controls.add(VButton()
      ..id = id * 10 + i * 2 + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..visible = true
      ..text = '$name button $i'
      ..bounds = _rect(170, 10 + i * 34, 140, 28));
  }
  return VComposite()
    ..id = id
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, y, 400, 150)
    ..children = controls;
}

void main() {
  setUp(freshClient);

  testWidgets('a control changing repaints its own Composite, not the one beside it', (tester) async {
    tester.view.physicalSize = const Size(400, 320);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.reset);
    final shell = VShell()
      ..id = 1
      ..style = SWT.SHELL_TRIM
      ..visible = true
      ..text = 'two sections'
      ..bounds = _rect(0, 0, 400, 320)
      ..children = [_section(10, 'First', 0), _section(20, 'Second', 160)];
    await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light, contentWidget: DisplaySwt(value: VDisplay()..shells = [shell])));
    await tester.pumpAndSettle();

    // Everything the second section draws: what is under the Composite holding its first label.
    final second = <RenderObject>{};
    void collect(RenderObject r) {
      second.add(r);
      r.visitChildren(collect);
    }

    final secondSection = tester.element(find.byWidgetPredicate(
        (w) => w is CompositeSwt && w.value.id == 20));
    collect(secondSection.renderObject!);

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(395, 315));
    await tester.pumpAndSettle();
    var paintedInSecond = 0;
    debugOnProfilePaint = (renderObject) {
      if (second.contains(renderObject)) paintedInSecond++;
    };
    for (var i = 0; i < 4; i++) {
      await mouse.moveTo(tester.getCenter(find.text('First button $i')));
      await tester.pumpAndSettle();
    }
    debugOnProfilePaint = null;
    await mouse.removePointer();

    expect(paintedInSecond, 0, reason: 'the buttons lighting up are in the first section only');
  });
}
