// Hovering an item of the parent menu makes it the current item, so a cascade the keyboard opened
// closes and stops being highlighted.

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
import 'package:swtflutter/src/theme/theme_extensions/menuitem_theme_extension.dart';

import 'support/menu_shown_ack.dart';

const _openId = 11;
const _moreId = 12;
const _firstId = 21;
const _secondId = 22;
const _displayId = 5;

VMenuItem _item(int id, String text, {int style = SWT.PUSH}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VMenu _menu() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    _item(_openId, 'Open...'),
    _item(_moreId, 'More', style: SWT.CASCADE)
      ..menu = (VMenu()
        ..id = 20
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = [_item(_firstId, 'First'), _item(_secondId, 'Second')]),
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

bool _selected(int id) =>
    _sent('Selection/Selection').any((channel) => channel.contains('/$id/'));

Future<void> _open(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('More'), findsOneWidget, reason: 'the menu must be open');
  EquoCommService.commForTesting.clearSentFrames();
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

bool _lit(WidgetTester tester, String label) {
  final theme = Theme.of(tester.element(find.text('Open...'))).extension<MenuItemThemeExtension>()!;
  bool painted(Color? c) => c == theme.hoverBackgroundColor;
  return tester
          .widgetList<Material>(find.ancestor(of: find.text(label), matching: find.byType(Material)))
          .any((m) => painted(m.color)) ||
      tester
          .widgetList<AnimatedContainer>(
              find.ancestor(of: find.text(label), matching: find.byType(AnimatedContainer)))
          .any((c) => painted((c.decoration as BoxDecoration?)?.color));
}

void main() {
  testWidgets('hovering a parent item closes a cascade opened from the keyboard', (tester) async {
    await _open(tester);
    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    await _press(tester, LogicalKeyboardKey.arrowDown);
    expect(find.text('Second'), findsOneWidget, reason: 'sanity: the keyboard is in the cascade');

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.text('Open...')));
    await tester.pumpAndSettle();

    expect(find.text('Second'), findsNothing, reason: 'the pointer left the cascade for a sibling');
    expect(_lit(tester, 'More'), isFalse);
    expect(_lit(tester, 'Open...'), isTrue);

    await _press(tester, LogicalKeyboardKey.enter);
    expect(_selected(_openId), isTrue);
    expect(_selected(_secondId), isFalse);
  });
}
