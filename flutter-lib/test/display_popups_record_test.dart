// The popups the display draws arrive in a record of their own, sent each time one shows or hides,
// while the display's own record is sent only when something else about it changes. A popup a
// control names as its menu stays held through that control when the display stops listing it, so
// showing it again needs only its name.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

const _displayId = 5;

VMenu _popup(int id) => VMenu()
  ..id = id
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = false
  ..items = [
    VMenuItem()
      ..id = id + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Item of $id',
  ];

void _popups(List<VMenu> popups) =>
    VRegistry.instance.deliverHolderForTesting('DisplayPopups', _displayId, VShellPopups()..popups = popups);

/// The display's layer for the popup [id].
Finder _drawn(int id) => find.byWidgetPredicate((w) => w is KeyedSubtree && w.key == ValueKey(id));

/// Every frame received so far applied: when the registry lets go of what nothing holds.
Future<void> _applied() {
  final applied = Completer<void>();
  EquoCommBase.whenApplied(applied.complete);
  return applied.future;
}

VRectangle _rect(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

/// The display's own record: one shell, and whichever shell is active.
VDisplay _display({int activeShellId = 0}) => VDisplay()
  ..shells = [
    VShell()
      ..id = 1001
      ..style = SWT.SHELL_TRIM
      ..visible = true
      ..bounds = _rect(800, 600)
      ..children = [
        VComposite()
          ..id = 1002
          ..style = SWT.NONE
          ..bounds = _rect(100, 100)
      ]
  ]
  ..mainShellId = 1001
  ..activeShellId = activeShellId;

Future<void> _pumpDisplay(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(value: VDisplay()..id = _displayId),
  ));
  VRegistry.instance.deliverHolderForTesting('Display', _displayId, _display());
  await tester.pump();
}

void main() {
  tearDown(VRegistry.instance.clear);

  testWidgets('the display draws the popups their own record lists, whatever its own record says',
      (tester) async {
    await _pumpDisplay(tester);
    final popup = _popup(4100);

    _popups([popup]);
    await tester.pump();
    expect(_drawn(4100), findsOneWidget);

    VRegistry.instance.deliverHolderForTesting('Display', _displayId, _display(activeShellId: 1001));
    await tester.pump();
    expect(_drawn(4100), findsOneWidget,
        reason: "the display's record does not carry its popups, so it does not take them down");

    _popups([]);
    await tester.pump();
    expect(_drawn(4100), findsNothing);
  });

  testWidgets('a popup a control names stays held when the display stops listing it', (tester) async {
    await _pumpDisplay(tester);
    final named = _popup(4100);
    final host = VComposite()
      ..id = 4001
      ..style = SWT.NONE
      ..menu = named;
    VRegistry.instance.register(host);
    VRegistry.instance.holds('Shell/1', [host]);
    final alone = _popup(4200);

    _popups([named, alone]);
    await tester.pump();
    _popups([]);
    await tester.pump();
    await tester.runAsync(_applied);

    expect(VRegistry.instance.valueOn('Menu/4100'), isNotNull, reason: 'its control still names it');
    expect(VRegistry.instance.valueOn('MenuItem/4101'), isNotNull);
    expect(VRegistry.instance.valueOn('Menu/4200'), isNull, reason: 'nothing else held it');
  });
}
