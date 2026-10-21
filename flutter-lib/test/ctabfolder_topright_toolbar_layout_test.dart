// A workbench puts the selected part's tool bar in its stack's topRight control. That control sits in
// a row that lays its children out at their natural width, and the tool bar's own row fills its
// parent: left unbounded, the tab strip cannot be laid out, and the selected page below it is not
// laid out or painted either.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _folderWidth = 400;
const _toolBarWidth = 46;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCTabFolder _folder() => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..selection = 0
  ..bounds = _rect(0, 0, _folderWidth, 300)
  ..topRightAlignment = SWT.RIGHT
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 2000
    ..visible = true
    ..bounds = _rect(_folderWidth - _toolBarWidth - 4, 2, _toolBarWidth, 22)
    ..children = [
      VToolBar()
        ..id = 2001
        ..style = SWT.FLAT
        ..visible = true
        ..bounds = _rect(0, 0, _toolBarWidth, 22)
        ..items = [
          VToolItem()
            ..id = 2002
            ..style = SWT.PUSH
            ..text = 'R',
        ],
    ])
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Master data'
      ..control = (VLabel()
        ..id = 3000
        ..visible = true
        ..text = 'Page body'
        ..bounds = _rect(3, 32, _folderWidth - 6, 260)),
  ];

void main() {
  setUp(() {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = false);
  });
  tearDown(resetConfigFlags);

  testWidgets('a tool bar in the topRight control leaves the strip and the page laid out',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _folderWidth.toDouble(),
          height: 300,
          child: CTabFolderSwt<VCTabFolder>(value: _folder()),
        ),
      ),
    ));

    expect(tester.takeException(), isNull);
    expect(tester.getSize(find.byType(ToolbarComposite)).width, _toolBarWidth.toDouble());
    expect(find.text('Page body'), findsOneWidget);
    expect(tester.getSize(find.text('Page body')).isEmpty, isFalse);
  });
}
