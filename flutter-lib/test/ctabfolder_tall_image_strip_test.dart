// A tab image taller than the drawn icon makes the tab strip taller, as native SWT sizes the header
// to its tallest tab. Java computes that height for its client area and sends it, so the strip is
// drawn exactly as tall as Java says and the page starts where the strip ends.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _folderWidth = 380;
const _folderHeight = 300;
const _imageHeight = 40;
const _sentStripHeight = 56;

VCTabFolder _folder(int? imageHeight, int? stripHeight, int tabPosition) => VCTabFolder()
  ..id = 1000
  ..tabHeight = stripHeight
  ..style = SWT.NONE
  ..tabPosition = tabPosition
  ..selection = 0
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _folderWidth
    ..height = _folderHeight)
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Search'
      ..showing = true,
    VCTabItem()
      ..id = 11
      ..text = 'Layers'
      ..showing = true
      ..image = imageHeight == null
          ? null
          : (VImage()
            ..id = 20
            ..width = imageHeight
            ..height = imageHeight),
  ];

/// Where the page area starts below the top of the folder, or ends above its bottom.
Future<double> _stripHeight(WidgetTester tester, int? imageHeight, int? stripHeight,
    {int tabPosition = SWT.TOP}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _folderWidth.toDouble(),
        height: _folderHeight.toDouble(),
        child: CTabFolderSwt<VCTabFolder>(value: _folder(imageHeight, stripHeight, tabPosition)),
      ),
    ),
  ));
  await tester.pump();
  await tester.pump();
  while (tester.takeException() != null) {}
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  final body = tester.getRect(find.byType(IndexedStack).first);
  return tabPosition == SWT.BOTTOM ? folder.bottom - body.bottom : body.top - folder.top;
}

void main() {
  setUp(resetConfigFlags);

  testWidgets('a tall image does not grow the strip on its own', (tester) async {
    expect(await _stripHeight(tester, _imageHeight, null), await _stripHeight(tester, null, null));
  });

  testWidgets('the strip is as tall as the height Java sends', (tester) async {
    expect(await _stripHeight(tester, _imageHeight, _sentStripHeight), _sentStripHeight);
  });

  testWidgets('tabs at the bottom take the sent height the same way', (tester) async {
    expect(await _stripHeight(tester, _imageHeight, _sentStripHeight, tabPosition: SWT.BOTTOM),
        _sentStripHeight);
  });

  test('a tab image is drawn in a square as tall as the image, never smaller than the icon', () {
    const iconSize = 16.0;
    expect(ctabIconSize(VImage()..height = _imageHeight, iconSize), _imageHeight);
    expect(ctabIconSize(VImage()..height = 8, iconSize), iconSize);
    expect(ctabIconSize(null, iconSize), iconSize);
  });
}
