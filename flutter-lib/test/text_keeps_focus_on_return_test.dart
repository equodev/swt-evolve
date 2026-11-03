// Return in a single-line Text submits it (DefaultSelection) and leaves the keyboard in it, as an
// SWT Text does. Java keeps routing keys to the field it still holds as focused, so a field the
// render side unfocused on Return would drop every keystroke typed after it.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

VText _field() => VText()
  ..id = 31
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = 'archivo-1';

void main() {
  testWidgets('Return in a single-line Text keeps the keyboard in the field', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: TextSwt<VText>(value: _field()),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    final focusNode = tester.widget<TextField>(find.byType(TextField)).focusNode!;
    expect(focusNode.hasFocus, isTrue, reason: 'sanity: the field took the keyboard');

    await tester.testTextInput.receiveAction(TextInputAction.done);
    await tester.pumpAndSettle();

    expect(focusNode.hasFocus, isTrue, reason: 'Return must not move the keyboard off the field');
  });

  testWidgets('Return in a single-line Text keeps its text input connection open', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: TextSwt<VText>(value: _field()),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    // The platform answers Return with the action the field configured.
    final configured = tester.testTextInput.setClientArgs!['inputAction'] as String;
    final action = TextInputAction.values.byName(configured.split('.').last);
    tester.testTextInput.log.clear();

    await tester.testTextInput.receiveAction(action);
    await tester.pumpAndSettle();

    expect(tester.testTextInput.log.map((call) => call.method), isNot(contains('TextInput.clearClient')),
        reason: 'on the web a restarted connection loses its input element, and the keys typed next');
  });
}
