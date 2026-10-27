// A Composite Java focuses must take the client's focus too. A grid's cell editor that frames its
// field closes by disposing the field and focusing the frame it leaves behind; if the frame ignored
// the request, the focus the field held would fall back to whatever control was focused before it,
// and Java would be told that control took focus.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int frameId = 3;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('a focus request for an empty Composite moves focus onto it', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 200,
        child: CompositeSwt<VComposite>(
          value: VComposite()
            ..id = 1
            ..style = SWT.NONE
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 400, 200)
            ..children = [
              VText()
                ..id = 2
                ..style = SWT.SINGLE | SWT.BORDER
                ..text = 'look for'
                ..visible = true
                ..enabled = true
                ..bounds = _rect(10, 10, 150, 24),
              VComposite()
                ..id = frameId
                ..style = SWT.NONE
                ..visible = true
                ..enabled = true
                ..bounds = _rect(200, 10, 100, 24),
            ],
        ),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.showKeyboard(find.byType(EditableText));
    await tester.pumpAndSettle();

    FocusRequests.instance.request(frameId);
    await tester.pumpAndSettle();
    await tester.pump();

    expect(tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus, isFalse,
        reason: 'the field focused earlier must not keep the focus Java gave the Composite');
    expect(FocusManager.instance.primaryFocus?.debugLabel, 'CompositeSurface');
  });

  testWidgets('a Composite focused while it still holds its field takes focus once the field is gone',
      (tester) async {
    const lookForId = 20, editorId = 21;
    VComposite frame(List<VText> children, int seq) => VComposite()
      ..id = frameId
      ..seq = seq
      ..style = SWT.NONE
      ..visible = true
      ..enabled = true
      ..bounds = _rect(0, 0, 200, 30)
      ..children = children;
    final editor = VText()
      ..id = editorId
      ..style = SWT.SINGLE
      ..text = ''
      ..visible = true
      ..enabled = true
      ..bounds = _rect(2, 2, 196, 26);
    Widget app(VComposite frameValue) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: Row(children: [
            SizedBox(
              width: 200,
              height: 30,
              child: TextSwt<VText>(
                value: VText()
                  ..id = lookForId
                  ..style = SWT.SINGLE | SWT.BORDER
                  ..text = 'look for'
                  ..visible = true
                  ..enabled = true
                  ..bounds = _rect(0, 0, 200, 30),
              ),
            ),
            SizedBox(width: 200, height: 30, child: CompositeSwt<VComposite>(value: frameValue)),
          ]),
        );

    await tester.pumpWidget(app(frame([editor], 1)));
    await tester.pumpAndSettle();
    await tester.showKeyboard(find.byType(EditableText).first);
    await tester.pumpAndSettle();
    FocusRequests.instance.request(editorId);
    await tester.pumpAndSettle();
    await tester.pump();

    // Escape in the editor: the frame is focused first, while the field is still in it, and the
    // field is removed by the next update.
    FocusRequests.instance.request(frameId);
    await tester.pumpAndSettle();
    final emptied = jsonDecode(jsonEncode(frame([], 2).toJson())) as Map<String, dynamic>;
    VRegistry.instance.apply('Composite/$frameId', emptied..['_s'] = 2);
    await tester.pumpAndSettle();
    await tester.pump();

    expect(tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus, isFalse,
        reason: 'focus must not fall back to the field focused before the editor');
    expect(FocusManager.instance.primaryFocus?.debugLabel, 'CompositeSurface');
  });
}
