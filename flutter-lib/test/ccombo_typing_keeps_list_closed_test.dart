// Typing into an editable CCombo's field edits the field and leaves the list closed, as a native
// CCombo's Text does: the list opens only from the arrow, Alt+Down or a read-only field's click.
// Java owns the field's text and fires its Verify and Modify, so each edit is reported to it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/key_edit_order.dart';

/// Modify/Modify, as the generated CComboSwt.sendModifyModify sends it.
const _modify = 72;

final _sent = <(int, VEvent?)>[];

class _RecordingCComboSwt extends CComboSwt<VCCombo> {
  const _RecordingCComboSwt({required super.value});

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) => _sent.add((channel, payload));
}

VCCombo _editableCombo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 3
  ..style = SWT.NONE
  ..enabled = true
  ..items = const ['alpha', 'bravo', 'charlie', 'delta']
  ..text = ''
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

Future<void> _focusField(WidgetTester tester) async {
  _sent.clear();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topCenter,
      child: SizedBox(width: 200, height: 60, child: _RecordingCComboSwt(value: _editableCombo())),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tapAt(tester.getTopLeft(find.byType(_RecordingCComboSwt)) + const Offset(60, 30));
  await tester.pumpAndSettle();
  expect(find.byType(MenuItemButton), findsNothing, reason: 'sanity: a click on the field opens no list');
}

void main() {
  testWidgets('typing into the field leaves the list closed', (tester) async {
    await _focusField(tester);

    await tester.enterText(find.byType(TextField), 'x');
    await tester.pumpAndSettle();

    expect(find.byType(MenuItemButton), findsNothing, reason: 'the list opened on an edit of the field');
    expect(tester.widget<TextField>(find.byType(TextField)).controller!.text, 'x');
  });

  testWidgets('the focused field holds the key events Java sees behind its edits', (tester) async {
    await _focusField(tester);

    expect(KeyEditOrder.instance.editorFocused, isTrue,
        reason: 'a KeyUp could overtake the edit its KeyDown typed');
  });

  testWidgets('each edit of the field is reported to Java with the new text and the caret', (tester) async {
    await _focusField(tester);

    await tester.enterText(find.byType(TextField), 'x');
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'xy');
    await tester.pumpAndSettle();

    final edits = _sent.where((s) => s.$1 == _modify).map((s) => '${s.$2?.text}@${s.$2?.start}').toList();
    expect(edits, ['x@1', 'xy@2']);
  });
}
