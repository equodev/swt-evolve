// A tree row whose SWT.EraseItem listener clears SWT.FOREGROUND gets its icon from the row's
// owner-draw overlay. Java says so with a null images[0]; the row must then draw no icon of its
// own, or the icon shows twice.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

/// 16x16, opaque #333333.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

VImage _image() => VImage()..imageData = (VImageData()..data = base64Decode(_png));

VTree _tree(VTreeItem item) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<int> _iconsDrawnByRow(WidgetTester tester, VTreeItem item) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree(item))),
  ));
  await tester.pumpAndSettle();
  return find.byType(FutureBuilder<Widget?>).evaluate().length;
}

void main() {
  testWidgets('an icon the overlay paints is not drawn by the row as well', (tester) async {
    final item = VTreeItem()
      ..id = 10
      ..text = 'f4demo'
      ..image = _image()
      ..images = [null]
      ..paintedTexts = [0];

    expect(await _iconsDrawnByRow(tester, item), 0);
  });

  testWidgets('a row that is not owner-drawn still draws its own icon', (tester) async {
    final item = VTreeItem()
      ..id = 10
      ..text = 'f4demo'
      ..image = _image();

    expect(await _iconsDrawnByRow(tester, item), 1);
  });
}
