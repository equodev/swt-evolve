// A cascade asks for its items by sending Show, and JFace fills or changes the submenu in answer.
// That answer is a delivery to the submenu's own channel, and the cascade shows it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'delivery/support/deliver.dart';
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
    _item(12, 'Show View', style: SWT.CASCADE)..menu = _submenu(20, []),
    _item(13, 'Window', style: SWT.CASCADE)..menu = _submenu(30, [_item(31, 'Minimize')]),
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

/// Java's answer to a Show: the submenu described whole, with the items JFace put in it.
void _javaFills(int menuId, List<VMenuItem> items) => applyWhole(_submenu(menuId, items)..seq = 1);

Future<void> _hover(WidgetTester tester, TestGesture mouse, String label) async {
  await mouse.moveTo(tester.getCenter(find.text(label)));
  await tester.pumpAndSettle();
}

Future<TestGesture> _open(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  EquoCommService.commForTesting.clearSentFrames();
  return mouse;
}

bool _sent(String channel) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .any((f) => f.$1 == channel);

void main() {
  tearDown(() => VRegistry.instance.clear());

  testWidgets('an empty cascade shows the items Java fills it with after its Show', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'Show View');
    expect(_sent('Menu/20/Menu/Show'), isTrue, reason: 'sanity: the cascade asked for its items');
    expect(find.text('Console'), findsNothing);

    _javaFills(20, [_item(21, 'Console'), _item(22, 'Problems')]);
    await tester.pumpAndSettle();

    expect(find.text('Console'), findsOneWidget);
    expect(find.text('Problems'), findsOneWidget);
  });

  testWidgets('a cascade shows an item Java adds when it is shown', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'Window');
    expect(find.text('Minimize'), findsOneWidget, reason: 'sanity: the submenu is open');

    _javaFills(30, [_item(31, 'Minimize'), _item(32, 'Editor')]);
    await tester.pumpAndSettle();

    expect(find.text('Editor'), findsOneWidget);
  });
}
