// A Canvas that holds the keyboard focus keeps it when the application places a control over it -- a
// grid's cell editor opening in the click that focused the grid. Dropping the Canvas's focus node at
// that moment sends the focus back to whichever control held it before, and Java is told that
// control took focus: the editor closes as it opens and the keys go to the other control.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

const int gridId = 30, lookForId = 31, editorId = 32;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

class _RecordingCanvasSwt extends CanvasSwt<VCanvas> {
  const _RecordingCanvasSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VCanvas val, String ev, VEvent? payload) => sent.add(ev);
}

VCanvas _grid(List<VText> children, int seq) => VCanvas()
  ..id = gridId
  ..seq = seq
  ..style = SWT.NONE
  ..visible = true
  ..enabled = true
  ..bounds = _rect(0, 0, 300, 200)
  ..children = children;

VText _text(int id, VRectangle bounds) => VText()
  ..id = id
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

  testWidgets('a focused Canvas keeps the focus when an editor is placed over it, and the editor takes it',
      (tester) async {
    final gridEvents = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Row(children: [
        SizedBox(
          width: 200,
          height: 30,
          child: TextSwt<VText>(value: _text(lookForId, _rect(0, 0, 200, 30))),
        ),
        SizedBox(
          width: 300,
          height: 200,
          child: _RecordingCanvasSwt(sent: gridEvents, value: _grid([], 1)),
        ),
      ]),
    ));
    await tester.pumpAndSettle();
    await tester.showKeyboard(find.byType(EditableText));
    await tester.pumpAndSettle();

    await tester.tapAt(tester.getCenter(find.byType(_RecordingCanvasSwt)));
    await tester.pumpAndSettle();
    final lookFor = tester.widget<EditableText>(find.byType(EditableText)).focusNode;
    expect(lookFor.hasFocus, isFalse, reason: 'the click gave the grid the focus');

    // The click opened an editor over a cell: the grid's next update carries it, and Java asks for
    // the editor to be focused.
    final withEditor = jsonDecode(jsonEncode(
        _grid([_text(editorId, _rect(40, 20, 100, 22))], 2).toJson())) as Map<String, dynamic>;
    VRegistry.instance.apply('Canvas/$gridId', withEditor..['_s'] = 2);
    await tester.pump();

    expect(lookFor.hasFocus, isFalse,
        reason: 'focus must not fall back to the field focused before the grid');

    gridEvents.clear();
    FocusRequests.instance.request(editorId);
    await tester.pumpAndSettle();
    await tester.pump();

    final editor = tester.widgetList<EditableText>(find.byType(EditableText)).last.focusNode;
    expect(editor.hasFocus, isTrue);
    expect(gridEvents.where((ev) => ev.contains('FocusIn')), isEmpty,
        reason: 'the editor holding the focus is not the grid holding it');

    // A key the editor does not consume bubbles up through the grid; the grid consuming it would
    // stop the platform from delivering the typed character to the editor.
    final handled = await tester.sendKeyEvent(LogicalKeyboardKey.keyA);
    expect(handled, isFalse, reason: 'the grid must leave its editor\'s keys alone');
    expect(gridEvents.where((ev) => ev.startsWith('Key/')), isEmpty);
  });
}
