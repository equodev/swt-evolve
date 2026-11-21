// Enter in a single-line Text: SWT reports KeyDown, DefaultSelection and only then KeyUp. Flutter
// submits the field on the text input action, a trip apart from the key events, so the release can
// come first: it still reaches Java after the DefaultSelection. Where no DefaultSelection comes, the
// release is not held back for good.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';
import 'package:swtflutter/src/impl/utils/traversal_order.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const int _displayId = 7;
const int _textId = 901;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

Future<void> _pumpText(WidgetTester tester, {int style = SWT.BORDER | SWT.SINGLE}) async {
  await tester.pumpWidget(ForceLive(
    child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..id = _displayId
          ..mainShellId = 900
          ..shells = [
            VShell()
              ..id = 900
              ..style = SWT.SHELL_TRIM
              ..text = 'shell'
              ..visible = true
              ..bounds = _rect(0, 0, 400, 300)
              ..children = [
                VText()
                  ..id = _textId
                  ..style = style
                  ..text = 'abc'
                  ..enabled = true
                  ..visible = true
                  ..bounds = _rect(10, 10, 160, 30),
              ],
          ],
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byType(TextField));
  await tester.pumpAndSettle();
}

/// The keys and DefaultSelections sent to Java, in order.
List<String> _sent() => EquoCommService.commForTesting.sentFrames
    .map((f) => EquoCommBase.decodeFrame(f).$1)
    .where((name) => name.contains('/Key/') || name.endsWith('/DefaultSelection'))
    .map((name) => name.split('/').last)
    .toList();

void main() {
  setUp(() {
    TraversalOrder.reset();
    resetConfigFlags();
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    FocusRequests.listen();
  });
  tearDown(() {
    EquoCommBase.recordSentFrames = false;
    resetConfigFlags();
    FocusRequests.instance.reset();
  });

  testWidgets('an Enter released before the Text submits reaches Java after its DefaultSelection',
      (tester) async {
    await _pumpText(tester);
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.enter);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.enter);
    await tester.pump();
    await tester.testTextInput.receiveAction(TextInputAction.done);
    await tester.pumpAndSettle(const Duration(milliseconds: 300));

    expect(_sent(), ['KeyDown', 'DefaultSelection', 'KeyUp']);
  });

  testWidgets('an Enter the Text never submits still reaches Java released', (tester) async {
    await _pumpText(tester);
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();

    await tester.sendKeyDownEvent(LogicalKeyboardKey.enter);
    await tester.sendKeyUpEvent(LogicalKeyboardKey.enter);
    await tester.pump(const Duration(seconds: 1));

    expect(_sent(), ['KeyDown', 'KeyUp']);
  });
}
