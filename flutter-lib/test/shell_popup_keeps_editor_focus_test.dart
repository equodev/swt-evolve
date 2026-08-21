// SWT parity: `Shell.setVisible(true)` shows a shell without activating it, so the control that
// opened it keeps the keyboard. JFace relies on that for content assist — `ContentAssistant`
// drives its `SWT.ON_TOP` proposal popup from a `VerifyKeyListener` on the StyledText, which only
// fires while the editor still owns the keys. A shell that grabbed focus on mount broke every
// keyboard path into the popup: the editor stopped forwarding its own keys, and the Display-level
// forwarder they fell back to is dropped Java-side for a focused StyledText.
//
// `VDisplay.activeShellId` carries SWT's active shell, and only that shell takes focus here.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

// DisplaySwt caches the last state it saw per display id, so each test uses its own id —
// otherwise the second one starts with the first one's popup already mounted.
int _idsFor(int n) => 7000 + n * 10;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VStyledText _editor(int base) => VStyledText()
  ..swt = 'StyledText'
  ..id = base + 3
  ..style = 0
  ..enabled = true
  ..editable = true
  ..caretOffset = 0
  ..text = 'String s = "hello";'
  ..bounds = _rect(0, 0, 400, 200);

VShell _mainShell(int base) => VShell()
  ..swt = 'Shell'
  ..id = base + 1
  ..style = SWT.SHELL_TRIM
  ..text = 'workbench'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [_editor(base)];

/// The JFace proposal popup: trim-less, always on top, shown with `setVisible(true)`.
VShell _popupShell(int base) => VShell()
  ..swt = 'Shell'
  ..id = base + 2
  ..style = SWT.ON_TOP | SWT.RESIZE
  ..bounds = _rect(120, 140, 300, 200);

VDisplay _display(int base,
        {required List<VShell> shells, required int activeShellId}) =>
    VDisplay()
      ..swt = 'Display'
      ..id = base
      ..shells = shells
      ..activeShellId = activeShellId;

/// Delivers an inbound frame exactly as the transport would (2-byte name length,
/// name, JSON body) so the Display's channel subscription receives it.
void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  /// Brings up the workbench with the editor holding the keyboard, then shows a second shell
  /// and reports whether the editor still owns its keys.
  Future<bool> editorKeepsKeysWhenShellAppears(
    WidgetTester tester, {
    required int base,
    required bool popupIsActivated,
  }) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: _display(base,
            shells: [_mainShell(base)], activeShellId: base + 1),
      ),
    ));
    await tester.pumpAndSettle();

    // Click into the editor, as the user does before asking for content assist.
    await tester.tapAt(const Offset(60, 40));
    await tester.pumpAndSettle();
    expect(focusedEditorHandlesOwnKeys, isTrue,
        reason: 'sanity: a clicked StyledText forwards its own key events');

    _receiveJson(
      'Display/$base',
      _display(
        base,
        shells: [_mainShell(base), _popupShell(base)],
        activeShellId: popupIsActivated ? base + 2 : base + 1,
      ).toJson(),
    );
    await tester.pumpAndSettle();

    return focusedEditorHandlesOwnKeys;
  }

  testWidgets('a shell SWT did not activate leaves the keyboard with the editor',
      (tester) async {
    expect(
      await editorKeepsKeysWhenShellAppears(tester,
          base: _idsFor(1), popupIsActivated: false),
      isTrue,
      reason: 'the content assist popup is shown without activating its shell, so the '
          'editor must keep forwarding its own keys — that is the only path that reaches '
          "ContentAssistant's VerifyKeyListener",
    );
  });

  testWidgets('a shell SWT activated does take the keyboard', (tester) async {
    expect(
      await editorKeepsKeysWhenShellAppears(tester,
          base: _idsFor(2), popupIsActivated: true),
      isFalse,
      reason: 'a shell opened with Shell.open() is the active shell and must take focus '
          'away from the editor, as it did before',
    );
  });
}
