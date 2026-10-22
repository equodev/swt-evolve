// A tab image taller than the drawn icon makes the tab strip taller by the difference, as native SWT
// sizes the header to its tallest tab. Java grows the folder's client area by the same amount, so
// the page starts where the strip ends.

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
const _tabStripHeight = 32;
const _iconSize = 16.0;

VCTabFolder _folder(int? imageHeight, int tabPosition) => VCTabFolder()
  ..id = 1000
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
Future<double> _stripHeight(WidgetTester tester, int? imageHeight, {int tabPosition = SWT.TOP}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _folderWidth.toDouble(),
        height: _folderHeight.toDouble(),
        child: CTabFolderSwt<VCTabFolder>(value: _folder(imageHeight, tabPosition)),
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

  testWidgets('without images the strip keeps its height', (tester) async {
    expect(await _stripHeight(tester, null), _tabStripHeight);
  });

  testWidgets('an image no taller than the icon leaves the strip as it is', (tester) async {
    expect(await _stripHeight(tester, _iconSize.toInt()), _tabStripHeight);
  });

  testWidgets('a tall image grows the strip by what it overshoots the icon', (tester) async {
    expect(await _stripHeight(tester, 40), _tabStripHeight + 40 - _iconSize);
  });

  testWidgets('tabs at the bottom grow the same way', (tester) async {
    expect(await _stripHeight(tester, 40, tabPosition: SWT.BOTTOM), _tabStripHeight + 40 - _iconSize);
  });

  test('the strip height follows the tallest image', () {
    final items = [
      VCTabItem()..image = (VImage()..height = 24),
      VCTabItem()..image = (VImage()..height = 40),
      VCTabItem(),
    ];
    expect(ctabStripHeight(items, _iconSize), _tabStripHeight + 40 - _iconSize);
    expect(ctabIconSize(items[1].image, _iconSize), 40);
    expect(ctabIconSize(null, _iconSize), _iconSize);
  });
}
