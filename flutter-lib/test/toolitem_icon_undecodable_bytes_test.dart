// A ToolItem whose image bytes do not decode still finishes loading its icon: the render resolves
// on the decode error, so the item never shows its loading indicator forever.

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
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VToolBar _toolBar(Uint8List bytes) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = bytes)),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 40);

Finder get _spinner =>
    find.descendant(of: find.byType(ToolItemSwt), matching: find.byType(CircularProgressIndicator));

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
  });
  tearDown(resetConfigFlags);

  testWidgets('a ToolItem whose image bytes do not decode stops showing its loading indicator',
      (tester) async {
    final notAnImage = Uint8List.fromList(List.generate(64, (i) => i));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          SizedBox(width: 200, height: 40, child: ToolBarSwt(value: _toolBar(notAnImage))),
    ));
    expect(_spinner, findsOneWidget, reason: 'the icon is loading on the first frame');

    // The decode fails on the real clock: give it ample time, then the indicator must be gone.
    for (var i = 0; i < 100 && _spinner.evaluate().isNotEmpty; i++) {
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 20)));
      await tester.pump();
    }
    expect(_spinner, findsNothing,
        reason: 'the icon render never resolved on bytes that do not decode');
  });
}
