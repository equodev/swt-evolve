// A desktop engine runs a key's platform messages as soon as the platform delivers them, while a
// pointer event waits in the engine's task queue. A Shift released right after a shift-click then
// reaches Dart ahead of the click. Java must still see the click with Shift held, and the Shift
// release after it, as a native Display reports them.

import 'dart:ui' as ui;

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/key_delivery_order.dart';

const _displayId = 5;

Widget _wrap() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Stack(children: [
        DisplaySwt(value: VDisplay()..id = _displayId),
        SizedBox(
          width: 200,
          height: 60,
          child: LabelSwt(
            value: VLabel()
              ..id = 1
              ..enabled = true
              ..text = 'target'
              ..bounds = (VRectangle()
                ..x = 0
                ..y = 0
                ..width = 200
                ..height = 60),
          ),
        ),
      ]),
    );

/// The key and mouse events sent to Java, in order, each with its stateMask.
List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .map((f) => (EquoCommBase.decodeFrame(f).$1, f))
    .where((f) => f.$1.contains('/Key/Key') || f.$1.contains('/Mouse/Mouse'))
    .map((f) {
      final mask = EquoCommBase.decodeEvent(f.$2).stateMask ?? 0;
      return '${f.$1.split('/').last}:0x${mask.toRadixString(16)}';
    })
    .toList();

/// Delivers a Shift press or release the way a desktop engine does: its key data, then its raw
/// key message, both run as soon as they arrive. The raw message's answer is not awaited, as the
/// engine does not block on it.
void _deliverShift(WidgetTester tester,
    {required bool down, required int ms, void Function(bool handled)? answered}) {
  tester.binding.platformDispatcher.onKeyData!(ui.KeyData(
    type: down ? ui.KeyEventType.down : ui.KeyEventType.up,
    physical: PhysicalKeyboardKey.shiftLeft.usbHidUsage,
    logical: LogicalKeyboardKey.shiftLeft.keyId,
    timeStamp: Duration(milliseconds: ms),
    character: null,
    synthesized: false,
  ));
  tester.binding.defaultBinaryMessenger.handlePlatformMessage(
    SystemChannels.keyEvent.name,
    SystemChannels.keyEvent.codec.encodeMessage(
        KeyEventSimulator.getKeyData(LogicalKeyboardKey.shiftLeft, platform: 'linux', isDown: down)),
    (ByteData? reply) {
      final decoded = SystemChannels.keyEvent.codec.decodeMessage(reply) as Map<String, dynamic>;
      answered?.call(decoded['handled'] as bool);
    },
  );
}

void main() {
  testWidgets('a Shift release that reaches Dart ahead of an earlier click neither clears the '
      "click's Shift nor overtakes it", (tester) async {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await KeyDeliveryOrder.install();
    addTearDown(KeyDeliveryOrder.uninstall);
    final at = tester.getCenter(find.text('target'));
    EquoCommService.commForTesting.clearSentFrames();

    // What happened: Shift down at 1000 ms, press at 1050, release at 1090, Shift up at 1110.
    _deliverShift(tester, down: true, ms: 1000);
    // A zero-duration pump runs the timers due now: the engine's next turn.
    await tester.pump(Duration.zero);
    // What Dart is handed: the Shift release first, then the click the engine had queued.
    _deliverShift(tester, down: false, ms: 1110);
    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.down(at, timeStamp: const Duration(milliseconds: 1050));
    await gesture.up(timeStamp: const Duration(milliseconds: 1090));
    await tester.pump(Duration.zero);

    final shift = SWT.SHIFT.toRadixString(16);
    final shiftButton1 = (SWT.SHIFT | SWT.BUTTON1).toRadixString(16);
    expect(_sent(), [
      'KeyDown:0x0',
      'MouseDown:0x$shift',
      'MouseUp:0x$shiftButton1',
      'KeyUp:0x$shift',
    ], reason: 'a native Display reports the click with Shift held and the Shift release after it');
  });

  testWidgets('the engine still gets the answer of a key handled a turn later', (tester) async {
    await tester.pumpWidget(_wrap());
    await tester.pumpAndSettle();
    await KeyDeliveryOrder.install();
    addTearDown(KeyDeliveryOrder.uninstall);
    bool claim(KeyEvent event) => true;
    HardwareKeyboard.instance.addHandler(claim);
    addTearDown(() => HardwareKeyboard.instance.removeHandler(claim));

    final answers = <bool>[];
    _deliverShift(tester, down: true, ms: 1000, answered: answers.add);
    await tester.pump(Duration.zero);
    expect(answers, [true], reason: 'the engine hands a key the framework claimed to no text input');

    _deliverShift(tester, down: false, ms: 1100);
    await tester.pump(Duration.zero);
  });
}
