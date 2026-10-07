// A CHECK or RADIO ToolItem lays out its image and text like a PUSH item: text under the image,
// unless the ToolBar has SWT.RIGHT. Otherwise one toggle item in a bar of push items stands out
// as a wider, shorter strip with its label beside the icon.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

/// A 1x1 opaque PNG.
final Uint8List _png = Uint8List.fromList(const [
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41, 0x54, 0x78, 0x9C, 0x63, 0xF8, 0xCF, 0xC0, 0xF0,
  0x1F, 0x00, 0x05, 0x00, 0x01, 0xFF, 0x89, 0x99, 0x3D, 0x1D, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
  0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
]);

const String _label = 'Double Buffer';

Future<void> _pumpBar(WidgetTester tester, {required int barStyle, required int itemStyle}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 80,
      child: ToolBarSwt(
        value: VToolBar()
          ..id = 1
          ..style = SWT.HORIZONTAL | barStyle
          ..enabled = true
          ..visible = true
          ..items = [
            VToolItem()
              ..id = 2
              ..style = itemStyle
              ..enabled = true
              ..text = _label
              ..image = (VImage()
                ..imageData = (VImageData()
                  ..width = 1
                  ..height = 1
                  ..data = _png)),
          ]
          ..bounds = (VRectangle()
            ..x = 0
            ..y = 0
            ..width = 300
            ..height = 80),
      ),
    ),
  ));
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
  for (var i = 0; i < 4; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
}

Rect _iconRect(WidgetTester tester) => tester.getRect(find.descendant(
    of: find.byType(ToolItemSwt), matching: find.byType(FutureBuilder<Widget?>)));

void main() {
  for (final (name, style) in [('CHECK', SWT.CHECK), ('RADIO', SWT.RADIO)]) {
    testWidgets('$name item text sits under its image on a bar without SWT.RIGHT', (tester) async {
      await _pumpBar(tester, barStyle: SWT.FLAT, itemStyle: style);

      final icon = _iconRect(tester);
      final text = tester.getRect(find.text(_label));
      expect(text.top, greaterThanOrEqualTo(icon.bottom));
      expect((text.center.dx - icon.center.dx).abs(), lessThanOrEqualTo(1));
    });

    testWidgets('$name item text sits beside its image on an SWT.RIGHT bar', (tester) async {
      await _pumpBar(tester, barStyle: SWT.FLAT | SWT.RIGHT, itemStyle: style);

      final icon = _iconRect(tester);
      final text = tester.getRect(find.text(_label));
      expect(text.left, greaterThanOrEqualTo(icon.right));
    });
  }
}
