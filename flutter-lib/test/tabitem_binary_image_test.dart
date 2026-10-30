// A TabItem whose image is only bytes (an application image, no file the icon map knows) draws
// those bytes next to its label, as SWT does.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/theme/theme_extensions/tabitem_theme_extension.dart';

/// A 16x16 RGB PNG.
const String _pngHex =
    '89504e470d0a1a0a0000000d4948445200000010000000100802000000909168360000001f49444154785e6360'
    '2003a44c3b41241abe1afe2381a1aa010f1a461a48020093236c4866dd5c710000000049454e44ae426082';

Uint8List _hex(String hex) => Uint8List.fromList([
      for (var i = 0; i < hex.length; i += 2) int.parse(hex.substring(i, i + 2), radix: 16),
    ]);

void main() {
  testWidgets('a TabItem draws an image it only has the bytes of', (tester) async {
    ImageUtils.clearCache();
    final bytes = _hex(_pngHex);
    final folder = VTabFolder()
      ..id = 1
      ..style = SWT.TOP
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 200)
      ..items = [
        VTabItem()
          ..id = 2
          ..text = 'Section'
          ..image = (VImage()
            ..id = 3
            ..imageData = (VImageData()
              ..width = 16
              ..height = 16
              ..data = bytes)),
      ];
    folder.selection = [folder.items!.first];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: TabFolderSwt<VTabFolder>(value: folder),
      ),
    ));
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    for (var i = 0; i < 4; i++) {
      await tester.pump(const Duration(milliseconds: 50));
    }
    while (tester.takeException() != null) {}

    final image = find.descendant(of: find.byType(TabItemSwt), matching: find.byType(Image));
    expect(image, findsOneWidget);
    expect((tester.widget<Image>(image).image as MemoryImage).bytes, bytes);
    final theme = Theme.of(tester.element(image)).extension<TabItemThemeExtension>()!;
    expect(tester.getSize(image).width, theme.iconSize);
  });
}
