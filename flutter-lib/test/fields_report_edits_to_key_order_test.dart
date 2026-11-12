// A Spinner and an editable Combo register with KeyEditOrder as fields that report their edits, so
// the KeyUp of a character typed into them waits for its edit, as a native one reports KeyDown,
// Verify, Modify and only then KeyUp. A Spinner sends a digit and a letter up by different paths.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/key_edit_order.dart';

class _QuietSpinnerSwt extends SpinnerSwt<VSpinner> {
  const _QuietSpinnerSwt({required super.value});

  @override
  void sendEvent(VSpinner val, int channel, VEvent? payload) {}
}

class _QuietComboSwt extends ComboSwt<VCombo> {
  const _QuietComboSwt({required super.value});

  @override
  void sendEvent(VCombo val, int channel, VEvent? payload) {}
}

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 160
  ..height = 32;

const int _id = 42;

Widget _spinner() => _QuietSpinnerSwt(
      value: VSpinner()
        ..id = _id
        ..style = SWT.BORDER
        ..enabled = true
        ..minimum = 0
        ..maximum = 1000
        ..selection = 0
        ..increment = 1
        ..digits = 0
        ..bounds = _bounds(),
    );

Widget _combo() => _QuietComboSwt(
      value: VCombo()
        ..swt = 'Combo'
        ..id = _id
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = ['Alpha', 'Bravo']
        ..text = ''
        ..bounds = _bounds(),
    );

void main() {
  setUp(FocusRequests.instance.reset);
  tearDown(FocusRequests.instance.reset);

  for (final (name, field, typed) in [
    ('digit into a Spinner', _spinner, '04'),
    ('letter into a Spinner', _spinner, '0a'),
    ('letter into a Combo', _combo, 'a'),
  ]) {
    testWidgets('the KeyUp of a $name waits for the edit it makes', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(alignment: Alignment.topLeft, child: SizedBox(width: 160, height: 32, child: field())),
      ));
      await tester.pumpAndSettle();
      if (field == _spinner) {
        await tester.tap(find.byType(TextField));
      } else {
        FocusRequests.instance.request(_id);
      }
      await tester.pumpAndSettle();

      final order = KeyEditOrder.instance;
      expect(order.editorFocused, isTrue, reason: 'the focused field takes typing and reports its edits');
      final sent = <String>[];
      order.forward(typed: true, send: () => sent.add('KeyDown'));
      tester.testTextInput.updateEditingValue(
        TextEditingValue(text: typed, selection: TextSelection.collapsed(offset: typed.length)),
      );
      await tester.pump();
      order.forward(typed: false, send: () => sent.add('KeyUp'));

      expect(sent, ['KeyDown', 'KeyUp'], reason: 'the KeyUp goes once the edit landed, not after a timeout');
      await tester.pumpAndSettle(const Duration(milliseconds: 300));
    });
  }
}
