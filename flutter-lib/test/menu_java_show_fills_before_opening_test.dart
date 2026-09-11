// A MenuManager with setRemoveAllWhenShown empties its menu and refills it on SWT.Show, and the
// client is what fires that event. Only the right-click path asked for it, so a menu shown from Java
// painted whatever the previous fill had left -- or a fill still under way: extra separators, or an
// empty popup when the removeAll had landed and the refill had not.
//
// Natively the platform fires Show as part of showing the menu (win32 does it from WM_INITMENUPOPUP,
// inside TrackPopupMenu), so filling is never separable from showing. Both paths now match that.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

VMenu _shownFromJava() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    VMenuItem()
      ..id = 11
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Section View Editor...',
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: Stack(
          children: [
            Positioned.fill(child: MenuSwt<VMenu>(value: _shownFromJava())),
          ],
        ),
      ),
    );

void main() {
  testWidgets('a menu shown from Java waits for its fill before opening',
      (tester) async {
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();

    expect(find.text('Section View Editor...'), findsNothing,
        reason: 'opening before the fill paints a stale or half-built menu');
  });

  testWidgets('it opens once the fill has been acknowledged', (tester) async {
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 10);

    expect(find.text('Section View Editor...'), findsOneWidget);
  });
}
