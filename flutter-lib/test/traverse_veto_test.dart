// SWT contract: a TraverseListener setting `e.doit = false` keeps focus where it is. Flutter
// traverses on Tab by itself, so an unheld Tab moved focus before Java could object.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/traversal_veto_scope.dart';
import 'package:swtflutter/src/impl/utils/veto_gate.dart';

const int _displayId = 900;

late VetoGate _gate;

void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

/// Mounts the traversal scope the Display installs around its shells, with two focusable fields
/// inside it, and puts the keyboard on the first one.
Future<List<FocusNode>> _pumpDisplayWithTwoFields(WidgetTester tester) async {
  final first = FocusNode(debugLabel: 'first');
  final second = FocusNode(debugLabel: 'second');
  _gate = VetoGate('traverse')..attach('Display', _displayId);
  addTearDown(first.dispose);
  addTearDown(second.dispose);
  addTearDown(_gate.detach);

  await tester.pumpWidget(MaterialApp(
    home: TraversalVetoScope(
      gate: _gate,
      child: Column(children: [
        Focus(focusNode: first, child: const SizedBox(width: 10, height: 10)),
        Focus(focusNode: second, child: const SizedBox(width: 10, height: 10)),
      ]),
    ),
  ));
  first.requestFocus();
  await tester.pump();
  expect(first.hasFocus, isTrue);
  return [first, second];
}

void main() {
  testWidgets('with no TraverseListener, Tab traverses immediately', (tester) async {
    final nodes = await _pumpDisplayWithTwoFields(tester);

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();

    expect(nodes[1].hasFocus, isTrue,
        reason: 'the fast path is unchanged when no veto is possible');
  });

  testWidgets('a vetoed Tab leaves focus where it was', (tester) async {
    final nodes = await _pumpDisplayWithTwoFields(tester);
    _receiveJson('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(nodes[0].hasFocus, isTrue,
        reason: 'focus must not move before Java has answered');

    _receiveJson('Display/$_displayId/traverse/verdict', {'doit': false});
    await tester.pump();
    expect(nodes[0].hasFocus, isTrue, reason: 'the listener vetoed the traversal');
  });

  testWidgets('an allowed Tab traverses once Java answers', (tester) async {
    final nodes = await _pumpDisplayWithTwoFields(tester);
    _receiveJson('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(nodes[0].hasFocus, isTrue, reason: 'held while Java is thinking');

    _receiveJson('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pump();
    expect(nodes[1].hasFocus, isTrue);
  });

  testWidgets('a Tab whose verdict never arrives still traverses', (tester) async {
    final nodes = await _pumpDisplayWithTwoFields(tester);
    _receiveJson('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump(const Duration(milliseconds: 400));

    expect(nodes[1].hasFocus, isTrue,
        reason: 'failing open: a lost verdict must not trap the keyboard');
  });

  testWidgets('focus moving to a control with no listener disarms the gate', (tester) async {
    final nodes = await _pumpDisplayWithTwoFields(tester);
    _receiveJson('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();
    // Java publishes the disarming false on every focus change, because the Display never hears
    // that the focused control changed.
    _receiveJson('Display/$_displayId/traverse/vetoable', {'value': false});
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(nodes[1].hasFocus, isTrue, reason: 'back on the fast path');
  });
}
