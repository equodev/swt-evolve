// An editable field keeps the focus while the application replaces its content (a master-detail
// view loading another record into it). Once Java has replaced the text with a value the field
// never sent, every value the field sent before is history: a later setText carrying one of them
// (the first record's value, edited or as loaded) must show, not be taken for a stale echo.
// Text, CCombo and Combo share that echo guard.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

import 'support/java_push.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

const _original = '078 902 47 15';
const _edited = '078 902 47 99';
const _other = '076 318 44 7000';

final _fields = <String, Widget Function(Key key, String text)>{
  'Text': (key, text) => TextSwt<VText>(
      key: key,
      value: VText()
        ..swt = 'Text'
        ..id = 1
        ..style = 0
        ..enabled = true
        ..editable = true
        ..text = text
        ..bounds = _bounds(200, 30)),
  'CCombo': (key, text) => CComboSwt<VCCombo>(
      key: key,
      value: VCCombo()
        ..swt = 'CCombo'
        ..id = 1
        ..style = SWT.SIMPLE
        ..enabled = true
        ..items = const []
        ..text = text
        ..bounds = _bounds(200, 40)),
  'Combo': (key, text) => ComboSwt<VCombo>(
      key: key,
      value: VCombo()
        ..swt = 'Combo'
        ..id = 1
        ..style = SWT.DROP_DOWN
        ..enabled = true
        ..items = const []
        ..text = text
        ..bounds = _bounds(200, 34)),
};

void main() {
  for (final MapEntry(key: widget, value: field) in _fields.entries) {
    final key = GlobalKey();

    Widget app(String text) => ForceLive(
        child: EvolveApp(
            theme: ThemeMode.light,
            contentWidget: SizedBox(width: 200, height: 300, child: Align(
                alignment: Alignment.topLeft, child: field(key, text)))));

    String shown(WidgetTester tester) => tester.widget<EditableText>(find.byType(EditableText)).controller.text;

    Future<void> javaPushes(WidgetTester tester, String text) =>
        javaPushesText(tester, find.byKey(key), text);

    Future<void> editThenSwitchAway(WidgetTester tester, {required bool acknowledged}) async {
      await tester.pumpWidget(app(_original));
      await tester.enterText(find.byType(EditableText), _edited);
      await tester.pump();
      if (acknowledged) await javaPushes(tester, _edited);
      await javaPushes(tester, _other);
      expect(shown(tester), _other, reason: 'a value the field never sent is applied');
    }

    for (final acknowledged in [true, false]) {
      final ack = acknowledged ? 'after Java acknowledged the edit' : 'before Java acknowledged the edit';

      testWidgets('a focused $widget shows the edited value set back by Java ($ack)', (tester) async {
        await editThenSwitchAway(tester, acknowledged: acknowledged);
        await javaPushes(tester, _edited);
        expect(shown(tester), _edited);
      });

      testWidgets('a focused $widget shows the starting value set back by Java ($ack)', (tester) async {
        await editThenSwitchAway(tester, acknowledged: acknowledged);
        await javaPushes(tester, _original);
        expect(shown(tester), _original);
      });
    }

    testWidgets('a focused $widget still ignores stale echoes of what it showed after a setText',
        (tester) async {
      await editThenSwitchAway(tester, acknowledged: false);
      await tester.enterText(find.byType(EditableText), '${_other}1');
      await tester.pump();
      await tester.enterText(find.byType(EditableText), '${_other}12');
      await tester.pump();
      await javaPushes(tester, _other);
      expect(shown(tester), '${_other}12', reason: 'the value set before typing is an echo');
      // CCombo forwards no Modify per keystroke, so it has no keystroke echo to recognise.
      if (widget != 'CCombo') {
        await javaPushes(tester, '${_other}1');
        expect(shown(tester), '${_other}12', reason: 'an earlier keystroke is an echo');
      }
    });
  }
}
