// A press on a StyledText reaches Java as a MouseDown, which focuses it the way the native does
// (ControlHelper.pressed and the StyledText's own press handling). A FocusIn sent ahead of that
// MouseDown activates the control without the press's SWT.MouseDown detail.

import 'package:flutter/gestures.dart' show kPrimaryButton, kSecondaryButton;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show resetConfigFlags;

VStyledText _styledText() => VStyledText()
  ..id = 4
  ..style = SWT.NONE
  ..enabled = true
  ..editable = true
  ..text = ''
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

/// Presses and releases [buttons] over the StyledText; returns the focus and MouseDown events sent.
Future<List<String>> _press(WidgetTester tester, int buttons) async {
  final sent = <String>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.contains('/Focus/') || action.endsWith('/Mouse/MouseDown')) {
      sent.add(action.split('/').last);
    }
  };
  addTearDown(() => EquoCommBase.sendObserverForTesting = null);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 200, height: 60, child: StyledTextSwt<VStyledText>(value: _styledText())),
    ),
  ));
  await tester.pump();
  sent.clear();
  final at = tester.getTopLeft(find.byType(StyledTextSwt<VStyledText>)) + const Offset(100, 30);
  final gesture = await tester.startGesture(at, buttons: buttons);
  await tester.pump();
  await gesture.up();
  await tester.pump(const Duration(seconds: 1));
  return sent;
}

void main() {
  setUp(resetConfigFlags);

  for (final (name, buttons) in [('primary', kPrimaryButton), ('secondary', kSecondaryButton)]) {
    testWidgets('a $name press sends its MouseDown with no FocusIn ahead of it', (tester) async {
      expect(await _press(tester, buttons), ['MouseDown']);
    });

    testWidgets('a $name press takes the keyboard once Java asks the client to focus it', (tester) async {
      final sent = await _press(tester, buttons);
      FocusRequests.instance.request(4);
      addTearDown(FocusRequests.instance.reset);
      await tester.pumpAndSettle();
      expect(FocusManager.instance.primaryFocus?.debugLabel, 'StyledText');
      expect(sent, ['MouseDown', 'FocusIn']);
    });
  }
}
