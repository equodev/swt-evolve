// A menu has one current item: hover sets it, the arrow keys move on from it, and each change fires SWT.Arm.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/menuitem_theme_extension.dart';

import 'support/menu_shown_ack.dart';

const _labels = ['Open...', 'Show grid', 'TVD', 'TVDSS'];

VMenu _menu() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    for (var i = 0; i < _labels.length; i++)
      VMenuItem()
        ..id = 11 + i
        ..style = SWT.PUSH
        ..enabled = true
        ..text = _labels[i],
  ];

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: Stack(
          children: [Positioned.fill(child: MenuSwt<VMenu>(value: _menu()))],
        ),
      ),
    );

int _idOf(String label) => 11 + _labels.indexOf(label);

List<String> _sent(String event) => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .map((f) => f.$1)
    .where((channel) => channel.endsWith('/$event'))
    .toList();

bool _sentFor(String event, String label) =>
    _sent(event).any((channel) => channel.contains('/${_idOf(label)}/'));

Future<TestGesture> _open(WidgetTester tester) async {
  EquoCommBase.recordSentFrames = true;
  addTearDown(() => EquoCommBase.recordSentFrames = false);
  await tester.pumpWidget(_wrap());
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('TVD'), findsOneWidget, reason: 'the menu must be open');
  EquoCommService.commForTesting.clearSentFrames();

  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  return mouse;
}

Future<void> _hover(WidgetTester tester, TestGesture mouse, String label) async {
  await mouse.moveTo(tester.getCenter(find.text(label)));
  await tester.pumpAndSettle();
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

List<String> _highlighted(WidgetTester tester) {
  final theme = Theme.of(tester.element(find.text('TVD')))
      .extension<MenuItemThemeExtension>()!;
  return [
    for (final label in _labels)
      if (tester
              .widgetList<AnimatedContainer>(find.ancestor(
                  of: find.text(label), matching: find.byType(AnimatedContainer)))
              .map((c) => (c.decoration as BoxDecoration?)?.color)
              .contains(theme.hoverBackgroundColor))
        label,
  ];
}

void main() {
  testWidgets('ArrowDown moves on from the hovered item', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'TVD');
    await _press(tester, LogicalKeyboardKey.arrowDown);
    await _press(tester, LogicalKeyboardKey.enter);

    expect(_sentFor('Selection/Selection', 'TVDSS'), isTrue,
        reason: 'Enter after hover TVD + ArrowDown must select TVDSS; sent: '
            '${_sent('Selection/Selection')}');
  });

  testWidgets('only one item is highlighted after hover and ArrowDown', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'TVD');
    expect(_highlighted(tester), ['TVD']);

    await _press(tester, LogicalKeyboardKey.arrowDown);
    expect(_highlighted(tester), ['TVDSS']);
  });

  testWidgets('hover and the arrow keys fire Arm on the item they land on', (tester) async {
    final mouse = await _open(tester);
    await _hover(tester, mouse, 'TVD');
    expect(_sentFor('Arm/Arm', 'TVD'), isTrue, reason: 'sent: ${_sent('Arm/Arm')}');

    await _press(tester, LogicalKeyboardKey.arrowDown);
    expect(_sentFor('Arm/Arm', 'TVDSS'), isTrue, reason: 'sent: ${_sent('Arm/Arm')}');
  });
}
