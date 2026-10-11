// On macOS a Command key equivalent typed in a focused StyledText still reaches the native menu bar,
// so the application menu's items (Settings…, Hide, Quit) answer their shortcuts from the editor.

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const double _kWidth = 300;
const double _kHeight = 120;

class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VStyledText val, String ev, VEvent? payload) => onEvent(ev, payload);
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

  Future<List<VEvent?>> focusEditor(WidgetTester tester) async {
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
    await tester.pump();
    expect(canvasEditorFocused, isTrue, reason: 'sanity: the clicked editor holds the keyboard');
    return keyDowns;
  }

  testWidgets('on macOS Command+comma is forwarded to Java and left for the menu bar',
      (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    try {
      final keyDowns = await focusEditor(tester);

      await tester.sendKeyDownEvent(LogicalKeyboardKey.metaLeft, platform: 'macos');
      final handled = await tester.sendKeyDownEvent(LogicalKeyboardKey.comma, platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.comma, platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.metaLeft, platform: 'macos');
      await tester.pump();

      expect(handled, isFalse,
          reason: 'a handled key equivalent never reaches the menu bar, so Settings… cannot open');
      expect(
        keyDowns.any((e) =>
            e != null && e.keyCode == ','.codeUnitAt(0) && ((e.stateMask ?? 0) & SWT.COMMAND) != 0),
        isTrue,
        reason: 'Java still sees the key first, as SWT does',
      );
    } finally {
      debugDefaultTargetPlatformOverride = null;
    }
  });

  testWidgets('on macOS an unmodified key stays with the editor', (tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    try {
      await focusEditor(tester);

      final handled = await tester.sendKeyDownEvent(LogicalKeyboardKey.keyA, platform: 'macos');
      await tester.sendKeyUpEvent(LogicalKeyboardKey.keyA, platform: 'macos');
      await tester.pump();

      expect(handled, isTrue, reason: "Flutter's own shortcuts must not act on the editor's keys");
    } finally {
      debugDefaultTargetPlatformOverride = null;
    }
  });
}
