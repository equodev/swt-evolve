// One gesture, one SWT.Show. The client asks Java to fill the menu and opens it on the answer, and
// the anchor then reported that opening as an open of its own -- so the application emptied and
// refilled a menu that was already on screen. That second fill is what left redundant separators
// showing: a MenuManager drops them on the turn after it fills, and the refill put them back.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

/// The generated MenuSwt with its outgoing events captured instead of handed to the comm service.
class _CapturingMenuSwt extends MenuSwt<VMenu> {
  const _CapturingMenuSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VMenu val, String ev, VEvent? payload) => onEvent(ev);
}

VMenu _shownFromJava() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Section View Editor...',
  ];

void main() {
  testWidgets('showing a menu asks Java to fill it exactly once', (tester) async {
    final events = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 300,
        child: Stack(children: [
          Positioned.fill(
            child: _CapturingMenuSwt(
              value: _shownFromJava(),
              onEvent: events.add,
            ),
          ),
        ]),
      ),
    ));
    await tester.pump();
    await ackMenuShown(tester, 100);

    expect(find.text('Section View Editor...'), findsOneWidget,
        reason: 'the menu still has to open');
    expect(events.where((e) => e.endsWith('Show')).length, 1,
        reason: 'a second Show refills a menu that is already on screen');
  });
}
