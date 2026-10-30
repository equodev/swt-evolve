// SWT parity (Windows): a focused drop-down Combo whose list is closed still answers the keyboard.
// The arrows -- and on a READ_ONLY one Home, End, Page Up/Down and a typed letter -- move the
// selection and notify it, F4 or Alt+Down opens the list, and Enter fires DefaultSelection. The
// Combo is focused the way Java focuses it (Control.setFocus), which must land on its own field.

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
const List<String> items = ['Alpha', 'Bravo', 'Charlie', 'Delta', 'Echo', 'Foxtrot', 'Golf', 'Hotel'];

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
  expect(_fieldFocus(tester).hasFocus, isTrue,
      reason: 'Java focusing the Combo must focus its field, where its keys are handled');
  events.clear();
  return events;
}

/// The node of the Combo's field: an editable one's, or the one a READ_ONLY Combo's value is drawn under.
FocusNode _fieldFocus(WidgetTester tester) {
  final editable = find.byType(EditableText);
  if (editable.evaluate().isNotEmpty) return tester.widget<EditableText>(editable).focusNode;
  return tester
      .widget<Focus>(find.ancestor(of: find.byType(ComboValueText), matching: find.byType(Focus)).first)
      .focusNode!;
}

/// The text the Combo's field shows.
String _fieldText(WidgetTester tester) {
  final editable = find.byType(EditableText);
  if (editable.evaluate().isNotEmpty) return tester.widget<EditableText>(editable).controller.text;
  return tester.widget<ComboValueText>(find.byType(ComboValueText)).controller.text;
}

List<String?> _selections(List<(String, VEvent?)> events) =>
    events.where((e) => e.$1 == 'Selection/Selection').map((e) => e.$2?.text).toList();

String _shown(WidgetTester tester) =>
    _fieldText(tester);

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

  for (final (name, style) in const [('editable', SWT.DROP_DOWN), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name: Down and Up move the selection with the list closed', (tester) async {
      final events = await _pumpFocused(tester, style);

      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _press(tester, LogicalKeyboardKey.arrowDown);
      await _press(tester, LogicalKeyboardKey.arrowUp);

      expect(_selections(events), ['Bravo', 'Charlie', 'Bravo']);
      expect(_shown(tester), 'Bravo');
      expect(find.text('Hotel'), findsNothing, reason: 'the arrows do not open the list');
    });

    testWidgets('$name: Up on the first item changes nothing', (tester) async {
      final events = await _pumpFocused(tester, style);

      await _press(tester, LogicalKeyboardKey.arrowUp);

      expect(_selections(events), isEmpty);
    });

    testWidgets('$name: F4 opens the list', (tester) async {
      await _pumpFocused(tester, style);

      await _press(tester, LogicalKeyboardKey.f4);

      expect(find.text('Hotel'), findsOneWidget);
    });

    testWidgets('$name: Alt+Down opens the list', (tester) async {
      await _pumpFocused(tester, style);

      await tester.sendKeyDownEvent(LogicalKeyboardKey.altLeft);
      await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.altLeft);
      await tester.pumpAndSettle();

      expect(find.text('Hotel'), findsOneWidget);
    });

    testWidgets('$name: Enter fires DefaultSelection', (tester) async {
      final events = await _pumpFocused(tester, style);

      await _press(tester, LogicalKeyboardKey.enter);

      expect(events.map((e) => e.$1), contains('Selection/DefaultSelection'));
      expect(_selections(events), isEmpty);
    });
  }

  testWidgets('READ_ONLY: End and Home select the last and the first item', (tester) async {
    final events = await _pumpFocused(tester, SWT.READ_ONLY);

    await _press(tester, LogicalKeyboardKey.end);
    await _press(tester, LogicalKeyboardKey.home);

    expect(_selections(events), ['Hotel', 'Alpha']);
  });

  testWidgets('READ_ONLY: Page Down and Page Up move by a page and stop at the ends', (tester) async {
    final events = await _pumpFocused(tester, SWT.READ_ONLY);

    await _press(tester, LogicalKeyboardKey.pageDown);
    await _press(tester, LogicalKeyboardKey.pageDown);
    await _press(tester, LogicalKeyboardKey.pageDown);
    await _press(tester, LogicalKeyboardKey.pageUp);

    expect(_selections(events), ['Echo', 'Hotel', 'Delta']);
  });

  testWidgets('READ_ONLY: a typed letter selects the next item starting with it', (tester) async {
    final events = await _pumpFocused(tester, SWT.READ_ONLY);

    await _press(tester, LogicalKeyboardKey.keyD);
    await _press(tester, LogicalKeyboardKey.keyG);

    expect(_selections(events), ['Delta', 'Golf']);
    expect(_shown(tester), 'Golf');
  });

  testWidgets('editable: Home and End stay in the text', (tester) async {
    final events = await _pumpFocused(tester, SWT.DROP_DOWN);

    await _press(tester, LogicalKeyboardKey.end);
    await _press(tester, LogicalKeyboardKey.home);

    expect(_selections(events), isEmpty);
    expect(_shown(tester), 'Alpha');
  });
}
