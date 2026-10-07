// A focus request that arrives together with a move of the control must focus it where it now is.
// Focusing opens the platform text input connection and tells it where the field sits on screen.
// Opened before the move is laid out, it is told the old place, and on desktop the field then takes
// no typed characters.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int editorId = 41;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VText _editor(VRectangle bounds, int seq) => VText()
  ..id = editorId
  ..seq = seq
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = ''
  ..visible = true
  ..enabled = true
  ..bounds = bounds;

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a control moved and focused in one update tells the text input where it now is',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: CompositeSwt<VComposite>(
          value: VComposite()
            ..id = 40
            ..style = SWT.NONE
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 400, 300)
            ..children = [_editor(_rect(0, 0, 100, 22), 1)],
        ),
      ),
    ));
    await tester.pumpAndSettle();
    tester.testTextInput.log.clear();

    // One Java pass: the editor is placed over its cell and focused.
    final moved = jsonDecode(jsonEncode(_editor(_rect(160, 120, 100, 22), 2).toJson()))
        as Map<String, dynamic>;
    VRegistry.instance.apply('Text/$editorId', moved..['_s'] = 2);
    FocusRequests.instance.request(editorId);
    await tester.pump();
    await tester.pump();

    final placed = tester.getTopLeft(find.byType(EditableText));
    final told = tester.testTextInput.log
        .where((call) => call.method == 'TextInput.setEditableSizeAndTransform')
        .map((call) => (call.arguments as Map)['transform'] as List)
        .first;
    expect(Offset((told[12] as num).toDouble(), (told[13] as num).toDouble()), placed,
        reason: 'the connection must be opened where the field is laid out, not where it was');
  });
}
