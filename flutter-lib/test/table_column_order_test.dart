// A table draws its columns in the order Java's column order names, header and rows alike. A moveable
// column dragged in the header reports where it was dropped; the reorder itself is Java's.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

VTable _table({List<int>? order, bool moveable = false}) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    for (var i = 0; i < 3; i++)
      VTableColumn()
        ..id = 2 + i
        ..text = 'Head$i'
        ..width = 120
        ..moveable = moveable,
  ]
  ..columnOrder = order
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['cell0', 'cell1', 'cell2'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 500
    ..height = 200);

Future<void> _pump(WidgetTester tester, VTable table) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 500, height: 200, child: TableSwt<VTable>(value: table)),
  ));
  await tester.pumpAndSettle();
}

double _left(WidgetTester tester, String text) => tester.getRect(find.text(text)).left;

List<(String, VEvent)> _moves() => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1.endsWith('/Control/Move'))
    .map((frame) => (EquoCommBase.decodeFrame(frame).$1, EquoCommBase.decodeEvent(frame)))
    .toList();

void main() {
  testWidgets('without an order the columns are drawn in creation order', (tester) async {
    await _pump(tester, _table());

    expect(_left(tester, 'Head0'), lessThan(_left(tester, 'Head1')));
    expect(_left(tester, 'Head1'), lessThan(_left(tester, 'Head2')));
    expect(_left(tester, 'cell0'), lessThan(_left(tester, 'cell1')));
    expect(_left(tester, 'cell1'), lessThan(_left(tester, 'cell2')));
  });

  testWidgets('the header and the cells follow the column order', (tester) async {
    await _pump(tester, _table(order: [1, 2, 0]));

    expect(_left(tester, 'Head1'), lessThan(_left(tester, 'Head2')));
    expect(_left(tester, 'Head2'), lessThan(_left(tester, 'Head0')));
    expect(_left(tester, 'cell1'), lessThan(_left(tester, 'cell2')));
    expect(_left(tester, 'cell2'), lessThan(_left(tester, 'cell0')));
    expect(_left(tester, 'cell0'), greaterThanOrEqualTo(_left(tester, 'Head0') - 1),
        reason: 'a cell sits under its own header');
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
      await _pump(tester, _table(moveable: true));

      await drag(tester, 'Head2', 'Head0');

      final moves = _moves();
      expect(moves, hasLength(1));
      expect(moves.single.$1, contains('TableColumn/4/'));
      expect(moves.single.$2.index, 0);
    });

    testWidgets('places the drop by the order the columns are shown in', (tester) async {
      await _pump(tester, _table(order: [2, 0, 1], moveable: true));

      await drag(tester, 'Head1', 'Head2');

      expect(_moves().single.$2.index, 0);
    });

    testWidgets('of a column that is not moveable reports nothing', (tester) async {
      await _pump(tester, _table());

      await drag(tester, 'Head2', 'Head0');

      expect(_moves(), isEmpty);
    });
  });
}
