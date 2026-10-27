// An open popup keeps the keyboard when the web engine parks Flutter's focus at the root scope, as
// a semantics reorder under the focused item does (a tooltip appearing over the menu, for one).

import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

class _CapturingMenuSwt extends MenuSwt<VMenu> {
  const _CapturingMenuSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VMenu val, String ev, VEvent? payload) => onEvent(ev);
}

VMenuItem _item(int id, String text) => VMenuItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text;

VMenu _openPopup() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [_item(11, 'More A'), _item(12, 'More B')];

Future<List<String>> _openMenu(WidgetTester tester) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: Stack(children: [
        Positioned.fill(
          child: _CapturingMenuSwt(value: _openPopup(), onEvent: events.add),
        ),
      ]),
    ),
  ));
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 10);
  expect(find.text('More A'), findsOneWidget, reason: 'the popup must be open');
  return events;
}

/// What the framework does when the web engine reports the view lost focus.
Future<void> _parkFocusAtRoot(WidgetTester tester) async {
  tester.binding.handleViewFocusChanged(ViewFocusEvent(
    viewId: tester.view.viewId,
    state: ViewFocusState.unfocused,
    direction: ViewFocusDirection.undefined,
  ));
  await tester.pump();
  expect(FocusManager.instance.primaryFocus, same(FocusManager.instance.rootScope),
      reason: 'precondition: the focus is parked at the root scope');
}

void main() {
  testWidgets('Escape closes an open popup whose focus was parked at the root', (tester) async {
    final events = await _openMenu(tester);
    await _parkFocusAtRoot(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();

    expect(find.text('More A'), findsNothing, reason: 'Escape must close the open popup');
    expect(events, contains('Menu/Hide'));
  });

  testWidgets('Down takes a parked focus back into the open popup', (tester) async {
    await _openMenu(tester);
    await _parkFocusAtRoot(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pumpAndSettle();

    expect(FocusManager.instance.primaryFocus, isNot(same(FocusManager.instance.rootScope)),
        reason: 'the arrow key must put the focus back on a menu item');
    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pumpAndSettle();
    expect(find.text('More A'), findsNothing,
        reason: 'once back in the menu, the menu\'s own Escape closes it');
  });
}
