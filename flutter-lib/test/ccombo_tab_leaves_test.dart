// Tab leaves an editable CCombo from its field for the next control, as SWT's CCombo, whose arrow is
// no Tab stop. A READ_ONLY one stops on its arrow instead (ccombo_keys_on_the_arrow_test.dart).

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

bool _focusWithin<T extends Widget>() {
  final context = FocusManager.instance.primaryFocus?.context;
  if (context == null) return false;
  var found = context.widget is T;
  context.visitAncestorElements((element) {
    found = element.widget is T;
    return !found;
  });
  return found;
}

void main() {
  setUp(() {
    FocusRequests.instance.reset();
    VRegistry.instance.clear();
    startDisplayKeyForwarding();
  });
  tearDown(() {
    stopDisplayKeyForwarding();
    FocusRequests.instance.reset();
  });

  testWidgets('Tab from an editable CCombo\'s field goes to the next control, not to the arrow', (tester) async {
    final combo = VCCombo()
      ..id = 5
      ..style = SWT.BORDER
      ..visible = true
      ..enabled = true
      ..items = const ['alpha', 'bravo', 'charlie']
      ..text = ''
      ..listVisible = false
      ..bounds = _rect(10, 10, 200, 30);
    final spinner = VSpinner()
      ..id = 6
      ..style = SWT.BORDER
      ..visible = true
      ..enabled = true
      ..maximum = 100
      ..bounds = _rect(250, 10, 100, 30);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 100,
        child: CompositeSwt<VComposite>(
          value: VComposite()
            ..id = 1
            ..style = SWT.NONE
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 400, 100)
            ..children = [combo, spinner],
        ),
      ),
    ));
    await tester.pumpAndSettle();
    // The pointer stays over the CCombo, which keeps its menu mounted, as in a click and a Tab.
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(110, 25));
    await tester.pumpAndSettle();
    await tester.tapAt(const Offset(110, 25), kind: PointerDeviceKind.mouse);
    await tester.pumpAndSettle();
    expect(_focusWithin<CComboSwt>(), isTrue, reason: 'sanity: the click focused the CCombo');

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();

    expect(_focusWithin<SpinnerSwt>(), isTrue);
  });
}
