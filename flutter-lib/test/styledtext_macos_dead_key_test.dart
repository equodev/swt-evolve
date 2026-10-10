// On macOS the platform's text input only receives the keys Flutter leaves unhandled. A dead key
// (Option+E on a U.S. layout) arrives with no character; it must be left to the text input, which
// composes it, instead of reaching Java as the bare letter. SWT Cocoa sends no KeyDown for it.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const double _kWidth = 300;
const double _kHeight = 120;

class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VStyledText val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VStyledText _editor() => VStyledText()
  ..swt = 'StyledText'
  ..id = 7
  ..style = 0
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = 'indented'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _kWidth.toInt()
    ..height = _kHeight.toInt());

void main() {
  late List<int> forwarded;

  Future<void> focusEditor(WidgetTester tester) async {
    forwarded = [];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _kWidth,
        height: _kHeight,
        child: _CapturingStyledTextSwt(
          value: _editor(),
          onEvent: (ev, payload) {
            // Modifier keys have their own KeyDown, natively too; only the typed keys matter here.
            final keyCode = payload?.keyCode ?? 0;
            if (ev == 'Key/KeyDown' && (keyCode & SWT.MODIFIER_MASK) == 0) forwarded.add(keyCode);
          },
        ),
      ),
    ));
    await tester.tapAt(tester.getCenter(find.byType(_CapturingStyledTextSwt)));
    await tester.pump();
    expect(canvasEditorFocused, isTrue, reason: 'sanity: the clicked editor holds the keyboard');
  }

  Future<bool> optionE(WidgetTester tester, String platform) async {
    await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft, platform: platform);
    final handled =
        await tester.sendKeyDownEvent(LogicalKeyboardKey.keyE, character: '', platform: platform);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyE, platform: platform);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft, platform: platform);
    return handled;
  }

  testWidgets('a dead key and the key that completes it are left to the text input on macOS',
      (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    try {
      await focusEditor(tester);

      expect(await optionE(tester, 'macos'), isFalse,
          reason: 'only a key Flutter leaves unhandled reaches the platform text input');
      expect(forwarded, isNot(contains('e'.codeUnitAt(0))),
          reason: 'SWT Cocoa sends no KeyDown for a dead key; the bare letter would be typed');

      // The platform opens the composition with the accent.
      tester.testTextInput.updateEditingValue(const TextEditingValue(
          text: '´',
          selection: TextSelection.collapsed(offset: 1),
          composing: TextRange(start: 0, end: 1)));
      await tester.pump();

      final completing =
          await tester.sendKeyDownEvent(LogicalKeyboardKey.keyE, character: 'e', platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.keyE, platform: 'macos');
      expect(completing, isFalse, reason: 'the open composition must see the key that completes it');
      expect(forwarded, isEmpty);

      tester.testTextInput.updateEditingValue(
          const TextEditingValue(text: 'é', selection: TextSelection.collapsed(offset: 1)));
      await tester.pump();

      final next =
          await tester.sendKeyDownEvent(LogicalKeyboardKey.keyX, character: 'x', platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.keyX, platform: 'macos');
      expect(next, isTrue, reason: 'ordinary typing still goes to Java');
      expect(forwarded, ['x'.codeUnitAt(0)]);
    } finally {
      debugDefaultTargetPlatformOverride = null;
    }
  });

  testWidgets('with a Display forwarding the keys, a dead key is not sent to Java on macOS',
      (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    EquoCommBase.recordSentFrames = true;
    try {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _kWidth,
          height: _kHeight,
          child: Stack(children: [
            Positioned.fill(child: DisplaySwt(value: VDisplay()..id = 5)),
            Positioned.fill(
                child: _CapturingStyledTextSwt(value: _editor(), onEvent: (_, __) {})),
          ]),
        ),
      ));
      await tester.tapAt(tester.getCenter(find.byType(_CapturingStyledTextSwt)));
      await tester.pump();
      expect(displayLevelKeyForwardingActive, isTrue);
      EquoCommService.commForTesting.clearSentFrames();

      expect(await optionE(tester, 'macos'), isFalse);
      await tester.sendKeyDownEvent(LogicalKeyboardKey.keyX, character: 'x', platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.keyX, platform: 'macos');

      final typed = EquoCommService.commForTesting.sentFrames
          .map(EquoCommBase.decodeFrame)
          .where((f) => f.$1 == 'Display/5/Key/KeyDown')
          .map((f) => f.$2)
          .where((body) => !body.contains('"keyCode":${SWT.ALT}'))
          .toList();
      expect(typed, hasLength(1), reason: 'only the x is typed: $typed');
      expect(typed.single, contains('"keyCode":${'x'.codeUnitAt(0)}'));
    } finally {
      EquoCommBase.recordSentFrames = false;
      debugDefaultTargetPlatformOverride = null;
    }
  });

  testWidgets('elsewhere a key with no character keeps going to Java', (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    try {
      await focusEditor(tester);

      expect(await optionE(tester, 'windows'), isTrue);
      expect(forwarded, contains('e'.codeUnitAt(0)));
    } finally {
      debugDefaultTargetPlatformOverride = null;
    }
  });
}
