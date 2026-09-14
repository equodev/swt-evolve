// A CTabItem must be draggable onto a *different* CTabFolder, not just within its own tab row.
//
// Tab dragging used to run on a private, row-local pointer tracker that hit-tested only its own
// Row's children, so no drop target outside that one folder could ever be resolved and no
// DND.DragEnter/DND.Drop ever reached Java. These tests pin both halves: the cross-folder drag
// must show the target folder's insertion marker, and the in-folder reorder must keep working on
// a folder that has no application DragSource/DropTarget installed at all.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

/// Same as the generated CTabFolderSwt, except sendEvent captures outgoing events instead of
/// handing them to EquoCommService (which needs a live transport a widget test doesn't have).
class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCTabFolder _folder(
  int id,
  List<String> labels, {
  bool dragSource = false,
  int? dropTargetId,
}) =>
    VCTabFolder()
      ..id = id
      ..style = SWT.NONE
      ..enabled = true
      ..selection = 0
      ..dragSource = dragSource
      ..dropTargetId = dropTargetId
      ..items = List.generate(
        labels.length,
        (i) => VCTabItem()
          ..id = id * 100 + i
          ..text = labels[i]
          ..showing = true,
      );

/// Stacks the folders side by side the way a Shell's NoLayout does — each one positioned at its
/// own bounds inside a shared Stack, rather than laid out by a Row. A Row bounds every child to
/// its own column; the Stack does not, which is what the real widget tree looks like.
Widget _host(List<Widget> folders) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 700,
        height: 240,
        child: Stack(
          children: [
            for (var i = 0; i < folders.length; i++)
              Positioned(
                left: i * 350.0,
                top: 0,
                width: 350,
                height: 240,
                child: folders[i],
              ),
          ],
        ),
      ),
    );

