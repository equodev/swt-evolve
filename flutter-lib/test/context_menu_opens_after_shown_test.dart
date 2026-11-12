// A context menu opens as soon as Java says it is filled (its `shown`), as a native one opens once
// SWT.Show has run: the ack arrives between frames, so the menu is put up in the frame that follows,
// not after another one has passed.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

const _menuId = 4100;

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

VMenu _menu() => VMenu()
  ..id = _menuId
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = false
  ..location = (VPoint()
    ..x = 150
    ..y = 153)
  ..items = [
    VMenuItem()
      ..id = _menuId + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Properties...',
  ];

VComposite _host() => VComposite()
  ..id = 4001
  ..style = SWT.NONE
  ..enabled = true
  ..menu = _menu()
  ..bounds = _rect(300, 300)
  ..children = [
    VCanvas()
      ..id = 4002
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(300, 300),
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 300,
        child: CompositeSwt<VComposite>(value: _host()),
      ),
    );

void main() {
  tearDown(VRegistry.instance.clear);

  testWidgets('a right-clicked menu shows in the frames that follow Java filling it', (tester) async {
    await tester.pumpWidget(_wrap());
    await tester.pump();
    await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
    await tester.pump();
    expect(find.text('Properties...'), findsNothing, reason: 'the menu waits for Java to fill it');

    deliverMenuShown(_menuId);
    // The frame the ack's delivery runs in, and the menu's own first one.
    await tester.pump();
    await tester.pump();

    expect(find.text('Properties...'), findsOneWidget);
  });
}
