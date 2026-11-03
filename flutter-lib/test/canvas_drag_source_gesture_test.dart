// Clicks, refused drags and Escape on a Canvas that is both DragSource and DropTarget.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'support/sent_events.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// Ids differ per test: a value mounted under an id keeps being served for it.
var _nextId = 300;

VCanvas _canvas(int id, {required int x, int? dropTargetId, bool dragSource = false}) => VCanvas()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..dragSource = dragSource
  ..dropTargetId = dropTargetId
  ..bounds = _rect(x, 20, 300, 400);

Future<void> _pump(WidgetTester tester, List<VControl> children) async {
  tester.view.physicalSize = const Size(1200, 800);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  final shell = VShell()
    ..id = _nextId++
    ..style = SWT.SHELL_TRIM
    ..text = 'window'
    ..visible = true
    ..bounds = _rect(0, 0, 900, 600)
    ..children = children;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..shells = [shell]
        ..mainShellId = shell.id,
    ),
  ));
  await tester.pumpAndSettle();
  DndDropAnswer.reset();
  EquoCommService.commForTesting.clearSentFrames();
}

void _deliver(String actionId, Map<String, dynamic> body) => deliverTagged(actionId, body);

/// A refusal as DragSource's `sendDragStartResult` writes it.
const _refused = <String, dynamic>{'doit': false};

/// Every frame sent so far, as (action, body).
List<(String, Map<String, dynamic>)> _frames() => sentEvents();

List<String> _actions(String prefix) =>
    _frames().map((f) => f.$1).where((a) => a.startsWith(prefix)).map((a) => a.substring(prefix.length)).toList();

const _button1 = 1 << 19;

/// [x], [y] in the canvas at x=[left], as a position in the view.
Offset _at(double left, double x, double y) => Offset(left + x, 20 + y);

Future<TestGesture> _press(WidgetTester tester, Offset at) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
  await gesture.addPointer(location: at);
  await gesture.down(at);
  await tester.pump(const Duration(milliseconds: 10));
  return gesture;
}

/// One pointer step, apart on the wall clock: MouseMove is throttled on it.
Future<void> _step(WidgetTester tester, TestGesture gesture, Offset to) async {
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
  await gesture.moveTo(to);
  await tester.pump(const Duration(milliseconds: 5));
}

