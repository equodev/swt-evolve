// An application that drags with a Tracker resolves the drop from the pointer position the Tracker
// loop reports, and draws its own feedback by moving a small shell to follow it. Those two halves
// are a round trip through this side: a pointer position is converted window -> display on the way
// out, and a shell's bounds come back in display coordinates to be placed in the window again.
//
// If the two directions disagree by any constant, the feedback lands somewhere the pointer is not,
// and the further the application trusts it the worse the drop. So this pins the round trip itself:
// a shell placed at the display position reported for a pointer must render where that pointer was.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/region.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/region_clip.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// The application window. SHELL_TRIM is what an ordinary main window carries, and it is what
/// decides whether this side draws a title bar of its own above the content.
VShell _mainShell() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'app'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [];

/// The drag feedback: a small shaped shell the application moves during the gesture, the shape a
/// hand-drawn dockable toolbar uses. Not window-sized -- a window-sized one at the origin hides
/// any origin error, which is why this one is small and placed away from it.
VShell _feedback(int x, int y) => VShell()
  ..id = 2
  ..style = SWT.NO_TRIM | SWT.ON_TOP
  ..bounds = _rect(x, y, 120, 40)
  ..region = (VRegion()..rects = [0, 0, 120, 40])
  ..children = [];

Future<void> _pump(WidgetTester tester, List<VShell> shells) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..shells = shells
        ..mainShellId = 1,
    ),
  ));
  await tester.pumpAndSettle();
}

void _deliver(String actionId, Map<String, dynamic> body) {
  final actionBytes = utf8.encode(actionId);
  final bodyBytes = utf8.encode(json.encode(body));
  final frame = Uint8List(2 + actionBytes.length + bodyBytes.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, bodyBytes);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  late List<VEvent> reported;

  setUp(() {
    reported = [];
    TrackerSession.sendForTesting = (id, action, event) {
      if (action == 'Control/Move') reported.add(event);
    };
  });
  tearDown(() => TrackerSession.sendForTesting = null);

  /// Drags to [pointer] with a Tracker open on [host], and answers the position the Tracker loop
  /// was told the pointer is.
  Future<Offset> reportedFor(
      WidgetTester tester, List<VShell> shells, String host, Offset pointer) async {
    reported.clear();
    await _pump(tester, shells);
    final gesture = await tester.startGesture(const Offset(50, 50));
    _deliver('$host/Tracker/open', {'itemId': 77});
    await tester.pump();
    await gesture.moveTo(pointer);
    await tester.pump();
    await gesture.up();
    await tester.pump();
    expect(reported, isNotEmpty,
        reason: 'the Tracker loop only advances on the positions reported from here');
    return Offset(reported.last.x!.toDouble(), reported.last.y!.toDouble());
  }

  testWidgets('a pointer over the main shell is reported at its own window position',
      (tester) async {
    const pointer = Offset(420, 310);

    expect(await reportedFor(tester, [_mainShell()], 'Shell/1', pointer), pointer);
  });

  testWidgets('a Tracker hosted by a shell away from the origin reports the same position',
      (tester) async {
    // The host's own origin cancels: the conversion subtracts where its content sits and adds the
    // bounds Java gave it back. A Tracker opened on a Composite in a secondary window must not
    // report a position shifted by that window's position.
    const pointer = Offset(420, 310);

    expect(
        await reportedFor(tester, [_mainShell(), _feedback(200, 150)..id = 2], 'Shell/2', pointer),
        pointer);
  });

  testWidgets('a shell renders where its bounds say, which is what makes the trip cancel',
      (tester) async {
    await _pump(tester, [_mainShell(), _feedback(200, 150)..id = 2]);

    final shells = tester.widgetList<ShellSwt>(find.byType(ShellSwt)).toList();
    expect(shells, hasLength(2));
    for (final shell in shells) {
      final b = shell.value.bounds!;
      expect(tester.getTopLeft(find.byWidget(shell)), Offset(b.x.toDouble(), b.y.toDouble()),
          reason: 'a shell drawn anywhere but at its own bounds breaks the round trip the '
              'Tracker conversion depends on');
    }
  });
}
