import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/theme/theme_extensions/tabitem_theme_extension.dart';

/// An Image painted with a GC is rendered on this side and Java sends only its `remoteRef`, no
/// pixels. A TabItem showing such an image draws that render next to its label.

VImage _gcRendered(int ref) => VImage()
  ..remoteRef = ref
  ..width = 16
  ..height = 16;

VTabFolder _folder(VImage image) {
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
        ..text = 'Drawn'
        ..image = image,
    ];
  folder.selection = [folder.items!.first];
  return folder;
}

Future<void> _pump(WidgetTester tester, VTabFolder folder) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 200,
      child: TabFolderSwt<VTabFolder>(value: folder),
    ),
  ));
  await tester.pump();
  await tester.pump();
}

Future<ui.Image> _render(WidgetTester tester) async =>
    (await tester.runAsync(() => createTestImage(width: 16, height: 16)))!;

Finder _tabImage() =>
    find.descendant(of: find.byType(TabItemSwt), matching: find.byType(RawImage));

void main() {
  setUp(ImageUtils.clearCache);

  testWidgets('a TabItem draws an image that was rendered here from a GC', (tester) async {
    final render = await _render(tester);
    ImageUtils.registerRemoteImage(51, render);

    await _pump(tester, _folder(_gcRendered(51)));

    expect(_tabImage(), findsOneWidget);
    expect(tester.widget<RawImage>(_tabImage()).image!.isCloneOf(render), isTrue);
    final theme = Theme.of(tester.element(_tabImage())).extension<TabItemThemeExtension>()!;
    expect(tester.getSize(_tabImage()).width, theme.iconSize);
  });

  testWidgets('a TabItem draws the render once it arrives after the tab was built', (tester) async {
    await _pump(tester, _folder(_gcRendered(52)));

    ImageUtils.registerRemoteImage(52, await _render(tester));
    await tester.pump();
    await tester.pump();

    expect(_tabImage(), findsOneWidget);
  });
}
