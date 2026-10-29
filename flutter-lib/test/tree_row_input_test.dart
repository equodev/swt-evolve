// How a Tree's rows answer the pointer and the keys, as Win32 and GTK do: a press selects its row
// between the tree's MouseDown and MouseUp, with the button it holds; Enter is the selected row's
// DefaultSelection; a typed letter selects the next row it starts; the numpad's + and - expand and
// collapse the selected row and * expands its whole subtree; Space on a CHECK tree reports the row,
// then toggles its box.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';

VTreeItem _item(int id, String text, [List<VTreeItem>? children]) => VTreeItem()
  ..id = id
  ..text = text
  ..texts = [text, '']
  ..items = children;

// Two columns: a single-column tree draws adjacent selected rows with a hairline border the test
// renderer refuses.
VTree _tree({int style = SWT.SINGLE}) => VTree()
  ..id = 1
  ..style = style
  ..headerVisible = true
  ..columns = [
    VTreeColumn()
      ..id = 2
      ..width = 200
      ..text = 'Name',
    VTreeColumn()
      ..id = 3
      ..width = 100
      ..text = 'Value',
  ]
  ..enabled = true
  ..items = [
    _item(10, 'alpha'),
    _item(11, 'bravo', [
      _item(20, 'bravo one', [_item(30, 'bravo one deep')]),
      _item(21, 'bravo two'),
    ]),
    _item(12, 'charlie'),
    _item(13, 'delta'),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 600
    ..height = 400);

class _Sent {
  final String action;
  final VEvent? event;
  _Sent(this.action, this.event);

  String get kind => action.substring(action.lastIndexOf('/') + 1);
}

Future<List<_Sent>> _pumpTree(WidgetTester tester, {int style = SWT.SINGLE}) async {
  final sent = <_Sent>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.startsWith('Tree/1/')) sent.add(_Sent(action, payload is VEvent ? payload : null));
  };
  addTearDown(() => EquoCommBase.sendObserverForTesting = null);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 600, height: 400, child: TreeSwt<VTree>(value: _tree(style: style))),
  ));
  await tester.pump();
  return sent;
}

Future<void> _click(WidgetTester tester, String text) async {
  await tester.tap(find.text(text));
  await tester.pump(const Duration(seconds: 1));
}

