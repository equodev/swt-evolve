// A state query for some widgets answers exactly those, mounted ones from their own state and items
// drawn inline by their parent (TableItem) from the parent's value, the same as the whole snapshot
// describes them.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/test_harness.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _tree() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..visible = true
  ..bounds = _rect(0, 0, 400, 200)
  ..children = [
    VLabel()
      ..id = 2
      ..style = SWT.NONE
      ..visible = true
      ..text = 'a label'
      ..bounds = _rect(0, 0, 200, 20),
    VTable()
      ..id = 3
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 20, 400, 150)
      ..columns = [VTableColumn()
        ..id = 4
        ..width = 150
        ..text = 'Name']
      ..items = [
        VTableItem()
          ..id = 10
          ..texts = ['first'],
        VTableItem()
          ..id = 11
          ..texts = ['second'],
      ],
  ];

Map<String, dynamic> _decode(String json) => jsonDecode(json) as Map<String, dynamic>;

void main() {
  testWidgets('a query for some widgets answers those, inline items included', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 200, child: CompositeSwt<VComposite>(value: _tree())),
    ));
    await tester.pumpAndSettle();

    final all = _decode(queryAllStatesJson(byReference: true));
    expect(all.keys, containsAll(['Label/2', 'Table/3', 'TableItem/10', 'TableItem/11']));

    final items = _decode(queryStatesOfKindJson(['TableItem/']));
    List<String> inSnapshotOrder(bool Function(String key) keep) => all.keys.where(keep).toList();
    expect(items.keys.toList(), inSnapshotOrder((k) => k.startsWith('TableItem/')),
        reason: 'in the order the whole snapshot lists them');
    expect(items['TableItem/10'], all['TableItem/10']);
    expect(items['TableItem/11'], all['TableItem/11']);

    final labelsAndTables = _decode(queryStatesOfKindJson(['Label/', 'Table/']));
    expect(labelsAndTables.keys.toList(),
        inSnapshotOrder((k) => k.startsWith('Label/') || k.startsWith('Table/')));
    expect(labelsAndTables['Table/3'], all['Table/3']);

    final some = _decode(queryStatesJson(['TableItem/11', 'Label/2', 'Shell/404']));
    expect(some.keys, unorderedEquals(['TableItem/11', 'Label/2']), reason: 'an unknown key is left out');
    expect(some['TableItem/11'], all['TableItem/11']);
    expect(some['Label/2'], all['Label/2']);

    final held = VLabel()
      ..id = 99
      ..style = SWT.NONE
      ..text = 'held and listened to, never rendered';
    VRegistry.instance.register(held);
    VRegistry.instance.watchAt(VRegistry.keyOf(held), (_) {});
    expect(_decode(queryStatesJson(['Label/99'])), isEmpty,
        reason: 'a widget nothing on screen renders or holds is not reported, whoever listens to it');
    expect(_decode(queryStatesOfKindJson(['Label/'])).keys, ['Label/2']);
  });
}
