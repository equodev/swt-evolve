// A double click on a Table's column header is that column's DefaultSelection on Win32 and GTK:
// the first click is its Selection, the second its DefaultSelection.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..linesVisible = false
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 200
      ..text = 'Name',
    VTableColumn()
      ..id = 3
      ..width = 200
      ..text = 'Value',
  ]
  ..items = [
    VTableItem()
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
      if (action.startsWith('TableColumn/') && action.contains('/Selection/')) sent.add(action);
    };
    addTearDown(() => EquoCommBase.sendObserverForTesting = null);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 300, child: TableSwt<VTable>(value: _table())),
    ));
    await tester.pump();

    var fake = DateTime(2020);
    DoubleTapDetector.clock = () => fake;
    await tester.tap(find.text('Value'));
    await tester.pump(Duration.zero);
    fake = fake.add(const Duration(milliseconds: 20));
    await tester.tap(find.text('Value'));
    await tester.pump(Duration.zero);

    expect(sent, ['TableColumn/3/Selection/Selection', 'TableColumn/3/Selection/DefaultSelection']);
  });
}
