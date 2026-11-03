// A drag can leave the window it started in for a shell another window draws, as when a row is
// dragged out of a modeless dialog onto its parent's tree. Neither Flutter view sees the other, so
// the drag crosses through Java: the source window reports where the pointer goes in display
// coordinates, and the window under it is told to treat that as a drag over its own drop targets.

import 'dart:typed_data';

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
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/dnd_utils.dart';

import 'support/sent_events.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

// Ids differ per test: a value mounted under an id keeps being served for it.
var _nextId = 100;

VShell _shell(int id, List<VControl> children) => VShell()
  ..id = id
  ..style = SWT.SHELL_TRIM
  ..text = 'window'
  ..visible = true
  ..bounds = _rect(0, 0, 800, 600)
  ..children = children;

VCanvas _canvas(int id, {int? dropTargetId, bool dragSource = false}) => VCanvas()
  ..id = id
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..dragSource = dragSource
  ..dropTargetId = dropTargetId
  ..bounds = _rect(20, 20, 200, 150);

VTree _tree(int id, int dropTargetId) => VTree()
  ..id = id
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..dropTargetId = dropTargetId
  ..items = [
    VTreeItem()
      ..id = id + 1
      ..text = 'Inventory'
  ]
  ..bounds = _rect(300, 20, 200, 200);

Future<void> _pump(WidgetTester tester, VShell shell) async {
  tester.view.physicalSize = const Size(1200, 800);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.reset);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..shells = [shell]
        ..mainShellId = shell.id,
    ),
  ));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
}

/// Java's message on [actionId], a tagged map as RemoteDrag sends it.
void _deliver(String actionId, Map<String, dynamic> body) => deliverTagged(actionId, body);

List<(String, Map<String, dynamic>)> _sent(String prefix) => sentEvents()
    .where((f) => f.$1.startsWith(prefix))
    .map((f) => (f.$1.substring(prefix.length), f.$2))
    .toList();

List<String> _drops(int dropTargetId) =>
    _sent('DropTarget/$dropTargetId/Drop/').map((f) => f.$1).toList();

/// [global], a position in this test's view, as Java reports it: relative to the shell's bounds.
Offset _shellRelative(WidgetTester tester, Offset global) =>
    tester.renderObject<RenderBox>(find.byType(ShellSwt<VShell>)).globalToLocal(global);

/// Java telling the shell [shellId] that a drag from another window is [phase] at [global].
Future<void> _remote(WidgetTester tester, int shellId, String phase, [Offset? global]) async {
  final body = <String, dynamic>{'text': phase};
  if (global != null) {
    final at = _shellRelative(tester, global);
    body['x'] = at.dx.round();
    body['y'] = at.dy.round();
  }
  _deliver('Shell/$shellId/DnD/remoteDrag', body);
  // One frame for the message to be handled, one for what it changed to be drawn.
  await tester.pump();
  await tester.pump();
}

/// A 1x1 PNG.
const _png = [
  137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31,
  21, 196, 137, 0, 0, 0, 13, 73, 68, 65, 84, 120, 156, 99, 248, 207, 192, 240, 31, 0, 5, 0, 1, 255, 137,
  153, 61, 29, 0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130,
];

final _dragImage = find.byWidgetPredicate((w) => w is Image && w.image is MemoryImage);


