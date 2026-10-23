// A SWT.WRAP topRight control that does not fit beside the tabs is moved by Java to a row of its
// own under the tab strip, as native SWT does. It is drawn on that row at Java's bounds, so a menu
// Java opens under one of its items appears under the item, and the tab strip keeps its full width.

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

const _folderWidth = 380;
const _folderHeight = 400;
const _tabStripHeight = 32;
const _tabNames = ['Search', 'Layers'];

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCTabFolder _folder(VRectangle topRightBounds, int tabPosition) => VCTabFolder()
  ..id = 1000
  ..style = SWT.NONE
  ..tabPosition = tabPosition
  ..selection = 1
  ..bounds = _rect(0, 0, _folderWidth, _folderHeight)
  ..minimizeVisible = true
  ..maximizeVisible = true
  ..topRightAlignment = SWT.RIGHT | SWT.WRAP
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 2000
    ..bounds = topRightBounds)
  ..items = [
    for (var i = 0; i < _tabNames.length; i++)
      VCTabItem()
        ..id = 10 + i
        ..text = _tabNames[i]
        ..showing = true,
  ];

Future<Offset> _pump(WidgetTester tester, VRectangle topRightBounds,
    {int tabPosition = SWT.TOP}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _folderWidth.toDouble(),
        height: _folderHeight.toDouble(),
        child: CTabFolderSwt<VCTabFolder>(value: _folder(topRightBounds, tabPosition)),
      ),
    ),
  ));
  await tester.pump();
  await tester.pump();
  while (tester.takeException() != null) {}
  return tester.getTopLeft(find.byType(CTabFolderSwt<VCTabFolder>));
}

void main() {
  setUp(() {
    resetConfigFlags();
    setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = false);
  });
  tearDown(resetConfigFlags);

  testWidgets('a wrapped control is drawn on its own row, at Java bounds', (tester) async {
    final origin = await _pump(tester, _rect(106, _tabStripHeight, 268, 22));

    final control = tester.getRect(find.byType(ToolbarComposite)).shift(-origin);
    expect(control, const Rect.fromLTWH(106, 32, 268, 22));
  });

  testWidgets('with the default auto-hide flags, a wrapped control is still drawn on its own row',
      (tester) async {
    resetConfigFlags();
    final origin = await _pump(tester, _rect(106, _tabStripHeight, 268, 22));

    final control = tester.getRect(find.byType(ToolbarComposite)).shift(-origin);
    expect(control, const Rect.fromLTWH(106, 32, 268, 22));
  });

  testWidgets('with tabs at the bottom, a wrapped control is drawn on its own row above them',
      (tester) async {
    const y = _folderHeight - _tabStripHeight - 22;
    final origin = await _pump(tester, _rect(106, y, 268, 22), tabPosition: SWT.BOTTOM);

    final control = tester.getRect(find.byType(ToolbarComposite)).shift(-origin);
    expect(control, const Rect.fromLTWH(106, y * 1.0, 268, 22));
  });

  testWidgets('with the control wrapped, every tab shows its whole title in the strip',
      (tester) async {
    final origin = await _pump(tester, _rect(106, _tabStripHeight, 268, 22));

    for (final name in _tabNames) {
      final label = find.text(name);
      expect(label, findsOneWidget, reason: name);
      final rect = tester.getRect(label).shift(-origin);
      expect(rect.right, lessThanOrEqualTo(_folderWidth.toDouble()), reason: name);
      expect(rect.bottom, lessThanOrEqualTo(_tabStripHeight), reason: name);
    }
    expect(find.byIcon(Icons.expand_more), findsNothing, reason: 'nothing overflows');
  });

  testWidgets('a WRAP control Java leaves in the tab row stays in the tab row', (tester) async {
    final origin = await _pump(tester, _rect(106, 2, 268, 22));

    final control = tester.getRect(find.byType(ToolbarComposite)).shift(-origin);
    expect(control.top, lessThan(_tabStripHeight));
  });
}
