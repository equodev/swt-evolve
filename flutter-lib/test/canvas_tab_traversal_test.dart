// A Canvas that takes keys keeps Tab unless its Traverse listener sets doit; whether the focus then
// leaves is Java's verdict, which the Display's traversal scope waits for. The Canvas must leave Tab
// to that scope rather than consume it.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireType;
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/impl/utils/traversal_veto_scope.dart';
import 'package:swtflutter/src/impl/utils/veto_gate.dart';

import 'support/channels.dart' as channels;

const int _displayId = 901;

class _SilentCanvasSwt extends CanvasSwt<VCanvas> {
  const _SilentCanvasSwt({required super.value});

  @override
  void sendEvent(VCanvas val, int channel, VEvent? payload) {}
}

VCanvas _canvas() => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..tabStop = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 100
    ..height = 50);

void _deliver(String actionId, Object payload) {
  EquoCommService.commForTesting.receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
}

void main() {
  testWidgets('Tab on a focused Canvas moves on once Java allows the traversal', (tester) async {
    final gate = VetoGate.traverse()..attach(WireType.display, _displayId);
    addTearDown(gate.detach);
    final next = FocusNode(debugLabel: 'next');
    addTearDown(next.dispose);
    startDisplayKeyForwarding();
    addTearDown(stopDisplayKeyForwarding);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: TraversalVetoScope(
        gate: gate,
        child: Column(children: [
          SizedBox(width: 100, height: 50, child: _SilentCanvasSwt(value: _canvas())),
          Focus(focusNode: next, child: const SizedBox(width: 10, height: 10)),
        ]),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tapAt(tester.getCenter(find.byType(_SilentCanvasSwt)));
    await tester.pumpAndSettle();
    expect(next.hasFocus, isFalse);

    _deliver('Display/$_displayId/traverse/vetoable', {'value': true});
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    _deliver('Display/$_displayId/traverse/verdict', {'doit': true});
    await tester.pump();

    expect(next.hasFocus, isTrue, reason: 'the Canvas consumed Tab, so the allowed traversal never ran');
  });
}