Future<void> _click(WidgetTester tester, Offset at) async {
  final gesture = await _press(tester, at);
  await gesture.up();
  await tester.pump(const Duration(milliseconds: 600));
  await gesture.removePointer();
}

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() => EquoCommBase.recordSentFrames = false);

  testWidgets('a click on a control that drags and takes drops is no drag', (tester) async {
    final id = _nextId++;
    final dropTargetId = _nextId++;
    await _pump(tester, [_canvas(id, x: 20, dragSource: true, dropTargetId: dropTargetId)]);

    for (var i = 0; i < 3; i++) {
      await _click(tester, _at(20, 100, 100 + 30.0 * i));
    }

    expect(_actions('DropTarget/$dropTargetId/'), isEmpty, reason: 'no DragEnter, DragOver or Drop');
    expect(_actions('Canvas/$id/DragDetect/'), isEmpty);
    expect(_actions('Canvas/$id/Mouse/').where((a) => a == 'MouseUp'), hasLength(3));
  });

  testWidgets('a refused drag after clicks gives the control every move with the button held',
      (tester) async {
    final id = _nextId++;
    final dropTargetId = _nextId++;
    await _pump(tester, [_canvas(id, x: 20, dragSource: true, dropTargetId: dropTargetId)]);
    // The select-first click before the drag.
    await _click(tester, _at(20, 200, 100));
    EquoCommService.commForTesting.clearSentFrames();

    // DragStart answers as soon as DragDetect is out, as on the real connection.
    final gesture = await _press(tester, _at(20, 40, 100));
    var answered = false;
    for (var i = 1; i <= 30; i++) {
      await _step(tester, gesture, _at(20, 40, 100.0 + 2 * i));
      if (!answered && _actions('Canvas/$id/DragDetect/').contains('DragDetect')) {
        _deliver('Canvas/$id/DragDetect/dragStartResult', _refused);
        await tester.pump();
        answered = true;
      }
    }
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 600));
    await gesture.removePointer();

    expect(answered, isTrue);
    final mouse = _frames()
        .where((f) => f.$1.startsWith('Canvas/$id/Mouse/') || f.$1.startsWith('Canvas/$id/MouseMove/'))
        .toList();
    final moves = mouse.where((f) => f.$1 == 'Canvas/$id/MouseMove/MouseMove').toList();
    // A NatTable enters its drag mode on the first MouseMove with the button held, before the release.
    expect(moves.map((m) => m.$2['y']), equals([for (var i = 1; i <= 30; i++) 100 + 2 * i]),
        reason: '${mouse.map((f) => f.$1)}');
    expect(moves.every((m) => (m.$2['stateMask'] as int) & _button1 != 0), isTrue);
    expect(mouse.last.$1, 'Canvas/$id/Mouse/MouseUp');
    expect(_actions('DropTarget/$dropTargetId/Drop/'), isNot(contains('drop')));
    expect(_actions('Canvas/$id/DragDetect/').where((a) => a != 'DragDetect'), isEmpty,
        reason: 'a refused drag reports nothing to other windows');
  });

  // DragStart's answer comes when Java gets to it: with the DragDetect, some moves later, or after
  // the drag went on for want of an answer.
  for (final (answered, stepsBefore, waitOut) in [
    ('with the DragDetect', 0, false),
    ('some moves after the DragDetect', 6, false),
    ('after the drag went on without it', 6, true),
  ]) {
    testWidgets('a drag refused $answered gives the control every move, in order, before the release',
        (tester) async {
      final id = _nextId++;
      final dropTargetId = _nextId++;
      await _pump(tester, [_canvas(id, x: 20, dragSource: true, dropTargetId: dropTargetId)]);

      final gesture = await _press(tester, _at(20, 40, 100));
      var detected = -1;
      var answeredAt = -1;
      var remoteBeforeAnswer = 0;
      for (var i = 1; i <= 30; i++) {
        await _step(tester, gesture, _at(20, 40, 100.0 + 2 * i));
        if (detected < 0 && _actions('Canvas/$id/DragDetect/').contains('DragDetect')) {
          detected = i;
          if (waitOut) await tester.pump(const Duration(milliseconds: 500));
        }
        if (detected > 0 && answeredAt < 0 && i >= detected + stepsBefore) {
          remoteBeforeAnswer = _actions('Canvas/$id/DragDetect/').length;
          _deliver('Canvas/$id/DragDetect/dragStartResult', _refused);
          answeredAt = i;
        }
      }
      expect(find.byWidgetPredicate((w) => w is Opacity && w.opacity == 0.3), findsNothing,
          reason: 'a refused drag dims nothing');
      await gesture.up();
      await tester.pump(const Duration(milliseconds: 600));
      await gesture.removePointer();

      expect(answeredAt, greaterThan(0));
      final mouse = _frames()
          .where((f) => f.$1.startsWith('Canvas/$id/Mouse/') || f.$1.startsWith('Canvas/$id/MouseMove/'))
          .toList();
      final moves = mouse.where((f) => f.$1 == 'Canvas/$id/MouseMove/MouseMove').toList();
      expect(moves.map((m) => m.$2['y']), equals([for (var i = 1; i <= 30; i++) 100 + 2 * i]),
          reason: '${mouse.map((f) => f.$1)}');
      expect(moves.every((m) => (m.$2['stateMask'] as int) & _button1 != 0), isTrue);
      expect(mouse.last.$1, 'Canvas/$id/Mouse/MouseUp');
      expect(_actions('DropTarget/$dropTargetId/Drop/'), isNot(contains('drop')));
      expect(_actions('Canvas/$id/DragDetect/').skip(remoteBeforeAnswer), isEmpty,
          reason: 'nothing more is reported to other windows once DragStart refuses');
    });
  }

  testWidgets('a drag allowed after it went on without an answer sends the control no MouseMove',
      (tester) async {
    final id = _nextId++;
    await _pump(tester, [_canvas(id, x: 20, dragSource: true)]);

    final gesture = await _press(tester, _at(20, 40, 100));
    var detectedAfter = -1;
    for (var i = 1; i <= 20; i++) {
      await _step(tester, gesture, _at(20, 40, 100.0 + 2 * i));
      if (detectedAfter < 0 && _actions('Canvas/$id/DragDetect/').contains('DragDetect')) {
        detectedAfter = _actions('Canvas/$id/MouseMove/').length;
        await tester.pump(const Duration(milliseconds: 500));
      }
      if (i == 12) _deliver('Canvas/$id/DragDetect/dragStartResult', {'doit': true});
    }
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 600));
    await gesture.removePointer();

    expect(detectedAfter, greaterThanOrEqualTo(0));
    expect(_actions('Canvas/$id/MouseMove/'), hasLength(detectedAfter));
  });

  testWidgets('Escape cancels a drag: DragLeave, no drop, the drag ends with nothing dropped',
      (tester) async {
    final source = _nextId++;
    final target = _nextId++;
    final dropTargetId = _nextId++;
    await _pump(tester, [
      _canvas(source, x: 20, dragSource: true),
      _canvas(target, x: 400, dropTargetId: dropTargetId),
    ]);

    final gesture = await _press(tester, _at(20, 100, 100));
    for (var i = 1; i <= 3; i++) {
      await _step(tester, gesture, _at(20, 100.0 + 5 * i, 100));
    }
    _deliver('Canvas/$source/DragDetect/dragStartResult', {'doit': true});
    await tester.pump();
    for (var i = 1; i <= 5; i++) {
      await _step(tester, gesture, _at(400, 20.0 * i, 100));
    }
    expect(_actions('DropTarget/$dropTargetId/Drop/'), contains('dragEnter'));

    await tester.sendKeyEvent(LogicalKeyboardKey.escape);
    await tester.pump();
    final afterEsc = _actions('DropTarget/$dropTargetId/Drop/');
    expect(afterEsc.last, 'dragLeave', reason: '$afterEsc');

    await _step(tester, gesture, _at(400, 120, 110));
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 600));
    await gesture.removePointer();

    final drops = _actions('DropTarget/$dropTargetId/Drop/');
    expect(drops.sublist(afterEsc.length), isEmpty, reason: 'nothing reaches the target after Escape: $drops');
    final remote = _actions('Canvas/$source/DragDetect/').where((a) => a != 'DragDetect' && a != 'dragMove');
    expect(remote, equals(['dragCancel']), reason: 'Java ends the drag with DROP_NONE');
    expect(_actions('Canvas/$source/Key/'), isEmpty, reason: 'the drag takes the Escape');
  });
}
