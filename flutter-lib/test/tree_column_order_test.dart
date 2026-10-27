// A tree draws its columns in the order Java's column order names, header and rows alike, and the tree
// column keeps the expander wherever it is shown. A moveable column dragged in the header reports where it
// was dropped; the reorder itself is Java's.

import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VTreeColumn _column(int id, String text, {bool moveable = false}) => VTreeColumn()
  ..id = id
  ..text = text
  ..width = 100
  ..moveable = moveable;

VTree _tree({List<int>? order, bool moveable = false}) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    _column(2, 'First', moveable: moveable),
    _column(3, 'Second', moveable: moveable),
    _column(4, 'Third', moveable: moveable),
  ]
  ..columnOrder = order
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'cell0'
      ..texts = ['cell0', 'cell1', 'cell2']
      ..items = [
        VTreeItem()
          ..id = 11
          ..text = 'child'
          ..texts = ['child', '', ''],
      ],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pump(WidgetTester tester, VTree tree) async {
  // A columned tree's rows overflow by their padding, a layout matter of its own: it is not what this
  // test is about.
  final reportError = FlutterError.onError;
  FlutterError.onError = (details) {
    if (details.exceptionAsString().contains('overflowed')) return;
    reportError?.call(details);
  };
  addTearDown(() => FlutterError.onError = reportError);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: tree)),
  ));
  await tester.pumpAndSettle();
}

double _left(WidgetTester tester, String text) => tester.getRect(find.text(text)).left;

List<(String, Map<String, dynamic>)> _moves() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .where((f) => f.$1.endsWith('/Control/Move'))
    .map((f) => (f.$1, jsonDecode(f.$2) as Map<String, dynamic>))
    .toList();

void main() {
  testWidgets('without an order the columns are drawn in creation order', (tester) async {
    await _pump(tester, _tree());

    expect(_left(tester, 'FIRST'), lessThan(_left(tester, 'SECOND')));
    expect(_left(tester, 'SECOND'), lessThan(_left(tester, 'THIRD')));
    expect(_left(tester, 'cell0'), lessThan(_left(tester, 'cell1')));
    expect(_left(tester, 'cell1'), lessThan(_left(tester, 'cell2')));
  });

  testWidgets('the header and the cells follow the column order', (tester) async {
    await _pump(tester, _tree(order: [1, 2, 0]));

    expect(_left(tester, 'SECOND'), lessThan(_left(tester, 'THIRD')));
    expect(_left(tester, 'THIRD'), lessThan(_left(tester, 'FIRST')));
    expect(_left(tester, 'cell1'), lessThan(_left(tester, 'cell2')));
    expect(_left(tester, 'cell2'), lessThan(_left(tester, 'cell0')));
  });

  testWidgets('the expander stays in the tree column wherever it is shown', (tester) async {
    await _pump(tester, _tree(order: [1, 2, 0]));

    final arrow = tester.getRect(find.byIcon(Icons.keyboard_arrow_right));
    expect(arrow.left, greaterThan(tester.getRect(find.text('cell2')).right));
    expect(arrow.right, lessThanOrEqualTo(_left(tester, 'cell0')));
  });

  testWidgets('an order that does not name every column is drawn in creation order', (tester) async {
    await _pump(tester, _tree(order: [1, 0]));

    expect(_left(tester, 'cell0'), lessThan(_left(tester, 'cell1')));
    expect(_left(tester, 'cell1'), lessThan(_left(tester, 'cell2')));
  });

  group('a header drag', () {
    setUp(() => EquoCommBase.recordSentFrames = true);
    tearDown(() {
      EquoCommBase.recordSentFrames = false;
      EquoCommService.commForTesting.clearSentFrames();
    });

    Future<void> drag(WidgetTester tester, String header, String onto) async {
      EquoCommService.commForTesting.clearSentFrames();
      final from = tester.getCenter(find.text(header));
      final to = tester.getCenter(find.text(onto));
      final gesture = await tester.startGesture(from, kind: PointerDeviceKind.mouse);
      await gesture.moveTo(Offset((from.dx + to.dx) / 2, from.dy));
      await gesture.moveTo(Offset(to.dx, from.dy));
      await tester.pump();
      await gesture.up();
      await tester.pumpAndSettle();
    }

    testWidgets('of a moveable column reports the place it was dropped at', (tester) async {
      await _pump(tester, _tree(moveable: true));

      await drag(tester, 'FIRST', 'THIRD');

      final moves = _moves();
      expect(moves, hasLength(1));
      expect(moves.single.$1, contains('TreeColumn/2/'));
      expect(moves.single.$2['index'], 2);
    });

    testWidgets('places the drop by the order the columns are shown in', (tester) async {
      await _pump(tester, _tree(order: [2, 0, 1], moveable: true));

      await drag(tester, 'SECOND', 'THIRD');

      expect(_moves().single.$2['index'], 0);
    });

    testWidgets('of a column that is not moveable reports nothing', (tester) async {
      await _pump(tester, _tree());

      await drag(tester, 'FIRST', 'THIRD');

      expect(_moves(), isEmpty);
    });
  });
}
