// Regression test for issue #826: in Katalon's Data Files table editor,
// clicking a cell opens the in-place editor on the row above the one that
// was clicked.
//
// SWT contract: mouse-event coordinates (event.x/event.y) are always relative
// to the origin (top-left corner) of the control that received them --
// Table.getItem(Point) on the Java side relies on that to translate a click
// into a row index. This test taps a real rendered row (through the actual
// GestureDetector -- the same code path a real click goes through) and checks
// that the reported y matches where the tap actually landed relative to the
// table's own top-left corner. It does not know or care *how* the
// implementation gets there (header height, row height, caching, ...) --
// only that the contract holds.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/event.dart';

VTableColumn _col(int id, {int? width}) => VTableColumn()
  ..id = id
  ..width = width
  ..text = 'Col$id';

VTableItem _item(int id, List<String> texts) => VTableItem()
  ..id = id
  ..texts = texts;

/// Same as the generated TableSwt, except sendEvent captures outgoing events
/// instead of handing them to EquoCommService (which needs a live transport
/// that doesn't exist in a widget test). Everything else -- the State, the
/// real GestureDetector, the real coordinate computation -- is untouched.
class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) => onEvent(ev, payload);
}

void main() {
  testWidgets(
    'a cell tap reports y relative to the table\'s own top-left corner',
    (tester) async {
      final cols = [_col(1, width: 400)];
      final items = [
        _item(10, ['Row 0']),
        _item(11, ['Row 1']),
        _item(12, ['Row 2']),
      ];
      final value = VTable()
        ..id = 1
        ..style = SWT.NONE
        ..enabled = true
        ..headerVisible = true
        ..linesVisible = false
        ..columns = cols
        ..items = items
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 800
          ..height = 400);

      VEvent? mouseDown;
      final tableWidget = _CapturingTableSwt(
        value: value,
        onEvent: (ev, payload) {
          if (ev == 'Mouse/MouseDown') mouseDown = payload;
        },
      );

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(width: 800, height: 400, child: tableWidget),
      ));
      await tester.pump();

      // Ground truth, measured independently from whatever the table's
      // internals compute: where the table control's own top-left corner is,
      // and where the tap actually landed on screen -- both in the same
      // (global) coordinate space, exactly like tester.tap() itself uses.
      final tableOrigin = tester.getTopLeft(find.byType(_CapturingTableSwt));
      final tapTarget = find.text('Row 2');
      final tapPosition = tester.getCenter(tapTarget);

      await tester.tap(tapTarget);
      await tester.pump(Duration.zero);

      expect(mouseDown, isNotNull,
          reason: 'tapping a cell must report a Mouse/MouseDown event');

      final expectedLocalY = tapPosition.dy - tableOrigin.dy;

      expect(
        mouseDown!.y,
        closeTo(expectedLocalY, 4),
        reason: 'event.y must be relative to the table\'s own top-left corner '
            '(the SWT coordinate contract Table.getItem(Point) relies on to '
            'resolve the clicked row) -- not missing the header\'s height, '
            'which is what makes the editor land on the row above the one '
            'that was actually clicked.',
      );
    },
  );
}
