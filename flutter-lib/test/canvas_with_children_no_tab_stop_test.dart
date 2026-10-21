// A Canvas hosting children -- a Shell is one -- is no Tab stop of its own. Tab from its only child has
// nowhere else to go and stays there, as natively; walking onto the Canvas would report the Canvas
// focused and make Java deactivate the child.

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

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
  });
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('Tab from the only child of a Canvas does not focus the Canvas', (tester) async {
    final canvasEvents = <String>[];
    final canvas = VCanvas()
      ..id = 40
      ..style = SWT.NONE
      ..visible = true
      ..enabled = true
      ..bounds = _rect(0, 0, 300, 200)
      ..children = [
        VText()
          ..id = 41
          ..style = SWT.SINGLE | SWT.BORDER
          ..text = ''
          ..visible = true
          ..enabled = true
          ..bounds = _rect(20, 20, 160, 30),
      ];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 200, child: _RecordingCanvasSwt(sent: canvasEvents, value: canvas)),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.showKeyboard(find.byType(EditableText));
    await tester.pumpAndSettle();
    canvasEvents.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(canvasEvents.where((ev) => ev.contains('FocusIn')), isEmpty, reason: '$canvasEvents');
  });
}
