// A focused StyledText takes typed keys from the raw key stream, so their text never reaches the
// platform's text-input buffer by itself. macOS press-and-hold replaces the letter before the caret
// in that buffer; the editor puts the typed letter there so the replacement has something to replace.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const double _kWidth = 300;
const double _kHeight = 120;

/// StyledTextSwt with sendEvent dropped (a widget test has no live transport).
class _QuietStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _QuietStyledTextSwt({required super.value});

  @override
  void sendEvent(VStyledText val, int channel, VEvent? payload) {}
}

void main() {
  VStyledText editor() => VStyledText()
    ..swt = 'StyledText'
    ..id = 7
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = ''
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _kWidth.toInt()
      ..height = _kHeight.toInt());

  Future<void> focusEditor(WidgetTester tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: _kWidth, height: _kHeight, child: _QuietStyledTextSwt(value: editor())),
    ));
    await tester.tapAt(tester.getCenter(find.byType(_QuietStyledTextSwt)));
    FocusRequests.instance.request(7);
    addTearDown(FocusRequests.instance.reset);
    await tester.pumpAndSettle();
    expect(canvasEditorFocused, isTrue, reason: 'sanity: the clicked editor holds the keyboard');
  }

  String? platformText(WidgetTester tester) => tester.testTextInput.editingState?['text'] as String?;

  testWidgets('a key the editor typed is left in the platform buffer for the platform to replace',
      (tester) async {
    await focusEditor(tester);

    final handled = await tester.sendKeyDownEvent(LogicalKeyboardKey.keyE, character: 'e');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyE);

    expect(handled, isTrue, reason: 'sanity: the editor takes the key, so the browser types nothing');
    expect(platformText(tester), 'e',
        reason: 'the accent menu replaces the e before the caret; an empty buffer gets é appended');
    expect(tester.testTextInput.editingState?['selectionBase'], 1);
  });

  testWidgets('a key that types nothing empties the platform buffer', (tester) async {
    await focusEditor(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyE, character: 'e');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyE);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.arrowLeft);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.arrowLeft);

    expect(platformText(tester), '', reason: 'the caret moved: the e is no longer before it');
  });

  testWidgets('a shortcut leaves nothing in the platform buffer', (tester) async {
    await focusEditor(tester);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await tester.sendKeyDownEvent(LogicalKeyboardKey.keyS, character: 's');
    await tester.sendKeyUpEvent(LogicalKeyboardKey.keyS);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

    expect(platformText(tester), anyOf(isNull, ''), reason: 'Ctrl+S typed no s into the document');
  });
}
