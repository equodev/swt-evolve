// A Tree drop target draws where a drop will land as a native tree does: an insertion line on the
// boundary before or after the item under the pointer, or the item drawn as selected. A drag that
// rests over it keeps sending DragOver while the listener asks to scroll or expand. A drag from
// another window gets the same feedback.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/sent_events.dart';

const int _mousePointer = 1;
const int _dropTargetId = 17;
const int _targetId = 12;
const int _firstFillerId = 100;
const int _shellId = 500;

VTreeItem _item(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

VTree _tree({int fillers = 0}) => VTree()
  ..id = 1
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..dragSource = true
  ..dropTargetId = _dropTargetId
  ..items = [
    _item(10, 'Source'),
    _item(11, 'Middle'),
    _item(_targetId, 'Target'),
    for (var i = 0; i < fillers; i++) _item(_firstFillerId + i, 'Row $i'),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

Future<void> _pumpTree(WidgetTester tester, {int fillers = 0}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree(fillers: fillers))),
  ));
  await tester.pump();
}

/// The tree alone in a shell, which is what a drag from another window is told about.
Future<void> _pumpTreeInShell(WidgetTester tester) async {
  tester.view.physicalSize = const Size(1200, 800);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  final shell = VShell()
    ..id = _shellId
    ..style = SWT.SHELL_TRIM
    ..text = 'window'
    ..visible = true
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 800
      ..height = 600)
    ..children = [_tree()];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..shells = [shell]
        ..mainShellId = _shellId,
    ),
  ));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
}

/// Presses on 'Source' and moves to [to] with a mouse, leaving the button held.
Future<TestGesture> _dragTo(WidgetTester tester, Offset to) async {
  final start = tester.getCenter(find.text('Source'));
  final gesture = await tester.createGesture(
      kind: PointerDeviceKind.mouse, pointer: _mousePointer, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 50));
  const steps = 6;
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(Offset.lerp(start, to, i / steps)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  await deliverFrame('Tree/1/DragDetect/dragStartResult', {'doit': true});
  await tester.pump();
  return gesture;
}

Future<void> _answer(WidgetTester tester, int feedback) async {
  await deliverEvent('DropTarget/$_dropTargetId/Drop/negotiationResult',
      VEvent()
        ..detail = Dnd.dropMove
        ..feedback = feedback);
  await tester.pump();
}

Future<void> _finish(WidgetTester tester, TestGesture gesture) async {
  await gesture.up();
  await gesture.removePointer();
  await tester.pumpAndSettle();
  await tester.pump(const Duration(milliseconds: 400));
}

/// Java telling the shell that a drag from another window is [phase] at [global].
Future<void> _remote(WidgetTester tester, String phase, [Offset? global]) async {
  final body = <String, dynamic>{'text': phase};
  if (global != null) {
    final at = tester.renderObject<RenderBox>(find.byType(ShellSwt<VShell>)).globalToLocal(global);
    body['x'] = at.dx.round();
    body['y'] = at.dy.round();
  }
  deliverTagged('Shell/$_shellId/DnD/remoteDrag', body);
  await tester.pump();
  await tester.pump();
}

List<String> _drops() => sentEvents()
    .where((f) => f.$1.startsWith('DropTarget/$_dropTargetId/Drop/'))
    .map((f) => f.$1.substring('DropTarget/$_dropTargetId/Drop/'.length))
    .toList();

List<Map<String, dynamic>> _dragOvers() => sentEvents()
    .where((f) => f.$1 == 'DropTarget/$_dropTargetId/Drop/dragOver')
    .map((f) => f.$2)
    .toList();

/// Where the drop feedback is drawn, in the test view's coordinates, and what is drawn there; null
/// when nothing is.
(Rect, BoxDecoration)? _feedback(WidgetTester tester) {
  for (final e in find.byType(DropRowLayer).evaluate()) {
    final layer = e.widget as DropRowLayer;
    final row = layer.rows.row;
    if (row == null) continue;
    final decoration = layer.decorationOver(row);
    if (decoration == null) continue;
    final box = e.renderObject! as RenderBox;
    final local = layer.rowRect(row, box.size.width);
    return (
      Rect.fromPoints(box.localToGlobal(local.topLeft), box.localToGlobal(local.bottomRight)),
      decoration,
    );
  }
  return null;
}

