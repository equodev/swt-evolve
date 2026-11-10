// Guards the stale-echo keystroke drop in PendingTextEchoes (Text). StyledText holds no
// local text: Java is its only editor.
//
// An editable field applies each keystroke optimistically and forwards it to Java as a
// Modify; Java echoes a full-state push back, but on a slow round trip the echo of an EARLIER
// keystroke can land AFTER the user has typed further. Unguarded, the incoming value resets
// the field to that older text, silently dropping the newer character. The mixin ignores
// echoes of our own in-flight edits so newer local keystrokes survive; a value we never sent
// is a genuine external change and is still applied.
//
// Each test types "A" then "AB" (two Modifies: "A", then "AB"), then simulates Java echoing
// the FIRST Modify ("A") back as a full-state push after the user already typed "AB". Before
// the guard the field collapses to "A"; with it, "AB" survives.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ccombo_evolve.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

import 'support/java_push.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: child,
    ));

void main() {
  testWidgets('Text keeps newer keystrokes when a stale Java echo arrives',
      (tester) async {
    final key = GlobalKey();

    VText value(String text) => VText()
      ..swt = 'Text'
      ..id = 1
      ..style = 0 // no READ_ONLY -> editable
      ..enabled = true
      ..editable = true
      ..text = text
      ..bounds = _bounds(200, 30);

    Widget appWith(VText v) => _app(SizedBox(
          width: 200,
          height: 30,
          child: TextSwt<VText>(key: key, value: v),
        ));

    await tester.pumpWidget(appWith(value('')));

    final field = find.byType(EditableText);
    await tester.enterText(field, 'A');
    await tester.pump();
    await tester.enterText(field, 'AB');
    await tester.pump();
    expect(find.text('AB'), findsOneWidget, reason: 'sanity: field holds typed text');

    // Stale echo of the first Modify ("A") arrives after "AB" was typed.
    await javaPushesText(tester, find.byKey(key), 'A');

    expect(find.text('AB'), findsOneWidget,
        reason: 'a stale echo of "A" must NOT clobber the newer local "AB"');
    expect(find.text('A'), findsNothing);
  });
}
