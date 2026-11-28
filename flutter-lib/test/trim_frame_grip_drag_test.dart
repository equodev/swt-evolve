// A trim contribution is moved by dragging the grip its frame Canvas draws beside the ToolBar: the
// application hears the frame's MouseDown, MouseMove and DragDetect and opens a Tracker from there.
// The trim composites build that Canvas themselves, so the frame has to report the pointer as any
// Canvas with children does, and a press on its grip leaves the keyboard focus where it was.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/sent_events.dart';

const int _frameId = 4201;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VToolBar _toolBar(int x, int y) => VToolBar()
  ..id = _frameId + 1
  ..style = SWT.FLAT | SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..items = const []
  ..bounds = _rect(x, y, 40, 22);

VCanvas _frame(VRectangle bounds, VToolBar toolBar) => VCanvas()
  ..id = _frameId
  ..swt = 'Canvas'
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..children = [toolBar]
  ..bounds = bounds;

VComposite _bar(String swt, VCanvas frame, VRectangle bounds) => VComposite()
  ..id = _frameId - 1
  ..swt = swt
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..children = [frame]
  ..bounds = bounds;

Finder get _frameWidget => find.byWidgetPredicate(
    (w) => (w is ToolbarComposite || w is SideBarComposite) && (w as CompositeSwt).value.id == _frameId);

/// The events the frame sent, by name, in the order they were sent.
List<String> _frameEvents() => sentEvents()
    .map((f) => f.$1)
    .where((channel) => channel.contains('/$_frameId/'))
    .map((channel) => channel.substring(channel.lastIndexOf('/') + 1))
    .toList();

/// Drags from [grip] in the frame by [delta], in steps apart on the wall clock: MouseMove is
/// throttled on it.
Future<void> _dragGrip(WidgetTester tester, Offset grip, Offset delta) async {
  final start = tester.getTopLeft(_frameWidget) + grip;
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, buttons: kPrimaryButton);
  await gesture.addPointer(location: start);
  await gesture.down(start);
  await tester.pump(const Duration(milliseconds: 10));
  for (var i = 1; i <= 4; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
    await gesture.moveTo(start + delta * (i / 4));
    await tester.pump(const Duration(milliseconds: 5));
  }
  await gesture.up();
  await tester.pump(const Duration(milliseconds: 600));
  await gesture.removePointer();
}

void _expectDragReported(List<String> events) {
  expect(events, contains('MouseDown'), reason: 'the press on the grip reaches the frame');
  expect(events, contains('MouseMove'), reason: 'the moves of the held pointer reach the frame');
  expect(events, contains('DragDetect'), reason: 'the drag on the grip is announced on the frame');
  expect(events, contains('MouseUp'), reason: 'the release reaches the frame');
  expect(events.indexOf('MouseDown'), lessThan(events.indexOf('DragDetect')));
}

void main() {
  for (final handles in [false, true]) {
    group('trim_drag_handles=$handles', () => _dragCases(handles));
  }
}

void _dragCases(bool handles) {
  setUp(() {
    EquoCommBase.recordSentFrames = true;
    setConfigFlags(ConfigFlags()..trim_drag_handles = handles);
  });
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
    resetConfigFlags();
  });

  testWidgets('dragging the grip of a frame in the main toolbar reports the drag on the frame',
      (tester) async {
    final bar = _bar('MainToolbar', _frame(_rect(40, 9, 89, 22), _toolBar(9, 0)), _rect(0, 0, 600, 40));
    final outside = FocusNode(debugLabel: 'outside');
    addTearDown(outside.dispose);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(children: [
        Focus(focusNode: outside, child: const SizedBox(width: 10, height: 10)),
        SizedBox(width: 600, height: 40, child: ToolbarComposite(value: bar, useBoundsLayout: true)),
      ]),
    ));
    await tester.pumpAndSettle();
    outside.requestFocus();
    await tester.pump();
    EquoCommService.commForTesting.clearSentFrames();

    await _dragGrip(tester, const Offset(3, 11), const Offset(-20, 0));

    _expectDragReported(_frameEvents());
    expect(FocusManager.instance.primaryFocus, outside, reason: 'a press on the grip takes no focus');
  });

  testWidgets('dragging the grip of a frame in a side trim reports the drag on the frame',
      (tester) async {
    final bar = _bar('SideBar', _frame(_rect(0, 0, 24, 80), _toolBar(0, 9)), _rect(0, 0, 24, 200));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 24, height: 200, child: SideBarComposite(value: bar))),
    ));
    await tester.pumpAndSettle();
    EquoCommService.commForTesting.clearSentFrames();

    await _dragGrip(tester, const Offset(12, 3), const Offset(0, 20));

    _expectDragReported(_frameEvents());
  });
}
