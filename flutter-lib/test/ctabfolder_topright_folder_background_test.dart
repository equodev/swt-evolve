// A workbench stack puts the view tool bar in the CTabFolder's topRight slot. A folder with no
// background of its own gives that control none either, only backgroundMode INHERIT_DEFAULT, so
// the control, its ToolBars and their items must paint nothing that differs from the themed row.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/color.dart';
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
import 'package:swtflutter/src/theme/theme_extensions/ctabfolder_theme_extension.dart';

const _folderWidth = 500;
const _folderHeight = 300;
const _tabHeight = 24;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VToolItem _item(int id, int style) => VToolItem()
  ..id = id
  ..style = style
  ..text = 'T$id';

VToolBar _bar(int id, int x, int width, List<VToolItem> items) => VToolBar()
  ..id = id
  ..style = SWT.FLAT | SWT.RIGHT | SWT.WRAP | SWT.HORIZONTAL
  ..visible = true
  ..bounds = _rect(x, 0, width, _tabHeight - 2)
  ..items = items;

VCTabFolder _folder() => VCTabFolder()
  ..id = 1000
  ..style = SWT.BORDER
  ..tabHeight = _tabHeight
  ..selection = 0
  ..selectionBackground = VColor.fromArgb(0xFFFFFFFF)
  ..bounds = _rect(0, 0, _folderWidth, _folderHeight)
  ..minimizeVisible = true
  ..maximizeVisible = true
  ..topRightAlignment = SWT.RIGHT | SWT.WRAP
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 2000
    ..visible = true
    ..backgroundMode = SWT.INHERIT_DEFAULT
    ..bounds = _rect(343, 1, 115, _tabHeight - 2)
    ..children = [
      _bar(2100, 0, 92, [
        _item(2101, SWT.PUSH),
        _item(2102, SWT.PUSH),
        _item(2103, SWT.PUSH),
        _item(2104, SWT.CHECK),
      ]),
      _bar(2200, 92, 23, [_item(2201, SWT.PUSH)]),
    ])
  ..items = [
    VCTabItem()
      ..id = 10
      ..text = 'Patienten'
      ..showClose = true
      ..showing = true
      ..control = (VLabel()
        ..id = 3000
        ..visible = true
        ..text = 'PAGE'
        ..bounds = _rect(1, _tabHeight + 1, _folderWidth - 2, _folderHeight - _tabHeight - 2)),
  ];

Color? _paint(Widget w) {
  if (w is Container) {
    final d = w.decoration;
    return w.color ?? (d is BoxDecoration ? d.color : null);
  }
  if (w is DecoratedBox) {
    final d = w.decoration;
    return d is BoxDecoration ? d.color : null;
  }
  if (w is ColoredBox) return w.color;
  return null;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('the topRight view tool bar paints no fill other than the tab row\'s',
      (tester) async {
    // The flags the workbench application ships with: application colours are honoured.
    setConfigFlags(ConfigFlags()
      ..use_swt_colors = true
      ..use_swt_border = true);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _folderWidth.toDouble(),
          height: _folderHeight.toDouble(),
          child: CTabFolderSwt<VCTabFolder>(value: _folder()),
        ),
      ),
    ));
    await tester.pump();
    await tester.pump();
    while (tester.takeException() != null) {}

    expect(find.text('T2101'), findsOneWidget);
    final topRight = find.byWidgetPredicate((w) => w is ToolbarComposite && w.value.id == 2000);
    expect(topRight, findsOneWidget);
    final row = Theme.of(tester.element(topRight))
        .extension<CTabFolderThemeExtension>()!
        .tabBarBackgroundColor
        .toARGB32();

    final fills = <String>{};
    for (final element in find
        .descendant(of: topRight, matching: find.byWidgetPredicate((w) => _paint(w) != null))
        .evaluate()) {
      final color = _paint(element.widget)!;
      if (color.a == 0 || color.toARGB32() == row) continue;
      fills.add('${element.widget.runtimeType} 0x${color.toARGB32().toRadixString(16)}');
    }
    expect(fills, isEmpty,
        reason: 'the topRight composite, its ToolBars and items paint a slab over the tab row '
            '0x${row.toRadixString(16)}');
  });
}
