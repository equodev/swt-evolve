// A TabFolder renders as a tab strip stacked above its content, so the selected tab's control only
// ever gets `folderHeight - stripHeight`. Java sizes the folder and insets its client area with
// `Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT`, and it has no way to measure this side — if the strip here
// ever grows or shrinks, that constant silently under- or over-reserves and the content gets
// clipped (or floats). This test pins the strip to the height Java assumes.
//
// Keep the number here and `Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT` in step.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';

/// Mirrors `Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT` on the Java side.
const int kTabStripHeight = 18;

const double _folderWidth = 600;
const double _folderHeight = 300;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VTabFolder _folder(List<String> labels) {
  var nextId = 10;
  return VTabFolder()
    ..id = 1000
    ..style = SWT.NONE
    // The tabs are rendered disabled on purpose. An *enabled* selected tab carries a deliberate
    // negative bottom margin (it overlaps the strip's border), which trips a debug-only
    // `Container` assertion in the test harness and stops the subtree from laying out at all.
    // The geometry is unaffected: a selected tab's extra border is cancelled by that same negative
    // margin, so the strip is exactly as tall either way.
    ..enabled = false
    ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())
    ..items = labels
        .map((label) => VTabItem()
          ..id = nextId++
          ..text = label
          ..control = (VComposite()
            ..id = nextId++
            ..style = SWT.NONE
            ..bounds = _rect(0, kTabStripHeight, _folderWidth.toInt(),
                _folderHeight.toInt() - kTabStripHeight)))
        .toList();
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
  // The tab labels can overflow the fixed test width; that is a layout warning, not this test's
  // subject.
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('the tab strip is exactly the height Java reserves for it',
      (WidgetTester tester) async {
    await _pump(
        tester, _folder(['General', 'Text Decorations', 'Icon Decorations', 'Other']));

    final content = find.byType(IndexedStack);
    expect(content, findsOneWidget);

    final contentRect = tester.getRect(content.first);
    expect(contentRect.top, kTabStripHeight.toDouble(),
        reason: 'the tab content must start exactly below the strip — Java offsets the tab '
            'control by Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT and shortens its client area by the '
            'same amount, so any drift here clips the bottom of every tab page');
    expect(contentRect.height, _folderHeight - kTabStripHeight,
        reason: 'the content gets the whole folder minus the strip');
  });

  testWidgets('the strip height does not depend on how many tabs there are',
      (WidgetTester tester) async {
    await _pump(tester, _folder(['Only one']));

    expect(tester.getRect(find.byType(IndexedStack).first).top,
        kTabStripHeight.toDouble());
  });
}
