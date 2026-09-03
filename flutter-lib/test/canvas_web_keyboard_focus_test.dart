// In Evolve web (whole-tree) mode, a Canvas that takes keyboard input (e.g. TM Terminal's
// TextCanvas) was fully non-interactive: typing never reached it. In whole-tree mode every keystroke
// is captured once at the Display root and routed to Java's *focus control* (DisplayBridge
// .routeKeyToFocused → api.getFocusControl()). A control only becomes that focus control by telling
// Java it gained focus (a "Focus/FocusIn" message — see DartControl's Focus/FocusIn handler, which
// calls bridge.setFocus(this)). Every other keyboard control (Text, List, …) wires its FocusNode to
// sendFocusFocusIn on focus; the Canvas grabbed Flutter keyboard focus on tap but never sent
// Focus/FocusIn, so it never became getFocusControl() and web-mode keystrokes were routed elsewhere
// (nowhere) — a dead terminal. The Canvas's own onKeyEvent forwarder is deliberately silent in
// whole-tree mode (displayLevelKeyForwardingActive), so Focus/FocusIn is the ONLY path that makes it
// typeable there.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// The generated CanvasSwt with sendEvent captured instead of handed to EquoCommService (a widget
/// test has no live transport). The State and its real focus path are untouched.
class _CapturingCanvasSwt extends CanvasSwt<VCanvas> {
  const _CapturingCanvasSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCanvas val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCanvas _childlessCanvas() => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

void main() {
  testWidgets(
      'a childless Canvas notifies Java (Focus/FocusIn) when it takes keyboard focus, so web-mode '
      'keystrokes route to it', (tester) async {
    final events = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: _CapturingCanvasSwt(
        value: _childlessCanvas(),
        onEvent: (ev, _) => events.add(ev),
      ),
    ));
    await tester.pumpAndSettle();

    // Click into the Canvas — this is what grabs its keyboard focus (CanvasImpl takes _keyboardFocus
    // on pointer-down for a childless Canvas).
    await tester.tapAt(tester.getCenter(find.byType(_CapturingCanvasSwt)));
    await tester.pumpAndSettle();

    expect(events, contains('Focus/FocusIn'),
        reason: 'a Canvas that took keyboard focus must send Focus/FocusIn so Java makes it the '
            'focus control that web-mode display-level key routing delivers keystrokes to; without '
            'it the terminal Canvas is un-typeable in web mode');
  });
}
