// One move in flight to Java once Java answers moves: a move made while the last one's updates have
// yet to be sent is held, replaced by later ones, and sent when Java answers or the wait times out.
// A Java that never answers is never waited for.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/control_evolve.dart' show ControlImpl;
import 'package:swtflutter/src/impl/utils/move_flow.dart';
import 'package:swtflutter/src/impl/utils/pending_move.dart';

VText _field() => VText()
  ..swt = 'Text'
  ..id = 4
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = 'hello'
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 260
    ..height = 30);

/// The clock the throttle and the flow read: moved by hand, so a wait depends on nothing else.
int _now = 1 << 30;

/// Pumps the field and returns the x of every move it sends from then on.
Future<List<int?>> _pumpField(WidgetTester tester) async {
  final moves = <int?>[];
  final previous = ControlImpl.nowMicros;
  ControlImpl.nowMicros = () => _now;
  // Asked already: the test answers for Java itself.
  MoveFlow.reset(asked: true);
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.endsWith('/MouseMove/MouseMove')) moves.add((payload as VEvent?)?.x);
  };
  addTearDown(() {
    EquoCommBase.sendObserverForTesting = null;
    ControlImpl.nowMicros = previous;
    PendingMove.reset();
    MoveFlow.reset();
  });
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 400, height: 200, child: TextSwt<VText>(value: _field())),
    ),
  ));
  await tester.pumpAndSettle();
  return moves;
}

/// Moves the pointer to (x, 15), a throttle window after the last move.
Future<void> _move(WidgetTester tester, TestPointer pointer, double x) async {
  _now += 20000;
  await tester.sendEventToBinding(pointer.hover(Offset(x, 15)));
}

void main() {
  testWidgets('moves are not held for a Java that never answered one', (tester) async {
    final moves = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await _move(tester, pointer, 100);
    await _move(tester, pointer, 110);
    await _move(tester, pointer, 120);
    await tester.pump();

    expect(moves, [100, 110, 120]);
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('once Java answers, moves made before its next answer go as the latest of them', (tester) async {
    final moves = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    MoveFlow.answered();
    await _move(tester, pointer, 100);
    await _move(tester, pointer, 110);
    await _move(tester, pointer, 120);
    await tester.pump(const Duration(milliseconds: 30));
    expect(moves, [100]);

    MoveFlow.answered();
    expect(moves, [100, 120]);
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a move held for an answer that does not come goes when the wait times out', (tester) async {
    final moves = await _pumpField(tester);
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    MoveFlow.answered();
    await _move(tester, pointer, 100);
    _now += 10000;
    await tester.sendEventToBinding(pointer.hover(const Offset(110, 15)));
    await tester.pump(const Duration(milliseconds: 80));
    expect(moves, [100]);

    await tester.pump(const Duration(milliseconds: 20));
    expect(moves, [100, 110]);
    await tester.sendEventToBinding(pointer.removePointer());
  });

  testWidgets('a press sends the move held for an answer ahead of it', (tester) async {
    final moves = <String>[];
    await _pumpField(tester);
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.contains('/MouseMove/') || action.contains('/MouseDown')) moves.add('${action.split('/').last} ${(payload as VEvent?)?.x}');
    };
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    MoveFlow.answered();
    await _move(tester, pointer, 100);
    await _move(tester, pointer, 110);
    _now += 20000;
    await tester.sendEventToBinding(pointer.down(const Offset(110, 15)));
    await tester.pump();

    expect(moves.take(3), ['MouseMove 100', 'MouseMove 110', 'MouseDown 110']);
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 600));
  });
  testWidgets('the moves of a held pointer wait for the answer too, and its release follows the latest', (tester) async {
    final sent = <String>[];
    await _pumpField(tester);
    EquoCommBase.sendObserverForTesting = (action, payload) {
      if (action.contains('/MouseMove/') || action.contains('/MouseUp')) {
        sent.add('${action.split('/').last} ${(payload as VEvent?)?.x}');
      }
    };
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(90, 15)));
    await tester.sendEventToBinding(pointer.down(const Offset(90, 15)));
    await tester.pump();
    MoveFlow.answered();
    sent.clear();
    _now += 20000;
    await tester.sendEventToBinding(pointer.move(const Offset(100, 15)));
    _now += 5000;
    await tester.sendEventToBinding(pointer.move(const Offset(110, 15)));
    _now += 5000;
    await tester.sendEventToBinding(pointer.move(const Offset(120, 15)));
    await tester.pump(const Duration(milliseconds: 30));
    expect(sent, ['MouseMove 100']);

    MoveFlow.answered();
    expect(sent, ['MouseMove 100', 'MouseMove 120']);

    await tester.sendEventToBinding(pointer.move(const Offset(130, 15)));
    await tester.sendEventToBinding(pointer.up());
    await tester.pump(const Duration(milliseconds: 600));
    expect(sent.skip(2).take(2), ['MouseMove 130', 'MouseUp 130']);
  });
}
