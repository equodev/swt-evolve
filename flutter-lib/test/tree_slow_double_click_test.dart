// Two clicks on a tree row pair into a double-click by when they happened, not by when each was
// handled: a frame that runs long between the two presses must not split them.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'Leaf 0.0',
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

/// Clicks 'Leaf 0.0' twice, the presses [apart] by their own clock, handled [handledApart].
Future<List<String>> _clickTwice(WidgetTester tester, Duration apart, Duration handledApart) async {
  setConfigFlags(ConfigFlags()..double_click_timeout_ms = 500);
  final sent = <String>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.contains('/Selection/')) sent.add(action.substring(action.indexOf('/Selection/') + 1));
  };
  var handled = DateTime(2020);
  DoubleTapDetector.clock = () => handled;
  addTearDown(() {
    EquoCommBase.sendObserverForTesting = null;
    DoubleTapDetector.clock = DateTime.now;
    resetConfigFlags();
  });
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: _tree())),
    ),
  ));
  await tester.pump();
  final at = tester.getCenter(find.text('Leaf 0.0'));
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.down(at, timeStamp: const Duration(seconds: 10));
  await mouse.up(timeStamp: const Duration(seconds: 10, milliseconds: 60));
  await tester.pump();
  handled = handled.add(handledApart);
  await mouse.down(at, timeStamp: const Duration(seconds: 10) + apart);
  await mouse.up(timeStamp: const Duration(seconds: 10, milliseconds: 60) + apart);
  await tester.pump();
  // A later test in the same isolate starts with no mouse over it.
  await mouse.removePointer();
  return sent;
}

void main() {
  testWidgets('two presses 400ms apart are a double-click though handled 700ms apart', (tester) async {
    final sent = await _clickTwice(
        tester, const Duration(milliseconds: 400), const Duration(milliseconds: 700));

    expect(sent, contains('Selection/DefaultSelection'));
  });

  testWidgets('two presses 600ms apart are two clicks though handled 100ms apart', (tester) async {
    final sent = await _clickTwice(
        tester, const Duration(milliseconds: 600), const Duration(milliseconds: 100));

    expect(sent, isNot(contains('Selection/DefaultSelection')));
  });
}
