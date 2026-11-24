// Native menus draw every label in one column: when any item has an image or is a CHECK/RADIO
// item, items without one leave that leading space empty. A menu with neither keeps no indent.

import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

import 'support/menu_shown_ack.dart';

/// 16x16, opaque grey.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

const _withImage = 'Open File...';
const _plain = 'Open Projects from File System...';
const _check = 'Show Toolbar';

VMenuItem _item(int id, int style, String text, {bool image = false}) => VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text
  ..image = image ? (VImage()..imageData = (VImageData()..data = base64Decode(_png))) : null;

VTree _tree(List<VMenuItem> items) => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Node 0',
  ]
  ..menu = (VMenu()
    ..id = 100
    ..style = SWT.POP_UP
    ..enabled = true
    ..items = items)
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

Future<void> _openPopup(WidgetTester tester, List<VMenuItem> items) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 800, height: 400, child: TreeSwt<VTree>(value: _tree(items))),
  ));
  await tester.pump();
  await tester.tap(find.text('Node 0'), buttons: kSecondaryButton);
  await tester.pump();
  await tester.pump();
  await ackMenuShown(tester, 100);
}

double _labelX(WidgetTester tester, String label) => tester.getTopLeft(find.text(label)).dx;

void main() {
  testWidgets('labels of image, plain and CHECK items start at the same x', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
      _item(103, SWT.CHECK, _check),
    ]);
    final xs = {
      _withImage: _labelX(tester, _withImage),
      _plain: _labelX(tester, _plain),
      _check: _labelX(tester, _check),
    };
    expect(xs.values.toSet(), hasLength(1), reason: 'every label starts in one column: $xs');
  });

  testWidgets('labels of an image item and a plain item start at the same x', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
    ]);
    expect(_labelX(tester, _plain), _labelX(tester, _withImage));
  });

  testWidgets('a menu with no image and no CHECK/RADIO item keeps its labels unindented',
      (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
    ]);
    final leadingX = tester.getTopLeft(find.byType(Image).first).dx;
    await tester.pumpWidget(const SizedBox());
    await tester.pumpAndSettle();

    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage),
      _item(102, SWT.PUSH, _plain),
    ]);
    expect(_labelX(tester, _plain), leadingX,
        reason: 'with no leading content a label starts where an icon would');
  });
}
