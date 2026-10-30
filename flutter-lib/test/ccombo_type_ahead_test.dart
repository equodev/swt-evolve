// A letter typed while a CCombo's list is open goes to the list, where native focus is, and gets
// that platform's list type-ahead. Win32: the next item starting with the letter, wrapping, selected
// at once (Modify, then Selection) with the list left open. Cocoa: the typed letters accumulate into
// a prefix and the first item starting with it is selected; no match keeps the selection. GTK's list
// ignores the letter. On every platform the letter stays out of the editable field.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VCCombo _combo(int style) => VCCombo()
  ..swt = 'CCombo'
  ..id = 11
  ..style = style
  ..enabled = true
  ..items = const ['Alpha', 'Tango', 'Bravo', 'Tiger', 'Charlie', 'tom', 'Delta']
  ..text = 'Alpha'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

Future<List<(String, String?)>> _openAndPress(
    WidgetTester tester, VCCombo value, List<LogicalKeyboardKey> keys) async {
  final events = <(String, String?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _CapturingCComboSwt(
          value: value,
          onEvent: (ev, payload) => events.add((ev, payload?.text)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();

  await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
  await tester.pumpAndSettle();
  expect(find.text('Charlie'), findsWidgets, reason: 'sanity: the list is open');

  for (final key in keys) {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
  }
  return events;
}

String _field(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText).first).controller.text;

List<String?> _selections(List<(String, String?)> events) =>
    [for (final e in events) if (e.$1 == 'Selection/Selection') e.$2];

LogicalKeyboardKey _letter(String c) =>
    LogicalKeyboardKey(LogicalKeyboardKey.keyA.keyId + c.codeUnitAt(0) - 0x61);

Future<List<(String, String?)>> _openAndType(WidgetTester tester, int style, String letters) async {
  final events = await _openAndPress(tester, _combo(style), []);
  events.clear();
  for (final c in letters.split('')) {
    await tester.sendKeyEvent(_letter(c), character: c);
    await tester.pumpAndSettle();
  }
  return events;
}

void main() {
  for (final (name, style) in [('editable', 0), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name, Windows: a letter selects the next item starting with it and leaves the list open', (
      tester,
    ) async {
      final events = await _openAndType(tester, style, 'tttb');

      expect(_selections(events), ['Tango', 'Tiger', 'tom', 'Bravo']);
      expect(_field(tester), 'Bravo');
      expect(find.text('Charlie'), findsWidgets, reason: 'the list stays open');
    }, variant: TargetPlatformVariant.only(TargetPlatform.windows));

    testWidgets('$name, GTK: a letter selects nothing', (tester) async {
      final events = await _openAndType(tester, style, 't');

      expect(_selections(events), isEmpty);
      expect(_field(tester), 'Alpha');
    }, variant: TargetPlatformVariant.only(TargetPlatform.linux));

    testWidgets('$name, macOS: letters accumulate into a prefix; no match keeps the selection', (
      tester,
    ) async {
      final events = await _openAndType(tester, style, 'ttb');

      expect(_selections(events), ['Tango']);
      expect(_field(tester), 'Tango');
    }, variant: TargetPlatformVariant.only(TargetPlatform.macOS));
  }
}
