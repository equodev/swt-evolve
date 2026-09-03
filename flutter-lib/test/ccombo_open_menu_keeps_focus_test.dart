// Opening a CCombo's dropdown must not report a FocusOut to Java. Flutter moves focus into the
// menu overlay when the popup opens, which is not the control losing focus in SWT terms — and a
// JFace cell editor treats FocusOut as "the user left": CellEditor.focusLost() applies the value
// and deactivates the editor, so the popup appears to open and immediately close, dropping the
// user out of edit mode before anything can be picked.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

/// The generated CComboSwt with sendEvent captured instead of handed to EquoCommService.
class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

/// A JFace ComboBoxCellEditor combo as the reported application builds it: style 0, i.e. editable
/// rather than READ_ONLY, so Flutter's DropdownMenu takes focus when it opens.
VCCombo _cellEditorCombo() => VCCombo()
  ..swt = 'CCombo'
  ..id = 7
  ..style = 0
  ..enabled = true
  ..items = const ['String', 'Number', 'Boolean', 'Null']
  ..text = 'String'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

void main() {
  testWidgets('opening the dropdown does not report a FocusOut to Java', (tester) async {
    final events = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(
        child: SizedBox(
          width: 200,
          height: 24,
          child: _CapturingCComboSwt(
            value: _cellEditorCombo(),
            onEvent: (ev, _) => events.add(ev),
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // The cell editor has just been activated: JFace gives the combo focus.
    await tester.tap(find.byType(EditableText));
    await tester.pumpAndSettle();
    expect(events, contains('Focus/FocusIn'), reason: 'sanity: the editor took focus');
    events.clear();

    // The user clicks the arrow to open the option list.
    await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
    await tester.pumpAndSettle();

    expect(find.text('Number'), findsWidgets, reason: 'sanity: the popup is open');
    expect(events, isNot(contains('Focus/FocusOut')),
        reason: 'focus moving into the open menu is not the control losing focus; reporting it '
            'makes a JFace cell editor apply and deactivate, closing the popup at once');
  });

  testWidgets('genuinely leaving the combo still reports a FocusOut', (tester) async {
    final events = <String>[];
    final outside = FocusNode();
    addTearDown(outside.dispose);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        children: [
          SizedBox(
            width: 200,
            height: 24,
            child: _CapturingCComboSwt(
              value: _cellEditorCombo(),
              onEvent: (ev, _) => events.add(ev),
            ),
          ),
          SizedBox(width: 200, height: 24, child: TextField(focusNode: outside)),
        ],
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(EditableText).first);
    await tester.pumpAndSettle();
    events.clear();

    outside.requestFocus();
    await tester.pumpAndSettle();

    expect(events, contains('Focus/FocusOut'),
        reason: 'suppressing the menu case must not suppress a real focus loss, or the cell '
            'editor would never commit');
  });
}
