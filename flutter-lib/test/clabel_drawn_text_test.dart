// A CLabel draws its text without the mnemonic markers and draws nothing for the placeholder '<none>',
// as a plain label and as the chip it becomes in a toolbar. The plain label's mnemonic case is in
// mnemonic_text_test.dart.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/swt.dart';

// Each label gets its own id: the widget registry keeps the state of an id it has seen.
int _nextId = 1;

VCLabel _label(String text) => VCLabel()
  ..id = _nextId++
  ..style = SWT.NONE
  ..enabled = true
  ..text = text;

// In a toolbar, a CLabel with an image, the hand cursor and a text or a tooltip is drawn as a chip.
VCLabel _chip(String text) => _label(text)
  ..cursor = (VCursor()..cursorStyle = SWT.CURSOR_HAND)
  ..image = VImage()
  ..toolTipText = 'Open';

Widget _host(VCLabel value, {bool inToolbar = false}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: ToolbarAreaMarker(
          active: inToolbar,
          child: CLabelSwt<VCLabel>(value: value),
        ),
      ),
    );

Finder _textsOf(String text) =>
    find.descendant(of: find.byType(CLabelSwt<VCLabel>), matching: find.text(text));

void main() {
  testWidgets("a CLabel draws nothing for the placeholder '<none>'", (tester) async {
    await tester.pumpWidget(_host(_label('<none>')));
    await tester.pumpAndSettle();

    expect(_textsOf('<none>'), findsNothing);
    expect(_textsOf(''), findsOneWidget);
  });

  testWidgets('a toolbar chip draws its text without the mnemonic marker', (tester) async {
    await tester.pumpWidget(_host(_chip('Sa&ve && Close'), inToolbar: true));
    await tester.pumpAndSettle();

    expect(_textsOf('Save & Close'), findsOneWidget);
    expect(_textsOf('Sa&ve && Close'), findsNothing);
  });

  testWidgets("a toolbar chip draws no text for the placeholder '<none>'", (tester) async {
    await tester.pumpWidget(_host(_chip('<none>'), inToolbar: true));
    await tester.pumpAndSettle();

    expect(
      find.descendant(of: find.byType(CLabelSwt<VCLabel>), matching: find.byType(Text)),
      findsNothing,
    );
  });
}
