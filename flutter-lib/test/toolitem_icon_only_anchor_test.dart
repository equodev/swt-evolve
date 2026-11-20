// An icon-only ToolItem has no text to be found by: its identifier is the only way to reach it,
// on screen and while its tooltip is up.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

// A 1x1 PNG, so the item is genuinely icon-only.
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

VToolBar _iconBar() => VToolBar()
  ..id = 200
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 201
      ..style = SWT.PUSH
      ..enabled = true
      ..toolTipText = 'Skip All Breakpoints (Ctrl+Alt+B)'
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = _pngBytes)),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

Widget _host() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 400, height: 40, child: ToolBarSwt(value: _iconBar())),
      ),
    );

Future<void> _settle(WidgetTester tester) async {
  // The png may never finish decoding under test, so pump fixed frames rather than pumpAndSettle.
  for (var i = 0; i < 20; i++) {
    await tester.pump(const Duration(milliseconds: 100));
  }
  while (tester.takeException() != null) {}
}

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);

  testWidgets('an icon-only ToolItem is found by its identifier', (tester) async {
    await tester.pumpWidget(_host());
    await _settle(tester);

    final item = TestAnchors.locate('ToolItem/201');
    expect(item, isNotNull);
    expect(item!['paints'], isTrue);
  });

  testWidgets('an icon-only ToolItem is still found by its identifier while hovered', (tester) async {
    await tester.pumpWidget(_host());
    await _settle(tester);

    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: Offset.zero);
    addTearDown(gesture.removePointer);
    await gesture.moveTo(tester.getCenter(find.byType(ToolBarSwt)));
    await _settle(tester);

    final item = TestAnchors.locate('ToolItem/201');
    expect(item, isNotNull, reason: 'still on screen while its tooltip is up');
    expect(item!['paints'], isTrue);
  });
}
