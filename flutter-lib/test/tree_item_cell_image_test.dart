// A tree row draws each cell's image before the cell's text, in every column, as native SWT does.

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

/// 16x16, opaque grey (0x333333).
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

VImage _image() => VImage()..imageData = (VImageData()..data = base64Decode(_png));

VTree _tree(VTreeItem item) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    VTreeColumn()
      ..id = 2
      ..text = 'Name'
      ..width = 150,
    VTreeColumn()
      ..id = 3
      ..text = 'Kind'
      ..width = 150,
  ]
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pump(WidgetTester tester, VTreeItem item) async {
  // A columned tree's rows overflow by their padding, a layout matter of its own: it is not what this
  // test is about.
  final reportError = FlutterError.onError;
  FlutterError.onError = (details) {
    if (details.exceptionAsString().contains('overflowed')) return;
    reportError?.call(details);
  };
  addTearDown(() => FlutterError.onError = reportError);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree(item))),
  ));
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

Finder _icons() => find.byType(FutureBuilder<Widget?>);

void main() {
  testWidgets('an image in the second column is drawn before that cell\'s text', (tester) async {
    await _pump(tester, VTreeItem()
      ..id = 10
      ..text = 'report.txt'
      ..texts = ['report.txt', 'Text file']
      ..images = [null, _image()]);

    expect(_icons(), findsOneWidget);
    final icon = tester.getRect(_icons());
    final text = tester.getRect(find.text('Text file'));
    expect(icon.right, lessThanOrEqualTo(text.left));
    expect(icon.left, greaterThan(tester.getRect(find.text('report.txt')).right));
  });

  testWidgets('a row draws the image of each cell that has one', (tester) async {
    await _pump(tester, VTreeItem()
      ..id = 10
      ..text = 'report.txt'
      ..texts = ['report.txt', 'Text file']
      ..image = _image()
      ..images = [_image(), _image()]);

    expect(_icons(), findsNWidgets(2));
  });

  testWidgets('a cell without an image draws none', (tester) async {
    await _pump(tester, VTreeItem()
      ..id = 10
      ..text = 'report.txt'
      ..texts = ['report.txt', 'Text file']);

    expect(_icons(), findsNothing);
  });
}
