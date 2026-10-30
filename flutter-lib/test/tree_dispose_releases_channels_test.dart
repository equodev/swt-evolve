// A handler registered on a widget's channel holds the widget's State, and through it the whole
// subtree it built: one left behind after the widget is gone keeps all of that alive.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const int _treeId = 920;

VTree _tree() => VTree()
  ..id = _treeId
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 400)
  ..items = [
    for (var i = 0; i < 3; i++)
      VTreeItem()
        ..id = 921 + i
        ..text = 'Node $i',
  ];

Iterable<String> _treeChannels() =>
    EquoCommService.commForTesting.handledChannels.where((c) => c.startsWith('Tree/$_treeId/'));

void main() {
  testWidgets('a disposed tree leaves no handler on its channels', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 300, height: 400, child: TreeSwt(value: _tree())),
    ));
    await tester.pumpAndSettle();

    await tester.pumpWidget(const EvolveApp(theme: ThemeMode.light, contentWidget: SizedBox()));
    await tester.pumpAndSettle();

    expect(_treeChannels(), isEmpty);
  });
}
