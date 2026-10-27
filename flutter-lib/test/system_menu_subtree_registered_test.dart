// The application menu is described inside the Display payload and nowhere else, so the display is
// the only thing that can register it. Java counts a widget it has written as delivered and names it
// from then on, and a name resolves to nothing on a client that never held the widget.
//
// A separator is what shows it: it never changes, so it is only ever named. Resolved against an
// empty registry it comes back styleless, and the menu draws it as a full-height blank row.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/menu_shown_ack.dart';

/// A display of its own per test: DisplaySwt seeds a fresh State from a module-level cache keyed
/// by display id, so sharing one would hand the next test the payload the previous one ended on.
int _nextDisplayId = 7701;
const int _systemMenuId = 7710;
const int _appCascadeId = 7711;
const int _dropDownId = 7712;
const int _aboutId = 7713;
const int _separatorId = 7714;
const int _preferencesId = 7715;
const int _popupId = 7799;

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body),
/// which is the only way a Display update reaches a mounted DisplaySwt.
void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

/// The application menu as Java first describes it: everything in full, once.
VMenu _systemMenuDescribedInFull() => VMenu()
  ..id = _systemMenuId
  ..style = SWT.BAR
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = _appCascadeId
      ..style = SWT.CASCADE
      ..enabled = true
      ..text = 'Example App'
      ..menu = (VMenu()
        ..id = _dropDownId
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = [
          VMenuItem()
            ..id = _aboutId
            ..style = SWT.PUSH
            ..enabled = true
            ..text = 'About Example App',
          VMenuItem()
            ..id = _separatorId
            ..style = SWT.SEPARATOR
            ..enabled = true,
          VMenuItem()
            ..id = _preferencesId
            ..style = SWT.PUSH
            ..enabled = true
            ..text = 'Preferences...',
        ]),
  ];

/// The next Display payload, where the items Java already delivered travel as names. Only the
/// separator was named in the application this came from -- the others had changed -- but naming all three keeps
/// the fixture about the mechanism rather than about which item happened to be dirty.
Map<String, dynamic> _systemMenuNamedNotDescribed(int displayId) => {
      'id': displayId,
      'swt': 'Display',
      'systemMenu': {
        'id': _systemMenuId,
        'swt': 'Menu',
        '_s': 20,
        'style': SWT.BAR,
        'enabled': true,
        'items': [
          {
            'id': _appCascadeId,
            'swt': 'MenuItem',
            '_s': 21,
            'style': SWT.CASCADE,
            'enabled': true,
            'text': 'Example App',
            'menu': {
              'id': _dropDownId,
              'swt': 'Menu',
              '_s': 22,
              'style': SWT.DROP_DOWN,
              'enabled': true,
              'items': [
                {'id': _aboutId, 'swt': 'MenuItem', '_r': 1},
                {'id': _separatorId, 'swt': 'MenuItem', '_r': 1},
                {'id': _preferencesId, 'swt': 'MenuItem', '_r': 1},
              ],
            },
          },
        ],
      },
    };

/// The two payloads the reported session sends, in order: the display that describes the
/// application menu, then the one that only names what is inside it.
Future<void> _pumpDisplayThenName(WidgetTester tester) async {
  final displayId = _nextDisplayId++;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..id = displayId
        ..systemMenu = _systemMenuDescribedInFull(),
    ),
  ));
  await tester.pumpAndSettle();

  _receiveJson('Display/$displayId', _systemMenuNamedNotDescribed(displayId));
  await tester.pumpAndSettle();
}

/// What the drop-down is built from: the cascade's own menu items, which is what
/// `_MenuBarItemState` and `_CascadeMenuItemRow` hand to `MenuItemSwt`.
List<VMenuItem> _dropDownItems() =>
    applicationMenuItems().single.menu!.items!;

int _separatorsOnScreen(WidgetTester tester) => tester
    .widgetList<MenuItemSwt>(find.byType(MenuItemSwt))
    .where((w) => (w.value.style & SWT.SEPARATOR) != 0)
    .length;

/// Rows with neither text nor a style that says what they are: the reported ghost rows.
int _blankRowsOnScreen(WidgetTester tester) => tester
    .widgetList<MenuItemSwt>(find.byType(MenuItemSwt))
    .where((w) => w.value.style == SWT.NONE && (w.value.text ?? '').isEmpty)
    .length;

void main() {
  setUp(() {
    resetConfigFlags();
    VRegistry.instance.clear();
  });
  tearDown(() {
    resetConfigFlags();
    VRegistry.instance.clear();
  });

  testWidgets('the display holds what its system menu carries', (tester) async {
    await _pumpDisplayThenName(tester);

    final held = VRegistry.instance.valueOn('MenuItem/$_separatorId');
    expect(held, isNotNull,
        reason: 'Java delivered this item inside the Display payload and names it from then on');
    expect(held!.style & SWT.SEPARATOR, isNot(0));
  });

  testWidgets('a named separator still resolves to a separator', (tester) async {
    await _pumpDisplayThenName(tester);

    final items = _dropDownItems();
    expect(items.map((i) => i.id), [_aboutId, _separatorId, _preferencesId]);
    expect(items[1].style & SWT.SEPARATOR, isNot(0),
        reason: 'the drop-down is built from these, so a nameless stub here is a blank row');
    expect(items[0].text, 'About Example App');
  });

  testWidgets('the first open of the menu draws lines, not blank rows', (tester) async {
    await _pumpDisplayThenName(tester);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        // A popup of its own rather than the drop-down's own id: this is about what the items
        // draw as, and the held drop-down is a DROP_DOWN that nothing has asked to be shown.
        child: MenuSwt<VMenu>(
          value: VMenu()
            ..id = _popupId
            ..style = SWT.POP_UP
            ..enabled = true
            ..visible = true
            ..items = _dropDownItems(),
        ),
      ),
    ));
    await tester.pump();
    await ackMenuShown(tester, _popupId);

    expect(_blankRowsOnScreen(tester), 0);
    expect(_separatorsOnScreen(tester), 1);
    expect(find.text('About Example App'), findsOneWidget);
    expect(find.text('Preferences...'), findsOneWidget);
  });
}
