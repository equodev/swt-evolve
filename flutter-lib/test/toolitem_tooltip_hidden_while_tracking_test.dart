// Native SWT opens no tooltip while a Tracker holds the pointer: a tab dragged over a tool item
// shows none, and the item's tooltip opens again once the drag ends.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/custom/main_toolbar_scope.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/tracker_session.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'support/channels.dart' as channels;

const _shellId = 8102;
const _trackerId = 4302;
const _tip = 'Link with editor';

VToolBar _toolBar() => VToolBar()
  ..id = 1
  ..style = SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 30)
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'L'
      ..toolTipText = _tip,
  ];

void _trackerOp(String op, Map<String, dynamic> body) =>
    EquoCommService.commForTesting.receiveBinary(
        channels.frame('Shell/$_shellId/Tracker/$op', TaggedValues.encode(body)));

Future<void> _pump(WidgetTester tester, {required bool card}) async {
  final Widget bar = SizedBox(width: 120, height: 30, child: ToolBarSwt(value: _toolBar()));
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: card ? MainToolbarScope(child: bar) : bar,
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

Future<void> _tooltipDragOverItem(WidgetTester tester) async {
  TrackerSession.attachHost('Shell', _shellId);
  final item = tester.getCenter(find.text('L'));
  final away = item + const Offset(0, 200);

  final gesture = await tester.startGesture(away, kind: PointerDeviceKind.mouse);
  _trackerOp('open', {'itemId': _trackerId});
  await gesture.moveTo(item);
  await tester.pump(const Duration(seconds: 2));
  await tester.pump();

  expect(TrackerSession.isTracking, isTrue);
  expect(find.text(_tip), findsNothing, reason: 'no tooltip opens while a Tracker holds the pointer');

  await gesture.up();
  await gesture.moveTo(away);
  await tester.pump(const Duration(seconds: 1));
  expect(TrackerSession.isTracking, isFalse);

  await gesture.moveTo(item);
  await tester.pump(const Duration(seconds: 2));
  await tester.pump();
  expect(find.text(_tip), findsOneWidget, reason: 'the tooltip opens again once the drag ends');

  await gesture.moveTo(away);
  await tester.pumpAndSettle();
  await gesture.removePointer();
  TrackerSession.detachHost(_shellId);
}

void main() {
  setUp(() {
    resetConfigFlags();
    TrackerSession.sendForTesting = (_, __, ___) {};
  });
  tearDown(() {
    resetConfigFlags();
    TrackerSession.sendForTesting = null;
  });

  testWidgets('a tool item opens no tooltip while a Tracker drags over it', (tester) async {
    await _pump(tester, card: false);
    await _tooltipDragOverItem(tester);
  });

  testWidgets('a main-toolbar card opens no tooltip while a Tracker drags over it',
      (tester) async {
    setConfigFlags(ConfigFlags()..custom_tooltip = true);
    await _pump(tester, card: true);
    await _tooltipDragOverItem(tester);
  });
}
