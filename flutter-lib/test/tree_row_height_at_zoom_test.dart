// A tree lays every row out at the one height it hit-tests and Java reports, also at a zoom whose
// text scaler makes a row's content (a line plus its cell padding) taller than the theme row:
// swt.autoScale=integer on a 150 % monitor draws SWT's 100 at two thirds, every Text at 1.5 times.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/tree_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// 16x16, opaque grey.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

const _rows = 16;

VImage _image() => VImage()..imageData = (VImageData()..data = base64Decode(_png));

VTree _tree(int style) => VTree()
  ..id = 1
  ..style = style
  ..enabled = true
  ..font = (VFont()
    ..fontData = [
      VFontData()
        ..name = 'Arial'
        ..height = 11
        ..style = SWT.NORMAL,
    ])
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Root'
      ..expanded = true
      ..image = _image()
      ..items = [
        for (var i = 1; i < _rows; i++)
          VTreeItem()
            ..id = 100 + i
            ..text = 'Row $i'
            ..image = _image(),
      ],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 900);

void main() {
  tearDown(() {
    appScaleNotifier.value = 1.0;
    resetConfigFlags();
  });

  for (final (name, style) in [('plain', SWT.MULTI), ('CHECK', SWT.MULTI | SWT.CHECK)]) {
    testWidgets('a $name tree draws its rows at the height it hit-tests, at two thirds', (tester) async {
      setConfigFlags(ConfigFlags()..font_point_scale = 96 / 72);
      appScaleNotifier.value = 2 / 3;
      await tester.binding.setSurfaceSize(const Size(400, 700));
      addTearDown(() => tester.binding.setSurfaceSize(null));
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget:
            SizedBox(width: 400, height: 900, child: TreeSwt<VTree>(value: _tree(style))),
      ));
      for (var i = 0; i < 5; i++) {
        await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
        await tester.pump();
      }
      await tester.pumpAndSettle();

      final tree = tester.state<TreeImpl>(find.byType(TreeSwt<VTree>));
      final rowHeight = tree.cachedItemHeight!;
      final treeBox = tester.renderObject<RenderBox>(find.byType(TreeSwt<VTree>));
      for (var i = 1; i < _rows; i++) {
        final text = tester.renderObject<RenderBox>(find.text('Row $i'));
        final textCentre =
            treeBox.globalToLocal(text.localToGlobal(text.size.center(Offset.zero))).dy;
        expect((textCentre / rowHeight).floor(), i,
            reason: 'Row $i is drawn in the band the tree hit-tests as row $i '
                '(centre $textCentre, rows of $rowHeight)');
      }
    });
  }
}
