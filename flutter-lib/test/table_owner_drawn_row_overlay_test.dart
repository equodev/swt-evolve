// What an owner-drawing Table paints into a row (its SWT.EraseItem/PaintItem drawing) is shown by an
// overlay laid under that row. Java paints a row into it only once the row reports its overlay
// listening, and from then on the row does not paint the text the overlay already draws.
//
// Under, not over: the overlay carries the EraseItem background, and an app that leaves
// SWT.FOREGROUND set still expects the item's own text on top of it.

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

  testWidgets('an owner-drawn row mounts an overlay across itself and asks Java to paint it',
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

  testWidgets('the overlay is painted under the row, so a background it draws hides no cell text',
      (tester) async {
    // The reported shape: the app's EraseItem fills each cell and its PaintItem draws no text, so
    // the row keeps its own text (paintedTexts names only the empty columns). An overlay stacked
    // over the row buried that text under an opaque fill.
    await _pump(tester, _table([]));

    final stack = tester.widget<Stack>(
      find.ancestor(of: find.byType(OwnerDrawnRowOverlay), matching: find.byType(Stack)).first,
    );
    final overlayIndex = stack.children.indexWhere(
      (child) => find
          .descendant(of: find.byWidget(child), matching: find.byType(OwnerDrawnRowOverlay))
          .evaluate()
          .isNotEmpty,
    );
    final rowsIndex = stack.children.indexWhere(
      (child) => find
          .descendant(of: find.byWidget(child), matching: find.text('check_0710'))
          .evaluate()
          .isNotEmpty,
    );

    expect(overlayIndex, isNonNegative);
    expect(rowsIndex, isNonNegative);
    expect(overlayIndex, lessThan(rowsIndex),
        reason: 'the overlay must paint before the row, or its background covers the cell text');
  });
}
