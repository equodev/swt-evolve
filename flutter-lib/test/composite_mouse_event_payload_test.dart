// An application that drives selection from raw SWT.MouseDown/SWT.MouseUp listeners reads
// Event.button and Event.count to tell a plain left click from a middle/right click or a
// double-click. Native SWT fills both on the down *and* the up; the interaction chrome of a
// Composite (or Canvas) that has children filled neither on the up and no count on the down, so
// such a handler saw button=0/count=0 and refused every click.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingCompositeSwt extends CompositeSwt<VComposite> {
  const _RecordingCompositeSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VComposite val, VEvent? payload) =>
      calls.add('down:${payload?.button}:${payload?.count}');

  @override
  void sendMouseMouseUp(VComposite val, VEvent? payload) =>
      calls.add('up:${payload?.button}:${payload?.count}');
}

/// Records stateMask instead of button/count, so the parity cases below read as masks.
class _MaskCompositeSwt extends CompositeSwt<VComposite> {
  const _MaskCompositeSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VComposite val, VEvent? payload) =>
      calls.add('down:${payload?.stateMask}');

  @override
  void sendMouseMouseUp(VComposite val, VEvent? payload) =>
      calls.add('up:${payload?.stateMask}');
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// A Composite carrying a child, so build() takes the buildComposite() branch and the
/// interaction chrome — not ControlImpl.wrap() — owns the pointer. The child sits in the
/// bottom strip; the clicks below land on the parent's own area.
VComposite _compositeWithChild() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 60)
  ..children = [
    VLabel()
      ..id = 2
      ..text = 'child'
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 45, 200, 15)
  ];

class _RecordingCanvasSwt extends CanvasSwt<VCanvas> {
  const _RecordingCanvasSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VCanvas val, VEvent? payload) =>
      calls.add('down:${payload?.button}:${payload?.count}');

  @override
  void sendMouseMouseUp(VCanvas val, VEvent? payload) =>
      calls.add('up:${payload?.button}:${payload?.count}');
}

/// The other shape an owner-drawn toolbar button takes: a childless Canvas that paints itself
/// from a SWT.Paint listener. It is served by ControlImpl.wrap(), not the with-children chrome.
VCanvas _childlessCanvas() => VCanvas()
  ..id = 3
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 60);

Future<List<String>> _setUp(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: _RecordingCompositeSwt(value: _compositeWithChild(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();
  return calls;
}

/// One press+release with explicit timestamps — the chrome's click-count chaining reads
/// PointerEvent.timeStamp, which tester.tap leaves at a constant in the test binding.
Future<void> _click(
  WidgetTester tester,
  Offset at,
  Duration when, {
  int buttons = kPrimaryButton,
}) async {
  final gesture = await tester.createGesture(buttons: buttons);
  await gesture.down(at, timeStamp: when);
  await gesture.up(timeStamp: when + const Duration(milliseconds: 20));
}

Future<List<String>> _setUpMask(WidgetTester tester, List<String> calls) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: _MaskCompositeSwt(value: _compositeWithChild(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();
  return calls;
}

void main() {
  const onParent = Offset(100, 20);

  testWidgets('a left click on a Composite with children carries button=1, count=1 on both edges',
      (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onParent, const Duration(seconds: 1));
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:1:1', 'up:1:1']),
        reason: 'native SWT reports the left button and a click count of 1 on MouseDown and MouseUp');
  });

  testWidgets('the second click of a double-click carries count=2 on a Composite with children',
      (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onParent, const Duration(seconds: 1));
    await _click(tester, onParent, const Duration(seconds: 1, milliseconds: 150));
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:1:1', 'up:1:1', 'down:1:2', 'up:1:2']),
        reason: 'consecutive downs inside the double-click window chain, as they do on a Label');
  });

  testWidgets('a click after the double-click window resets to count=1', (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onParent, const Duration(seconds: 1));
    await _click(tester, onParent, const Duration(seconds: 1) + kDoubleTapTimeout * 2);
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:1:1', 'up:1:1', 'down:1:1', 'up:1:1']));
  });

  testWidgets('a childless Canvas reports the same payload as a Composite with children',
      (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 60,
        child: _RecordingCanvasSwt(value: _childlessCanvas(), calls: calls),
      ),
    ));
    await tester.pumpAndSettle();

    await _click(tester, onParent, const Duration(seconds: 1));
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:1:1', 'up:1:1']));
  });

  testWidgets('a middle click reports button=2, not the left button', (tester) async {
    final calls = await _setUp(tester, <String>[]);

    await _click(tester, onParent, const Duration(seconds: 1), buttons: kMiddleMouseButton);
    await tester.pump(Duration.zero);

    expect(calls, equals(['down:2:1', 'up:2:1']),
        reason: 'a handler gated on the left button must not fire for a middle click');
  });

  // Parity with native SWT's Widget.setInputState, read from the upstream 3.134 sources for
  // win32, cocoa and gtk -- all three agree: a mouse event carries the modifiers held plus the
  // buttons down, and the event's own button is EXCLUDED on the press (the state before it) and
  // INCLUDED on the release.
  group('stateMask parity', () {
    testWidgets('a plain left click: nothing on the press, BUTTON1 on the release',
        (tester) async {
      final calls = await _setUpMask(tester, <String>[]);

      await _click(tester, onParent, const Duration(seconds: 1));
      await tester.pump(Duration.zero);

      expect(calls, equals(['down:0', 'up:${SWT.BUTTON1}']));
    });

    testWidgets('a right click reports BUTTON3 on the release, not BUTTON1', (tester) async {
      final calls = await _setUpMask(tester, <String>[]);

      await _click(tester, onParent, const Duration(seconds: 1),
          buttons: kSecondaryMouseButton);
      await tester.pump(Duration.zero);

      expect(calls, equals(['down:0', 'up:${SWT.BUTTON3}']));
    });

    testWidgets('a ctrl+click carries SWT.CTRL on both edges', (tester) async {
      final calls = await _setUpMask(tester, <String>[]);

      await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft);
      await _click(tester, onParent, const Duration(seconds: 1));
      await tester.pump(Duration.zero);
      await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft);

      expect(calls, equals(['down:${SWT.CTRL}', 'up:${SWT.CTRL | SWT.BUTTON1}']));
    });

    testWidgets('a shift+click carries SWT.SHIFT on both edges', (tester) async {
      final calls = await _setUpMask(tester, <String>[]);

      await simulateKeyDownEvent(LogicalKeyboardKey.shiftLeft);
      await _click(tester, onParent, const Duration(seconds: 1));
      await tester.pump(Duration.zero);
      await simulateKeyUpEvent(LogicalKeyboardKey.shiftLeft);

      expect(calls, equals(['down:${SWT.SHIFT}', 'up:${SWT.SHIFT | SWT.BUTTON1}']));
    });
  });
}
