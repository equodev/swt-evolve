// A CTabFolder sizes each tab's body to its control's bounds. Those bounds reach the client on the
// control's own channel: when the folder is resized, Java lays the selected control out and sends
// that control an update of its own, with nothing new for the folder to rebuild from. The body has
// to follow that update, or the page stays at the size it had before the resize until something
// happens to rebuild the folder - selecting a tab, say.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCanvas _page(VRectangle bounds, {int seq = 0}) => VCanvas()
  ..id = 2001
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = bounds
  ..seq = seq;

VCTabFolder _folder(VCanvas page) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = 0
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Welcome'
      ..control = page,
  ];

// The tab strip trips an unrelated debug-only paint assertion in the isolated harness (a hairline
// border with a non-zero radius); it has no bearing on the body's size.
void _drainPaintExceptions(WidgetTester tester) {
  while (tester.takeException() != null) {}
}

void main() {
  setUp(freshClient);

  testWidgets('the tab body follows its control being resized on its own channel',
      (WidgetTester tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 780,
        height: 560,
        child: CTabFolderSwt<VCTabFolder>(value: _folder(_page(_rect(1, 33, 400, 300)))),
      ),
    ));
    await tester.pumpAndSettle();
    _drainPaintExceptions(tester);

    expect(tester.getSize(find.byType(CanvasSwt<VCanvas>)), const Size(400, 300),
        reason: 'precondition: the page starts at the bounds the folder was built with');

    await deliverWhole(_page(_rect(1, 33, 700, 480), seq: 2));
    await tester.pumpAndSettle();
    _drainPaintExceptions(tester);

    expect(tester.getSize(find.byType(CanvasSwt<VCanvas>)), const Size(700, 480),
        reason: 'the page was resized; its tab body must not keep the size it was built with');
  });
}
