// An SWT text field has no autofill. On the web, a field with autofill enabled leaves the form the
// engine built for it in the page when it loses the focus, so every field focused added one.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

VText _field() => VText()
  ..id = 61
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = ''
  ..visible = true
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 22);

void main() {
  setUp(() {
    VRegistry.instance.clear();
    FocusRequests.instance.reset();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a text field asks for no autofill', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 300, height: 100, child: TextSwt<VText>(value: _field())),
    ));
    await tester.pumpAndSettle();
    FocusRequests.instance.request(61);
    await tester.pump();
    await tester.pump();

    final editable = tester.state<EditableTextState>(find.byType(EditableText));
    expect(editable.textInputConfiguration.autofillConfiguration.enabled, isFalse);
  });
}
