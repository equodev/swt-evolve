// A CCombo at rest has no DropdownMenu, and still has to behave as one: focus arriving by traversal,
// or a press, mounts the menu and hands it the focus, which Java hears about once; a press on a
// read-only field, or on an editable one's arrow, opens the list.

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
  const _CapturingCComboSwt({required super.value, required this.events});

  final List<String> events;

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) => events.add(wireChannelNames[channel]!);
}

VCCombo _combo(int style) => VCCombo()
  ..id = 7
  ..style = style
  ..enabled = true
  ..items = const ['String', 'Number', 'Boolean']
  ..text = 'String'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

Future<List<String>> _mount(WidgetTester tester, int style) async {
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 200, height: 24, child: _CapturingCComboSwt(value: _combo(style), events: events)),
    ),
  ));
  await tester.pumpAndSettle();
  expect(find.byType(DropdownMenu<String>), findsNothing, reason: 'the field is not at rest');
  return events;
}

bool _listOpen(WidgetTester tester) => find.text('Number').hitTestable().evaluate().isNotEmpty;

void main() {
  testWidgets('Tab reaches a field at rest and hands the focus to its menu', (tester) async {
    final events = await _mount(tester, SWT.NONE);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();
    expect(find.byType(DropdownMenu<String>), findsOneWidget);
    expect(tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus, isTrue);
    expect(events.where((e) => e.startsWith('Focus/')), ['Focus/FocusIn']);
    expect(_listOpen(tester), isFalse);
  });

  testWidgets('a press on an editable field at rest focuses it without opening the list', (tester) async {
    final events = await _mount(tester, SWT.NONE);
    await tester.tapAt(const Offset(20, 12));
    await tester.pumpAndSettle();
    expect(tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus, isTrue);
    expect(events.where((e) => e.startsWith('Focus/')), ['Focus/FocusIn']);
    expect(_listOpen(tester), isFalse);
  });

  testWidgets("a press on an editable field's arrow at rest opens the list", (tester) async {
    await _mount(tester, SWT.NONE);
    await tester.tapAt(const Offset(190, 12));
    await tester.pumpAndSettle();
    expect(_listOpen(tester), isTrue);
  });

  testWidgets('a press on a read-only field at rest opens the list', (tester) async {
    await _mount(tester, SWT.READ_ONLY);
    await tester.tapAt(const Offset(20, 12));
    await tester.pumpAndSettle();
    expect(_listOpen(tester), isTrue);
  });
}
