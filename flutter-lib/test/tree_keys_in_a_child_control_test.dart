// A key typed into a control the tree holds -- a cell editor's field -- is the field's: the tree's
// keyboard navigation answers only the keys it receives itself. A key the tree marks handled is never
// typed on the web, so a space in an inline editor went missing.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..columns = []
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'a',
  ]
  ..selection = [
    VTreeItem()
      ..id = 10
      ..text = 'a',
  ]
  ..children = [
    VText()
      ..id = 60
      ..enabled = true
      ..visible = true
      ..text = ''
      ..bounds = _rect(0, 0, 120, 22),
  ]
  ..bounds = _rect(0, 0, 400, 300);

void main() {
  testWidgets('a space typed into a control inside the tree is left to that control', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree())),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(TextSwt<VText>));
    await tester.pump();

    expect(await tester.sendKeyEvent(LogicalKeyboardKey.space), isFalse);
  });
}
