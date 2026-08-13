// Guards a Text against dropping fast-typed characters when a full-state push echoes its pre-edit
// value. That value is never sent as a Modify, so PendingTextEchoes can't recognise it unless the
// baseline was seeded at focus-in — without the seed it is applied over live typing and resets the
// field. Type "acces", then deliver the unrecorded baseline "" as a stale push: it must survive.

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/text.dart';

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
      'Text keeps fast-typed text when a stale unrecorded baseline push arrives',
      (tester) async {
    final key = GlobalKey();

    VText value(String text) => VText()
      ..swt = 'Text'
      ..id = 1
      ..style = 0
      ..enabled = true
      ..editable = true
      ..text = text
      ..bounds = _bounds(200, 30);

    Widget appWith(VText v) => _app(SizedBox(
          width: 200,
          height: 30,
          child: TextSwt<VText>(key: key, value: v),
        ));

    // Empty filter field; focusing + typing begins the edit session.
    await tester.pumpWidget(appWith(value('')));

    final field = find.byType(EditableText);
    // Type a word quickly, one Modify per keystroke.
    for (final t in const ['a', 'ac', 'acc', 'acce', 'acces']) {
      await tester.enterText(field, t);
      await tester.pump();
    }
    expect(find.text('acces'), findsOneWidget, reason: 'sanity: field holds the typed word');

    // A Shell-level re-serialization lands carrying the pre-edit baseline "" — a value the client
    // never sent as a Modify (so it is not in the recorded in-flight set) while Java's async queue
    // was still behind the burst.
    await tester.pumpWidget(appWith(value('')));
    await tester.pump();

    expect(find.text('acces'), findsOneWidget,
        reason: 'a stale unrecorded baseline push must NOT wipe the fast-typed text');
  });
}
