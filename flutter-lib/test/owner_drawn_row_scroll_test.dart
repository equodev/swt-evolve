// A scroll moves an owner-drawn row without unmounting its overlay, so Java does not repaint a row
// it only moved: the overlay keeps the drawing it holds, which is in the row's own coordinates. Java
// sends a GC's state as a change from what the row's channel holds, so a row's drawer that was let go
// of hands that state to the next one, which the frames held for the row meanwhile change.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/gcdrawer.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, wireChannelNames;
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';

import 'delivery/support/deliver.dart';
import 'support/gc_frames.dart';

const _rows = 60;
const _height = 200.0;

class _CapturingTableSwt extends TableSwt<VTable> {
  const _CapturingTableSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTable val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

class _CapturingTreeSwt extends TreeSwt<VTree> {
  const _CapturingTreeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VTree val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 300
  ..height = _height.toInt();

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.V_SCROLL
  ..headerVisible = false
  ..linesVisible = false
  ..columns = []
  ..items = [
    for (var i = 0; i < _rows; i++)
      VTableItem()
        ..id = 100 + i
        ..texts = ['row $i']
        ..paintedTexts = [],
  ]
  ..bounds = _bounds();

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE | SWT.V_SCROLL
  ..enabled = true
  ..items = [
    for (var i = 0; i < _rows; i++)
      VTreeItem()
        ..id = 100 + i
        ..text = 'row $i'
        ..paintedTexts = [],
  ]
  ..bounds = _bounds();

/// Mounts [control], recording the rows each PaintItem request names.
Future<List<int>> _pump(WidgetTester tester, Widget Function(void Function(String, VEvent?)) control) async {
  final requested = <int>[];
  void onEvent(String ev, VEvent? payload) {
    if (ev == 'PaintItem/PaintItem') requested.addAll(payload?.segments ?? const []);
  }

  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: _height, child: control(onEvent)),
  ));
  await tester.pumpAndSettle();
  return requested;
}

/// Paints row [id] as Java paints a row whose overlay listens.
Future<void> _paint(WidgetTester tester, int id) async {
  receive('GC/$id/b', GcFrame().whole(VGC.empty()..id = id).opNamed('fillRectangleintintintint', [0, 0, 20, 10]).bytes);
  await deliverFrame('GC/$id/gcDispose', {'fullRepaint': true});
  await tester.pumpAndSettle();
}

Finder _overlayOf(int id) =>
    find.byWidgetPredicate((w) => w is OwnerDrawnRowOverlay && w.itemId == id);

/// Scrolls the rows down by [rows] of their own height.
Future<void> _scrollRows(WidgetTester tester, int rows) async {
  final rowHeight = tester.getRect(_overlayOf(100)).height;
  final vertical = find.byWidgetPredicate((w) => w is Scrollable && w.axisDirection == AxisDirection.down);
  tester.state<ScrollableState>(vertical.first).position.jumpTo(rows * rowHeight);
  await tester.pumpAndSettle();
}

void main() {
  for (final kind in ['Table', 'Tree']) {
    testWidgets('a $kind row a scroll only moves keeps its overlay and its drawing', (tester) async {
      final requested = await _pump(
        tester,
        (onEvent) => kind == 'Table'
            ? _CapturingTableSwt(value: _table(), onEvent: onEvent)
            : _CapturingTreeSwt(value: _tree(), onEvent: onEvent),
      );
      const row = 105;
      expect(requested, contains(row));
      await _paint(tester, row);
      final overlay = tester.state(_overlayOf(row));
      final drawer = GCDrawerBase.live[row];
      final top = tester.getRect(_overlayOf(row)).top;
      requested.clear();

      await _scrollRows(tester, 3);

      expect(tester.getRect(_overlayOf(row)).top, lessThan(top), reason: 'the row moved up');
      expect(identical(tester.state(_overlayOf(row)), overlay), isTrue,
          reason: 'a remounted overlay would start empty and ask Java to paint the row again');
      expect(identical(GCDrawerBase.live[row], drawer), isTrue);
      expect(requested, isNot(contains(row)));
      expect(find.descendant(of: _overlayOf(row), matching: find.byType(IgnorePointer)), findsOneWidget,
          reason: 'the overlay still shows what Java painted');
    });
  }

  testWidgets('a row\'s overlay mounted again takes up the GC state the last one held', (tester) async {
    const row = 105;
    Widget table(void Function(String, VEvent?) onEvent) => _CapturingTableSwt(value: _table(), onEvent: onEvent);
    await _pump(tester, table);
    receive('GC/$row/b', GcFrame().whole(VGC.empty()
      ..id = row
      ..lineWidth = 3
      ..background = colourOf(192, 192, 192)).bytes);
    await tester.pumpAndSettle();

    await tester.pumpWidget(const SizedBox());
    // Sent before the client asks again, so held for the row and applied to its next drawer.
    receive('GC/$row/b', GcFrame().change(VGC.empty()..foreground = colourOf(128, 0, 0), {WireKey.k_GC_foreground}).bytes);
    await _pump(tester, table);

    final state = GCDrawerBase.live[row]!.state;
    expect(state.foreground?.red, 128, reason: 'the change held for the row');
    expect(state.background?.blue, 192, reason: 'what the change did not name, as the last overlay held it');
    expect(state.lineWidth, 3);
  });
}
