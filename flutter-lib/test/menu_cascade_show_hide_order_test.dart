// A cascade's submenu reports its Hide when it closes, and a chosen item is selected only after
// every open menu has reported its Hide, in the order native SWT sends them.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'support/menu_shown_ack.dart';

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenu _submenu(int id, List<VMenuItem> items) => VMenu()
  ..id = id
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = items;

VMenu _menu() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    _item(11, 'Open...'),
    _item(12, 'Geology', style: SWT.CASCADE)
      ..menu = _submenu(20, [_item(21, 'Faults'), _item(22, 'Horizons', style: SWT.CHECK)]),
    _item(13, 'Geophysics', style: SWT.CASCADE)
      ..menu = _submenu(30, [_item(31, 'Seismic')]),
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: Stack(
          children: [
            Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
            Positioned.fill(child: MenuSwt<VMenu>(value: _menu())),
          ],
        ),
      ),
    );

const _tracked = ['/Menu/Show', '/Menu/Hide', '/Arm/Arm', '/Selection/Selection'];

List<String> _events() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => _tracked.any(channel.endsWith))
    .toList();

Future<TestGesture> _open(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('Geology'), findsOneWidget, reason: 'the menu must be open');
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  EquoCommService.commForTesting.clearSentFrames();
  return mouse;
}

Future<void> _hover(WidgetTester tester, TestGesture mouse, String label) async {
  await mouse.moveTo(tester.getCenter(find.text(label)));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('moving from one cascade to the next hides the first submenu', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'Geology');
    expect(find.text('Faults'), findsOneWidget, reason: 'sanity: the Geology submenu is open');
    await _hover(tester, mouse, 'Geophysics');
    expect(find.text('Faults'), findsNothing, reason: 'sanity: the Geology submenu closed');

    expect(_events(), [
      'MenuItem/12/Arm/Arm',
      'Menu/20/Menu/Show',
      'Menu/20/Menu/Hide',
      'MenuItem/13/Arm/Arm',
      'Menu/30/Menu/Show',
    ]);
  });

  for (final (label, id) in [('Faults', 21), ('Horizons', 22)]) {
    testWidgets('choosing $label in a submenu hides every menu before selecting it',
        (tester) async {
      final mouse = await _open(tester);
      await _hover(tester, mouse, 'Geology');
      await _hover(tester, mouse, label);
      EquoCommService.commForTesting.clearSentFrames();

      await tester.tap(find.text(label));
      await tester.pumpAndSettle();

      expect(_events(), [
        'Menu/20/Menu/Hide',
        'Menu/10/Menu/Hide',
        'MenuItem/$id/Selection/Selection',
      ]);
    });
  }

  testWidgets('choosing a top-level item hides the menu before selecting it', (tester) async {
    await _open(tester);
    await tester.tap(find.text('Open...'));
    await tester.pumpAndSettle();

    expect(_events().where((e) => !e.endsWith('/Arm/Arm')), [
      'Menu/10/Menu/Hide',
      'MenuItem/11/Selection/Selection',
    ]);
  });
}
