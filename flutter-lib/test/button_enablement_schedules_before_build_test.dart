// A button's enablement decides whether it can take focus, and a focus node whose properties change
// tells the Focus widgets that depend on it in the next frame. A build scheduled while a frame is
// building makes the build owner re-sort everything waiting to rebuild, once per such build, which
// with many buttons outweighs the rebuild itself: everything an enablement change rebuilds has to be
// scheduled before the frame starts building. Nor is the change a focus change to report to Java,
// although a Focus widget calls its onFocusChange for it.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VButton _button(int i, int style, {required bool enabled, int seq = 10}) => VButton()
  ..id = 10 + i
  ..style = style
  ..seq = seq
  ..text = 'Button $i'
  ..enabled = enabled
  ..bounds = (VRectangle()
    ..x = 0
    ..y = i * 30
    ..width = 100
    ..height = 26);

const _styles = [SWT.PUSH, SWT.TOGGLE, SWT.CHECK, SWT.RADIO, SWT.ARROW];

Future<void> _mount(WidgetTester tester) async {
  freshClient();
  final root = VComposite()
    ..id = 1
    ..style = SWT.NONE
    ..seq = 1
    ..bounds = (VRectangle()
      ..width = 200
      ..height = 200)
    ..children = [for (var i = 0; i < _styles.length; i++) _button(i, _styles[i], enabled: true)];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 200, height: 200, child: CompositeSwt<VComposite>(value: root)),
  ));
  await tester.pumpAndSettle();
}

/// Disables every button, then enables them again.
Future<void> _toggle(WidgetTester tester) async {
  for (final (seq, enabled) in [(11, false), (12, true)]) {
    for (var i = 0; i < _styles.length; i++) {
      await deliverChange(_button(i, _styles[i], enabled: enabled, seq: seq), keys: [WireKey.k_Control_enabled], base: seq - 1);
    }
    await tester.pumpAndSettle();
  }
}

void main() {
  testWidgets('an enablement change schedules no build during a build', (tester) async {
    await _mount(tester);

    final duringBuild = <String>[];
    final print = debugPrint;
    debugPrint = (String? message, {int? wrapWidth}) {
      if (message != null &&
          message.startsWith('scheduleBuildFor()') &&
          WidgetsBinding.instance.buildOwner!.debugBuilding) {
        duringBuild.add(message);
      }
    };
    debugPrintScheduleBuildForStacks = true;
    try {
      await _toggle(tester);
    } finally {
      debugPrintScheduleBuildForStacks = false;
      debugPrint = print;
    }

    expect(duringBuild, isEmpty);
  });

  testWidgets('an enablement change reports no focus change', (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await _mount(tester);
    EquoCommService.commForTesting.clearSentFrames();

    await _toggle(tester);

    final focusEvents = EquoCommService.commForTesting.sentFrames
        .map((frame) => EquoCommBase.decodeFrame(frame).$1)
        .where((channel) => channel.contains('/Focus/'));
    expect(focusEvents, isEmpty);
  });
}
