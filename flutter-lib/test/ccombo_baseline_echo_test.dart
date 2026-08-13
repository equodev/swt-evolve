// CCombo counterpart of the Text baseline-echo guard: an editable combo must not drop fast-typed
// characters when a full-state push echoes its pre-edit value (unrecorded, so only recognised as an
// echo when the baseline is seeded at focus-in). Drives the SWT.SIMPLE combo's per-keystroke path:
// type "acces", then deliver the unrecorded baseline "" as a stale push — it must survive.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ccombo_evolve.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: child,
    );

void main() {
  testWidgets(
      'CCombo keeps fast-typed text when a stale unrecorded baseline push arrives',
      (tester) async {
    final key = GlobalKey<CComboImpl>();

    VCCombo value(String text) => VCCombo()
      ..swt = 'CCombo'
      ..id = 1
      ..style = SWT.SIMPLE
      ..enabled = true
      ..editable = true
      ..items = const []
      ..text = text
      ..bounds = _bounds(200, 40);

    Widget appWith(VCCombo v) => _app(SizedBox(
          width: 200,
          height: 300,
          child: CComboSwt<VCCombo>(key: key, value: v),
        ));

    await tester.pumpWidget(appWith(value('')));

    final field = find.byType(EditableText);
    for (final t in const ['a', 'ac', 'acc', 'acce', 'acces']) {
      await tester.enterText(field, t);
      await tester.pump();
    }
    expect(find.text('acces'), findsOneWidget, reason: 'sanity: field holds the typed word');

    // A stale full-state push carrying the pre-edit baseline "" — a value the client never sent as
    // a Modify — lands after the user has typed past it.
    await tester.pumpWidget(appWith(value('')));
    await tester.pump();

    expect(find.text('acces'), findsOneWidget,
        reason: 'a stale unrecorded baseline push must NOT wipe the fast-typed text');
  });
}
