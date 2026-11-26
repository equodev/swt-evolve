// Upstream builds a CTabFolder's minimize, maximize and chevron buttons as ToolBars among its
// children and moves them on every layout of the folder: a sash drag beside it does it on every
// step. This folder draws those buttons itself and never those bars, so a bar moving is no change to
// it. Another child it holds is drawn at its bounds, and moves with them.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VToolBar _bar(int x, {int seq = 10}) => VToolBar()
  ..swt = 'ToolBar'
  ..id = 650
  ..seq = seq
  ..style = SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = _rect(x, 2, 50, 24);

VComposite _other(int x, {int seq = 10}) => VComposite()
  ..swt = 'Composite'
  ..id = 660
  ..seq = seq
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(x, 2, 20, 20);

VCTabFolder _folder() => VCTabFolder()
  ..swt = 'CTabFolder'
  ..id = 601
  ..seq = 10
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = 0
  ..minimizeVisible = true
  ..maximizeVisible = true
  ..bounds = _rect(0, 0, 500, 200)
  ..children = [_bar(440), _other(400)]
  ..items = List.generate(
    2,
    (i) => VCTabItem()
      ..id = 700 + i
      ..text = 'Part ${i + 1}'
      ..showing = true,
  );

Future<void> _mount(WidgetTester tester, {bool fresh = true, bool padded = false}) async {
  if (fresh) {
    await tester.pumpWidget(const SizedBox());
    freshClient();
  }
  final window = VComposite()
    ..swt = 'Composite'
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 600, 300)
    ..children = [_folder()];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 600,
        height: 300,
        // Another widget above it builds everything below anew.
        child: padded ? Padding(padding: EdgeInsets.zero, child: CompositeSwt<VComposite>(value: window))
            : CompositeSwt<VComposite>(value: window),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

int Function() _countFolderBuilds() {
  var builds = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is CTabFolderSwt) builds++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return () => builds;
}

void main() {
  testWidgets('one of its own button bars moving does not build the folder again', (tester) async {
    await _mount(tester);
    expect(find.byWidgetPredicate((w) => w is ToolBarSwt), findsNothing, reason: 'the folder never draws its bars');
    final builds = _countFolderBuilds();

    for (var step = 1; step <= 3; step++) {
      await deliverChange(_bar(440 - 6 * step, seq: 10 + step), keys: [WireKey.k_Control_bounds], base: 9 + step);
      await tester.pump();
    }

    expect(builds(), 0);
  });

  testWidgets('a folder drawn again elsewhere still ignores its bars', (tester) async {
    await _mount(tester);
    // Drawn anew in one frame: the folder's new state starts before the old one is disposed.
    await _mount(tester, fresh: false, padded: true);
    final builds = _countFolderBuilds();

    await deliverChange(_bar(430, seq: 11), keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pump();

    expect(builds(), 0);
  });

  testWidgets('another child it holds is drawn, and follows its bounds', (tester) async {
    await _mount(tester);
    final other = find.byWidgetPredicate((w) => w is CompositeSwt && w.value.id == 660);
    expect(tester.getTopLeft(other).dx, 400);

    await deliverChange(_other(390, seq: 11), keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pump();

    expect(tester.getTopLeft(other).dx, 390);
  });
}
