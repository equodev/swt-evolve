// A workbench stack puts the selected part's tool bar and its view menu in the folder's topRight
// control (SWT.RIGHT | SWT.WRAP). With the tool bar too wide for the tab row, Java moves the control
// to a row of its own beside a strip of fixed height and lays the page out past that row. The tool
// bar is drawn there, at Java's bounds, without hovering, and the tabs keep the fixed height.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
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

// A Properties view stack as a workbench lays it out: the tool bar (5 items) and the view menu
// wrapped beside the tab row, the page past them, in a folder that fits the test surface.
const _folderWidth = 519;
const _folderHeight = 560;
const _rowHeight = 22;
const _tabNames = ['Outline', 'Properties', 'Assistant', 'Favorites'];
const _toolItems = ['A', 'B', 'C', 'D', 'E'];

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VToolBar _toolBar(int id, int x, int width, List<String> items) => VToolBar()
  ..id = id
  ..style = SWT.FLAT | SWT.RIGHT
  ..visible = true
  ..bounds = _rect(x, 0, width, _rowHeight)
  ..items = [
    for (var i = 0; i < items.length; i++)
      VToolItem()
        ..id = id + 1 + i
        ..style = SWT.PUSH
        ..text = items[i],
  ];

/// Java's bounds for the wrapped control: right under the tabs, or right over them at the bottom.
VRectangle _topRight(int tabHeight, int tabPosition) => _rect(
      380,
      tabPosition == SWT.BOTTOM ? _folderHeight - tabHeight - _rowHeight : tabHeight,
      138,
      _rowHeight,
    );

VCTabFolder _folder(int tabHeight, int tabPosition) {
  final pageHeight = _folderHeight - tabHeight - _rowHeight - 3;
  return VCTabFolder()
    ..id = 1000
    ..style = SWT.BORDER
    ..tabPosition = tabPosition
    ..tabHeight = tabHeight
    ..selection = 1
    ..bounds = _rect(0, 0, _folderWidth, _folderHeight)
    ..minimizeVisible = true
    ..maximizeVisible = true
    ..chevronVisible = true
    ..topRightAlignment = SWT.RIGHT | SWT.WRAP
    ..topRight = (VComposite()
      ..swt = 'Composite'
      ..id = 2000
      ..visible = true
      ..bounds = _topRight(tabHeight, tabPosition)
      ..children = [
        _toolBar(2100, 0, 115, _toolItems),
        _toolBar(2200, 115, 23, ['M']),
      ])
    ..items = [
      for (var i = 0; i < _tabNames.length; i++)
        VCTabItem()
          ..id = 10 + i
          ..text = _tabNames[i]
          ..showClose = true
          ..showing = true
          ..control = i == 1
              ? (VLabel()
                ..id = 3000
                ..visible = true
                ..text = 'PROPERTY'
                ..bounds = _rect(3, tabPosition == SWT.BOTTOM ? 1 : tabHeight + _rowHeight,
                    _folderWidth - 6, pageHeight))
              : null,
    ];
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  for (final (tabHeight, tabPosition, where) in [
    (40, SWT.TOP, 'under a 40px tab row'),
    (24, SWT.TOP, 'under a 24px tab row'),
    (40, SWT.BOTTOM, 'over a 40px tab row at the bottom'),
  ]) {
    testWidgets('a view tool bar wrapped $where is drawn on its row, at Java bounds',
        (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: _folderWidth.toDouble(),
            height: _folderHeight.toDouble(),
            child: CTabFolderSwt<VCTabFolder>(value: _folder(tabHeight, tabPosition)),
          ),
        ),
      ));
      await tester.pump();
      await tester.pump();
      while (tester.takeException() != null) {}
      final origin = tester.getTopLeft(find.byType(CTabFolderSwt<VCTabFolder>));
      final topRight = _topRight(tabHeight, tabPosition);
      final row = Rect.fromLTWH(topRight.x!.toDouble(), topRight.y!.toDouble(),
          topRight.width!.toDouble(), topRight.height!.toDouble());

      for (final text in [..._toolItems, 'M']) {
        final item = find.text(text);
        expect(item, findsOneWidget, reason: text);
        final rect = tester.getRect(item).shift(-origin);
        expect(
            row.inflate(0.5).contains(rect.topLeft) && row.inflate(0.5).contains(rect.bottomRight),
            isTrue,
            reason: '$text at $rect, outside the wrapped row $row');
        expect(item.hitTestable(), findsOneWidget, reason: '$text is clipped or covered at $rect');
      }

      final page = tester.getRect(find.text('PROPERTY')).shift(-origin);
      if (tabPosition == SWT.BOTTOM) {
        expect(page.bottom, lessThanOrEqualTo(row.top), reason: 'the page ends over the row');
      } else {
        expect(page.top, greaterThanOrEqualTo(row.bottom), reason: 'the page starts under the row');
      }

      final strip = tabPosition == SWT.BOTTOM
          ? Rect.fromLTWH(0, (_folderHeight - tabHeight).toDouble(), _folderWidth.toDouble(),
              tabHeight.toDouble())
          : Rect.fromLTWH(0, 0, _folderWidth.toDouble(), tabHeight.toDouble());
      final tab = tester.getRect(find.text('Outline')).shift(-origin);
      expect(strip.inflate(0.5).contains(tab.center), isTrue,
          reason: 'the tab at $tab, outside the ${tabHeight}px strip $strip');
      expect(find.text('Outline').hitTestable(), findsOneWidget,
          reason: 'the tab is clipped or covered at $tab');
    });
  }
}
