// A state query by reference describes every mounted widget under its own key, so the parent that
// holds it names it by `swt` and `id` only. The plain full query keeps each subtree in place.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
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
  ..bounds = _rect(0, 0, 400, 100)
  ..children = [
    VComposite()
      ..id = 2
      ..style = SWT.NONE
      ..visible = true
      ..bounds = _rect(0, 0, 400, 50)
      ..children = [
        VLabel()
          ..id = 3
          ..style = SWT.NONE
          ..visible = true
          ..text = 'inner label'
          ..bounds = _rect(0, 0, 200, 20)
      ],
    VToolBar()
      ..id = 4
      ..style = SWT.FLAT
      ..visible = true
      ..bounds = _rect(0, 50, 400, 30)
      ..items = [
        VToolItem()
          ..id = 5
          ..style = SWT.PUSH
          ..text = 'Run'
      ],
  ];

/// Every widget map nested anywhere inside [node], [node] itself left out.
List<Map<String, dynamic>> _nestedWidgets(Map<String, dynamic> node) {
  final found = <Map<String, dynamic>>[];
  void walk(Object? v) {
    if (v is List) {
      v.forEach(walk);
    } else if (v is Map<String, dynamic>) {
      if (v.containsKey('swt') && v.containsKey('id')) found.add(v);
      v.values.forEach(walk);
    }
  }

  for (final entry in node.entries) {
    if (entry.key != 'swt' && entry.key != 'id') walk(entry.value);
  }
  return found;
}

void main() {
  testWidgets('a parent names its mounted children by reference',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 100, child: CompositeSwt<VComposite>(value: _tree())),
    ));
    await tester.pumpAndSettle();

    final all = jsonDecode(queryAllStatesJson(byReference: true)) as Map<String, dynamic>;

    expect(all.keys, containsAll(['Composite/1', 'Composite/2', 'Label/3', 'ToolBar/4', 'ToolItem/5']));
    expect(all['Label/3']['swt'], 'Label');
    expect(all['Label/3'].length, greaterThan(2), reason: 'a control is described in full under its own key');

    final nestedInRoot = _nestedWidgets(all['Composite/1'] as Map<String, dynamic>);
    expect(nestedInRoot.map((w) => '${w['swt']}/${w['id']}'), unorderedEquals(['Composite/2', 'ToolBar/4']),
        reason: 'a grandchild is not reached through a child that is only named');
    for (final control in nestedInRoot) {
      expect(control.keys, unorderedEquals(['swt', 'id']),
          reason: '${control['swt']}/${control['id']} has a key of its own, so its parent only names it');
    }

    final item = _nestedWidgets(all['ToolBar/4'] as Map<String, dynamic>).single;
    expect(item, {'swt': 'ToolItem', 'id': 5}, reason: 'a mounted item is named the same way');
    expect(all['ToolItem/5']['swt'], 'ToolItem');
    expect(all['ToolItem/5'].length, greaterThan(2));

    final some = jsonDecode(queryStatesJson(['Composite/2', 'Label/3'])) as Map<String, dynamic>;
    expect(some.keys, unorderedEquals(['Composite/2', 'Label/3']));
    expect(_nestedWidgets(some['Composite/2'] as Map<String, dynamic>).single.keys,
        unorderedEquals(['swt', 'id']));
    expect(some['Label/3'], all['Label/3']);
    expect(jsonDecode(queryStateJson(3)!), all['Label/3'],
        reason: 'one widget is answered from its own state, whatever its ancestors hold');

    final full = jsonDecode(queryAllStatesJson()) as Map<String, dynamic>;
    expect(_nestedWidgets(full['Composite/1'] as Map<String, dynamic>).map((w) => '${w['swt']}/${w['id']}'),
        containsAll(['Composite/2', 'Label/3', 'ToolBar/4', 'ToolItem/5']),
        reason: 'the full query still nests every descendant');
  });
}
