// SWT parity: typing into an editable Combo must reach Java as it is typed.
//
// The value only lives on the client until a Modify carries it over, so anything that reads
// Combo.getText() from a key/modify listener — JFace's encoding field editor validates in
// keyReleased, and the KeyUp does reach Java — sees the pre-edit text and never revalidates.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// The generated ComboSwt with sendEvent captured instead of handed to EquoCommService
/// (a widget test has no live transport). The State and its real editing path are untouched.
class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCombo _editableCombo(String text) => VCombo()
  ..swt = 'Combo'
  ..id = 3
  ..style = SWT.DROP_DOWN
  ..enabled = true
  ..items = const ['UTF-8', 'US-ASCII']
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 30);

void main() {
  testWidgets('typing in an editable Combo forwards each edit as a Modify',
      (tester) async {
    final modifies = <VEvent?>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 30,
        child: _CapturingComboSwt(
          value: _editableCombo('UTF-8'),
          onEvent: (ev, payload) {
            if (ev == 'Modify/Modify') modifies.add(payload);
          },
        ),
      ),
    ));

    final field = find.byType(EditableText);
    for (final t in const ['UTF-8x', 'UTF-8xy']) {
      await tester.enterText(field, t);
      await tester.pump();
    }

    expect(find.text('UTF-8xy'), findsOneWidget,
        reason: 'sanity: the field holds what was typed');
    expect(modifies.length, 2,
        reason: 'every keystroke must reach Java, not only the committed value');
    expect(modifies.map((e) => e?.text).toList(), ['UTF-8x', 'UTF-8xy']);
  });

  testWidgets('a stale echo of an in-flight edit must not wipe fast-typed text',
      (tester) async {
    Widget appWith(VCombo v) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 200,
            height: 30,
            child: _CapturingComboSwt(value: v, onEvent: (_, __) {}),
          ),
        );

    await tester.pumpWidget(appWith(_editableCombo('')));

    final field = find.byType(EditableText);
    for (final t in const ['U', 'UT', 'UTF']) {
      await tester.enterText(field, t);
      await tester.pump();
    }

    // Java's full-state push for the first keystroke lands after the user typed past it.
    await tester.pumpWidget(appWith(_editableCombo('U')));
    await tester.pump();

    expect(find.text('UTF'), findsOneWidget,
        reason: 'a stale echo of our own Modify must not drop the later keystrokes');
  });
}
