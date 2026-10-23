// What an SWT drag shows while it is in flight. Native SWT draws the operation the drop target
// answered as the cursor (copy, move, link, or "not allowed"), and a Tree drop target draws the
// DND.FEEDBACK_* its listener asked for on the item under the pointer: an insertion line before or
// after it, or the item highlighted as the target.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'delivery/support/deliver.dart';

const int _mousePointer = 1;
const int _dropTargetId = 7;

VTreeItem _item(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..dragSource = true
  ..dropTargetId = _dropTargetId
  ..items = [_item(10, 'Source'), _item(11, 'Middle'), _item(12, 'Target')]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

Future<void> _pumpTree(WidgetTester tester) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree())),
  ));
  await tester.pump();
}

/// Presses on 'Source' and moves onto 'Target' with a mouse, leaving the button held.
Future<TestGesture> _dragOntoTarget(WidgetTester tester) async {
  final start = tester.getCenter(find.text('Source'));
  final end = tester.getCenter(find.text('Target'));
  final gesture = await tester.createGesture(
      kind: PointerDeviceKind.mouse, pointer: _mousePointer, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 50));
  const steps = 6;
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  return gesture;
}

Future<void> _answer({required int detail, required int feedback}) => deliverFrame(
    'DropTarget/$_dropTargetId/Drop/negotiationResult', {'detail': detail, 'feedback': feedback});

Future<void> _finish(WidgetTester tester, TestGesture gesture) async {
  await gesture.up();
  await gesture.removePointer();
  await tester.pumpAndSettle();
  // Nothing answers the dragStart verdict here; let the fail-open timer run out.
  await tester.pump(const Duration(milliseconds: 400));
}

MouseCursor _activeCursor() =>
    RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mousePointer)!;

BoxDecoration? _dropFeedbackOn(WidgetTester tester, String text) {
  final boxes = find.ancestor(
    of: find.text(text),
    matching: find.byType(DropFeedbackBox),
  );
  if (boxes.evaluate().isEmpty) return null;
  return tester.widget<DropFeedbackBox>(boxes.first).decoration;
}

/// Presses on 'Source' and moves [by] with a mouse, leaving the button held.
Future<TestGesture> _dragBy(WidgetTester tester, Offset by) async {
  final start = tester.getCenter(find.text('Source'));
  final gesture = await tester.createGesture(
      kind: PointerDeviceKind.mouse, pointer: _mousePointer, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 50));
  const steps = 6;
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(start + by * (i / steps));
    await tester.pump(const Duration(milliseconds: 20));
  }
  return gesture;
}

