// A native scroll bar sits outside the client area, so a press on StyledText's drawn bars must
// not reach Java's mouse listeners, which would move the caret or select.

import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/canvas_evolve.dart';

class _RecordingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _RecordingStyledTextSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VStyledText val, VEvent? payload) => calls.add('down');

  @override
  void sendMouseMouseUp(VStyledText val, VEvent? payload) => calls.add('up');

  @override
  void sendFocusFocusIn(VStyledText val, VEvent? payload) => calls.add('focus');
}

VScrollBar _bar(int id) => VScrollBar()
  ..swt = 'ScrollBar'
  ..id = id
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..minimum = 0
  ..maximum = 1000
  ..thumb = 50
  ..selection = 0;

VStyledText _styledText() => VStyledText()
  ..swt = 'StyledText'
  ..id = 42
  ..style = SWT.MULTI | SWT.V_SCROLL
  ..enabled = true
  ..visible = true
  ..editable = true
  ..text = List.generate(200, (i) => 'line $i').join('\n')
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200)
  // What StyledText's constructor asks for: the text cursor over the whole control.
  ..cursor = (VCursor()..cursorStyle = SWT.CURSOR_IBEAM)
  ..verticalBar = _bar(11);

Future<List<String>> _pump(WidgetTester tester) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 300,
        height: 200,
        child: _RecordingStyledTextSwt(value: _styledText(), calls: calls),
      ),
    ),
  ));
  await tester.pump();
  return calls;
}

void main() {
  testWidgets('pressing and dragging on the scroll bar is not a click in the text', (tester) async {
    final calls = await _pump(tester);

    // Near the right edge, halfway down: on the vertical bar's track.
    final gesture = await tester.startGesture(const Offset(294, 100), kind: PointerDeviceKind.mouse);
    await gesture.moveBy(const Offset(0, 30));
    await gesture.up();
    await tester.pump(const Duration(milliseconds: 400));

    expect(calls, isNot(contains('down')));
    expect(calls, isNot(contains('up')));
  });

  testWidgets('a click in the text still is', (tester) async {
    final calls = await _pump(tester);

    await tester.tapAt(const Offset(100, 80), kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 400));

    expect(calls, containsAllInOrder(['down', 'up']));
  });

  testWidgets('over the scroll bar the pointer is an arrow, not a text cursor', (tester) async {
    await _pump(tester);
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(100, 80));
    await tester.pump();
    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(1),
        SystemMouseCursors.text,
        reason: 'sanity: over the text it is a text cursor');

    await mouse.moveTo(const Offset(294, 100));
    await tester.pump();

    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(1),
        SystemMouseCursors.basic);
    await mouse.removePointer();
  });

  group('a press on the track', () {
    int? at(double y) => scrollBarSelectionAt(y,
        trackSize: 200, thumbSize: 20, minimum: 0, maximum: 1000, thumb: 100);

    test('centres the thumb on it', () {
      // Halfway down the track is halfway through the range the thumb can travel.
      expect(at(100), 450);
    });

    test('stops at either end', () {
      expect(at(0), 0);
      expect(at(200), 900);
    });

    test('has nothing to do when everything is already shown', () {
      expect(scrollBarSelectionAt(50,
          trackSize: 200, thumbSize: 200, minimum: 0, maximum: 100, thumb: 100), isNull);
    });
  });
}
