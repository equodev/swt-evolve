// Native menus draw every label in one column: when any item has an image or is a CHECK/RADIO
// item, items without one leave that leading space empty. A menu with neither keeps no indent.

import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/decorations_align.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';
import 'support/menu_bar_fixtures.dart';
import 'support/menu_shown_ack.dart';

/// 16x16, opaque grey.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

const _withImage = 'Open File...';
const _plain = 'Open Projects from File System...';
const _check = 'Show Toolbar';
const _checkWithImage = 'Link with Editor';

VMenuItem _item(int id, int style, String text, {bool image = false, List<VMenuItem>? submenu}) =>
    VMenuItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text
  ..image = image ? (VImage()..imageData = (VImageData()..data = base64Decode(_png))) : null
  ..menu = submenu == null
      ? null
      : (VMenu()
        ..id = id * 10
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = submenu);

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

/// Opens the File drop-down of a menu bar holding [items].
Future<void> _openDropDown(WidgetTester tester, List<VMenuItem> items) async {
  final bar = VMenu()
    ..id = 200
    ..style = SWT.BAR
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = 201
        ..style = SWT.CASCADE
        ..enabled = true
        ..text = 'File'
        ..menu = (VMenu()
          ..id = 202
          ..style = SWT.DROP_DOWN
          ..enabled = true
          ..items = items),
    ];
  setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.hleft);
  addTearDown(resetConfigFlags);
  await pumpDecorations(tester, decorations(menuBar: bar, children: [mainToolbar(withMenuStrip: true)]));
  await tester.tap(find.text('File'));
  await tester.pumpAndSettle();
  await ackMenuShown(tester, 202);
}

/// Opens the menu bar collapsed into its button, then the drop-down of its `File` entry, holding
/// [items]; the bar's other entry, `Window`, has an image.
Future<void> _openCollapsedDropDown(WidgetTester tester, List<VMenuItem> items) async {
  final bar = VMenu()
    ..id = 200
    ..style = SWT.BAR
    ..enabled = true
    ..items = [
      _item(201, SWT.CASCADE, 'File', submenu: items),
      _item(203, SWT.CASCADE, 'Window', image: true, submenu: [_item(2031, SWT.PUSH, 'New Window')]),
    ];
  setConfigFlags(ConfigFlags()..decorations_align = DecorationsAlign.vleft);
  addTearDown(resetConfigFlags);
  await pumpDecorations(tester, decorations(menuBar: bar, children: [mainToolbar()]));
  await tester.tap(find.byIcon(Icons.menu));
  await tester.pumpAndSettle();
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  await mouse.moveTo(tester.getCenter(find.text('File')));
  await tester.pumpAndSettle();
}

/// The x of [finder] within the menu item that holds it, so menus placed anywhere compare.
double _xInItem(WidgetTester tester, Finder finder) {
  final item = find.ancestor(of: finder, matching: find.byWidgetPredicate((w) => w is MenuItemSwt));
  return tester.getTopLeft(finder).dx - tester.getTopLeft(item.first).dx;
}

double _labelX(WidgetTester tester, String label) => _xInItem(tester, find.text(label));

Map<String, double> _labelXs(WidgetTester tester, List<String> labels) =>
    {for (final label in labels) label: _labelX(tester, label)};

void _expectOneColumn(WidgetTester tester, List<String> labels) {
  final xs = _labelXs(tester, labels);
  expect(xs.values.toSet(), hasLength(1), reason: 'every label starts in one column: $xs');
}

void main() {
  testWidgets('labels of image, plain and CHECK items start at the same x', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
      _item(103, SWT.CHECK, _check),
    ]);
    _expectOneColumn(tester, [_withImage, _plain, _check]);
  });

  testWidgets('labels of an image item and a plain item start at the same x', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
    ]);
    expect(_labelX(tester, _plain), _labelX(tester, _withImage));
  });

  testWidgets('a CHECK item with an image keeps its image in the image column', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
      _item(103, SWT.CHECK, _check),
      _item(104, SWT.CHECK, _checkWithImage, image: true),
    ]);
    _expectOneColumn(tester, [_withImage, _plain, _check, _checkWithImage]);
    final images = find.byType(Image);
    expect(_xInItem(tester, images.at(0)), _xInItem(tester, images.at(1)),
        reason: 'both images sit in the image column');
  });

  testWidgets('a cascade item without an image lines up with image items', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.CASCADE, 'Open With', submenu: [_item(1021, SWT.PUSH, 'Text Editor')]),
    ]);
    expect(_labelX(tester, 'Open With'), _labelX(tester, _withImage));
  });

  testWidgets('labels in a menu bar drop-down share one column', (tester) async {
    await _openDropDown(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
    ]);
    _expectOneColumn(tester, [_withImage, _plain]);
  });

  testWidgets('labels in a cascade submenu share one column of their own', (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, 'Copy'),
      _item(102, SWT.CASCADE, 'New', submenu: [
        _item(1021, SWT.PUSH, 'Project...', image: true),
        _item(1022, SWT.PUSH, 'Other...'),
      ]),
    ]);
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.text('New')));
    await tester.pumpAndSettle();

    _expectOneColumn(tester, ['Project...', 'Other...']);
    expect(_labelX(tester, 'New'), _labelX(tester, 'Copy'),
        reason: "the submenu's image does not indent its parent menu");
  });

  testWidgets('a menu with no image and no CHECK/RADIO item keeps its labels unindented',
      (tester) async {
    await _openPopup(tester, [
      _item(101, SWT.PUSH, _withImage, image: true),
      _item(102, SWT.PUSH, _plain),
    ]);
    final leadingX = _xInItem(tester, find.byType(Image).first);
    await tester.pumpWidget(const SizedBox());
    await tester.pumpAndSettle();
    VRegistry.instance.clear();

    await _openPopup(tester, [
      _item(111, SWT.PUSH, _withImage),
      _item(112, SWT.PUSH, _plain),
    ]);
    expect(_labelX(tester, _plain), leadingX,
        reason: 'with no leading content a label starts where an icon would');
  });

  testWidgets('labels in a drop-down of the collapsed menu bar share one column', (tester) async {
    await _openCollapsedDropDown(tester, [
      _item(2011, SWT.PUSH, _withImage, image: true),
      _item(2012, SWT.PUSH, _plain),
    ]);
    _expectOneColumn(tester, [_withImage, _plain]);
    _expectOneColumn(tester, ['File', 'Window']);
  });

  testWidgets('an item that gets an image while the menu is open moves every label to one column',
      (tester) async {
    await _openPopup(tester, [_item(101, SWT.PUSH, _plain), _item(102, SWT.PUSH, 'Close Project')]);
    expect(find.byType(Image), findsNothing);

    await deliverWhole(_item(101, SWT.PUSH, _plain, image: true));
    await tester.pumpAndSettle();

    expect(find.byType(Image), findsOneWidget, reason: 'the item shows its new image');
    _expectOneColumn(tester, [_plain, 'Close Project']);
  });
}
