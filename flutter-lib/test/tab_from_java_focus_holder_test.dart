// SWT traverses from the focus control. A Text focused from Java with setFocus() is the focus
// control, but Java does not move the client's keyboard focus for an ordinary field, so the client's
// own focus can sit elsewhere (nowhere, or the shell). Tab must still go to the control after the
// one Java focused, not start from wherever the client's focus happens to be.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/traversal_veto_scope.dart';
import 'package:swtflutter/src/impl/utils/veto_gate.dart';

import 'support/batch.dart';

const int _firstId = 51;
const int _buttonId = 52;
const int _secondId = 53;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _text(int id, int y, String text) => VText()
  ..id = id
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = text
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, y, 120, 24);

Future<void> _pumpTextButtonText(WidgetTester tester) async {
  final gate = VetoGate('traverse')..attach('Display', 950);
  addTearDown(gate.detach);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: TraversalVetoScope(
      gate: gate,
      child: SizedBox(
        width: 400,
        height: 300,
        child: CompositeSwt<VComposite>(
          value: VComposite()
            ..id = 50
            ..style = SWT.NONE
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 400, 300)
            ..children = [
              _text(_firstId, 0, 'first'),
              VButton()
                ..id = _buttonId
                ..style = SWT.PUSH
                ..text = 'Button'
                ..visible = true
                ..enabled = true
                ..bounds = _rect(0, 30, 120, 28),
              _text(_secondId, 64, 'second'),
            ],
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

FocusNode _textNode(WidgetTester tester, String text) => tester
    .widget<EditableText>(find.descendant(
        of: find.byWidgetPredicate((w) => w is TextField && w.controller?.text == text),
        matching: find.byType(EditableText)))
    .focusNode;

bool _buttonHasFocus(WidgetTester tester) {
  final primary = FocusManager.instance.primaryFocus?.context;
  if (primary == null) return false;
  final button = tester.element(find.byWidgetPredicate((w) => w is ButtonSwt));
  var inside = primary == button;
  primary.visitAncestorElements((e) {
    if (e == button) inside = true;
    return !inside;
  });
  return inside;
}

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    FocusRequests.listen();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('Tab after Java focused the first field reaches the next control', (tester) async {
    await _pumpTextButtonText(tester);
    expect(_textNode(tester, 'first').hasFocus, isFalse,
        reason: 'the client was never asked to focus the field');

    receiveTagged(FocusRequests.holderChannel, {'id': _firstId});
    await tester.pump();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(_buttonHasFocus(tester), isTrue,
        reason: 'SWT traverses from the focus control Java holds, the first Text');
  });

  testWidgets('Shift+Tab goes back from the control Java focused', (tester) async {
    await _pumpTextButtonText(tester);
    receiveTagged(FocusRequests.holderChannel, {'id': _secondId});
    await tester.pump();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pumpAndSettle();

    expect(_buttonHasFocus(tester), isTrue);
  });

  testWidgets('a field the client already focused traverses from where it is', (tester) async {
    await _pumpTextButtonText(tester);
    receiveTagged(FocusRequests.holderChannel, {'id': _firstId});
    _textNode(tester, 'second').requestFocus();
    await tester.pump();
    // The client's own move reaches Java and comes back as the new holder.
    receiveTagged(FocusRequests.holderChannel, {'id': _secondId});
    await tester.pump();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pumpAndSettle();

    expect(_buttonHasFocus(tester), isTrue);
  });

  testWidgets('a focus the client holds on another control is left where it is', (tester) async {
    await _pumpTextButtonText(tester);
    receiveTagged(FocusRequests.holderChannel, {'id': _firstId});
    _textNode(tester, 'second').requestFocus();
    await tester.pump();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pumpAndSettle();

    expect(_buttonHasFocus(tester), isTrue,
        reason: 'only a client focus on no control, or on one around the holder, is overridden');
  });

  testWidgets('a holder named by the Display state is traversed from too', (tester) async {
    await _pumpTextButtonText(tester);
    // A client that connects or reloads learns the holder from the Display push alone.
    FocusRequests.instance.holderFromDisplay(_firstId);

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(_buttonHasFocus(tester), isTrue);
  });
}
