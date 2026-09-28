// What an owner-drawing Table paints into a row (its SWT.EraseItem/PaintItem drawing) is shown by an
// overlay laid over that row. Java paints a row into it only once the row reports its overlay
// listening, and from then on the row does not paint the text the overlay already draws.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';

class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VTable _table(List<int>? paintedTexts) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..headerVisible = false
  ..linesVisible = false
  ..columns = []
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['stable'],
    VTableItem()
      ..id = 11
      ..texts = ['check_0710']
      ..paintedTexts = paintedTexts,
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 300);

Future<List<List<int>?>> _pump(WidgetTester tester, VTable value) async {
  final requests = <List<int>?>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 300,
      child: _CapturingTableSwt(
        value: value,
        onEvent: (ev, payload) {
          if (ev == 'PaintItem/PaintItem') requests.add(payload?.segments);
        },
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return requests;
}

void main() {
  testWidgets('a row that is not owner-drawn gets no overlay and asks for nothing', (tester) async {
    final requests = await _pump(tester, _table(null));

    expect(find.byType(OwnerDrawnRowOverlay), findsNothing);
    expect(requests, isEmpty);
  });

  testWidgets('an owner-drawn row mounts an overlay over itself and asks Java to paint it',
      (tester) async {
    final requests = await _pump(tester, _table([]));

    expect(find.byType(OwnerDrawnRowOverlay), findsOneWidget);
    expect(requests, [
      [11]
    ]);
    final row = tester.getRect(find.text('check_0710'));
    final overlay = tester.getRect(find.byType(OwnerDrawnRowOverlay));
    expect(overlay.top, lessThanOrEqualTo(row.top));
    expect(overlay.bottom, greaterThanOrEqualTo(row.bottom));
  });

  testWidgets('text the overlay paints keeps its place but is not painted by the row',
      (tester) async {
    await _pump(tester, _table([0]));

    final painted = tester.widget<Text>(find.text('check_0710'));
    expect(painted.style?.color, Colors.transparent);
    final own = tester.widget<Text>(find.text('stable'));
    expect(own.style?.color, isNot(Colors.transparent));
  });
}
