// Every ToolItem icon comes out the same size whatever else its bar holds. Under `bigger_icon_size`
// Java measures every image as an icon of the theme's size and it is drawn at that size, unshrunk.
// Without it an item is measured from its image and its icon shrinks into that item's own box: a
// bar that also hosts a control (drawn at its Java size) must not draw its icons larger.

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
import 'package:swtflutter/src/theme/theme_settings/toolitem_theme_settings.dart';

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

/// What Sizes.computeSize(DartToolItem) measures every image at under the flag.
final int _themeIconSize = toolItemIconSize.round();
const int _imageSize = 16;

/// The platform padding Sizes.computeSize(DartToolItem) adds around an item's icon.
const int _padW = 7;
const int _padH = 6;
const int _iconCount = 4;

const int _hostedWidth = 300;
const int _hostedHeight = 31;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// Sizes.computeSize(DartToolItem) adds the arrow's room to a drop-down item.
const int _arrowWidth = 15;

VToolItem _iconItem(int id, int itemWidth, {bool dropDown = false}) => VToolItem()
  ..id = id
  ..style = dropDown ? SWT.DROP_DOWN : SWT.PUSH
  ..enabled = true
  ..width = itemWidth + (dropDown ? _arrowWidth : 0)
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

/// A bar whose icon items Java measured from an icon of [measuredIcon] px.
VToolBar _bar(int id, int measuredIcon, {bool hostsControl = false, bool dropDown = false}) {
  final itemWidth = measuredIcon + _padW;
  final itemHeight = measuredIcon + _padH;
  final items = [
    for (var i = 1; i <= _iconCount; i++)
      _iconItem(id * 100 + i, itemWidth, dropDown: dropDown && i == 1),
    if (hostsControl) _hostingItem(id * 100 + 50),
  ];
  final width = _iconCount * itemWidth +
      (dropDown ? _arrowWidth : 0) +
      (hostsControl ? _hostedWidth : 0);
  final height = hostsControl && _hostedHeight > itemHeight ? _hostedHeight : itemHeight;
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
  testWidgets('with bigger_icon_size every bar draws its icons at the theme size', (tester) async {
    final withDropDown = await _iconSize(tester, _bar(3, _themeIconSize, dropDown: true));
    final withControl = await _iconSize(tester, _bar(2, _themeIconSize, hostsControl: true));
    final iconsOnly = await _iconSize(tester, _bar(1, _themeIconSize));

    expect(withDropDown.width, moreOrLessEquals(_themeIconSize.toDouble(), epsilon: 0.5),
        reason: 'a drop-down item must fit its icon and arrow in the width Java measured');
    expect(iconsOnly.width, moreOrLessEquals(_themeIconSize.toDouble(), epsilon: 0.5),
        reason: 'an icon-only bar must not shrink its icons');
    expect(withControl.width, moreOrLessEquals(_themeIconSize.toDouble(), epsilon: 0.5),
        reason: 'a bar that hosts a control must draw its icons at the same size');
    expect(tester.getRect(find.byType(ToolItemSwt<VToolItem>).first).width,
        lessThanOrEqualTo(_themeIconSize + _padW + 0.5),
        reason: 'an icon item must fit the width Java measured for it');
  });

  testWidgets('measured from its image, a bar that hosts a control draws its icons at the size an '
      'icon-only bar does', (tester) async {
    final withControl = await _iconSize(tester, _bar(2, _imageSize, hostsControl: true));
    final iconsOnly = await _iconSize(tester, _bar(1, _imageSize));

    expect(withControl.width, moreOrLessEquals(iconsOnly.width, epsilon: 0.5),
        reason: 'the icon must shrink to its own item box, not by a factor shared with the '
            'hosted control');
    expect(tester.getRect(find.byType(ToolItemSwt<VToolItem>).first).width,
        lessThanOrEqualTo(_imageSize + _padW + 0.5),
        reason: 'an icon item must fit the width Java measured for it');
  });
}
