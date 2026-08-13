// A composite WITH children builds through buildComposite()/
// wrapCompositeInteractionChrome(), which used to skip applyMenu(), so a
// context menu set on the composite (Control.setMenu) never opened. That is
// exactly the editor line-number ruler: Eclipse's CompositeRuler sets the
// #RulerContext menu on a composite fully covered by child canvases, so a
// right-click over the line numbers must open the composite's menu.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VMenu _menu() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Add Bookmark...',
  ];

// The CompositeRuler shape: a composite whose whole area is covered by a
// childless child canvas (the line-number column), with the context menu set
// on the composite itself.
VComposite _ruler() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..menu = _menu()
  ..bounds = _rect(0, 0, 60, 400)
  ..children = [
    VCanvas()
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(0, 0, 60, 400),
  ];

Widget _wrap(VComposite value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 60,
        height: 400,
        child: CompositeSwt<VComposite>(value: value),
      ),
    );

void main() {
  testWidgets(
      'right-clicking a child of a composite opens the composite context menu',
      (tester) async {
    await tester.pumpWidget(_wrap(_ruler()));
    await tester.pump();

    await tester.tapAt(const Offset(30, 200), buttons: kSecondaryButton);
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, 100);

    expect(find.text('Add Bookmark...'), findsOneWidget,
        reason: 'the menu set on the composite must open on right-click '
            'over its child (the line-number canvas of a CompositeRuler)');
  });
}
