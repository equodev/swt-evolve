// An SWT_AWT island under swing-evolve's engine: Java describes the composite SWT_AWT.new_Frame
// creates as a "SwingIsland" and, asked, answers with the AWT frame's windowId and the engine's
// port. The region hands that window to the host's mirror builder and owns the one thing only the
// embedder can: focus. A press into the island must tell Java (Focus/FocusIn, so
// Display.getFocusControl() is the island), make the mirror claim keys, and take the keyboard from
// the Display-level forwarder so a key is dispatched to exactly one engine; focus leaving reverses
// all three.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/custom/swing_island_evolve.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const int islandId = 7;
const int windowId = 3;
const int enginePort = 8765;

/// The island with sendEvent captured instead of handed to EquoCommService (a widget test has no
/// live transport). Its State and the region under it are untouched.
class _CapturingIsland extends SwingIslandSwt {
  const _CapturingIsland({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) => onEvent(ev);
}

VComposite _island() => VComposite()
  ..swt = 'SwingIsland'
  ..id = islandId
  ..style = SWT.EMBEDDED
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300);

/// What Java sends on the island's channel once the frame exists.
void _answerIsland() {
  const channel = 'SwingIsland/$islandId/swingIsland';
  final actionBytes = utf8.encode(channel);
  final bodyBytes = utf8.encode(json.encode({'windowId': windowId, 'port': enginePort}));
  final frame = Uint8List(2 + actionBytes.length + bodyBytes.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, bodyBytes);
  EquoCommService.commForTesting.receiveBinary(frame);
}

Future<void> _pumpIsland(WidgetTester tester, List<String> events) async {
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: _CapturingIsland(value: _island(), onEvent: events.add),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// Stands in for the host's SwingMirror: what the region gave the builder.
class _Mirror extends StatelessWidget {
  const _Mirror(this.window);

  final SwingIslandWindow window;

  @override
  Widget build(BuildContext context) => const SizedBox.expand();
}

void main() {
  setUp(() {
    swingMirrorBuilder = (context, window) => _Mirror(window);
  });

  tearDown(() {
    swingMirrorBuilder = null;
  });

  testWidgets('asks Java for its frame on mount and mirrors nothing until answered', (
    tester,
  ) async {
    final events = <String>[];
    await _pumpIsland(tester, events);

    expect(
      events,
      contains('swingIsland'),
      reason:
          'Java pushes the frame\'s identity when it creates the frame, which may be before '
          'this region exists; asking is what makes the answer arrive either way',
    );
    expect(find.byType(_Mirror), findsNothing);
  });

  testWidgets('builds the mirror for the window and port Java answers with', (tester) async {
    await _pumpIsland(tester, <String>[]);

    _answerIsland();
    await tester.pumpAndSettle();

    final mirror = tester.widget<_Mirror>(find.byType(_Mirror));
    expect(mirror.window.windowId, windowId);
    expect(mirror.window.port, enginePort);
  });

  testWidgets('without a mirror builder the island is empty and takes no focus', (tester) async {
    swingMirrorBuilder = null;
    await _pumpIsland(tester, <String>[]);
    _answerIsland();
    await tester.pumpAndSettle();

    expect(
      find
          .byType(Focus)
          .evaluate()
          .where((e) => (e.widget as Focus).focusNode?.debugLabel == 'SwingIsland'),
      isEmpty,
    );
  });

  testWidgets(
    'a press into the island takes focus: Java is told, the mirror claims keys, the Display '
    'forwarder stays out; focus leaving reverses all three',
    (tester) async {
      final events = <String>[];
      await _pumpIsland(tester, events);
      _answerIsland();
      await tester.pumpAndSettle();

      await tester.tapAt(tester.getCenter(find.byType(_Mirror)));
      await tester.pumpAndSettle();

      final focused = tester.widget<_Mirror>(find.byType(_Mirror)).window.focused;
      expect(events, contains('Focus/FocusIn'));
      expect(
        focused.value,
        isTrue,
        reason: 'the mirror claims keys and Java\'s window gains focus',
      );
      expect(
        keyboardClaimed,
        isTrue,
        reason: 'the Display-level forwarder must not dispatch the same key a second time',
      );

      FocusManager.instance.primaryFocus?.unfocus();
      await tester.pumpAndSettle();

      expect(events, contains('Focus/FocusOut'));
      expect(focused.value, isFalse);
      expect(keyboardClaimed, isFalse);
    },
  );
}
