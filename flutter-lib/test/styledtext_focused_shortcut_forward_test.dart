// A focused StyledText forwards every keystroke it sees, editing or not: focus alone claims
// keyboard ownership, which makes it the only path to Java. Clicking an editor whose text shape
// has not arrived yet leaves precisely that state — focused, but not editing.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

const double _kWidth = 300;
const double _kHeight = 120;

/// The generated StyledTextSwt with sendEvent captured instead of handed to EquoCommService
/// (a widget test has no live transport). The State and its real key path are untouched.
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

    // A single push: initState doesn't run extraSetState, so no text shape exists yet and the
    // click below focuses the editor without entering an edit — where a workbench-opened editor
    // sits before anyone types into it.
    await tester.pumpWidget(appWith(editor()));
    await tester.tapAt(tester.getCenter(find.byType(_CapturingStyledTextSwt)));
    await tester.pump();

    // Java's state push then lands and builds the text shape; that alone starts no edit either.
    await tester.pumpWidget(appWith(editor()));

    expect(focusedEditorHandlesOwnKeys, isTrue,
        reason: 'sanity: the focused editor owns the keyboard, so nothing else forwards for it');

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
}
