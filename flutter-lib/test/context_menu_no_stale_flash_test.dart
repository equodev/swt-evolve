// A right-click context menu is rebuilt from the live selection on SWT.Show
// (Eclipse/E4: visibleWhen filtering, removeAllWhenShown, dynamic contributions).
// The popup, however, still holds the item set from the PREVIOUS show, so opening
// it immediately paints that stale set for the whole Show round-trip before the
// correct one arrives — the wrong-menu flash. The popup must defer opening until
// Java acks the rebuild, so it never shows the stale items.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VTreeItem _item(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

VMenu _menu(List<String> labels) => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    for (var i = 0; i < labels.length; i++)
      VMenuItem()
        ..id = 101 + i
        ..style = SWT.PUSH
        ..enabled = true
        ..text = labels[i],
  ];

VTree _tree({required List<VTreeItem> items, required VMenu menu}) => VTree()
  ..id = 1
  ..style = SWT.MULTI
  ..enabled = true
  ..items = items
  ..menu = menu
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: TreeSwt<VTree>(value: value),
      ),
    );

void main() {
  testWidgets(
      'a right-click does not open the context menu with the stale pre-Show item set',
      (tester) async {
    // The popup still holds the previous show's item set — for an E4 popup that is
    // NOT what belongs on the current selection.
    final value = _tree(
      items: [_item(10, 'Node 0'), _item(11, 'Node 1')],
      menu: _menu(['Stale Action']),
    );
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await tester.tap(find.text('Node 1'), buttons: kSecondaryButton);
    await tester.pump();
    await tester.pump();

    // Without the fix the popup opened immediately, painting the stale item; with
    // the fix it waits for the SWT.Show rebuild ack before opening.
    expect(find.text('Stale Action'), findsNothing,
        reason: 'the popup must wait for the SWT.Show rebuild instead of opening '
            'with the stale item set');
  });
}
