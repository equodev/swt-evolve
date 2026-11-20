// A control's identifier is how anything outside the canvas finds it, whatever its enablement.
// A control whose enablement changes is rebuilt, not mounted again: what it holds, as its focus or
// a running animation, stays.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

import 'delivery/support/deliver.dart';

VButton _button({required bool enabled, int seq = 10}) => VButton()
  ..id = 7
  ..seq = seq
  ..style = SWT.PUSH
  ..text = '&Next >'
  ..enabled = enabled
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 92
    ..height = 26);

Widget _app(VButton button) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 92, height: 26, child: ButtonSwt<VButton>(value: button)),
      ),
    );

/// The button's own focus, deepest in what it builds.
final _innermostFocus = find.descendant(of: find.byType(ButtonSwt<VButton>), matching: find.byType(Focus)).last;

/// The text the widget tagged [identifier] paints, or null when it is not laid out.
String? _textOf(String identifier) {
  final found = TestAnchors.nodes([identifier], paintedText: true);
  return found.isEmpty ? null : found.single['text'] as String;
}

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);
  setUp(freshClient);

  for (final (from, to) in [(false, true), (true, false)]) {
    testWidgets('a button going from ${from ? 'enabled' : 'disabled'} to ${to ? 'enabled' : 'disabled'} '
        'is still found by its identifier and read by its text', (tester) async {
      final button = _button(enabled: from);
      await tester.pumpWidget(_app(button));
      await tester.pumpAndSettle();
      expect(TestAnchors.locate('Button/7'), isNotNull);
      expect(_textOf('Button/7'), 'Next >');
      final focus = tester.state(_innermostFocus);

      button
        ..enabled = to
        ..seq = 20;
      await deliverChange(button, keys: [WireKey.k_Control_enabled], base: 10);
      await tester.pumpAndSettle();

      expect(TestAnchors.locate('Button/7'), isNotNull, reason: 'the button is found by its identifier');
      expect(_textOf('Button/7'), 'Next >', reason: 'and read by its text');
      expect(tester.state(_innermostFocus), same(focus), reason: 'the button is rebuilt, not mounted again');
    });
  }

  testWidgets('a button changing anything but its enablement is still found by its identifier', (tester) async {
    final button = _button(enabled: true)..primary = true;
    await tester.pumpWidget(_app(button));
    await tester.pumpAndSettle();
    expect(TestAnchors.locate('Button/7'), isNotNull);

    button
      ..primary = false
      ..seq = 20;
    await deliverChange(button, keys: [WireKey.k_Button_primary], base: 10);
    await tester.pumpAndSettle();

    expect(TestAnchors.locate('Button/7'), isNotNull);
    expect(_textOf('Button/7'), 'Next >');
  });
}
