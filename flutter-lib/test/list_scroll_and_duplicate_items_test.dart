// Java owns where a List is scrolled: List.setTopIndex, showSelection and selecting name the item to bring
// to the top, and the client reports how far the user scrolled, which List.getTopIndex is computed from.
// The selection is a set of indices, so items with the same text are selected and highlighted apart.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const double _itemExtent = 24;

VList _list({
  List<String>? items,
  List<int> selection = const [],
  int? topIndex,
  int seq = 1,
  int style = SWT.SINGLE | SWT.V_SCROLL,
}) => VList()
  ..id = 1
  ..seq = seq
  ..style = style
  ..enabled = true
  ..topIndex = topIndex
  ..items = items ?? [for (var i = 0; i < 60; i++) 'item $i']
  ..selection = selection
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 120);

late Rect _listRect;

Future<void> _pump(WidgetTester tester, VList list) async {
  await tester.pumpWidget(const SizedBox.shrink());
  freshClient();
  final widget = ListSwt<VList>(value: list);
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 120, child: widget),
      ),
    ),
  );
  await tester.pumpAndSettle();
  _listRect = tester.getRect(find.byWidget(widget));
}

/// Whether item [index] is the one at the top of the list.
bool _isAtTheTop(WidgetTester tester, int index) {
  final top = tester.getRect(find.text('item $index')).top - _listRect.top;
  return top >= 0 && top < _itemExtent;
}

List<VEvent> _sent(String suffix) => EquoCommService.commForTesting.sentFrames
    .where((frame) => EquoCommBase.decodeFrame(frame).$1.endsWith(suffix))
    .map(EquoCommBase.decodeEvent)
    .toList();

/// The rows drawn as selected.
Iterable<Rect> _highlightedRows(WidgetTester tester) => tester
    .widgetList<AnimatedOpacity>(find.byType(AnimatedOpacity))
    .where((w) => w.opacity > 0.2)
    .map((w) => tester.getRect(find.byWidget(w)));

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  testWidgets('a list that names a top index shows that item at the top', (
    tester,
  ) async {
    await _pump(tester, _list(topIndex: 20));

    expect(_isAtTheTop(tester, 20), isTrue);
  });

  testWidgets('a new top index from Java scrolls the items to it', (
    tester,
  ) async {
    await _pump(tester, _list());
    expect(_isAtTheTop(tester, 0), isTrue);

    await deliverChange(
      _list(topIndex: 30, seq: 10),
      keys: [WireKey.k_List_topIndex],
      base: 1,
    );
    await tester.pumpAndSettle();

    expect(_isAtTheTop(tester, 30), isTrue);
  });

  testWidgets(
    'a scroll by the user is reported as the distance the items moved and their height',
    (tester) async {
      await _pump(tester, _list());
      EquoCommService.commForTesting.clearSentFrames();

      await tester.drag(find.byType(ListView), const Offset(0, -100));
      await tester.pumpAndSettle();

      final position = tester
          .state<ScrollableState>(
            find.descendant(
              of: find.byType(ListView),
              matching: find.byType(Scrollable),
            ),
          )
          .position;
      expect(position.pixels, greaterThan(0));
      final scrolls = _sent('/Scroll/Scroll');
      expect(scrolls, isNotEmpty);
      expect(scrolls.last.y, position.pixels.round());
      expect(scrolls.last.height, _itemExtent);
    },
  );

  testWidgets(
    'of two items with the same text, only the selected one is highlighted',
    (tester) async {
      await _pump(
        tester,
        _list(items: ['same', 'same', 'other'], selection: [1]),
      );

      final rows = _highlightedRows(tester).toList();
      expect(rows, hasLength(1));
      expect(
        rows.single.top,
        closeTo(tester.getRect(find.text('same').at(1)).top, _itemExtent / 2),
      );
    },
  );

  testWidgets(
    'a click on the second of two items with the same text selects that one',
    (tester) async {
      await _pump(tester, _list(items: ['same', 'same', 'other']));
      EquoCommService.commForTesting.clearSentFrames();

      await tester.tap(find.text('same').at(1), warnIfMissed: false);
      await tester.pump(const Duration(milliseconds: 600));

      final selections = _sent('/Selection/Selection');
      expect(selections, isNotEmpty);
      expect(selections.last.segments, [1]);
    },
  );

  testWidgets('the wheel scrolls a list with a vertical scroll bar', (tester) async {
    await _pump(tester, _list());

    tester.binding.handlePointerEvent(
        PointerScrollEvent(position: _listRect.center, scrollDelta: const Offset(0, 100)));
    await tester.pumpAndSettle();

    expect(find.text('item 0'), findsNothing);
  });

  testWidgets(
    'the wheel leaves a list without V_SCROLL where it is, as Win32 and GTK do; Java still scrolls it',
    (tester) async {
      await _pump(tester, _list(style: SWT.SINGLE));

      tester.binding.handlePointerEvent(
          PointerScrollEvent(position: _listRect.center, scrollDelta: const Offset(0, 100)));
      await tester.pumpAndSettle();
      expect(_isAtTheTop(tester, 0), isTrue);
      expect(_sent('/Scroll/Scroll'), isEmpty);

      await _pump(tester, _list(style: SWT.SINGLE, topIndex: 10));
      expect(_isAtTheTop(tester, 10), isTrue);
    },
  );
}
