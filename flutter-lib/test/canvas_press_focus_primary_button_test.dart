// A childless Canvas takes the keyboard focus from a primary-button press only: on every native a
// right or middle press leaves the focus where it was, so a TableCursor's context menu and its
// Escape stay with the Shell that had the focus.

import 'package:flutter/gestures.dart' show kMiddleMouseButton, kPrimaryButton, kSecondaryButton;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingCanvasSwt extends CanvasSwt<VCanvas> {
  const _CapturingCanvasSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VCanvas val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

VCanvas _childlessCanvas() => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

/// Presses and releases [buttons] over a childless Canvas; returns the focus events it sent.
Future<List<String>> _press(WidgetTester tester, int buttons) async {
  final focus = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: _CapturingCanvasSwt(
      value: _childlessCanvas(),
      onEvent: (ev) {
        if (ev.startsWith('Focus/')) focus.add(ev);
      },
    ),
  ));
  await tester.pumpAndSettle();
  focus.clear();
  final gesture =
      await tester.startGesture(tester.getCenter(find.byType(_CapturingCanvasSwt)), buttons: buttons);
  await tester.pump();
  await gesture.up();
  await tester.pumpAndSettle();
  return focus;
}

void main() {
  testWidgets('a primary press focuses a childless Canvas', (tester) async {
    expect(await _press(tester, kPrimaryButton), contains('Focus/FocusIn'));
  });

  for (final (name, buttons) in [('secondary', kSecondaryButton), ('middle', kMiddleMouseButton)]) {
    testWidgets('a $name press leaves a childless Canvas unfocused', (tester) async {
      expect(await _press(tester, buttons), isEmpty);
    });
  }
}
