import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
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

/// The measured box a size model produces for this bar: a 16px image plus insets per item, and a
/// text item whose label is measured with the widget's own font -- both far below what the item
/// theme paints at.
const int _measuredWidth = 127;
const int _measuredHeight = 42;

VImage _icon() => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = _pngBytes);

VToolItem _item(int id, {String? text}) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text
  ..image = _icon();

VToolBar _bar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    _item(2),
    _item(3),
    _item(4),
    _item(5),
    _item(6, text: 'Help'),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _measuredWidth
    ..height = _measuredHeight);

Future<double> _paintedIconWidth(WidgetTester tester, Widget parent) async {
  await tester.pumpWidget(
      EvolveApp(theme: ThemeMode.light, contentWidget: parent));
  await tester.pumpAndSettle();
  final icons = find.byType(Image);
  expect(icons, findsWidgets, reason: 'the bar must render its item icons');
  return tester.getRect(icons.first).width;
}

void main() {
  testWidgets(
      'a bar laid out intrinsically paints at the same scale as one given its measured box',
      (tester) async {
    // A parent that positions children at the bounds the size model measured.
    final inMeasuredBox = await _paintedIconWidth(
      tester,
      SizedBox(
        width: _measuredWidth.toDouble(),
        height: _measuredHeight.toDouble(),
        child: ToolBarSwt(value: _bar()),
      ),
    );

    // A parent that sizes children to their intrinsic content instead, which is what the
    // CTabFolder topRight slot does -- the bar arrives with unbounded constraints.
    final intrinsic = await _paintedIconWidth(
      tester,
      Row(
        mainAxisSize: MainAxisSize.min,
        children: [ToolBarSwt(value: _bar())],
      ),
    );

    expect(intrinsic, moreOrLessEquals(inMeasuredBox, epsilon: 0.5),
        reason: 'an unbounded parent must not let the items paint at the raw '
            'theme size while a measured parent scales them down');
  });
}
