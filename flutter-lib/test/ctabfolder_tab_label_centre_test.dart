// A tab's label sits in the middle of the tab strip, selected or not. A tabHeight below the tab's
// natural height cuts the tab to the strip Java reserves; the label keeps the place in that strip
// it has in an uncut tab. A tab's image sits in the middle too, as native draws it by the same rule.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const double _folderWidth = 600;
const double _folderHeight = 400;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

final Uint8List _png = Uint8List.fromList(const [
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41, 0x54, 0x78, 0x9C, 0x63, 0xF8, 0xCF, 0xC0, 0xF0,
  0x1F, 0x00, 0x05, 0x00, 0x01, 0xFF, 0x89, 0x99, 0x3D, 0x1D, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
  0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
]);

VImage _image(int id) => VImage()
  ..id = id
  ..imageData = (VImageData()
    ..width = 1
    ..height = 1
    ..data = _png);

// Widgets of the same id share state, so each folder a test pumps takes ids of its own.
VCTabFolder _folder({int? tabHeight, int tabPosition = SWT.TOP, bool withImages = false}) {
  final id = 10 + (tabHeight ?? 0) * 10 + (withImages ? 5 : 0);
  final page = VComposite()
    ..id = id + 2
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())
    ..children = [];
  return VCTabFolder()
    ..id = id
    ..style = SWT.BORDER | SWT.SINGLE
    ..enabled = true
    ..visible = true
    ..tabPosition = tabPosition
    ..tabHeight = tabHeight
    ..selection = 0
    ..items = [
      VCTabItem()
        ..id = id + 1
        ..text = 'Recipes'
        ..image = withImages ? _image(1000 + id + 1) : null,
      VCTabItem()
        ..id = id + 3
        ..text = 'Notes'
        ..image = withImages ? _image(1000 + id + 3) : null,
    ]
    ..children = [page];
}

Future<void> _pump(WidgetTester tester, VCTabFolder folder) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _folderWidth,
      height: _folderHeight,
      child: CTabFolderSwt<VCTabFolder>(value: folder),
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

/// How far the label's vertical centre sits from the strip's.
Future<double> _labelOffset(WidgetTester tester,
    {int? tabHeight, required int tabPosition, String text = 'Recipes'}) async {
  await _pump(tester, _folder(tabHeight: tabHeight, tabPosition: tabPosition));
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  final body = tester.getRect(find.byType(IndexedStack).first);
  final label = tester.getRect(find.text(text).first);
  final strip = tabPosition == SWT.BOTTOM
      ? Rect.fromLTRB(folder.left, body.bottom, folder.right, folder.bottom)
      : Rect.fromLTRB(folder.left, folder.top, folder.right, body.top);
  return label.center.dy - strip.center.dy;
}

/// How far the vertical centre of the image on the tab labelled [text] sits from the strip's.
Future<double> _imageOffset(WidgetTester tester,
    {required int tabPosition, required String text}) async {
  await _pump(tester, _folder(tabPosition: tabPosition, withImages: true));
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  final body = tester.getRect(find.byType(IndexedStack).first);
  final item = find.ancestor(of: find.text(text).first, matching: find.byType(Row)).first;
  final image = tester.getRect(find
      .descendant(of: item, matching: find.byWidgetPredicate((w) => w is FutureBuilder<Widget?>))
      .first);
  final strip = tabPosition == SWT.BOTTOM
      ? Rect.fromLTRB(folder.left, body.bottom, folder.right, folder.bottom)
      : Rect.fromLTRB(folder.left, folder.top, folder.right, body.top);
  expect(image.height, greaterThan(0));
  return image.center.dy - strip.center.dy;
}

void main() {
  for (final (position, name) in [(SWT.TOP, 'top'), (SWT.BOTTOM, 'bottom')]) {
    for (final (text, state) in [('Recipes', 'selected'), ('Notes', 'unselected')]) {
      testWidgets('a $state label on the $name sits in the middle of an uncut strip',
          (tester) async {
        expect(await _labelOffset(tester, tabPosition: position, text: text), closeTo(0, 0.01));
      });

      testWidgets('a $state tab image on the $name sits in the middle of an uncut strip',
          (tester) async {
        expect(await _imageOffset(tester, tabPosition: position, text: text), closeTo(0, 0.01));
      });
    }
  }

  for (final (position, name) in [(SWT.TOP, 'top'), (SWT.BOTTOM, 'bottom')]) {
    for (final tabHeight in [23, 27]) {
      testWidgets('a label on the $name sits as in an uncut tab when tabHeight is $tabHeight',
          (tester) async {
        final uncut = await _labelOffset(tester, tabPosition: position);
        final fixed = await _labelOffset(tester, tabHeight: tabHeight, tabPosition: position);

        expect(fixed, closeTo(uncut, 0.01));
      });
    }
  }
}
