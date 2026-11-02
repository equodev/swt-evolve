// Enter on a focused push button is the shell's: Java runs the traversal and presses the default
// button. Flutter's own activation on Enter would press the focused button as well, firing two
// Selections for one key. Space still presses the focused button, as on every native.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  const _RecordingButtonSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VButton val, int channel, VEvent? payload) => sent.add(wireChannelNames[channel]!);
}

VButton _button(int id, int style) => VButton()
  ..swt = 'Button'
  ..id = id
  ..style = style
  ..text = 'Apply'
  ..visible = true
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 28);

/// Mounts one button after a plain focus stop and tabs onto the button.
Future<List<String>> _pumpFocusedButton(WidgetTester tester, int style) async {
  final sent = <String>[];
  final before = FocusNode(debugLabel: 'before');
  addTearDown(before.dispose);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Column(children: [
      Focus(focusNode: before, child: const SizedBox(width: 10, height: 10)),
      SizedBox(width: 120, height: 28, child: _RecordingButtonSwt(sent: sent, value: _button(7, style))),
    ]),
  ));
  await tester.pumpAndSettle();
  before.requestFocus();
  await tester.pump();
  await tester.sendKeyEvent(LogicalKeyboardKey.tab);
  await tester.pumpAndSettle();
  expect(sent.where((ev) => ev.contains('FocusIn')), isNotEmpty, reason: 'Tab must focus the button: $sent');
  sent.clear();
  return sent;
}

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    // The whole-tree model: the Display forwards keys, so wrap() mounts no forwarding Focus of its own.
    startDisplayKeyForwarding();
  });
  tearDown(() {
    stopDisplayKeyForwarding();
    FocusRequests.instance.reset();
  });

  for (final (name, style) in [('push', SWT.PUSH), ('toggle', SWT.TOGGLE), ('check', SWT.CHECK)]) {
    testWidgets('Enter on a focused $name button fires no Selection', (tester) async {
      final sent = await _pumpFocusedButton(tester, style);

      await tester.sendKeyEvent(LogicalKeyboardKey.enter);
      await tester.pumpAndSettle();

      expect(sent.where((ev) => ev.contains('Selection')), isEmpty, reason: '$sent');
    });
  }

  testWidgets('Space on a focused push button still fires its Selection', (tester) async {
    final sent = await _pumpFocusedButton(tester, SWT.PUSH);

    await tester.sendKeyEvent(LogicalKeyboardKey.space);
    await tester.pumpAndSettle();

    expect(sent.where((ev) => ev.contains('Selection')), hasLength(1), reason: '$sent');
  });
}
