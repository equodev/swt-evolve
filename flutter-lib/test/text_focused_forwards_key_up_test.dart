// Without a whole-tree Display to forward keys, a focused Text forwards its own keystrokes, the
// release as well as the press, and the release after the Modify of the character it typed: an
// application that reads the text in keyReleased (a live filter committing what was typed) gets
// nothing otherwise. With a whole-tree Display the Display forwards them, so the Text sends none.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const double _kWidth = 300;
const double _kHeight = 24;

/// The generated TextSwt with sendEvent captured instead of handed to EquoCommService.
class _CapturingTextSwt extends TextSwt<VText> {
  const _CapturingTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VText val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VText _field() => VText()
  ..swt = 'Text'
  ..id = 11
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = ''
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _kWidth.toInt()
    ..height = _kHeight.toInt());

/// Focuses the field, types `0` and returns the Key and Modify events it sent, in order.
Future<List<String>> _type0(WidgetTester tester) async {
  final sent = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _kWidth,
      height: _kHeight,
      child: _CapturingTextSwt(
        value: _field(),
        onEvent: (ev, payload) {
          if (ev.startsWith('Key/')) sent.add('$ev ${payload?.character}');
          if (ev.startsWith('Modify/')) sent.add('$ev ${payload?.text}');
        },
      ),
    ),
  ));
  await tester.tapAt(tester.getCenter(find.byType(_CapturingTextSwt)));
  FocusRequests.instance.request(11);
  await tester.pumpAndSettle();
  sent.clear();

  // The edit lands after the release, as it can on a busy machine: the release still goes after it.
  await tester.sendKeyDownEvent(LogicalKeyboardKey.digit0, character: '0');
  await tester.sendKeyUpEvent(LogicalKeyboardKey.digit0);
  await tester.pump();
  tester.testTextInput.enterText('0');
  await tester.pumpAndSettle(const Duration(milliseconds: 300));
  return sent;
}

void main() {
  final zero = '0'.codeUnitAt(0);
  tearDown(FocusRequests.instance.reset);

  testWidgets('a focused Text forwards the release of a typed key after its Modify', semanticsEnabled: false,
      (tester) async {
    expect(displayLevelKeyForwardingActive, isFalse, reason: 'sanity: no whole-tree Display forwards keys');

    expect(await _type0(tester), ['Key/KeyDown $zero', 'Modify/Modify 0', 'Key/KeyUp $zero']);
  });

  testWidgets('a focused Text forwards no key while a whole-tree Display does', semanticsEnabled: false,
      (tester) async {
    startDisplayKeyForwarding();
    addTearDown(stopDisplayKeyForwarding);

    expect(await _type0(tester), ['Modify/Modify 0']);
  });
}
