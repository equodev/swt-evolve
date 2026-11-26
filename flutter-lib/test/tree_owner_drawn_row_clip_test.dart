// Java clips an owner-drawn row of a Tree with no columns with no right edge, so a resize leaves the
// row's painting as it was. The row's overlay cuts it at the row's own edge instead, where native
// SWT's one column ends; a Tree with columns clips each cell in Java and needs no such cut.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';

VTree _tree({List<VTreeColumn>? columns}) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..columns = columns
  ..items = [
    VTreeItem()
      ..id = 11
      ..texts = ['src', 'more']
      ..text = 'src'
      ..paintedTexts = [0],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _mount(WidgetTester tester, VTree tree) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: tree)),
  ));
  await tester.pumpAndSettle();
}

Finder _overlayClip() =>
    find.descendant(
        of: find.byType(OwnerDrawnRowOverlay, skipOffstage: false),
        matching: find.byType(ClipRect, skipOffstage: false),
        skipOffstage: false);

void main() {
  testWidgets('a row of a Tree with no columns cuts its painting at the row\'s edge', (tester) async {
    await _mount(tester, _tree());

    expect(tester.widget<OwnerDrawnRowOverlay>(find.byType(OwnerDrawnRowOverlay)).clipped, isTrue);
    expect(_overlayClip(), findsOneWidget);
  });

  testWidgets('a row of a Tree with columns leaves the cut to Java\'s cell clips', (tester) async {
    await _mount(
        tester,
        _tree(columns: [
          VTreeColumn()
            ..id = 21
            ..width = 150,
          VTreeColumn()
            ..id = 22
            ..width = 150,
        ]));

    expect(tester.widget<OwnerDrawnRowOverlay>(find.byType(OwnerDrawnRowOverlay)).clipped, isFalse);
    expect(_overlayClip(), findsNothing);
  });
}
