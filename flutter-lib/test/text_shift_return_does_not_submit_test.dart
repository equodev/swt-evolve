// Shift+Return in a single-line Text is a key, not a submit: a native Cocoa or GTK Text sends the
// KeyDown and no DefaultSelection. The web's input element submits on any Enter, Shift held or not.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

class _RecordingTextSwt extends TextSwt<VText> {
  final List<String> calls;
  const _RecordingTextSwt({required super.value, required this.calls});

  @override
  void sendSelectionDefaultSelection(VText val, VEvent? payload) => calls.add('DefaultSelection');
}

VText _field() => VText()
  ..id = 32
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = 'edsger';

void main() {
  testWidgets('Shift+Return in a single-line Text sends no DefaultSelection; Return does',
      (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: _RecordingTextSwt(value: _field(), calls: calls),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    final configured = tester.testTextInput.setClientArgs!['inputAction'] as String;
    final action = TextInputAction.values.byName(configured.split('.').last);

    await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    await tester.testTextInput.receiveAction(action);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await tester.pumpAndSettle();
    expect(calls, isEmpty, reason: 'the web submits Shift+Return; a native single-line Text does not');

    await tester.testTextInput.receiveAction(action);
    await tester.pumpAndSettle();
    expect(calls, equals(['DefaultSelection']));
  });
}
