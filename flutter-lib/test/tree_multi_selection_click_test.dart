// A multi-selection Tree builds its selection the same way on every OS: Control or Command adds
// the clicked row or takes it out and moves the anchor, Shift selects the rows from the anchor, and
// a plain click selects one. On macOS, Control+click is a secondary click instead.
// Every Selection event names the clicked row and carries the ids of the rows the tree now shows
// selected, which is what Java selects, and the button the press holds.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

class _RecordingTree extends TreeSwt<VTree> {
  _RecordingTree({required super.value});

  final List<VEvent> selections = [];

  @override
  void sendSelectionSelection(VTree val, VEvent? payload) => selections.add(payload!);
}

VTreeItem _item(int id) => VTreeItem()
  ..id = id
  ..text = 'Node ${id - 10}';

VTree _tree({bool dragSource = false}) => VTree()
  ..id = 1
  ..style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..dragSource = dragSource
  ..items = [for (var id = 10; id < 16; id++) _item(id)]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Future<_RecordingTree> _mount(WidgetTester tester, VTree value) async {
  final tree = _RecordingTree(value: value);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 800, height: 400, child: tree),
  ));
  await tester.pump();
  return tree;
}

List<int> _selectedIds(VTree value) => (value.selection ?? []).map((item) => item.id).toList();

// A second click within the double-click window is a DefaultSelection, and DoubleTapDetector
// compares DateTime.now() rather than the fake test clock.
Future<void> _leaveDoubleClickWindow(WidgetTester tester) =>
    tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 400)));

Future<void> _click(WidgetTester tester, int id, [LogicalKeyboardKey? modifier]) async {
  await _leaveDoubleClickWindow(tester);
  if (modifier != null) await tester.sendKeyDownEvent(modifier);
  await tester.tap(find.text('Node ${id - 10}'));
  if (modifier != null) await tester.sendKeyUpEvent(modifier);
  await tester.pump();
}

void main() {
  final macOS = TargetPlatformVariant.only(TargetPlatform.macOS);
  final windowsAndLinux = TargetPlatformVariant({TargetPlatform.windows, TargetPlatform.linux});
  final everyPlatform =
      TargetPlatformVariant({TargetPlatform.macOS, TargetPlatform.windows, TargetPlatform.linux});

  LogicalKeyboardKey toggleKey() => defaultTargetPlatform == TargetPlatform.macOS
      ? LogicalKeyboardKey.metaLeft
      : LogicalKeyboardKey.controlLeft;

  int toggleMask() =>
      defaultTargetPlatform == TargetPlatform.macOS ? SWT.COMMAND : SWT.CTRL;

  testWidgets('the toggle modifier adds the clicked row, and Java is told the whole selection',
      (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 13, toggleKey());

    expect(_selectedIds(value), [10, 13]);
    final event = tree.selections.last;
    expect(event.itemId, 13, reason: 'the event is about the row clicked');
    expect(event.segments, [10, 13]);
    expect(event.stateMask, toggleMask() | SWT.BUTTON1,
        reason: 'the event reports the modifier the user held, by its SWT name');
  }, variant: everyPlatform);

  testWidgets('the toggle modifier takes a selected row out, and the event still names it',
      (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 13, toggleKey());
    await _click(tester, 10, toggleKey());

    expect(_selectedIds(value), [13]);
    expect(tree.selections.last.itemId, 10);
    expect(tree.selections.last.segments, [13]);
  }, variant: everyPlatform);

  testWidgets('Shift selects the rows from the anchor a toggle click moved', (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 12, toggleKey());
    await _click(tester, 14, LogicalKeyboardKey.shiftLeft);

    expect(_selectedIds(value), [12, 13, 14],
        reason: 'the toggle click on Node 2 made it the anchor, as natively');
    expect(tree.selections.last.itemId, 14);
    expect(tree.selections.last.segments, [12, 13, 14]);
    expect(tree.selections.last.stateMask, SWT.SHIFT | SWT.BUTTON1);
  }, variant: everyPlatform);

  testWidgets('a plain click after a multi-selection selects only the clicked row', (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 12, toggleKey());
    await _click(tester, 14);

    expect(_selectedIds(value), [14]);
    expect(tree.selections.last.segments, [14]);
    expect(tree.selections.last.stateMask, SWT.BUTTON1);
  }, variant: everyPlatform);

  testWidgets('Command toggles on Windows and GTK too, as Control does', (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 13, LogicalKeyboardKey.metaLeft);

    expect(_selectedIds(value), [10, 13]);
    expect(tree.selections.last.segments, [10, 13]);
    expect(tree.selections.last.stateMask, SWT.COMMAND | SWT.BUTTON1);
  }, variant: windowsAndLinux);

  testWidgets('on macOS Control does not toggle: Control+click is a secondary click', (tester) async {
    final value = _tree();
    await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 10, LogicalKeyboardKey.controlLeft);

    expect(_selectedIds(value), [10]);
  }, variant: macOS);

  testWidgets('Shift+arrow extends the selection, and Java is told all of it', (tester) async {
    final value = _tree();
    final tree = await _mount(tester, value);

    await _click(tester, 11);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump();

    expect(_selectedIds(value), [11, 12]);
    expect(tree.selections.last.itemId, 12);
    expect(tree.selections.last.segments, [11, 12]);
  });

  testWidgets('dragging a selected row keeps the rows selected with it', (tester) async {
    final value = _tree(dragSource: true);
    final tree = await _mount(tester, value);

    await _click(tester, 10);
    await _click(tester, 12, toggleKey());
    final sent = tree.selections.length;

    final gesture = await tester.startGesture(tester.getCenter(find.text('Node 2')));
    await gesture.moveBy(const Offset(0, 40));
    await tester.pump();
    await gesture.moveBy(const Offset(0, 40));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    expect(_selectedIds(value), [10, 12]);
    expect(tree.selections.length, sent, reason: 'the selection did not change');
  }, variant: macOS);
}
