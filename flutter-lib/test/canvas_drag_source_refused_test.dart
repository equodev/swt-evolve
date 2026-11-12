// A DragStart listener may refuse a drag by where it was pressed: a self-drawn table refuses it on
// a column whose press-and-drag selects rows. Natively the refused drag never starts, so the
// control gets every MouseMove and the MouseUp, and nothing on screen shows a drag. A drag the
// listener allows takes the pointer: no MouseMove reaches the control until its MouseUp.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show SystemMouseCursor;
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/utils/dnd_utils.dart' show DropOperationBadge;

import 'support/channels.dart' as channels;

class _RecordingCanvasSwt extends CanvasSwt<VCanvas> {
  const _RecordingCanvasSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendEvent(VCanvas val, int channel, VEvent? payload) {
    final ev = wireChannelNames[channel]!;
    if (ev.startsWith('MouseMove/')) {
      calls.add('$ev@${payload?.y}');
    } else if (ev.startsWith('Mouse/') || ev.startsWith('DragDetect/')) {
      calls.add(ev);
    }
  }
}

VCanvas _canvas() => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..dragSource = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 400);

/// The verdict as DragSource's `sendDragStartResult` writes it: `{doit}`, false included.
void _dragStartResult(bool doit) {
  final frame = channels.frame('Canvas/1/DragDetect/dragStartResult', TaggedValues.encode({'doit': doit}));
  EquoCommService.commForTesting.receiveBinary(frame);
}

/// Anything that shows a drag: the region that sets its cursor, its operation badge, or the source
/// drawn dimmed.
final _dragFeedback = find.byWidgetPredicate((w) =>
    (w is MouseRegion && !w.opaque && w.cursor is SystemMouseCursor) ||
    w is DropOperationBadge ||
    (w is Opacity && w.opacity < 1));

bool _showsDrag() => _dragFeedback.evaluate().isNotEmpty;

/// Presses at y=20 and moves down 20 px at a time; Java answers [doit] once DragDetect is out.
/// Returns the calls and whether drag feedback showed before and after the answer.
Future<(List<String>, bool, bool)> _drag(WidgetTester tester, {required bool doit}) async {
  // The operation badge is drawn on Windows only.
  debugDefaultTargetPlatformOverride = TargetPlatform.windows;
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 400,
      child: _RecordingCanvasSwt(value: _canvas(), calls: calls),
    ),
  ));
  await tester.pumpAndSettle();

  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
  await gesture.addPointer(location: const Offset(100, 20));
  await gesture.down(const Offset(100, 20));
  await tester.pump(const Duration(milliseconds: 10));
  var shownBefore = false;
  var shownAfter = false;
  var answered = false;
  for (var i = 1; i <= 12; i++) {
    // MouseMove is throttled on the wall clock.
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await gesture.moveTo(Offset(100, 20.0 + 20 * i));
    await tester.pump(const Duration(milliseconds: 10));
    if (answered) {
      shownAfter |= _showsDrag();
    } else {
      shownBefore |= _showsDrag();
    }
    // Java answers a few moves after DragDetect, as it does over a real connection.
    if (!answered && i >= 6 && calls.contains('DragDetect/DragDetect')) {
      _dragStartResult(doit);
      await tester.pump();
      answered = true;
      shownAfter |= _showsDrag();
    }
  }
  expect(answered, isTrue, reason: '$calls');
  await gesture.up();
  await tester.pump(const Duration(seconds: 1));
  shownAfter |= _showsDrag();
  await gesture.removePointer();
  debugDefaultTargetPlatformOverride = null;
  return (calls, shownBefore, shownAfter);
}

void main() {
  testWidgets('a refused drag leaves every move and the release to the control', (tester) async {
    final (calls, _, __) = await _drag(tester, doit: false);
    final moves = calls.where((c) => c.startsWith('MouseMove/')).map((c) => c.split('@').last).toList();
    expect(moves, equals([for (var i = 1; i <= 12; i++) '${20 + 20 * i}']), reason: '$calls');
    expect(calls.last, 'Mouse/MouseUp', reason: '$calls');
    expect(calls.where((c) => c.startsWith('DragDetect/') && c != 'DragDetect/DragDetect'), isEmpty,
        reason: 'a refused drag reports nothing to other windows: $calls');
  });

  // The moves held until the answer go out with it, and the next move goes at once: a held
  // pointer's every move is sent.
  testWidgets('the moves a refused drag held go out at the answer, and the next at once', (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 400,
        child: _RecordingCanvasSwt(value: _canvas(), calls: calls),
      ),
    ));
    await tester.pumpAndSettle();
    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
    await gesture.addPointer(location: const Offset(100, 20));
    await gesture.down(const Offset(100, 20));
    await tester.pump(const Duration(milliseconds: 10));
    var y = 20.0;
    for (var i = 0; i < 4 || !calls.contains('DragDetect/DragDetect'); i++) {
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
      y += 20;
      await gesture.moveTo(Offset(100, y));
      await tester.pump(const Duration(milliseconds: 10));
    }
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    y += 20;
    await gesture.moveTo(Offset(100, y));
    final heldUntilTheAnswer = 'MouseMove/MouseMove@${y.round()}';
    expect(calls, isNot(contains(heldUntilTheAnswer)), reason: 'held until Java answers: $calls');

    _dragStartResult(false);
    await tester.pump();
    expect(calls, contains(heldUntilTheAnswer), reason: '$calls');
    await gesture.moveTo(Offset(100, y + 20));
    final next = 'MouseMove/MouseMove@${(y + 20).round()}';
    expect(calls, contains(next), reason: 'sent as it comes: $calls');

    await gesture.up();
    await tester.pump(const Duration(seconds: 1));
    await gesture.removePointer();
  });

  testWidgets('a refused drag shows no cursor, badge or dimming, before or after the answer',
      (tester) async {
    final (_, shownBefore, shownAfter) = await _drag(tester, doit: false);
    expect(shownBefore, isFalse, reason: 'nothing shows while Java has not answered');
    expect(shownAfter, isFalse, reason: 'nothing shows once Java refused');
  });

  testWidgets('an allowed drag shows once Java answers and keeps its moves from the control',
      (tester) async {
    final (calls, shownBefore, shownAfter) = await _drag(tester, doit: true);
    expect(shownBefore, isFalse);
    expect(shownAfter, isTrue);
    final detect = calls.indexOf('DragDetect/DragDetect');
    final afterDetect = calls.sublist(detect + 1).where((c) => !c.startsWith('DragDetect/')).toList();
    expect(afterDetect, equals(['Mouse/MouseUp']), reason: '$calls');
  });
}
