// A plain click on a multi-selection Tree must replace the selection, even when the
// deprecated RawKeyboard still believes a modifier is held.
//
// On the web, a modifier released while the page has no focus (any browser shortcut that
// moves focus, Cmd+Tab) never produces a keyup for the page. Flutter's PointerBinding
// repairs that on the next pointer event — it re-reads the modifier flags carried by the
// mouse event and synthesizes the missing key-ups — but only for HardwareKeyboard. The
// legacy flutter/keyevent channel behind RawKeyboard sees no pointer events at all, so its
// snapshot keeps the modifier until the next keystroke. Reading it makes every plain click
// a toggle-click, and rows pile up one per click.
//
// The stale state is installed here through RawKeyboard alone, which is exactly the split
// the browser produces.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VTreeItem _item(int id, String text) => VTreeItem()
  ..id = id
  ..text = text;

// SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL — the style a JFace TreeViewer is given by
// default, and the one the tree in the report carries.
VTree _tree({required List<VTreeItem> items}) => VTree()
  ..id = 1
  ..style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Widget _wrap(VTree value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: TreeSwt<VTree>(value: value),
      ),
    );

List<int> _selectedIds(VTree value) =>
    (value.selection ?? []).map((item) => item.id).toList();

RawKeyEvent _metaEvent({required bool down}) {
  const data = RawKeyEventDataWeb(
    code: 'MetaLeft',
    key: 'Meta',
    metaState: RawKeyEventDataWeb.modifierMeta,
  );
  return down
      ? const RawKeyDownEvent(data: data)
      : const RawKeyUpEvent(data: data);
}

// A second click on the same row within the double-click window is a DefaultSelection, and
// DoubleTapDetector compares DateTime.now() rather than the fake test clock.
Future<void> _leaveDoubleClickWindow(WidgetTester tester) =>
    tester.runAsync(
        () => Future<void>.delayed(const Duration(milliseconds: 400)));

void main() {
  final macOS = TargetPlatformVariant.only(TargetPlatform.macOS);

  tearDown(() {
    RawKeyboard.instance.handleRawKeyEvent(_metaEvent(down: false));
  });

  testWidgets('a plain click replaces the selection while RawKeyboard holds a '
      'stale Cmd', (tester) async {
    final value = _tree(items: [
      _item(10, 'Node 0'),
      _item(11, 'Node 1'),
      _item(12, 'Node 2'),
      _item(13, 'Node 3'),
    ]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    RawKeyboard.instance.handleRawKeyEvent(_metaEvent(down: true));
    expect(HardwareKeyboard.instance.isMetaPressed, isFalse,
        reason: 'the pointer-driven repair has already cleared the real '
            'keyboard state — only the legacy snapshot is stale');

    await tester.tap(find.text('Node 0'));
    await tester.pump();
    expect(_selectedIds(value), equals([10]));

    await _leaveDoubleClickWindow(tester);
    await tester.tap(find.text('Node 2'));
    await tester.pump();

    expect(_selectedIds(value), equals([12]),
        reason: 'no modifier is really held, so the second click must not add '
            'a row to the selection');
  }, variant: macOS);

  testWidgets('plain clicks do not pile up highlights while RawKeyboard holds '
      'a stale Cmd', (tester) async {
    final value = _tree(items: [
      _item(10, 'Node 0'),
      _item(11, 'Node 1'),
      _item(12, 'Node 2'),
      _item(13, 'Node 3'),
    ]);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    RawKeyboard.instance.handleRawKeyEvent(_metaEvent(down: true));

    for (final row in ['Node 0', 'Node 2', 'Node 1', 'Node 3']) {
      await _leaveDoubleClickWindow(tester);
      await tester.tap(find.text(row));
      await tester.pump();
      expect(_selectedIds(value).length, equals(1),
          reason: 'exactly one row stays selected after a plain click on $row');
    }

    expect(_selectedIds(value), equals([13]));
  }, variant: macOS);
}
