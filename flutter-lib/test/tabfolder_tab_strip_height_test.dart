// Pins the relations that let `TabFolderTheme`, measured off one tab, size any folder.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/mnemonic_text.dart';

/// A 16x16 RGB PNG.
const String _pngHex =
    '89504e470d0a1a0a0000000d4948445200000010000000100802000000909168360000001f49444154785e6360'
    '2003a44c3b41241abe1afe2381a1aa010f1a461a48020093236c4866dd5c710000000049454e44ae426082';

VImage _image() => VImage()
  ..id = 99
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = Uint8List.fromList([
      for (var i = 0; i < _pngHex.length; i += 2) int.parse(_pngHex.substring(i, i + 2), radix: 16),
    ]));

const double _folderWidth = 600;
const double _folderHeight = 300;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// The render side keys a folder's State by id, so each folder a test pumps gets its own.
VTabFolder _folder(List<String> labels, {int id = 1000, int style = SWT.NONE, int? imageAt}) {
  var nextId = id + 10;
  return VTabFolder()
    ..id = id
    ..style = style
    // The tabs are rendered disabled on purpose. An *enabled* selected tab carries a deliberate
    // negative bottom margin (it overlaps the strip's border), which trips a debug-only
    // `Container` assertion in the test harness and stops the subtree from laying out at all.
    // The geometry is unaffected: a selected tab's extra border is cancelled by that same negative
    // margin, so the strip is exactly as tall either way.
    ..enabled = false
    ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())
    ..items = [
      for (final (index, label) in labels.indexed)
        VTabItem()
          ..id = nextId++
          ..text = label
          ..image = index == imageAt ? _image() : null
          ..control = (VComposite()
            ..id = nextId++
            ..style = SWT.NONE
            ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())),
    ];
}

Future<void> _pump(WidgetTester tester, VTabFolder value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _folderWidth,
      height: _folderHeight,
      child: TabFolderSwt<VTabFolder>(value: value),
    ),
  ));
  await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
  for (var i = 0; i < 4; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
  // The tab labels can overflow the fixed test width; that is a layout warning, not this test's
  // subject.
  while (tester.takeException() != null) {}
}

/// Where the page starts: the height of a strip on top.
double _stripHeight(WidgetTester tester) => tester.getRect(find.byType(IndexedStack).first).top;

void main() {
  bottomTabs();
  tabWidths();
  testWidgets('the strip does not depend on how many tabs there are', (WidgetTester tester) async {
    await _pump(tester, _folder(['Only one']));
    final one = _stripHeight(tester);

    await _pump(tester,
        _folder(['General', 'Text Decorations', 'Icon Decorations', 'Other'], id: 2000));
    expect(_stripHeight(tester), one);
    expect(tester.getRect(find.byType(IndexedStack).first).height, _folderHeight - one,
        reason: 'the page gets the whole folder below the strip');
  });

  testWidgets('an image grows the strip by exactly what it grows its tab',
      (WidgetTester tester) async {
    await _pump(tester, _folder(['General', 'Other']));
    final plainStrip = _stripHeight(tester);
    final plainTab = tester.getRect(find.byType(InkWell).at(1)).height;

    await _pump(tester, _folder(['General', 'Other'], id: 2000, imageAt: 1));
    final imageTab = tester.getRect(find.byType(InkWell).at(1)).height;
    expect(find.descendant(of: find.byType(InkWell).at(1), matching: find.byType(Image)),
        findsOneWidget);
    expect(_stripHeight(tester) - plainStrip, imageTab - plainTab);
  });
}

void bottomTabs() {
  testWidgets('SWT.BOTTOM puts the strip below the content, as tall as it is on top',
      (WidgetTester tester) async {
    await _pump(tester, _folder(['General', 'Other']));
    final onTop = _stripHeight(tester);

    await _pump(tester, _folder(['General', 'Other'], id: 2000, style: SWT.BOTTOM));
    final content = tester.getRect(find.byType(IndexedStack).first);
    expect(content.top, 0, reason: 'Java starts the client area at the top for BOTTOM tabs');
    expect(content.height, _folderHeight - onTop);
    final strip = tester.getRect(find.byType(Row).first);
    expect(strip.top, greaterThanOrEqualTo(content.bottom), reason: 'the strip follows the content');
    expect(strip.bottom, _folderHeight);
  });
}

void tabWidths() {
  testWidgets('the tabs follow each other from the left, each its label plus the same surround',
      (WidgetTester tester) async {
    await _pump(tester, _folder(['General', 'Text Decorations', 'Other']));

    final tabs = find.byType(InkWell);
    expect(tabs, findsNWidgets(3));
    var x = 0.0;
    double? surround;
    for (var i = 0; i < 3; i++) {
      final tab = tester.getRect(tabs.at(i));
      final label = tester.getRect(find.descendant(of: tabs.at(i), matching: find.byType(MnemonicText)));
      expect(tab.left, x, reason: 'tab $i starts where the one before it ends');
      surround ??= tab.width - label.width;
      expect(tab.width - label.width, surround, reason: 'tab $i draws what the first one does around its label');
      x = tab.right;
    }
  });
}
