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

VImage _icon() => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = _pngBytes);

VToolItem _item({required int width}) => VToolItem()
  ..id = 2
  ..style = SWT.PUSH
  ..enabled = true
  ..toolTipText = 'Collapse All'
  ..width = width
  ..image = _icon();

VToolBar _toolBar(VToolItem item) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

void main() {
  const double themeIconSize = 30.0;

  testWidgets('a push item renders its icon at the toolbar icon size', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 40,
        child: ToolBarSwt(value: _toolBar(_item(width: 24))),
      ),
    ));
    await tester.pumpAndSettle();

    final icon = find.byType(Image);
    expect(icon, findsOneWidget, reason: 'the item must render its icon');
    expect(tester.getSize(icon).width, themeIconSize,
        reason: 'the derived item width must not shrink the icon below the size '
            'the toolbar theme draws at');
    expect(tester.getSize(find.byType(ToolItemSwt)).width,
        greaterThanOrEqualTo(themeIconSize),
        reason: 'and the box has to hold that icon rather than being pinned to '
            'the size model\'s 24');
  });
}
