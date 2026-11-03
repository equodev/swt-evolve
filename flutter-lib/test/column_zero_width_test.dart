// A column whose width is 0 is hidden, as in native SWT: no header cell, no cells, no hit target.
// `column.setWidth(0)` is the standard way an application hides a column it still keeps in the viewer.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

const _hiddenHeader = 'Masked';
const _hiddenCell = 'SECRET';

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 600
  ..height = 300;

Widget _wrap(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 300, child: child),
    );

VTable _table({required bool lastHidden}) {
  VTableColumn col(int id, String text, int width) => VTableColumn()
    ..id = id
    ..text = text
    ..width = width
    ..alignment = SWT.LEFT
    ..resizable = width != 0;
  return VTable()
    ..id = 1
    ..style = SWT.NONE
    ..headerVisible = true
    ..columns = lastHidden
        ? [col(2, 'Name', 120), col(3, 'Value', 120), col(4, _hiddenHeader, 0)]
        : [col(2, 'Name', 120), col(4, _hiddenHeader, 0), col(3, 'Value', 120)]
    ..items = [
      VTableItem()
        ..id = 10
        ..texts = lastHidden ? ['host', 'example', _hiddenCell] : ['host', _hiddenCell, 'example'],
    ]
    ..bounds = _bounds();
}

VTree _tree({required bool lastHidden}) {
  VTreeColumn col(int id, String text, int width) => VTreeColumn()
    ..id = id
    ..text = text
    ..width = width
    ..alignment = SWT.LEFT;
  return VTree()
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..headerVisible = true
    ..columns = lastHidden
        ? [col(2, 'Name', 120), col(3, 'Value', 120), col(4, _hiddenHeader, 0)]
        : [col(2, 'Name', 120), col(4, _hiddenHeader, 0), col(3, 'Value', 120)]
    ..items = [
      VTreeItem()
        ..id = 10
        ..text = 'host'
        ..texts = lastHidden ? ['host', 'example', _hiddenCell] : ['host', _hiddenCell, 'example'],
    ]
    ..bounds = _bounds();
}

// Case-insensitive: a multi-column Tree upper-cases its headers.
Finder _text(String text) =>
    find.textContaining(RegExp('^$text\$', caseSensitive: false));

/// The part of [element]'s box left on screen by the clips above it.
Rect _visibleRect(Element element) {
  final box = element.renderObject! as RenderBox;
  var visible = box.localToGlobal(Offset.zero) & box.size;
  RenderObject? node = box.parent;
  while (node != null) {
    if (node is RenderClipRect && node.clipBehavior != Clip.none) {
      visible = visible.intersect(node.localToGlobal(Offset.zero) & node.size);
    }
    node = node.parent;
  }
  return visible;
}

void _expectHidden(WidgetTester tester) {
  expect(_text('Value'), findsWidgets, reason: 'the visible columns still render');
  for (final text in [_hiddenHeader, _hiddenCell]) {
    for (final element in _text(text).evaluate()) {
      final visible = _visibleRect(element);
      expect(visible.width <= 0 || visible.height <= 0, isTrue,
          reason: '"$text" sits in a zero-width column and must not show, got $visible');
    }
    expect(_text(text).hitTestable(), findsNothing,
        reason: '"$text" sits in a zero-width column and must not be a hit target');
  }
}

void main() {
  for (final lastHidden in [false, true]) {
    final where = lastHidden ? 'last' : 'middle';

    testWidgets('Table: a $where column of width 0 takes no space', (tester) async {
      await tester.pumpWidget(_wrap(TableSwt<VTable>(value: _table(lastHidden: lastHidden))));
      await tester.pumpAndSettle();
      _expectHidden(tester);
    });

    testWidgets('Tree: a $where column of width 0 takes no space', (tester) async {
      await tester.pumpWidget(_wrap(TreeSwt<VTree>(value: _tree(lastHidden: lastHidden))));
      await tester.pumpAndSettle();
      _expectHidden(tester);
    });
  }
}
