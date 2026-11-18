// Without a whole-tree Display to forward keys, a focused StyledText forwards every keystroke it
// sees, including shortcuts it has no binding for.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

const double _kWidth = 300;
const double _kHeight = 120;

/// The generated StyledTextSwt with sendEvent captured instead of handed to EquoCommService
/// (a widget test has no live transport). The State and its real key path are untouched.
class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VStyledText val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

void main() {
  VStyledText editor() => VStyledText()
    ..swt = 'StyledText'
    ..id = 7
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = 'println "hello"'
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _kWidth.toInt()
      ..height = _kHeight.toInt());

  testWidgets('a focused StyledText forwards a shortcut it does not handle itself',
      (tester) async {
    final keyDowns = <VEvent?>[];

    Widget appWith(VStyledText v) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: _kWidth,
            height: _kHeight,
            child: _CapturingStyledTextSwt(
              value: v,
              onEvent: (ev, payload) {
                if (ev == 'Key/KeyDown') keyDowns.add(payload);
              },
            ),
          ),
        );

    await tester.pumpWidget(appWith(editor()));
    await tester.tapAt(tester.getCenter(find.byType(_CapturingStyledTextSwt)));
    // Java focuses the pressed editor and asks the client to move its keyboard focus there.
    FocusRequests.instance.request(7);
    addTearDown(FocusRequests.instance.reset);
    await tester.pumpAndSettle();

    expect(canvasEditorFocused, isTrue, reason: 'sanity: the clicked editor holds the keyboard');

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyS);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyS);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);
    await tester.pump();

    expect(
      keyDowns.any((e) =>
          e != null &&
          e.keyCode == 's'.codeUnitAt(0) &&
          ((e.stateMask ?? 0) & SWT.CTRL) != 0),
      isTrue,
      reason: 'Ctrl+S must reach Java, or the app command bound to it can never run',
    );
  });

  testWidgets('a focused StyledText leaves Control+Command+Space to the browser on macOS',
      (tester) async {
    final keyDowns = <VEvent?>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _kWidth,
        height: _kHeight,
        child: _CapturingStyledTextSwt(
          value: editor(),
          onEvent: (ev, payload) {
            if (ev == 'Key/KeyDown') keyDowns.add(payload);
          },
        ),
      ),
    ));
    await tester.tapAt(tester.getCenter(find.byType(_CapturingStyledTextSwt)));
    FocusRequests.instance.request(7);
    addTearDown(FocusRequests.instance.reset);
    await tester.pumpAndSettle();
    expect(canvasEditorFocused, isTrue, reason: 'sanity: the clicked editor holds the keyboard');

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.metaLeft);
    final spaceHandled = await tester.sendKeyDownEvent(LogicalKeyboardKey.space);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.space);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.metaLeft);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);
    await tester.pump();

    expect(spaceHandled, isFalse,
        reason: 'a handled keydown has its default prevented, and macOS then never opens the '
            'Emoji & Symbols palette');
    expect(keyDowns.any((e) => e != null && e.keyCode == ' '.codeUnitAt(0)), isTrue,
        reason: 'Java still sees the key');

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    final ctrlSpaceHandled = await tester.sendKeyDownEvent(LogicalKeyboardKey.space);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.space);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);
    expect(ctrlSpaceHandled, isTrue, reason: 'Control+Space stays with the editor');
  }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
}
