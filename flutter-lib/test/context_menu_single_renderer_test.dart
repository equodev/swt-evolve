// A popup menu has two mounted widgets: the Display renders every shown popup, and the control mounts
// the same menu for its own right-click. Both acting on Menu.setVisible painted one menu twice, as two
// overlapping popups over a single model.

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

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VComposite _host() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..menu = (VMenu()
    ..id = 100
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = 101
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'Section View Editor...',
    ])
  ..bounds = _rect(300, 300)
  ..children = [
    VCanvas()
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(300, 300),
  ];

Widget _wrap(VComposite value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 300,
        child: CompositeSwt<VComposite>(value: value),
      ),
    );

Future<void> _rightClick(WidgetTester tester) async {
  await tester.pumpWidget(_wrap(_host()));
  await tester.pump();
  await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
  await tester.pump();
}

void main() {
  testWidgets('a control does not render a popup Java is already showing',
      (tester) async {
    // Menu.setVisible(true) puts the menu in the Display's popup list, which renders it. The control
    // mounts the same menu for its own right-click, and acting on the flag here too painted the one
    // menu twice, as two overlapping popups over a single model.
    final host = _host()..menu!.visible = true;
    await tester.pumpWidget(_wrap(host));
    await tester.pump();
    await ackMenuShown(tester, 100);

    expect(find.text('Section View Editor...'), findsNothing,
        reason: 'the Display renders the shown popup; this one only serves the right-click');
  });
}
