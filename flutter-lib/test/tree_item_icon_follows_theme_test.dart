// A tree row's icon that is a monochrome glyph takes the theme's icon colour, so a dark glyph is drawn
// light on a dark tree; with preserve_icon_colors it keeps the colour it was given.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// 16x16, opaque grey (0x333333): a monochrome glyph.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Project'
      ..image = (VImage()..imageData = (VImageData()..data = base64Decode(_png))),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

/// Pumps the tree in [theme] and reads back the pixel at the centre of the row's icon.
Future<Color> _iconCentre(WidgetTester tester, ThemeMode theme) async {
  final boundary = GlobalKey();
  await tester.pumpWidget(EvolveApp(
    theme: theme,
    contentWidget: RepaintBoundary(
      key: boundary,
      child: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree())),
    ),
  ));
  // The row decodes its icon off the frame: let that finish, then lay out what it produced.
  for (var i = 0; i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();

  final icon = find.byType(FutureBuilder<Widget?>);
  expect(icon, findsOneWidget);
  final origin = tester.getTopLeft(find.byKey(boundary));
  final centre = tester.getCenter(icon) - origin;

  late ByteData bytes;
  late int width;
  await tester.runAsync(() async {
    final render = tester.renderObject(find.byKey(boundary)) as RenderRepaintBoundary;
    final image = await render.toImage();
    width = image.width;
    bytes = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
  });
  final offset = (centre.dy.round() * width + centre.dx.round()) * 4;
  return Color.fromARGB(bytes.getUint8(offset + 3), bytes.getUint8(offset),
      bytes.getUint8(offset + 1), bytes.getUint8(offset + 2));
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a dark glyph is drawn light on a dark tree', (tester) async {
    final centre = await _iconCentre(tester, ThemeMode.dark);
    expect(centre.computeLuminance(), greaterThan(0.4), reason: 'drawn $centre');
  });

  testWidgets('the same glyph is drawn dark on a light tree', (tester) async {
    final centre = await _iconCentre(tester, ThemeMode.light);
    expect(centre.computeLuminance(), lessThan(0.4), reason: 'drawn $centre');
  });

  testWidgets('preserve_icon_colors keeps the glyph\'s own colour on a dark tree', (tester) async {
    setConfigFlags(ConfigFlags()..preserve_icon_colors = true);
    final centre = await _iconCentre(tester, ThemeMode.dark);
    expect(centre, const Color(0xFF333333));
  });
}
