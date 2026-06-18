// Issue #597 — Dart-side root cause.
//
// `ButtonImpl._onPressed` (button_evolve.dart) optimistically toggles ONLY the
// clicked radio's `state.selection` and sends a Selection event. Nothing on the
// Dart side deselects the sibling radios — that is left entirely to Java's
// `DartButton.selectRadio` round-trip. So whenever that round-trip is delayed or
// lost, the rendered UI shows more than one radio selected at once, which is
// exactly the reported symptom ("I can check all 3 at the same time").
//
// This test renders three RADIO buttons in a Composite and taps them. With no
// Dart-side radio-group exclusivity, all three end up selected — the test
// asserts the correct "at most one selected" invariant, so it FAILS on the
// current code, reproducing the bug at the Flutter layer (no Java involved).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';

VButton _radio(int id) => VButton()
  ..id = id
  ..style = SWT.RADIO
  ..enabled = true
  ..selection = false
  ..text = "Option $id";

int _selectedCount(WidgetTester tester) => tester
    .stateList<ButtonImpl>(find.byType(ButtonSwt<VButton>))
    .where((s) => s.state.selection ?? false)
    .length;

void main() {
  testWidgets('a radio group never shows more than one selected',
      (WidgetTester tester) async {
    // Three radios rendered as siblings (a Column stands in for the Composite so
    // no parent re-render interferes — this isolates the Dart-side group logic).
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 160,
        child: Column(
          children: [
            for (var i = 1; i <= 3; i++)
              SizedBox(
                height: 40,
                child: ButtonSwt<VButton>(key: ValueKey(i), value: _radio(i)),
              ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    expect(_selectedCount(tester), 0);

    await tester.tap(find.text('Option 1'));
    await tester.pump();
    expect(_selectedCount(tester), 1, reason: 'Option 1 selected');

    await tester.tap(find.text('Option 2'));
    await tester.pump();
    expect(_selectedCount(tester), 1,
        reason: 'selecting Option 2 must deselect Option 1 (radio group)');

    await tester.tap(find.text('Option 3'));
    await tester.pump();
    expect(_selectedCount(tester), 1,
        reason: 'a radio group must keep exactly one selection (issue #597)');
  });
}
