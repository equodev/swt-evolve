// An item's icon is drawn at the theme's size and shrunk to the box Java measured for it. The
// shrink must come from that item's own box: a bar that also hosts a control (a spinner, a combo)
// drawn at its Java size must not draw its icons larger than a bar of icons alone.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const List<int> _pngBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

/// What Sizes.computeSize(DartToolItem) gives a 16x16 image: the image plus the platform padding.
const int _iconItemWidth = 23;
const int _iconItemHeight = 22;
const int _iconCount = 4;

const int _hostedWidth = 300;
const int _hostedHeight = 31;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VToolItem _iconItem(int id) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..width = _iconItemWidth
  ..image = (VImage()
    ..imageData = (VImageData()
      ..width = 16
      ..height = 16
      ..data = _pngBytes));

VToolItem _hostingItem(int id) => VToolItem()
  ..id = id
  ..style = SWT.SEPARATOR
  ..enabled = true
  ..width = _hostedWidth
  ..control = (VComposite()
    ..id = id + 1
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, _hostedWidth, _hostedHeight)
    ..children = [
      VLabel()
        ..id = id + 2
        ..style = SWT.NONE
        ..enabled = true
        ..visible = true
        ..text = 'Network layer'
        ..bounds = _rect(0, 0, _hostedWidth, _hostedHeight),
    ]);

VToolBar _bar(int id, {bool hostsControl = false}) {
  final items = [
    for (var i = 1; i <= _iconCount; i++) _iconItem(id * 100 + i),
    if (hostsControl) _hostingItem(id * 100 + 50),
  ];
  final width = _iconCount * _iconItemWidth + (hostsControl ? _hostedWidth : 0);
  final height = hostsControl ? _hostedHeight : _iconItemHeight;
  return VToolBar()
    ..id = id
    ..style = SWT.HORIZONTAL | SWT.FLAT
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, width, height)
    ..items = items;
}

Future<Size> _iconSize(WidgetTester tester, VToolBar bar) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: bar.bounds!.width.toDouble(),
        height: bar.bounds!.height.toDouble(),
        child: ToolBarSwt<VToolBar>(value: bar),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  final icons = find.byType(Image);
  expect(icons, findsNWidgets(_iconCount), reason: 'every icon item must render its image');
  return tester.getRect(icons.first).size;
}

void main() {
  testWidgets('a bar that hosts a control draws its icons at the size an icon-only bar does',
      (tester) async {
    final iconsOnly = await _iconSize(tester, _bar(1));
    final withControl = await _iconSize(tester, _bar(2, hostsControl: true));

    expect(withControl.width, moreOrLessEquals(iconsOnly.width, epsilon: 0.5),
        reason: 'the icon must shrink to its own item box, not by a factor shared with the '
            'hosted control');
    expect(tester.getRect(find.byType(ToolItemSwt<VToolItem>).first).width,
        lessThanOrEqualTo(_iconItemWidth + 0.5),
        reason: 'an icon item must fit the width Java measured for it');
  });
}
