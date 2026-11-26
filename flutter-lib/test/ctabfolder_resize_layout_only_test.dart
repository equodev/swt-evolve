// A CTabFolder resized by Java, which lays its tabs out again and numbers that layout
// (stripLayoutSeq), is laid out again, not rebuilt: a sash drag does it on every step. It ends where
// a fresh mount at the new bounds puts the folder and its tabs, and it reports its strip for the new
// layout, at the new width, as Java waits for.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
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

VCTabFolder _folder(VRectangle bounds, {int seq = 10, int layoutSeq = 7}) => VCTabFolder()
  ..id = 601
  ..seq = seq
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = 0
  ..stripLayoutSeq = layoutSeq
  ..bounds = bounds
  ..items = List.generate(
    3,
    (i) => VCTabItem()
      ..id = 700 + i
      ..text = 'Editor ${i + 1}'
      ..showing = true,
  );

/// Mounts [folder] as the one child of a window-sized Composite, which lays it out by its bounds.
Future<void> _mount(WidgetTester tester, VCTabFolder folder) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 700, 300)
    ..children = [folder];
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 700, height: 300, child: CompositeSwt<VComposite>(value: window)),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// The strip reports sent so far, newest last.
List<String> _reports() => [
      for (final f in EquoCommService.commForTesting.sentFrames)
        if (EquoCommBase.decodeFrame(f).$1.endsWith('stripLaidOut'))
          if (EquoCommBase.decodeEvent(f).text case final String text) text,
    ];

/// The folder's rectangle and each tab label's.
List<Rect> _geometry(WidgetTester tester) => [
      tester.getRect(find.byWidgetPredicate((w) => w is CTabFolderSwt)),
      for (var i = 1; i <= 3; i++) tester.getRect(find.text('Editor $i')),
    ];

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);
  tearDown(() => EquoCommBase.recordSentFrames = false);

  testWidgets('a folder Java resizes and lays out again is laid out, not rebuilt, and reports its strip',
      (tester) async {
    await _mount(tester, _folder(_rect(0, 0, 400, 200)));
    var folderBuilds = 0;
    debugOnRebuildDirtyWidget = (element, _) {
      if (element.widget is CTabFolderSwt) folderBuilds++;
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);
    final before = _reports().length;

    await deliverChange(_folder(_rect(0, 0, 520, 230), seq: 11, layoutSeq: 8),
        keys: [WireKey.k_Control_bounds, WireKey.k_CTabFolder_stripLayoutSeq], base: 10);
    await tester.pumpAndSettle();
    debugOnRebuildDirtyWidget = null;

    expect(folderBuilds, 0);
    final reports = _reports();
    expect(reports.length, greaterThan(before), reason: 'the new layout is reported');
    final sections = reports.last.split(';');
    expect(sections[4], '520', reason: 'drawn at the new width');
    expect(sections[5], '8', reason: 'for the layout Java numbered last');
    final resized = _geometry(tester);

    await _mount(tester, _folder(_rect(0, 0, 520, 230), layoutSeq: 8));
    expect(resized, _geometry(tester), reason: 'where a folder mounted at those bounds lays it all out');
  });

  testWidgets('a new layout at the same size is reported, without a rebuild', (tester) async {
    await _mount(tester, _folder(_rect(0, 0, 400, 200)));
    var folderBuilds = 0;
    debugOnRebuildDirtyWidget = (element, _) {
      if (element.widget is CTabFolderSwt) folderBuilds++;
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);

    await deliverChange(_folder(_rect(0, 0, 400, 200), seq: 11, layoutSeq: 9),
        keys: [WireKey.k_CTabFolder_stripLayoutSeq], base: 10);
    await tester.pumpAndSettle();
    debugOnRebuildDirtyWidget = null;

    expect(folderBuilds, 0);
    expect(_reports().last.split(';')[5], '9');
  });
}
