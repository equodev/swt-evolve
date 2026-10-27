// Java places the topRight control from the folder's right edge, leaving room for the folder's own
// buttons. The control is drawn at that position, so a menu Java opens under one of its items with
// toDisplay() appears under the item.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _folderWidth = 344;
const _controlX = 32;
const _controlWidth = 268;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCTabFolder _folder(int alignment) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..selection = 0
  ..bounds = _rect(0, 0, _folderWidth, 200)
  ..minimizeVisible = true
  ..maximizeVisible = true
  ..topRightAlignment = alignment
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 2000
    ..bounds = _rect(_controlX, 1, _controlWidth, 22))
  ..items = [
    for (var i = 0; i < 4; i++)
      VCTabItem()
        ..id = 10 + i
        ..text = 'Tab ${i + 1}',
  ];

Future<Rect> _controlRect(WidgetTester tester, int alignment) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _folderWidth.toDouble(),
        height: 200,
        child: CTabFolderSwt<VCTabFolder>(value: _folder(alignment)),
      ),
    ),
  ));
  while (tester.takeException() != null) {}
  final folderLeft = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>)).left;
  return tester.getRect(find.byType(ToolbarComposite)).shift(Offset(-folderLeft, 0));
}

void main() {
  setUp(() {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = false);
  });
  tearDown(resetConfigFlags);

  for (final (name, alignment) in [
    ('RIGHT', SWT.RIGHT),
    ('RIGHT | WRAP', SWT.RIGHT | SWT.WRAP),
  ]) {
    testWidgets('$name: the control ends where Java placed its right edge', (tester) async {
      final rect = await _controlRect(tester, alignment);

      // The composite has no children here, so only its trailing edge is laid out.
      expect(rect.right, (_controlX + _controlWidth).toDouble());
    });
  }
}
