// A double click on a Tree's column header is that column's DefaultSelection on Win32 and GTK:
// the first click is its Selection, the second its DefaultSelection.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..linesVisible = false
  ..columns = [
    VTreeColumn()
      ..id = 2
      ..width = 200
      ..text = 'Name',
    VTreeColumn()
      ..id = 3
      ..width = 200
      ..text = 'Value',
  ]
  ..items = [
    VTreeItem()
      ..id = 10
      ..texts = ['a', 'b'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 600
    ..height = 300);

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);
  tearDown(() => DoubleTapDetector.clock = DateTime.now);

  testWidgets('a double click on a header is its Selection, then its DefaultSelection', (tester) async {
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);
    final sent = <String>[];
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.startsWith('TreeColumn/') && action.contains('/Selection/')) sent.add(action);
    };
    addTearDown(() => EquoCommBase.sendObserverForTesting = null);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 300, child: TreeSwt<VTree>(value: _tree())),
    ));
    await tester.pump();

    var fake = DateTime(2020);
    DoubleTapDetector.clock = () => fake;
    await tester.tap(find.text('VALUE'));
    await tester.pump(Duration.zero);
    fake = fake.add(const Duration(milliseconds: 20));
    await tester.tap(find.text('VALUE'));
    await tester.pump(Duration.zero);

    expect(sent, ['TreeColumn/3/Selection/Selection', 'TreeColumn/3/Selection/DefaultSelection']);
  });
}
