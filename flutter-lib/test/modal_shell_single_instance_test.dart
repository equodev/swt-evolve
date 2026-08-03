import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VButton _checkButton(int id) => VButton()
  ..id = id
  ..style = SWT.CHECK
  ..enabled = true
  ..selection = false
  ..text = "expr"
  ..bounds = _rect(20, 20, 120, 40);

VShell _shell(int id, int style, VRectangle bounds, {List<VButton>? children}) =>
    VShell()
      ..id = id
      ..style = style
      ..text = "shell $id"
      ..bounds = bounds
      ..children = children;

void main() {
  testWidgets(
      'a modal dialog blocks input to the dialog beneath it (single-instance)',
      (WidgetTester tester) async {
    const buttonId = 9001;
    final button = find.byWidgetPredicate(
        (w) => w is ButtonSwt<VButton> && w.value.id == buttonId);

    final main = _shell(1, SWT.SHELL_TRIM, _rect(0, 0, 800, 600));

    final dialogA = _shell(
      2,
      SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL,
      _rect(40, 40, 300, 220),
      children: [_checkButton(buttonId)],
    );

    final dialogB = _shell(
      3,
      SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL,
      _rect(450, 320, 300, 220),
    );

    final display = VDisplay()..shells = [main, dialogA, dialogB];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: display),
    ));
    await tester.pumpAndSettle();

    expect(button, findsOneWidget,
        reason: 'the expression-cell CHECK button in dialog A must render');

    ButtonImpl buttonState() => tester.state<ButtonImpl>(button);
    expect(buttonState().state.selection, isFalse,
        reason: 'sanity: the button starts unchecked');

    await tester.tapAt(tester.getCenter(button));
    await tester.pump();

    expect(
      buttonState().state.selection,
      isFalse,
      reason: 'while a modal dialog is open, the dialog beneath it must not '
          'receive pointer input — otherwise the nested dialog can be re-opened '
          'more than once.',
    );
  });
}
