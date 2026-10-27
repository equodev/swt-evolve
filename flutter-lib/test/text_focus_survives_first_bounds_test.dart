// A cell editor is created, focused, and only then placed: its first state push carries no bounds
// and the real ones arrive at least one frame later. Whatever the Text does with them, the field
// itself must survive -- a remount disposes its EditableTextState, which closes the text input
// connection, and the keyboard falls back to whatever held it before. That is how a NatTable cell
// editor loses focus to the navigation Tree the moment its bounds land.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

const int _editorId = 7311;

/// The editor as it first reaches the client: created and about to be focused, not yet placed.
VText _unplacedEditor() => VText()
  ..swt = 'Text'
  ..id = _editorId
  ..style = SWT.SINGLE
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = '';

/// The bounds, on the Text's own channel, one frame after it was created and focused.
Future<void> _javaPlacesTheEditor(WidgetTester tester) async {
  VRegistry.instance.apply('Text/$_editorId', <String, dynamic>{
    'id': _editorId,
    'swt': 'Text',
    '_s': 1,
    '_b': 0,
    '_d': <String>['bounds'],
    'bounds': <String, dynamic>{'x': 40, 'y': 300, 'width': 99, 'height': 19},
  });
  await tester.pump();
}

State _field(WidgetTester tester) => tester.state(find.byType(EditableText));

void main() {
  testWidgets('a focused Text keeps its field when the bounds arrive after it', (tester) async {
    VRegistry.instance.clear();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(child: TextSwt<VText>(value: _unplacedEditor())),
    ));
    await tester.pumpAndSettle();

    final focusNode = tester.widget<TextField>(find.byType(TextField)).focusNode!;
    focusNode.requestFocus();
    await tester.pumpAndSettle();
    expect(focusNode.hasFocus, isTrue,
        reason: 'sanity: the editor holds the keyboard before its bounds arrive');
    final opened = _field(tester);

    await _javaPlacesTheEditor(tester);

    expect(_field(tester), same(opened),
        reason: 'the bounds remounted the field: a fresh EditableTextState means the old one was '
            'disposed, which closes the input connection and drops the keyboard');
    expect(focusNode.hasFocus, isTrue);
  });
}
