// TreeItem.setImage(index, image) puts an icon in any column, not only the tree column, and Java
// lays the cell's text out after it (Sizes.getTextBounds shifts it by TreeSizes.getIconSpace), so
// an owner-drawing listener that paints at those bounds expects the icon to be there.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// 16x16, opaque #333333.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

const double _treeWidth = 500;
const double _treeHeight = 200;

VImage _image() => VImage()..imageData = (VImageData()..data = base64Decode(_png));

VTree _tree(VTreeItem item) => VTree()
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    for (var i = 0; i < 3; i++)
      VTreeColumn()
        ..id = 2 + i
        ..width = 150
        ..alignment = SWT.LEFT
        ..text = 'Column $i',
  ]
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = _treeHeight.toInt());

Future<void> _pump(WidgetTester tester, VTreeItem item) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _treeWidth,
        height: _treeHeight,
        child: TreeSwt<VTree>(value: _tree(item)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

Finder get _icons => find.byType(FutureBuilder<Widget?>);

void main() {
  testWidgets('an image set on a non-tree column is drawn in that cell', (tester) async {
    await _pump(
      tester,
      VTreeItem()
        ..id = 10
        ..text = 'name'
        ..texts = ['name', 'link', 'other']
        ..images = [_image(), _image(), null],
    );

    expect(_icons, findsNWidgets(2));
  });

  testWidgets('the cell text starts after its icon, where Java lays it out', (tester) async {
    await _pump(
      tester,
      VTreeItem()
        ..id = 10
        ..text = 'name'
        ..texts = ['name', 'link', 'other']
        ..images = [null, _image(), null],
    );

    final icon = tester.getRect(_icons.first);
    final text = tester.getTopLeft(find.text('link'));
    final other = tester.getTopLeft(find.text('other'));
    // Same start a cell without an image gives its text: the column's left plus the text inset.
    expect(icon.left, closeTo(other.dx - 150, 1.0));
    // TreeSizes.getIconSpace: the 16 px icon and its 8 px spacing.
    expect(text.dx - icon.left, closeTo(24, 1.0));
    expect(icon.center.dy, closeTo(tester.getCenter(find.text('link')).dy, 1.0));
  });

  testWidgets('a cell without an image keeps its text where it was', (tester) async {
    await _pump(
      tester,
      VTreeItem()
        ..id = 10
        ..text = 'name'
        ..texts = ['name', 'link', 'other'],
    );

    expect(_icons, findsNothing);
    expect(tester.getTopLeft(find.text('other')).dx - tester.getTopLeft(find.text('link')).dx,
        closeTo(150, 1.0));
  });
}
