// An application can attach a Menu to a control and also show it itself, from its own mouse
// listener. The right-click opens the control's copy; the Display then renders the same menu as a
// shown popup. Both opening painted one menu as two panels a few pixels apart, and the second copy
// took over the menu's channel, so the control's copy stopped hearing that its fill was done.

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

/// [shownByJava] adds the copy the Display mounts for every popup in its shown list.
Widget _wrap({required bool shownByJava}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 300,
        child: Stack(children: [
          Positioned.fill(child: CompositeSwt<VComposite>(value: _host())),
          if (shownByJava)
            Positioned.fill(
              child: MenuSwt<VMenu>(key: const ValueKey(_menuId), value: _menu()),
            ),
        ]),
      ),
    );

int _seq = 0;

/// A partial delivery, as Java sends when Menu.setVisible changes.
void _javaSetsVisible(bool visible) {
  final base = _seq;
  _seq += 1;
  VRegistry.instance.apply('Menu/$_menuId', <String, dynamic>{
    'id': _menuId,
    'swt': 'Menu',
    '_s': _seq,
    '_b': base,
    '_d': <String>['visible'],
    'visible': visible,
  });
}

void main() {
  tearDown(() {
    VRegistry.instance.clear();
    _seq = 0;
  });

  testWidgets('a menu Java shows after the right-click opened it stays one panel',
      (tester) async {
    await tester.pumpWidget(_wrap(shownByJava: false));
    await tester.pump();
    await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    expect(find.text('Properties...'), findsOneWidget);

    _javaSetsVisible(true);
    await tester.pumpWidget(_wrap(shownByJava: true));
    await tester.pump();
    await ackMenuShown(tester, _menuId);

    expect(find.text('Properties...'), findsOneWidget);
  });

  testWidgets('the right-click keeps opening the menu after Java has shown and hidden it',
      (tester) async {
    await tester.pumpWidget(_wrap(shownByJava: false));
    await tester.pump();
    await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
    await tester.pump();
    // Java's show lands before the fill is acknowledged, so both copies are mounted for the ack.
    _javaSetsVisible(true);
    await tester.pumpWidget(_wrap(shownByJava: true));
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    expect(find.text('Properties...'), findsOneWidget);

    await tester.tapAt(const Offset(280, 280));
    await tester.pumpAndSettle();
    _javaSetsVisible(false);
    await tester.pumpWidget(_wrap(shownByJava: false));
    await tester.pumpAndSettle();
    expect(find.text('Properties...'), findsNothing);

    await tester.tapAt(const Offset(150, 150), buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);

    expect(find.text('Properties...'), findsOneWidget);
  });
}