void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() => EquoCommBase.recordSentFrames = false);

  group('a drag from another window', () {
    testWidgets('enters, is dragged over and drops on the control under it', (tester) async {
      final shellId = _nextId++;
      const dropTargetId = 901;
      await _pump(tester, _shell(shellId, [_canvas(_nextId++, dropTargetId: dropTargetId)]));
      final inside = tester.getCenter(find.byType(CanvasSwt<VCanvas>));

      await _remote(tester, shellId, 'over', inside);
      expect(_drops(dropTargetId), ['dragEnter', 'dragOver']);

      await _remote(tester, shellId, 'over', inside + const Offset(5, 5));
      await _remote(tester, shellId, 'drop', inside + const Offset(5, 5));
      expect(_drops(dropTargetId), ['dragEnter', 'dragOver', 'dragOver', 'drop']);
      expect(_sent('Shell/$shellId/DnD/remoteDropNone'), isEmpty);
    });

    testWidgets('leaves the control when the pointer goes elsewhere', (tester) async {
      final shellId = _nextId++;
      const dropTargetId = 902;
      await _pump(tester, _shell(shellId, [_canvas(_nextId++, dropTargetId: dropTargetId)]));
      final inside = tester.getCenter(find.byType(CanvasSwt<VCanvas>));

      await _remote(tester, shellId, 'over', inside);
      await _remote(tester, shellId, 'over', const Offset(600, 400));
      expect(_drops(dropTargetId), ['dragEnter', 'dragOver', 'dragLeave']);

      await _remote(tester, shellId, 'over', inside);
      await _remote(tester, shellId, 'leave');
      expect(_drops(dropTargetId), ['dragEnter', 'dragOver', 'dragLeave', 'dragEnter', 'dragOver', 'dragLeave']);
    });

    testWidgets('dropped where no drop target takes it is answered as not taken', (tester) async {
      final shellId = _nextId++;
      const dropTargetId = 903;
      await _pump(tester, _shell(shellId, [_canvas(_nextId++, dropTargetId: dropTargetId)]));

      await _remote(tester, shellId, 'drop', const Offset(600, 400));
      expect(_drops(dropTargetId), isEmpty);
      expect(_sent('Shell/$shellId/DnD/remoteDropNone'), hasLength(1));
    });

    testWidgets('draws the drag image the source sent at the pointer, until the drag leaves', (tester) async {
      final shellId = _nextId++;
      await _pump(tester, _shell(shellId, [_canvas(_nextId++, dropTargetId: 905)]));
      const at = Offset(600, 400);

      final body = <String, dynamic>{'text': 'over', 'segments': Uint8List.fromList(_png), 'width': 40, 'height': 20};
      final local = _shellRelative(tester, at);
      body['x'] = local.dx.round();
      body['y'] = local.dy.round();
      _deliver('Shell/$shellId/DnD/remoteDrag', body);
      await tester.pump();
      await tester.pump();
      expect(_dragImage, findsOneWidget, reason: 'the source window cannot draw over this one');
      expect(tester.getTopLeft(_dragImage), at);
      expect(tester.getSize(_dragImage), const Size(40, 20));

      await _remote(tester, shellId, 'over', at + const Offset(30, 10));
      expect(tester.getTopLeft(_dragImage), at + const Offset(30, 10), reason: 'it follows the pointer');

      await _remote(tester, shellId, 'leave');
      expect(_dragImage, findsNothing);
    });

    testWidgets('drops on the tree item under it', (tester) async {
      final shellId = _nextId++;
      const dropTargetId = 904;
      final treeId = _nextId;
      _nextId += 2;
      await _pump(tester, _shell(shellId, [_tree(treeId, dropTargetId)]));
      final item = tester.getCenter(find.text('Inventory'));

      await _remote(tester, shellId, 'over', item);
      await _remote(tester, shellId, 'drop', item);
      final frames = _sent('DropTarget/$dropTargetId/Drop/');
      expect(frames.map((f) => f.$1), ['dragEnter', 'dragOver', 'drop']);
      expect(frames.last.$2['itemId'], treeId + 1);
    });

    testWidgets('moved one row at a time, names each row it is over', (tester) async {
      final shellId = _nextId++;
      const dropTargetId = 906;
      final treeId = _nextId;
      _nextId += 10;
      final rows = ['Wells', 'Horizons', 'Faults', 'Grids', 'Volumes'];
      final tree = _tree(treeId, dropTargetId)
        ..items = [
          for (var i = 0; i < rows.length; i++)
            VTreeItem()
              ..id = treeId + 1 + i
              ..text = rows[i]
        ];
      await _pump(tester, _shell(shellId, [tree]));

      for (var i = 0; i < rows.length; i++) {
        await _remote(tester, shellId, 'over', tester.getCenter(find.text(rows[i])));
        final overs = _sent('DropTarget/$dropTargetId/Drop/').where((f) => f.$1 == 'dragOver');
        expect(overs.last.$2['itemId'], treeId + 1 + i, reason: rows[i]);
      }
      await _remote(tester, shellId, 'leave');
    });
  });

  testWidgets('a drag from a DragSource reports where it goes in display coordinates, and its release',
      (tester) async {
    final shellId = _nextId++;
    final canvasId = _nextId++;
    await _pump(tester, _shell(shellId, [_canvas(canvasId, dragSource: true)]));

    final start = tester.getCenter(find.byType(CanvasSwt<VCanvas>));
    const end = Offset(1100, 700);
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    // Moves 5 ms apart: more than one a frame.
    const steps = 120;
    for (var i = 1; i <= steps; i++) {
      await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
      await tester.pump(const Duration(milliseconds: 5));
    }
    // The frame the last move is in ends.
    await tester.pump(const Duration(milliseconds: 16));
    final moves = _sent('Canvas/$canvasId/DragDetect/dragMove');
    const frames = steps * 5 ~/ 16;
    expect(moves.length, inInclusiveRange(2, frames + 1), reason: 'at most one move a frame');
    final expected = _shellRelative(tester, end);
    expect(moves.last.$2['x'], expected.dx.round());
    expect(moves.last.$2['y'], expected.dy.round());

    // Small moves a frame or more apart: each one is sent.
    EquoCommService.commForTesting.clearSentFrames();
    for (var i = 1; i <= 10; i++) {
      await gesture.moveTo(end + Offset(0, -i * 2.0));
      await tester.pump(const Duration(milliseconds: 20));
    }
    expect(_sent('Canvas/$canvasId/DragDetect/dragMove').map((m) => m.$2['y']),
        [for (var i = 1; i <= 10; i++) _shellRelative(tester, end + Offset(0, -i * 2.0)).dy.round()]);
    await gesture.moveTo(end);
    await tester.pump(const Duration(milliseconds: 20));

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    final releases = _sent('Canvas/$canvasId/DragDetect/dragRelease');
    expect(releases, hasLength(1));
    expect(releases.single.$2['x'], expected.dx.round());
    expect(releases.single.$2['y'], expected.dy.round());

    EquoCommService.commForTesting.clearSentFrames();
    await tester.pump(const Duration(milliseconds: 300));
    expect(_sent('Canvas/$canvasId/DragDetect/'), isEmpty, reason: 'nothing is reported once the drag ended');
  });

  testWidgets('a drag released where this window takes no drop leaves its DragEnd to Java', (tester) async {
    final shellId = _nextId++;
    final canvasId = _nextId++;
    final dragSourceId = _nextId++;
    await _pump(tester, _shell(shellId, [_canvas(canvasId, dragSource: true)..dragSourceId = dragSourceId]));

    final start = tester.getCenter(find.byType(CanvasSwt<VCanvas>));
    final gesture = await tester.startGesture(start, kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    await gesture.moveTo(start + const Offset(20, 0));
    await tester.pump(const Duration(milliseconds: 20));
    _deliver('Canvas/$canvasId/DragDetect/dragStartResult', {'doit': true});
    await tester.pump();
    await gesture.moveTo(const Offset(1100, 700));
    await tester.pump(const Duration(milliseconds: 20));
    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));

    expect(_sent('Canvas/$canvasId/DragDetect/dragRelease'), hasLength(1));
    expect(_sent('DragSource/$dragSourceId/Drag/dragFinished'), isEmpty,
        reason: 'the window under the release may still take the drop; Java ends the drag');
  });

  testWidgets('outside its window, the drag shows the cursor for what the other window answered',
      (tester) async {
    final shellId = _nextId++;
    final canvasId = _nextId++;
    await _pump(tester, _shell(shellId, [_canvas(canvasId, dragSource: true)]));
    String kind(int op) => (DndDropAnswer.cursorFor(op) as SystemMouseCursor).kind;
    final cursors = <String>[];
    tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(SystemChannels.mouseCursor, (call) async {
      if (call.method == 'activateSystemCursor') cursors.add((call.arguments as Map)['kind'] as String);
      return null;
    });
    addTearDown(() => tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(SystemChannels.mouseCursor, null));

    final start = tester.getCenter(find.byType(CanvasSwt<VCanvas>));
    final gesture = await tester.startGesture(start, kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    for (final to in [start + const Offset(20, 0), const Offset(1100, 400), const Offset(1250, 400)]) {
      await gesture.moveTo(to);
      await tester.pump(const Duration(milliseconds: 20));
      if (to == start + const Offset(20, 0)) {
        _deliver('Canvas/$canvasId/DragDetect/dragStartResult', {'doit': true});
      }
    }
    expect(cursors.last, kind(Dnd.dropNone), reason: 'over another window with no answer yet, nothing takes the drop');

    _deliver('Canvas/$canvasId/DragDetect/remoteAnswer', {'detail': Dnd.dropCopy});
    await tester.pump();
    expect(cursors.last, kind(Dnd.dropCopy));

    await gesture.moveTo(const Offset(1260, 410));
    await tester.pump(const Duration(milliseconds: 20));
    expect(cursors.last, kind(Dnd.dropCopy), reason: 'kept while the pointer moves');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('a drag sends its image once, for the window it crosses into to draw', (tester) async {
    final shellId = _nextId++;
    final treeId = _nextId;
    _nextId += 2;
    await _pump(tester, _shell(shellId, [_tree(treeId, 906)..dragSource = true]));

    final item = tester.getCenter(find.text('Inventory'));
    final gesture = await tester.startGesture(item, kind: PointerDeviceKind.mouse);
    await tester.pump(const Duration(milliseconds: 50));
    await gesture.moveTo(item + const Offset(30, 30));
    await tester.pump();
    _deliver('Tree/$treeId/DragDetect/dragStartResult', {'doit': true});
    await tester.pump();
    await tester.pump();
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 200)));
    await tester.pump();

    final images = _sent('Tree/$treeId/DragDetect/dragImage');
    expect(images, hasLength(1));
    final png = (images.single.$2['segments'] as List).cast<int>();
    expect(png.take(4), [137, 80, 78, 71]);
    expect(images.single.$2['width'], greaterThan(0));
    expect(images.single.$2['height'], greaterThan(0));

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
  });
}
