// A menu made only of cascades still takes the keyboard as soon as it opens. As natively, a cascade
// opens on Right or hover, not because Up or Down made it the current item.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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

const _analysisId = 11;
const _reportsId = 12;
const _chartId = 21;
const _tableId = 22;
const _displayId = 5;

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenuItem _cascade(int id, String text, int menuId, List<VMenuItem> items) =>
    _item(id, text, style: SWT.CASCADE)
      ..menu = (VMenu()
        ..id = menuId
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = items);

VMenu _menu() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    _cascade(_analysisId, 'Analysis', 20, [_item(_chartId, 'Chart'), _item(_tableId, 'Table')]),
    _cascade(_reportsId, 'Reports', 30, [_item(31, 'Summary')]),
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 400,
        child: Stack(
          children: [
            Positioned.fill(child: DisplaySwt(value: VDisplay()..id = _displayId)),
            Positioned.fill(child: MenuSwt<VMenu>(value: _menu())),
          ],
        ),
      ),
    );

List<String> _sent(String event) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.endsWith('/$event'))
    .toList();

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('the keyboard opens a cascade and selects its item', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 10);
    expect(find.text('Analysis'), findsOneWidget, reason: 'the menu must be open');

    expect(_sent('Arm/Arm').where((c) => c.contains('/$_analysisId/')), isNotEmpty,
        reason: 'the first cascade must be the current item; focus: ${FocusManager.instance.primaryFocus}');

    expect(find.text('Chart'), findsNothing, reason: 'being the current item must not open the cascade');

    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(find.text('Chart'), findsOneWidget, reason: 'ArrowRight must open the cascade');
    expect(_sent('Menu/Show').where((c) => c.startsWith('Menu/20/')), isNotEmpty,
        reason: 'the opened cascade must ask Java to fill it; sent: ${_sent('Menu/Show')}');

    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.enter);

    expect(_sent('Selection/Selection').where((c) => c.contains('/$_tableId/')), isNotEmpty,
        reason: 'sent: ${_sent('Selection/Selection')}; focus: ${FocusManager.instance.primaryFocus}');
    expect(_sent('Key/KeyDown'), isEmpty, reason: 'the open menu must keep every key');
  });

  testWidgets('ArrowDown moves between the cascades', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 10);
    EquoCommService.commForTesting.clearSentFrames();

    await _press(tester, LogicalKeyboardKey.arrowDown);

    expect(_sent('Arm/Arm').where((c) => c.contains('/$_reportsId/')), isNotEmpty,
        reason: 'sent: ${_sent('Arm/Arm')}; focus: ${FocusManager.instance.primaryFocus}');
    expect(find.text('Summary'), findsNothing, reason: 'passing over a cascade must not open it');
    expect(_sent('Menu/Show'), isEmpty, reason: 'no cascade was opened');
  });

  testWidgets('hovering a cascade opens it, with one SWT.Show', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 10);
    EquoCommService.commForTesting.clearSentFrames();

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.text('Analysis')));
    await tester.pumpAndSettle();

    expect(find.text('Chart'), findsOneWidget, reason: 'hover must open the cascade');
    expect(_sent('Menu/Show').where((c) => c.startsWith('Menu/20/')), hasLength(1),
        reason: 'one opening, one SWT.Show; sent: ${_sent('Menu/Show')}');
  });

  testWidgets('a tap opens a cascade', (tester) async {
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await ackMenuShown(tester, 10);

    await tester.tap(find.text('Reports'));
    await tester.pumpAndSettle();

    expect(find.text('Summary'), findsOneWidget, reason: 'a touch has no hover, the tap must open it');
  });
}