void _drain(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

/// Drags from [source] to [target] in small steps, pumping between them — a Draggable needs to
/// cross its touch slop and a DragTarget needs a frame to update its hover state. Returns the
/// still-active gesture so the caller can inspect mid-drag state before calling `up()`.
Future<TestGesture> _dragTo(WidgetTester tester, Finder source, Offset end) async {
  final start = tester.getCenter(source);
  final gesture = await tester.startGesture(start);
  await tester.pump(const Duration(milliseconds: 50));
  const steps = 8;
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  return gesture;
}

Finder _markerIn(Finder folder) => find.descendant(
      of: folder,
      matching: find.byType(CTabInsertionMarker),
    );

Finder _folderWithId(int id) => find.byWidgetPredicate(
    (w) => w is CTabFolderSwt<VCTabFolder> && w.value.id == id);

void main() {
  testWidgets('dragging a tab over another CTabFolder marks the drop position on that folder',
      (WidgetTester tester) async {
    final source = _folder(1, ['A one', 'A two'], dragSource: true, dropTargetId: 1);
    final target = _folder(2, ['B one', 'B two'], dragSource: true, dropTargetId: 2);

    await tester.pumpWidget(_host([
      CTabFolderSwt<VCTabFolder>(value: source),
      CTabFolderSwt<VCTabFolder>(value: target),
    ]));
    await tester.pump();
    _drain(tester);

    final gesture =
        await _dragTo(tester, find.text('A one'), tester.getCenter(find.text('B two')));
    await tester.pump();

    expect(_markerIn(_folderWithId(2)), findsOneWidget,
        reason: 'the folder under the pointer must show where the tab would land');
    expect(_markerIn(_folderWithId(1)), findsNothing,
        reason: 'the tab left its own folder, so that folder must not claim the drop');

    await gesture.up();
    await tester.pumpAndSettle();
    // Nothing answers the dragStart verdict here; let the fail-open timer run out.
    await tester.pump(const Duration(milliseconds: 400));
    _drain(tester);
  });

  testWidgets('the empty part of a folder tab strip is a drop target too',
      (WidgetTester tester) async {
    final source = _folder(1, ['A one', 'A two'], dragSource: true, dropTargetId: 1);
    final target = _folder(2, ['B one'], dragSource: true, dropTargetId: 2);

    await tester.pumpWidget(_host([
      CTabFolderSwt<VCTabFolder>(value: source),
      CTabFolderSwt<VCTabFolder>(value: target),
    ]));
    await tester.pump();
    _drain(tester);

    // Well past the target folder's only tab, but still inside its tab strip: a tab released
    // here must land in that folder, not fall back to the folder it came from.
    final emptyStrip = tester.getTopRight(find.text('B one')) + const Offset(120, 8);
    final gesture = await _dragTo(tester, find.text('A one'), emptyStrip);
    await tester.pump();

    expect(_markerIn(_folderWithId(2)), findsOneWidget,
        reason: 'the empty strip belongs to the folder that owns it');
    expect(_markerIn(_folderWithId(1)), findsNothing);

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    _drain(tester);
  });

  testWidgets('a folder that has run out of tabs can still be dropped on',
      (WidgetTester tester) async {
    final source = _folder(1, ['A one', 'A two'], dragSource: true, dropTargetId: 1);
    final empty = _folder(2, const [], dragSource: true, dropTargetId: 2);

    await tester.pumpWidget(_host([
      CTabFolderSwt<VCTabFolder>(value: source),
      CTabFolderSwt<VCTabFolder>(value: empty),
    ]));
    await tester.pump();
    _drain(tester);

    // The empty folder occupies the right half of the host; aim at its tab strip.
    final emptyStrip = tester.getTopLeft(_folderWithId(2)) + const Offset(80, 12);
    final gesture = await _dragTo(tester, find.text('A one'), emptyStrip);
    await tester.pump();

    expect(_markerIn(_folderWithId(2)), findsOneWidget,
        reason: 'a folder with no tabs left must still accept one back');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    _drain(tester);
  });

  /// A folder with no application DragSource/DropTarget — the only shape CTabFolder tab
  /// reordering has ever had — must keep reordering itself.
  Future<List<VEvent>> reorderEventsFor(
    WidgetTester tester,
    Offset Function(WidgetTester tester) dropAt,
  ) async {
    final events = <MapEntry<String, VEvent?>>[];
    await tester.pumpWidget(_host([
      _CapturingCTabFolderSwt(
        value: _folder(3, ['One', 'Two', 'Three']),
        onEvent: (ev, payload) => events.add(MapEntry(ev, payload)),
      ),
    ]));
    await tester.pump();
    _drain(tester);

    final gesture = await _dragTo(tester, find.text('One'), dropAt(tester));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();
    _drain(tester);

    return events
        .where((e) => e.key == 'CTabFolder/reorderItems')
        .map((e) => e.value!)
        .toList();
  }

  testWidgets('dropping a tab past the last one moves it to the end',
      (WidgetTester tester) async {
    final reorders = await reorderEventsFor(
        tester, (t) => t.getBottomRight(find.text('Three')) + const Offset(20, -8));

    expect(reorders, hasLength(1),
        reason: 'an in-folder drag must still ask Java to reorder the items');
    expect(reorders.single.index, 0);
    expect(reorders.single.detail, 2);
  });

  testWidgets('dropping a tab on the left half of another inserts it before that one',
      (WidgetTester tester) async {
    // The tab lands at the gap the pointer is over, which is what the insertion marker
    // points at — not on whichever tab happens to be under the cursor.
    final reorders =
        await reorderEventsFor(tester, (t) => t.getCenter(find.text('Three')));

    expect(reorders, hasLength(1));
    expect(reorders.single.index, 0);
    expect(reorders.single.detail, 1);
  });

  testWidgets('a tab dragged within its own folder marks where it would land',
      (WidgetTester tester) async {
    // Cross-folder is not the only case worth showing: reordering inside one row is the commoner
    // gesture, and it is the one with no other feedback at all once the tab is picked up.
    final folder = _folder(1, ['A one', 'A two', 'A three']);

    await tester.pumpWidget(_host([CTabFolderSwt<VCTabFolder>(value: folder)]));
    await tester.pump();
    _drain(tester);

    final gesture =
        await _dragTo(tester, find.text('A one'), tester.getCenter(find.text('A three')));
    await tester.pump();

    expect(_markerIn(_folderWithId(1)), findsOneWidget,
        reason: 'reordering within a folder must show the drop position too');

    await gesture.up();
    await tester.pumpAndSettle();
    _drain(tester);
  });

  testWidgets('the insertion marker follows the pointer across the gaps it can land in',
      (WidgetTester tester) async {
    // A marker pinned to one place would satisfy "a marker is shown" while telling the reader
    // nothing. What makes the gesture readable is that it moves as the drop position changes.
    final folder = _folder(1, ['A one', 'A two', 'A three']);

    await tester.pumpWidget(_host([CTabFolderSwt<VCTabFolder>(value: folder)]));
    await tester.pump();
    _drain(tester);

    final marker = _markerIn(_folderWithId(1));
    final gesture =
        await _dragTo(tester, find.text('A one'), tester.getCenter(find.text('A two')));
    await tester.pumpAndSettle();
    final atSecondTab = tester.getTopLeft(marker).dx;

    await gesture.moveTo(tester.getCenter(find.text('A three')));
    await tester.pumpAndSettle();
    final atThirdTab = tester.getTopLeft(marker).dx;

    expect(atThirdTab, greaterThan(atSecondTab),
        reason: 'dragging further right must move the mark further right');

    await gesture.up();
    await tester.pumpAndSettle();
    _drain(tester);
  });
}
