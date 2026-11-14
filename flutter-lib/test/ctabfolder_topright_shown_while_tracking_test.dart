// The workbench refuses a Tracker drop over a folder's topRight tool bar, which native SWT always
// draws: the auto-hidden block is drawn while a Tracker is open, though a held button blocks hover.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'support/channels.dart' as channels;

const _shellId = 8101;
const _trackerId = 4301;
const _folderWidth = 400;
const _tabHeight = 24;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

// A navigator stack: two tabs, the view's tool bar in the tab row beside the min/max buttons.
VCTabFolder _folder() => VCTabFolder()
  ..id = 1000
  ..style = SWT.BORDER
  ..tabHeight = _tabHeight
  ..selection = 0
  ..bounds = _rect(0, 0, _folderWidth, 300)
  ..minimizeVisible = true
  ..maximizeVisible = true
  ..topRightAlignment = SWT.RIGHT
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 2000
    ..visible = true
    ..bounds = _rect(276, 2, 69, 22)
    ..children = [
      VToolBar()
        ..id = 2100
        ..style = SWT.FLAT | SWT.RIGHT
        ..visible = true
        ..bounds = _rect(0, 0, 46, 22)
        ..items = [
          VToolItem()
            ..id = 2101
            ..style = SWT.PUSH
            ..text = 'T',
        ],
    ])
  ..items = [
    for (final (i, name) in ['Connections', 'Projects'].indexed)
      VCTabItem()
        ..id = 10 + i
        ..text = name
        ..showing = true,
  ];

void _trackerOp(String op, Map<String, dynamic> body) =>
    EquoCommService.commForTesting.receiveBinary(
        channels.frame('Shell/$_shellId/Tracker/$op', TaggedValues.encode(body)));

double _toolBarOpacity(WidgetTester tester) => tester
    .widget<AnimatedOpacity>(
        find.ancestor(of: find.text('T'), matching: find.byType(AnimatedOpacity)).first)
    .opacity;

void main() {
  setUp(() {
    resetConfigFlags();
    TrackerSession.sendForTesting = (_, __, ___) {};
  });
  tearDown(() {
    resetConfigFlags();
    TrackerSession.sendForTesting = null;
  });

  testWidgets('the topRight tool bar is drawn while a Tracker drags over the tab row',
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
    await tester.pump();
    while (tester.takeException() != null) {}
    TrackerSession.attachHost('Shell', _shellId);

    final origin = tester.getTopLeft(find.byType(CTabFolderSwt<VCTabFolder>));
    final overToolBar = origin + const Offset(300, 12);
    final gesture =
        await tester.startGesture(origin + const Offset(200, 200), kind: PointerDeviceKind.mouse);
    await gesture.moveTo(overToolBar);
    await tester.pump(const Duration(seconds: 1));
    expect(_toolBarOpacity(tester), 0.0, reason: 'a held button reveals nothing on its own');

    _trackerOp('open', {'itemId': _trackerId});
    await gesture.moveBy(const Offset(2, 0));
    await tester.pump(const Duration(seconds: 1));

    expect(TrackerSession.isTracking, isTrue);
    expect(_toolBarOpacity(tester), 1.0,
        reason: 'Java refuses a drop over this tool bar, so it must be visible during the drag');
    expect(find.text('T').hitTestable(), findsOneWidget);

    await gesture.up();
    await gesture.moveTo(origin + const Offset(200, 200));
    await tester.pump(const Duration(seconds: 1));

    expect(TrackerSession.isTracking, isFalse);
    expect(_toolBarOpacity(tester), 0.0, reason: 'once the drag ends it hides until hovered');

    await gesture.removePointer();
    TrackerSession.detachHost(_shellId);
  });
}