Border _border(BoxDecoration d) => d.border! as Border;

double _rowHeight(WidgetTester tester) =>
    tester.getCenter(find.text('Target')).dy - tester.getCenter(find.text('Middle')).dy;

double _topOf(WidgetTester tester, String text) =>
    tester.getCenter(find.text(text)).dy - _rowHeight(tester) / 2;

/// INSERT_AFTER drawn on the bottom boundary of [text]'s row.
void _expectLineAfter(WidgetTester tester, String text, double rowHeight) {
  final feedback = _feedback(tester);
  expect(feedback, isNotNull, reason: 'a line after $text');
  final (rect, decoration) = feedback!;
  expect(_border(decoration).bottom.width, greaterThanOrEqualTo(2));
  expect(rect.height, moreOrLessEquals(rowHeight, epsilon: 1.0), reason: 'the rows as drawn');
  expect(rect.bottom, moreOrLessEquals(tester.getCenter(find.text(text)).dy + rowHeight / 2, epsilon: 1.0),
      reason: 'on the bottom boundary of $text');
}

void main() {
  setUp(() {
    freshClient();
    EquoCommBase.recordSentFrames = true;
  });
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    appScaleNotifier.value = 1.0;
  });

  testWidgets('INSERT_BEFORE and INSERT_AFTER draw a line on the boundary before or after the item',
      (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
    final rowHeight = _rowHeight(tester);
    final targetTop = _topOf(tester, 'Target');

    await _answer(tester, 0x1a);
    var (rect, decoration) = _feedback(tester)!;
    expect(_border(decoration).top.width, greaterThanOrEqualTo(2));
    expect(_border(decoration).bottom, BorderSide.none);
    expect(decoration.color, isNull, reason: 'the row itself is drawn as before');
    expect(rect.top, moreOrLessEquals(targetTop, epsilon: 1.0), reason: 'on the top boundary');
    expect(rect.width, greaterThan(300), reason: 'across the rows');

    await _answer(tester, 0x1c);
    (rect, decoration) = _feedback(tester)!;
    expect(_border(decoration).bottom.width, greaterThanOrEqualTo(2));
    expect(_border(decoration).top, BorderSide.none);
    expect(rect.bottom, moreOrLessEquals(targetTop + rowHeight, epsilon: 1.0), reason: 'on the bottom boundary');
    expect(_topOf(tester, 'Target'), targetTop, reason: 'no row moves');

    await _answer(tester, Dnd.feedbackNone);
    expect(_feedback(tester), isNull);

    await _finish(tester, gesture);
  });

  testWidgets('SELECT draws the item highlighted', (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));

    await _answer(tester, 0x19);
    final (rect, decoration) = _feedback(tester)!;
    expect(decoration.color, isNotNull);
    expect(rect.center.dy, moreOrLessEquals(tester.getCenter(find.text('Target')).dy, epsilon: 1.0));

    await _answer(tester, 0x1c);
    expect(_feedback(tester)!.$2.color, isNull, reason: 'an insertion line instead');

    await _finish(tester, gesture);
    expect(_feedback(tester), isNull, reason: 'nothing once the drag ends');
  });

  testWidgets('over no item nothing is drawn, whatever the feedback', (tester) async {
    await _pumpTree(tester);
    final below = tester.getCenter(find.text('Target')) + Offset(0, _rowHeight(tester) * 3);
    final gesture = await _dragTo(tester, below);

    for (final feedback in [0x19, 0x1a, 0x1c]) {
      await _answer(tester, feedback);
      expect(_feedback(tester), isNull);
    }

    await _finish(tester, gesture);
  });

  for (final scale in [2 / 3, 2.0]) {
    testWidgets('at a zoom of ${scale.toStringAsFixed(2)} the line is on the row under the pointer',
        (tester) async {
      appScaleNotifier.value = scale;
      await _pumpTree(tester);
      final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
      await _answer(tester, 0x1c);

      expect(_dragOvers().last['itemId'], _targetId);
      _expectLineAfter(tester, 'Target', _rowHeight(tester));

      await _finish(tester, gesture);
    });
  }

  testWidgets('the feedback goes when the drag leaves the tree', (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
    await _answer(tester, 0x1c);
    expect(_feedback(tester), isNotNull);

    await gesture.moveTo(const Offset(600, 500));
    await tester.pump(const Duration(milliseconds: 20));
    expect(_feedback(tester), isNull);

    await _finish(tester, gesture);
  });

  testWidgets('Escape takes the feedback away with the drag', (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
    await _answer(tester, 0x1c);
    expect(_feedback(tester), isNotNull);

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pump();
    expect(_drops().last, 'dragLeave');
    expect(_feedback(tester), isNull);

    await gesture.moveTo(tester.getCenter(find.text('Middle')));
    await tester.pump(const Duration(milliseconds: 20));
    expect(_feedback(tester), isNull, reason: 'the cancelled drag draws nothing on');

    await _finish(tester, gesture);
  });

  testWidgets('a drag resting over the tree keeps sending DragOver while SCROLL or EXPAND is asked',
      (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
    await _answer(tester, 0x1c);
    EquoCommService.commForTesting.clearSentFrames();

    await tester.pump(const Duration(milliseconds: 300));
    final overs = _dragOvers();
    expect(overs.length, greaterThanOrEqualTo(4), reason: 'the pointer rests, DragOver goes on');
    expect(overs.every((o) => o['itemId'] == _targetId), isTrue);

    await _answer(tester, Dnd.feedbackInsertAfter);
    EquoCommService.commForTesting.clearSentFrames();
    await tester.pump(const Duration(milliseconds: 300));
    expect(_dragOvers(), isEmpty, reason: 'nothing to wait for without SCROLL or EXPAND');

    await _answer(tester, 0x1c);
    await gesture.moveTo(const Offset(600, 500));
    await tester.pump(const Duration(milliseconds: 20));
    EquoCommService.commForTesting.clearSentFrames();
    await tester.pump(const Duration(milliseconds: 300));
    expect(_dragOvers(), isEmpty, reason: 'the drag left the tree');

    await _finish(tester, gesture);
  });

  testWidgets('a resting drag over a tree that scrolled names the item now under the pointer',
      (tester) async {
    await _pumpTree(tester, fillers: 30);
    final gesture = await _dragTo(tester, tester.getCenter(find.text('Target')));
    await _answer(tester, 0x1c);
    final rowHeight = _rowHeight(tester);

    final rows = tester.state<ScrollableState>(find.byWidgetPredicate(
        (w) => w is Scrollable && axisDirectionToAxis(w.axisDirection) == Axis.vertical));
    rows.position.jumpTo(rowHeight * 2);
    await tester.pump();
    EquoCommService.commForTesting.clearSentFrames();
    await tester.pump(const Duration(milliseconds: 120));
    // Two rows past 'Target'.
    expect(_dragOvers().last['itemId'], _firstFillerId + 1);
    _expectLineAfter(tester, 'Row 1', rowHeight);

    await _finish(tester, gesture);
  });

  group('a drag from another window', () {
    testWidgets('draws the feedback Java answered on the item under it, until it drops',
        (tester) async {
      await _pumpTreeInShell(tester);
      final target = tester.getCenter(find.text('Target'));

      await _remote(tester, 'over', target);
      expect(_drops(), ['dragEnter', 'dragOver']);
      expect(_dragOvers().last['itemId'], _targetId);
      await _answer(tester, 0x1c);
      _expectLineAfter(tester, 'Target', _rowHeight(tester));

      await _remote(tester, 'drop', target);
      expect(_drops().last, 'drop');
      expect(_feedback(tester), isNull);
    });

    testWidgets('takes the feedback away when it leaves', (tester) async {
      await _pumpTreeInShell(tester);
      final target = tester.getCenter(find.text('Target'));

      await _remote(tester, 'over', target);
      await _answer(tester, 0x19);
      expect(_feedback(tester)!.$2.color, isNotNull);

      await _remote(tester, 'leave');
      expect(_drops().last, 'dragLeave');
      expect(_feedback(tester), isNull);
    });
  });
}
