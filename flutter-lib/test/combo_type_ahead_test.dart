// A letter typed into a focused READ_ONLY Combo, measured against native SWT. Win32 selects the
// next item starting with it, wrapping, with the list closed or open, and an open list stays open
// with its highlight on the new selection; Enter then closes it and notifies nothing. GTK ignores
// the letter either way.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/combo_evolve.dart' show ComboValueText;
import 'package:swtflutter/src/impl/focus_requests.dart';

const int comboId = 7;
const List<String> items = ['Alpha', 'Tango', 'Bravo', 'Tiger', 'Charlie', 'tom', 'Delta'];

class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

Future<List<(String, VEvent?)>> _pumpFocused(WidgetTester tester, int style) async {
  final events = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 120,
        height: 30,
        child: _CapturingComboSwt(
          value: VCombo()
            ..swt = 'Combo'
            ..id = comboId
            ..style = style
            ..enabled = true
            ..items = items
            ..text = 'Alpha'
            ..bounds = (VRectangle()
              ..x = 0
              ..y = 0
              ..width = 120
              ..height = 30),
          onEvent: (ev, payload) => events.add((ev, payload)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  FocusRequests.instance.request(comboId);
  await tester.pumpAndSettle();
  // A READ_ONLY Combo draws its value without an EditableText; its field's node is the Focus around it.
  expect(Focus.of(tester.element(find.byType(ComboValueText))).hasFocus, isTrue,
      reason: 'Java focusing the Combo must focus its field, where its keys are handled');
  events.clear();
  return events;
}

List<String?> _selections(List<(String, VEvent?)> events) =>
    events.where((e) => e.$1 == 'Selection/Selection').map((e) => e.$2?.text).toList();

String _shown(WidgetTester tester) =>
    tester.widget<ComboValueText>(find.byType(ComboValueText)).controller.text;

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pumpAndSettle();
}

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  bool listOpen() => find.text('Charlie').evaluate().isNotEmpty;

  testWidgets('Windows: with the list open, a letter selects the next item starting with it', (
    tester,
  ) async {
    final events = await _pumpFocused(tester, SWT.READ_ONLY);
    await _press(tester, LogicalKeyboardKey.f4);
    expect(listOpen(), isTrue, reason: 'sanity: F4 opens the list');

    await _press(tester, LogicalKeyboardKey.keyT);
    await _press(tester, LogicalKeyboardKey.keyT);
    await _press(tester, LogicalKeyboardKey.keyT);

    expect(_selections(events), ['Tango', 'Tiger', 'tom']);
    expect(_shown(tester), 'tom');
    expect(listOpen(), isTrue, reason: 'a typed letter leaves the list open');

    events.clear();
    await _press(tester, LogicalKeyboardKey.enter);

    expect(listOpen(), isFalse);
    expect(events.where((e) => e.$1.startsWith('Selection/')), isEmpty,
        reason: 'the letter already selected tom: Enter only closes the list');
  }, variant: TargetPlatformVariant.only(TargetPlatform.windows));

  testWidgets('GTK: a letter selects nothing, with the list closed or open', (tester) async {
    final events = await _pumpFocused(tester, SWT.READ_ONLY);

    await _press(tester, LogicalKeyboardKey.keyT);
    expect(_selections(events), isEmpty, reason: 'list closed');

    await _press(tester, LogicalKeyboardKey.f4);
    expect(listOpen(), isTrue, reason: 'sanity: F4 opens the list');
    await _press(tester, LogicalKeyboardKey.keyT);

    expect(_selections(events), isEmpty, reason: 'list open');
    expect(_shown(tester), 'Alpha');
  }, variant: TargetPlatformVariant.only(TargetPlatform.linux));
}
