// A right-click on another tree item while a context menu is open must close
// that menu, select the item and open its menu, as native SWT does.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'support/menu_shown_ack.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingTreeSwt extends TreeSwt<VTree> {
  const _CapturingTreeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTree val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

const _menuId = 100;

VTreeItem _item(int id, String text, {List<VTreeItem>? items}) => VTreeItem()
  ..id = id
  ..text = text
  ..items = items
  ..itemCount = items?.length ?? 0
  ..expanded = items != null;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..menu = (VMenu()
    ..id = _menuId
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = 101
        ..style = SWT.PUSH
        ..enabled = true
        ..text = 'Reload',
    ])
  ..items = [
    _item(10, 'Folder', items: [
      _item(20, 'Child A'),
      _item(21, 'Child B'),
      _item(22, 'Child C'),
    ]),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

List<int> _selectedIds(VTree value) =>
    (value.selection ?? []).map((item) => item.id).toList();

void main() {
  final desktop = TargetPlatformVariant(<TargetPlatform>{
    TargetPlatform.macOS,
    TargetPlatform.linux,
    TargetPlatform.windows,
  });

  testWidgets('right-click on another item while a menu is open opens its menu',
      (tester) async {
    final events = <String>[];
    final value = _tree();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: _CapturingTreeSwt(value: value, onEvent: (ev, _) => events.add(ev)),
      ),
    ));
    await tester.pump();

    final first = tester.getCenter(find.text('Child C'));
    final second = tester.getCenter(find.text('Child A'));

    await tester.tapAt(first, buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);
    expect(find.text('Reload'), findsOneWidget);
    expect(_selectedIds(value), equals([22]));

    events.clear();
    // Above the open popup, which hangs below the pointer that opened it.
    await tester.tapAt(second, buttons: kSecondaryButton);
    await tester.pump();
    await ackMenuShown(tester, _menuId);

    expect(_selectedIds(value), equals([20]),
        reason: 'the right-click that dismisses the menu still reaches the tree');
    expect(events, contains('MenuDetect/MenuDetect'));
    expect(find.text('Reload'), findsOneWidget,
        reason: 'the right-clicked item opens its own menu');
  }, variant: desktop);
}
