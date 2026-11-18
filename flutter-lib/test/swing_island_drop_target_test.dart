// An SWT drag over a Swing island. Java gives the island an SWT DropTarget while its AWT frame has
// AWT drop targets, and replays the SWT drag events to them; the island's region must therefore
// take part in the drag like any control with a drop target, or the drag shows no-drop over it and
// the drop never reaches Java.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/swing_island_evolve.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/channels.dart' as channels;
import 'support/drag_gesture.dart';
import 'support/java_push.dart';

const int hostId = 1;
const int islandId = 7;
const int sourceId = 8;
const int dropTargetId = 9;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// A drag source below the island, which fills the host's top 200 pixels.
VComposite _host({required int? islandDropTarget}) => VComposite()
  ..id = hostId
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    VComposite()
      ..swt = 'SwingIsland'
      ..id = islandId
      ..style = SWT.EMBEDDED
      ..enabled = true
      ..visible = true
      ..dropTargetId = islandDropTarget
      ..bounds = _rect(0, 0, 400, 200),
    VLabel()
      ..id = sourceId
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..text = 'source'
      ..dragSource = true
      ..bounds = _rect(20, 230, 100, 30),
  ];

/// Stands in for swing-evolve's mirror, which takes every pointer over the island.
class _Mirror extends StatelessWidget {
  const _Mirror();

  @override
  Widget build(BuildContext context) =>
      const MetaData(behavior: HitTestBehavior.opaque, child: SizedBox.expand());
}

List<String> _sent() =>
    EquoCommService.commForTesting.sentFrames.map(EquoCommBase.decodeFrame).map((f) => f.$1).toList();

Iterable<String> _toDropTarget() => _sent().where((c) => c.startsWith('DropTarget/$dropTargetId/'));

Future<void> _pumpIsland(WidgetTester tester, {required int? islandDropTarget}) async {
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: CompositeSwt<VComposite>(value: _host(islandDropTarget: islandDropTarget)),
      ),
    ),
  );
  await tester.pump();
  EquoCommService.commForTesting.receiveBinary(
    channels.frame('SwingIsland/$islandId/swingIsland', TaggedValues.encode({'windowId': 3, 'port': 8765})),
  );
  await tester.pumpAndSettle();
  expect(find.byType(_Mirror), findsOneWidget);
  EquoCommService.commForTesting.clearSentFrames();
}

Future<void> _dragOntoIsland(WidgetTester tester) async {
  await dragInSteps(tester, tester.getCenter(find.text('source')), const Offset(200, 100));
  await tester.pumpAndSettle();
  // Nothing answers the DragStart verdict here; let the fail-open timer run out.
  await tester.pump(const Duration(milliseconds: 400));
}

void main() {
  setUp(() {
    swingMirrorBuilder = (context, window) => const _Mirror();
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    swingMirrorBuilder = null;
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
    VRegistry.instance.clear();
  });

  testWidgets('an SWT drag over an island with a drop target enters it and drops on it', (tester) async {
    await _pumpIsland(tester, islandDropTarget: dropTargetId);

    await _dragOntoIsland(tester);

    final events = _toDropTarget().toList();
    expect(events, contains('DropTarget/$dropTargetId/Drop/dragEnter'),
        reason: 'the island\'s drop target hears the drag come in');
    expect(events, contains('DropTarget/$dropTargetId/Drop/dragOver'));
    expect(events.last, 'DropTarget/$dropTargetId/Drop/drop', reason: 'the drop reaches Java');
  });

  testWidgets('a drop target attached to a live island keeps its mirror and takes the drag',
      (tester) async {
    await _pumpIsland(tester, islandDropTarget: null);

    await javaPushes(tester, find.byType(SwingIslandSwt), (state) => state.dropTargetId = dropTargetId);
    await tester.pumpAndSettle();

    expect(find.byType(_Mirror), findsOneWidget,
        reason: 'the region keeps the window Java named, so the mirror stays connected');
    expect(_sent(), isNot(contains('SwingIsland/$islandId/swingIsland')),
        reason: 'a region built again asks Java for its window again');
    await _dragOntoIsland(tester);
    expect(_toDropTarget().last, 'DropTarget/$dropTargetId/Drop/drop');
  });

  testWidgets('an island with no drop target hears nothing of the drag', (tester) async {
    await _pumpIsland(tester, islandDropTarget: null);

    await _dragOntoIsland(tester);

    expect(_sent().where((c) => c.startsWith('DropTarget/')), isEmpty);
  });
}
