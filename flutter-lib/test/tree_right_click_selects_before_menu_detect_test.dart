// A right-click on an unselected tree item must select it before MenuDetect
// reaches Java, as Win32 and GTK do on the button press: an application that
// builds its menu from the selection in a MenuDetect listener otherwise builds
// it for the previous selection.

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

class _CapturingTreeSwt extends TreeSwt<VTree> {
  const _CapturingTreeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTree val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VTreeItem _item(int id, String text, {List<VTreeItem>? items}) => VTreeItem()
  ..id = id
  ..text = text
  ..items = items
  ..itemCount = items?.length ?? 0
  ..expanded = items != null;

VMenu _menu() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Menu Action',
  ];

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..menu = _menu()
  ..items = [
    _item(10, 'Folder', items: [_item(20, 'Child A'), _item(21, 'Child B')]),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

void main() {
  final desktop = TargetPlatformVariant(<TargetPlatform>{
    TargetPlatform.macOS,
    TargetPlatform.linux,
    TargetPlatform.windows,
  });

  testWidgets('right-click on an unselected child item selects it before MenuDetect',
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

    await tester.tap(find.text('Child B'), buttons: kSecondaryButton);
    await tester.pump();

    expect(events.where((e) => e == 'Selection/Selection'), hasLength(1));
    expect(events, contains('MenuDetect/MenuDetect'));
    expect(events.indexOf('Mouse/MouseDown'),
        lessThan(events.indexOf('Selection/Selection')));
    expect(events.indexOf('Selection/Selection'),
        lessThan(events.indexOf('MenuDetect/MenuDetect')),
        reason: 'MenuDetect listeners read the selection, so it must already '
            'be the right-clicked item');
  }, variant: desktop);
}
