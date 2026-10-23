// Java sends the tab strip height it reserves (absent at the default 32); the page starts below it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

const double _folderWidth = 600;
const double _folderHeight = 400;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCTabFolder _folder({int? tabHeight, int tabPosition = SWT.TOP}) {
  final page = VComposite()
    ..id = 20
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())
    ..children = [];
  return VCTabFolder()
    ..id = 10
    ..style = SWT.BORDER | SWT.SINGLE
    ..enabled = true
    ..visible = true
    ..tabPosition = tabPosition
    ..tabHeight = tabHeight
    ..minimizeVisible = true
    ..maximizeVisible = true
    ..selection = 0
    ..items = [VCTabItem()..id = 11]
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

/// The page area, in the folder's own coordinates.
Rect _bodyIn(WidgetTester tester) {
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  final body = tester.getRect(find.byType(IndexedStack).first);
  return body.translate(-folder.left, -folder.top);
}

void main() {
  testWidgets('the default strip keeps its height', (tester) async {
    await _pump(tester, _folder());

    expect(_bodyIn(tester).top, 32);
  });

  testWidgets('a fixed strip height sizes the strip', (tester) async {
    await _pump(tester, _folder(tabHeight: 9));

    expect(_bodyIn(tester).top, 9,
        reason: 'Java lays the page out 9 px down; a taller strip is the band above the page');
  });

  testWidgets('a strip of 0 gives the page the whole folder', (tester) async {
    await _pump(tester, _folder(tabHeight: 0));

    expect(_bodyIn(tester), const Rect.fromLTWH(0, 0, _folderWidth, _folderHeight));
  });

  testWidgets('a fixed strip height sizes a strip on the bottom', (tester) async {
    await _pump(tester, _folder(tabHeight: 9, tabPosition: SWT.BOTTOM));

    expect(_folderHeight - _bodyIn(tester).bottom, 9);
  });
}
