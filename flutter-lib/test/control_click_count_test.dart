// SWT reports a truthful click count on mouse events: 1 for a single click,
// 2 on the second click of a double-click, and back to 1 once the double-click
// window has passed. Java-side consumers key real behavior off it —
// StyledText takes its word/line-selection branch whenever MouseDown arrives
// with count > 1, so a chrome that reports every down as count=2 makes every
// editor click word-select and desyncs the caret JFace painters read.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

class _RecordingLabelSwt extends LabelSwt<VLabel> {
  final List<String> calls;
  const _RecordingLabelSwt({required VLabel value, required this.calls})
      : super(value: value);

  @override
  void sendMouseMouseDown(VLabel val, VEvent? payload) =>
      calls.add('down:${payload?.count}');

  @override
  void sendMouseMouseUp(VLabel val, VEvent? payload) =>
      calls.add('up:${payload?.count}');
}

VLabel _label() => VLabel()
  ..id = 1
  ..enabled = true
  ..text = 'target'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

Future<List<String>> _pumpAndReset(WidgetTester tester, List<String> calls) async {
  await tester.pump(Duration.zero);
  final snapshot = List<String>.from(calls);
  calls.clear();
  return snapshot;
}

/// One press+release at [at], with explicit pointer timestamps — the chrome's
/// click-count chaining reads PointerEvent.timeStamp, which tester.tap leaves
/// at a constant in the test binding.
Future<void> _click(WidgetTester tester, Offset at, Duration when) async {
  final gesture = await tester.createGesture();
  await gesture.down(at, timeStamp: when);
  await gesture.up(timeStamp: when + const Duration(milliseconds: 20));
}

Future<List<String>> _setUpLabel(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: _RecordingLabelSwt(value: _label(), calls: calls),
    ),
  ));
  await tester.pump();
  return calls;
}

void main() {
  testWidgets('a single click carries count=1 on MouseDown and MouseUp',
      (tester) async {
    final calls = await _setUpLabel(tester, <String>[]);
    final at = tester.getCenter(find.text('target'));

    await _click(tester, at, const Duration(seconds: 1));
    final events = await _pumpAndReset(tester, calls);

    expect(events, equals(['down:1', 'up:1']),
        reason: 'a plain click is a single click, not a double-click');
  });

  testWidgets('the second click of a double-click carries count=2',
      (tester) async {
    final calls = await _setUpLabel(tester, <String>[]);
    final at = tester.getCenter(find.text('target'));

    await _click(tester, at, const Duration(seconds: 1));
    await _click(tester, at, const Duration(seconds: 1, milliseconds: 150));
    final events = await _pumpAndReset(tester, calls);

    expect(events, equals(['down:1', 'up:1', 'down:2', 'up:2']),
        reason: 'consecutive downs inside the double-click window chain');
  });

  testWidgets('a click after the double-click window resets to count=1',
      (tester) async {
    final calls = await _setUpLabel(tester, <String>[]);
    final at = tester.getCenter(find.text('target'));

    await _click(tester, at, const Duration(seconds: 1));
    await _click(
        tester, at, const Duration(seconds: 1) + kDoubleTapTimeout * 2);
    final events = await _pumpAndReset(tester, calls);

    expect(events, equals(['down:1', 'up:1', 'down:1', 'up:1']),
        reason: 'clicks separated by more than the double-click window do not chain');
  });
}