VTable _table() => VTable()
  ..id = 2
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..dragSource = true
  ..dropTargetId = _dropTargetId
  ..columns = [VTableColumn()
    ..id = 3
    ..width = 400
    ..text = 'Col']
  ..items = [
    for (final (id, text) in [(20, 'Source'), (21, 'Middle'), (22, 'Target')])
      VTableItem()
        ..id = id
        ..texts = [text]
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

void main() {
  setUp(freshClient);

  testWidgets('the drag cursor shows the operation the drop target answered', (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragOntoTarget(tester);

    await _answer(detail: Dnd.dropCopy, feedback: Dnd.feedbackSelect);
    await tester.pump();
    expect(_activeCursor(), SystemMouseCursors.copy);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackSelect);
    await tester.pump();
    expect(_activeCursor(), SystemMouseCursors.move);

    await _answer(detail: Dnd.dropNone, feedback: Dnd.feedbackNone);
    await tester.pump();
    expect(_activeCursor(), SystemMouseCursors.noDrop);

    await _finish(tester, gesture);
  });

  testWidgets('a drag over nothing that accepts drops shows "not allowed"', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(children: [
        SizedBox(
            width: 400,
            height: 300,
            child: TreeSwt<VTree>(value: _tree()..dropTargetId = null)),
        const SizedBox(width: 200, height: 300),
      ]),
    ));
    await tester.pump();
    final start = tester.getCenter(find.text('Source'));
    final gesture = await tester.createGesture(
        kind: PointerDeviceKind.mouse, pointer: _mousePointer, buttons: kPrimaryButton);
    await gesture.addPointer(location: start);
    await gesture.down(start);
    await tester.pump(const Duration(milliseconds: 50));
    for (var i = 1; i <= 6; i++) {
      await gesture.moveTo(start + Offset(60.0 * i, 0));
      await tester.pump(const Duration(milliseconds: 20));
    }

    expect(_activeCursor(), SystemMouseCursors.noDrop);

    await _finish(tester, gesture);
  });

  testWidgets('the Tree draws the feedback the drop listener asked for', (tester) async {
    await _pumpTree(tester);
    final gesture = await _dragOntoTarget(tester);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackInsertBefore);
    await tester.pump();
    var feedback = _dropFeedbackOn(tester, 'Target');
    expect(feedback?.border?.top.width, 2, reason: 'insertion line before the item');
    expect(feedback?.border?.bottom.width, 0);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackInsertAfter);
    await tester.pump();
    feedback = _dropFeedbackOn(tester, 'Target');
    expect(feedback?.border?.bottom.width, 2, reason: 'insertion line after the item');
    expect(feedback?.border?.top.width, 0);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackSelect);
    await tester.pump();
    feedback = _dropFeedbackOn(tester, 'Target');
    expect(feedback?.color, isNotNull, reason: 'the item is highlighted as the drop target');

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackNone);
    await tester.pump();
    expect(_dropFeedbackOn(tester, 'Target'), isNull);

    expect(_dropFeedbackOn(tester, 'Source'), isNull);
    expect(_dropFeedbackOn(tester, 'Middle'), isNull);

    await _finish(tester, gesture);
  });

  testWidgets('the Table draws the feedback the drop listener asked for', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: TableSwt<VTable>(value: _table())),
    ));
    await tester.pump();
    final gesture = await _dragOntoTarget(tester);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackInsertBefore);
    await tester.pump();
    var feedback = _dropFeedbackOn(tester, 'Target');
    expect(feedback?.border?.top.width, 2, reason: 'insertion line before the row');
    expect(feedback?.border?.bottom.width, 0);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackInsertAfter);
    await tester.pump();
    feedback = _dropFeedbackOn(tester, 'Target');
    expect(feedback?.border?.bottom.width, 2, reason: 'insertion line after the row');
    expect(feedback?.border?.top.width, 0);

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackSelect);
    await tester.pump();
    expect(_dropFeedbackOn(tester, 'Target')?.color, isNotNull,
        reason: 'the row is highlighted as the drop target');

    await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackNone);
    await tester.pump();
    expect(_dropFeedbackOn(tester, 'Target'), isNull);
    expect(_dropFeedbackOn(tester, 'Source'), isNull);

    await _finish(tester, gesture);
  });

  testWidgets('leaving a nested drop target for the one around it shows the outer answer',
      (tester) async {
    const outerId = 8;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: wrapDropTarget<DndDragPayload>(
        state: VControl()..dropTargetId = outerId,
        onDrop: (_, __, ___, ____) {},
        child: Column(children: [
          SizedBox(width: 400, height: 300, child: TreeSwt<VTree>(value: _tree())),
          const SizedBox(width: 400, height: 200),
        ]),
      ),
    ));
    await tester.pump();
    final gesture = await _dragBy(tester, const Offset(0, 330));

    await deliverFrame(
        'DropTarget/$outerId/Drop/negotiationResult', {'detail': Dnd.dropCopy, 'feedback': 0});
    await tester.pump();
    expect(_activeCursor(), SystemMouseCursors.copy);

    await _finish(tester, gesture);
  });

  testWidgets('a widget that accepts drags for itself but is no drop target shows "not allowed"',
      (tester) async {
    var accepted = false;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(children: [
        SizedBox(
            width: 400,
            height: 300,
            child: TreeSwt<VTree>(value: _tree()..dropTargetId = null)),
        wrapDropTarget<DndDragPayload>(
          state: VControl(),
          alwaysAccepts: true,
          onDrop: (_, __, ___, ____) {},
          builder: (_, child, __, isHovering) {
            accepted = accepted || isHovering;
            return child;
          },
          child: const SizedBox(width: 200, height: 300),
        ),
      ]),
    ));
    await tester.pump();
    final gesture = await _dragBy(tester, const Offset(300, 0));

    expect(accepted, isTrue, reason: 'the widget handles the drag itself');
    expect(_activeCursor(), SystemMouseCursors.noDrop);

    await _finish(tester, gesture);
  });
  testWidgets('on Windows the arrow stays and a badge next to it shows the operation',
      (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    try {
      await _pumpTree(tester);
      final gesture = await _dragOntoTarget(tester);
      DropOperationBadge badge() => tester.widget<DropOperationBadge>(find.byType(DropOperationBadge));

      await _answer(detail: Dnd.dropCopy, feedback: Dnd.feedbackSelect);
      await tester.pump();
      expect(_activeCursor(), SystemMouseCursors.basic);
      expect(badge().operation, Dnd.dropCopy);
      expect(find.byIcon(Icons.add), findsOneWidget);

      await _answer(detail: Dnd.dropMove, feedback: Dnd.feedbackSelect);
      await tester.pump();
      expect(_activeCursor(), SystemMouseCursors.basic);
      expect(badge().operation, Dnd.dropMove);

      await _answer(detail: Dnd.dropNone, feedback: Dnd.feedbackNone);
      await tester.pump();
      expect(_activeCursor(), SystemMouseCursors.noDrop);
      expect(find.byType(DropOperationBadge), findsNothing);

      await _finish(tester, gesture);
    } finally {
      debugDefaultTargetPlatformOverride = null;
    }
  });
}
