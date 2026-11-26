// A workbench's part stacks are a MainComposite's panels: a panel is laid out at the bounds Java
// gives it and fitted into its slot. Resizing one, as a sash drag does on every step, lays it out
// again without building it again, and it ends where a fresh mount at the new bounds draws it. A
// hidden control, which draws nothing at any size, is not built again for new bounds either.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VCTabFolder _folder(VRectangle bounds, {int seq = 10, int layoutSeq = 7, bool visible = true}) => VCTabFolder()
  ..swt = 'CTabFolder'
  ..id = 601
  ..seq = seq
  ..style = SWT.NONE
  ..enabled = true
  ..visible = visible
  ..selection = 0
  ..stripLayoutSeq = layoutSeq
  ..bounds = bounds
  ..items = List.generate(
    2,
    (i) => VCTabItem()
      ..id = 700 + i
      ..text = 'Part ${i + 1}'
      ..showing = true,
  );

Future<void> _mount(WidgetTester tester, VCTabFolder folder) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..swt = 'Composite'
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 700, 400)
    ..children = [folder];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 700, height: 400, child: MainComposite(value: window)),
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

List<Rect> _geometry(WidgetTester tester) => [
      tester.getRect(find.byWidgetPredicate((w) => w is CTabFolderSwt)),
      for (var i = 1; i <= 2; i++) tester.getRect(find.text('Part $i')),
    ];

void main() {
  testWidgets('a part stack resized in its panel is laid out again, not built again', (tester) async {
    await _mount(tester, _folder(_rect(0, 0, 300, 200)));
    final builds = _countFolderBuilds();

    await deliverChange(_folder(_rect(0, 0, 420, 260), seq: 11, layoutSeq: 8),
        keys: [WireKey.k_Control_bounds, WireKey.k_CTabFolder_stripLayoutSeq], base: 10);
    await tester.pumpAndSettle();
    debugOnRebuildDirtyWidget = null;

    expect(builds(), 0);
    final resized = _geometry(tester);

    await _mount(tester, _folder(_rect(0, 0, 420, 260), layoutSeq: 8));
    expect(resized, _geometry(tester), reason: 'where a part stack mounted at those bounds is drawn');
  });

  testWidgets('a hidden control given new bounds is not built again', (tester) async {
    await _mount(tester, _folder(_rect(0, 0, 300, 200), visible: false));
    final builds = _countFolderBuilds();

    await deliverChange(_folder(_rect(10, 10, 420, 260), seq: 11, visible: false),
        keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pumpAndSettle();

    expect(builds(), 0);
  });

  testWidgets('a hidden control shown again is drawn at its newest bounds', (tester) async {
    await _mount(tester, _folder(_rect(0, 0, 300, 200), visible: false));
    await deliverChange(_folder(_rect(0, 0, 420, 260), seq: 11, visible: false),
        keys: [WireKey.k_Control_bounds], base: 10);
    await tester.pumpAndSettle();
    await deliverChange(_folder(_rect(0, 0, 420, 260), seq: 12), keys: [WireKey.k_Control_visible], base: 11);
    await tester.pumpAndSettle();
    final shown = _geometry(tester);

    await _mount(tester, _folder(_rect(0, 0, 420, 260)));
    expect(shown, _geometry(tester));
  });
}
