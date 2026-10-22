// A tree column's header shows what a native header shows: the text without its mnemonic marks, and
// the column's image before it.

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
import 'package:swtflutter/src/impl/treecolumn_evolve.dart';

/// 16x16, opaque grey (0x333333).
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

VTree _tree(VTreeColumn column) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [column, VTreeColumn()
    ..id = 3
    ..text = 'Second'
    ..width = 120]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pump(WidgetTester tester, VTreeColumn column) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree(column))),
  ));
  // The header decodes its image off the frame: let that finish, then lay out what it produced.
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

Finder _inHeader(Finder finder) =>
    find.descendant(of: find.byWidgetPredicate((w) => w is TreeColumnSwt), matching: finder);

void main() {
  testWidgets('a header shows its text without the mnemonic marks', (tester) async {
    await _pump(tester, VTreeColumn()
      ..id = 2
      ..text = '&Name && kind'
      ..width = 120);

    // A tree with several columns draws its headers in capitals.
    expect(_inHeader(find.text('NAME & KIND')), findsOneWidget);
    expect(_inHeader(find.textContaining('&N')), findsNothing);
  });

  testWidgets('a & that ends the header text is not drawn, as on Win32 and GTK', (tester) async {
    await _pump(tester, VTreeColumn()
      ..id = 2
      ..text = 'R&D &'
      ..width = 120);

    expect(_inHeader(find.text('RD ')), findsOneWidget);
  });

  testWidgets('a header shows its image before its text', (tester) async {
    await _pump(tester, VTreeColumn()
      ..id = 2
      ..text = 'Name'
      ..width = 120
      ..image = (VImage()..imageData = (VImageData()..data = base64Decode(_png))));

    final image = _inHeader(find.byType(FutureBuilder<Widget?>));
    expect(image, findsOneWidget);
    final imageRect = tester.getRect(image);
    expect(imageRect.width, greaterThan(0));
    expect(imageRect.right, lessThanOrEqualTo(tester.getRect(_inHeader(find.text('NAME'))).left));
  });

  testWidgets('a header without an image draws none', (tester) async {
    await _pump(tester, VTreeColumn()
      ..id = 2
      ..text = 'Name'
      ..width = 120);

    expect(_inHeader(find.byType(FutureBuilder<Widget?>)), findsNothing);
  });
}