void main() {
  testWidgets('a press selects its row between the MouseDown and the MouseUp, with the button it holds', (tester) async {
    final sent = await _pumpTree(tester);
    final gesture = await tester.startGesture(tester.getCenter(find.text('charlie')), kind: PointerDeviceKind.mouse);
    await tester.pump();
    await gesture.up();
    await tester.pump(const Duration(seconds: 1));

    final kinds = sent.map((s) => s.kind).where((k) => ['MouseDown', 'Selection', 'MouseUp'].contains(k)).toList();
    expect(kinds, ['MouseDown', 'Selection', 'MouseUp']);
    final selection = sent.firstWhere((s) => s.kind == 'Selection').event!;
    expect(selection.stateMask, SWT.BUTTON1);
    expect(selection.button ?? 0, 0);
    expect(selection.count ?? 0, 0);
  });

  testWidgets('the pointer over a row reports no MouseEnter of its own', (tester) async {
    final sent = await _pumpTree(tester);
    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: Offset.zero);
    await gesture.moveTo(tester.getCenter(find.text('alpha')));
    await tester.pump();
    await gesture.moveTo(tester.getCenter(find.text('charlie')));
    await tester.pump(const Duration(seconds: 1));

    expect(sent.where((s) => s.kind == 'MouseEnter' && s.event == null), isEmpty);
  });

  testWidgets('Enter is the selected row\'s DefaultSelection', (tester) async {
    final sent = await _pumpTree(tester);
    await _click(tester, 'bravo');
    sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.enter);
    await tester.pump();

    expect(sent.map((s) => s.kind), contains('DefaultSelection'));
    expect(sent.firstWhere((s) => s.kind == 'DefaultSelection').event!.itemId, 11);
  });

  testWidgets('a typed letter selects the next row it starts', (tester) async {
    final sent = await _pumpTree(tester);
    await _click(tester, 'alpha');
    sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.keyD, character: 'd');
    await tester.pump();

    final selections = sent.where((s) => s.kind == 'Selection').toList();
    expect(selections, hasLength(1));
    expect(selections.single.event!.itemId, 13);
  });

  testWidgets('the numpad expands and collapses the selected row, and * its whole subtree', (tester) async {
    final sent = await _pumpTree(tester);
    await _click(tester, 'bravo');
    sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.numpadAdd);
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.numpadSubtract);
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.numpadMultiply);
    await tester.pump();

    final tree = sent.where((s) => s.kind == 'Expand' || s.kind == 'Collapse').map((s) => '${s.kind} ${s.event!.itemId}');
    expect(tree, ['Expand 11', 'Collapse 11', 'Expand 11', 'Expand 20']);
  });

  testWidgets('Space on a CHECK tree reports the row, then toggles its box', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.CHECK);
    await _click(tester, 'charlie');
    sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.space);
    await tester.pump();

    final selections = sent.where((s) => s.kind == 'Selection').map((s) => '${s.event!.itemId}/${s.event!.detail ?? 0}');
    expect(selections, ['12/0', '12/${SWT.CHECK}']);
  });

  testWidgets('Shift+Down extends a MULTI selection, naming every selected row and the key held', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.MULTI);
    await _click(tester, 'alpha');
    sent.clear();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pump();

    final selection = sent.lastWhere((s) => s.kind == 'Selection').event!;
    expect(selection.stateMask, SWT.SHIFT);
    expect(selection.segments, [10, 11]);
  });

  testWidgets('a click on another row in between keeps a third click from being a double click', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.MULTI);
    await tester.tap(find.text('alpha'));
    await tester.pump();
    await tester.tap(find.text('charlie'));
    await tester.pump();
    await tester.tap(find.text('alpha'));
    await tester.pump(const Duration(seconds: 1));

    expect(sent.where((s) => s.kind == 'DefaultSelection'), isEmpty);
  });

  testWidgets('a right click selects its row with the button it holds', (tester) async {
    final sent = await _pumpTree(tester);
    await tester.tap(find.text('charlie'), buttons: kSecondaryButton);
    await tester.pump(const Duration(seconds: 1));

    final selection = sent.firstWhere((s) => s.kind == 'Selection').event!;
    expect(selection.itemId, 12);
    expect(selection.stateMask, SWT.BUTTON3);
  });

  testWidgets('a click on a CHECK row\'s box toggles it without selecting the row on the press', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.CHECK);
    await tester.tap(find.byKey(const ValueKey('checkbox_12')));
    await tester.pump(const Duration(seconds: 1));

    final selections = sent.where((s) => s.kind == 'Selection').map((s) => '${s.event!.itemId}/${s.event!.detail ?? 0}');
    expect(selections, ['12/${SWT.CHECK}']);
  });

  testWidgets('a Cmd+click reports COMMAND, a Ctrl+click CTRL, with the button held', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.MULTI);
    await _click(tester, 'alpha');
    for (final key in [LogicalKeyboardKey.metaLeft, LogicalKeyboardKey.controlLeft]) {
      sent.clear();
      await tester.sendKeyDownEvent(key);
      await tester.tap(find.text(key == LogicalKeyboardKey.metaLeft ? 'charlie' : 'delta'));
      await tester.sendKeyUpEvent(key);
      await tester.pump(const Duration(seconds: 1));
      final mask = key == LogicalKeyboardKey.metaLeft ? SWT.COMMAND : SWT.CTRL;
      expect(sent.firstWhere((s) => s.kind == 'Selection').event!.stateMask, SWT.BUTTON1 | mask);
    }
  });

  testWidgets('a press on a row of a multiple selection keeps it until the release', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.MULTI);
    await _click(tester, 'alpha');
    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await _click(tester, 'charlie');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    sent.clear();

    final gesture = await tester.startGesture(tester.getCenter(find.text('bravo')), kind: PointerDeviceKind.mouse);
    await tester.pump();
    expect(sent.where((s) => s.kind == 'Selection'), isEmpty);
    await gesture.up();
    await tester.pump(const Duration(seconds: 1));

    final selection = sent.singleWhere((s) => s.kind == 'Selection').event!;
    expect(selection.segments, [11]);
  });

  testWidgets('a NO_SEARCH tree selects nothing for a typed letter', (tester) async {
    final sent = await _pumpTree(tester, style: SWT.SINGLE | SWT.NO_SEARCH);
    await _click(tester, 'alpha');
    sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.keyD, character: 'd');
    await tester.pump();

    expect(sent.where((s) => s.kind == 'Selection'), isEmpty);
  });

  testWidgets('a row counts its clicks by the presses\' own time, so a long frame does not split a double click',
      (tester) async {
    final sent = await _pumpTree(tester);
    addTearDown(() => DoubleTapDetector.clock = DateTime.now);
    var now = DateTime(2020);
    DoubleTapDetector.clock = () => now;
    final at = tester.getCenter(find.text('charlie'));

    final first = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await first.down(at, timeStamp: const Duration(milliseconds: 10));
    await first.up(timeStamp: const Duration(milliseconds: 20));
    await tester.pump();
    // The wall clock runs past the double-click window while the second press is still in flight.
    now = now.add(const Duration(seconds: 5));
    final second = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await second.down(at, timeStamp: const Duration(milliseconds: 120));
    await second.up(timeStamp: const Duration(milliseconds: 130));
    await tester.pump(const Duration(seconds: 1));

    expect(sent.where((s) => s.kind == 'DefaultSelection'), hasLength(1));
  });
}
