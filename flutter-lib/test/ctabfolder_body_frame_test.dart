// Java hands a CTabFolder's page the client area it computes and nothing else: the page's bounds
// come from `Sizes.getClientArea(DartCTabFolder)`, and everything the application then positions --
// a docked toolbar, a ruler, its own drag feedback -- is measured from there.
//
// So the frame this side draws around the page and the area Java reserves for it are two halves of
// one number. If Java reserves a border this side does not paint, the page floats in a gutter; if
// this side paints edge to edge while Java reserves a border, every page is laid out over the frame
// and lands one border too far left, once per folder in the chain.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// What `CTabFolderSizes` carries, and therefore what Java reserves. Pinned here rather than read
// back from this side's own constants: a renderer change that moves the frame has to fail
// something, and a test that asks the renderer what it draws moves with it. One of these going red
// means re-running `tool/measure_ctabfolder.dart`, not editing the number.
const double _stripHeight = 32;
const double _bodyBorder = 2;
const double _bodyBorderStyled = 3;

const double _folderWidth = 600;
const double _folderHeight = 400;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCTabFolder _folder({int style = SWT.NONE, int tabPosition = SWT.TOP}) {
  final page = VComposite()
    ..id = 20
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, _folderWidth.toInt(), _folderHeight.toInt())
    ..children = [];
  return VCTabFolder()
    ..id = 10
    ..style = style
    ..enabled = true
    ..visible = true
    ..tabPosition = tabPosition
    ..selection = 0
    ..items = [
      VCTabItem()
        ..id = 11
        ..text = 'Section'
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

/// The page area, in the folder's own coordinates.
Rect _bodyIn(WidgetTester tester) {
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  final body = tester.getRect(find.byType(IndexedStack).first);
  return body.translate(-folder.left, -folder.top);
}

void main() {
  testWidgets('the page starts one frame in from the folder\'s left edge', (tester) async {
    await _pump(tester, _folder());

    expect(_bodyIn(tester).left, _bodyBorder,
        reason: 'Java lays the page out at this x; painting the body at 0 would put the page '
            'under the frame and everything it positions one border too far left');
  });

  testWidgets('both sides and the bottom carry the same frame', (tester) async {
    await _pump(tester, _folder());
    final body = _bodyIn(tester);

    expect(_folderWidth - body.right, _bodyBorder);
    expect(_folderHeight - body.bottom, _bodyBorder);
  });

  testWidgets('the tab strip takes the top, not the frame', (tester) async {
    await _pump(tester, _folder());

    expect(_bodyIn(tester).top, _stripHeight,
        reason: 'the strip is what separates the page from the top edge; adding the frame there '
            'too would push every page further down than Java reserved for it');
  });

  testWidgets('SWT.BORDER widens the frame, matching what Java reserves', (tester) async {
    await _pump(tester, _folder(style: SWT.BORDER));

    expect(_bodyBorderStyled, greaterThan(_bodyBorder));
    final body = _bodyIn(tester);
    expect(body.left, _bodyBorderStyled);
    expect(_folderWidth - body.right, _bodyBorderStyled);
  });

  testWidgets('tabs on the bottom move the strip, not the side frames', (tester) async {
    await _pump(tester, _folder(tabPosition: SWT.BOTTOM));
    final body = _bodyIn(tester);

    expect(body.left, _bodyBorder);
    expect(_folderWidth - body.right, _bodyBorder);
    expect(body.top, _bodyBorder);
    expect(_folderHeight - body.bottom, _stripHeight);
  });
}
