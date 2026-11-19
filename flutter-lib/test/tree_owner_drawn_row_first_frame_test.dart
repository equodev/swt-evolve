// A row Java paints ahead (one it brings on screen, as an expand does) has its owner-draw paint in
// hand before it mounts. Its first frame must not show the row's own text and icon: they sit in the
// theme font at the row's own place, so the overlay replacing them a frame later reads as the row's
// content jumping. A row whose paint has not arrived keeps drawing its own, so it is never blank.

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
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';

import 'support/gc_frames.dart';

/// 16x16, opaque #333333.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

VImage _image() => VImage()..imageData = (VImageData()..data = base64Decode(_png));

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 11
      ..text = 'src'
      ..image = _image()
      ..paintedTexts = [0]
      ..paintedImages = [0],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _mount(WidgetTester tester) => tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree())),
    ));

Color? _rowTextColor(WidgetTester tester) => tester.widget<Text>(find.text('src')).style?.color;

int _rowIcons() => find.byType(FutureBuilder<Widget?>).evaluate().length;

void main() {
  testWidgets('a row whose paint arrived before it mounted shows the paint from its first frame',
      (tester) async {
    await paintRow(11);

    await _mount(tester);

    expect(_rowTextColor(tester), Colors.transparent,
        reason: 'the row\'s own text in its first frame is replaced by the overlay\'s in the next');
    expect(_rowIcons(), 0, reason: 'nor may it draw the icon the overlay paints');

    await tester.pumpAndSettle();
    expect(_rowTextColor(tester), Colors.transparent);
    expect(find.descendant(of: find.byType(OwnerDrawnRowOverlay), matching: find.byType(IgnorePointer)),
        findsOneWidget,
        reason: 'the overlay shows the paint');
  });

  testWidgets('a row whose paint has not arrived draws its own text and icon meanwhile', (tester) async {
    await _mount(tester);
    await tester.pumpAndSettle();

    expect(_rowTextColor(tester), isNot(Colors.transparent));
    expect(_rowIcons(), 1);
  });
}
