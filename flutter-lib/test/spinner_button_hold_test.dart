// A native spinner's button steps the value as it is pressed, not when it is let go, and keeps stepping while it
// is held: Win32's up-down control and GTK's spin button repeat after a delay until the button comes up.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, wireChannelNames;

import 'delivery/support/deliver.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingSpinnerSwt extends SpinnerSwt<VSpinner> {
  const _CapturingSpinnerSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSpinner val, int channel, VEvent? payload) =>
      onEvent(wireChannelNames[channel]!, payload);
}

VSpinner _spinner({int style = SWT.BORDER, bool enabled = true, int seq = 10, int id = 42}) => VSpinner()
  ..id = id
  ..seq = seq
  ..style = style
  ..enabled = enabled
  ..minimum = 0
  ..maximum = 100
  ..selection = 50
  ..increment = 10
  ..digits = 0
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 40);

Future<List<(String, VEvent?)>> _pump(WidgetTester tester, {int style = SWT.BORDER, int id = 42}) async {
  final sent = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: _CapturingSpinnerSwt(
        value: _spinner(style: style, id: id),
        onEvent: (ev, payload) => sent.add((ev, payload)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return sent;
}

List<int?> _steps(List<(String, VEvent?)> sent) => [
      for (final (ev, payload) in sent)
        if (ev == 'Selection/Selection') payload!.detail
    ];

// Which steps a press made (1) and which a held button repeated (0).
List<int?> _pressed(List<(String, VEvent?)> sent) => [
      for (final (ev, payload) in sent)
        if (ev == 'Selection/Selection') payload!.button
    ];

void main() {
  testWidgets('a press on the up button steps before it is let go', (WidgetTester tester) async {
    final sent = await _pump(tester);

    final gesture = await tester.startGesture(tester.getCenter(find.byIcon(Icons.arrow_drop_up)));
    await tester.pump();
    expect(_steps(sent), [SWT.ARROW_UP]);

    await gesture.up();
    await tester.pumpAndSettle();
    expect(_steps(sent), [SWT.ARROW_UP], reason: 'the release steps no further');
  });

  testWidgets('a held down button keeps stepping until it is let go', (WidgetTester tester) async {
    final sent = await _pump(tester);

    final gesture = await tester.startGesture(tester.getCenter(find.byIcon(Icons.arrow_drop_down)));
    await tester.pump(const Duration(seconds: 1));
    final held = _steps(sent).length;
    expect(held, greaterThan(2));
    expect(_steps(sent), everyElement(SWT.ARROW_DOWN));
    expect(_pressed(sent).first, 1, reason: 'the press made the first step');
    expect(_pressed(sent).skip(1), everyElement(0), reason: 'the rest repeat it');

    await gesture.up();
    await tester.pump(const Duration(seconds: 1));
    expect(_steps(sent).length, held, reason: 'nothing repeats once the button is up');
  });

  testWidgets('a secondary press steps nothing', (WidgetTester tester) async {
    final sent = await _pump(tester);

    final gesture = await tester.startGesture(tester.getCenter(find.byIcon(Icons.arrow_drop_up)),
        buttons: kSecondaryButton);
    await tester.pump(const Duration(seconds: 1));
    await gesture.up();
    await tester.pumpAndSettle();
    expect(_steps(sent), isEmpty);
  });

  // Win32's and Cocoa's READ_ONLY spinner takes no typing, but its buttons still step it.
  testWidgets("a READ_ONLY spinner's button still steps", (WidgetTester tester) async {
    final sent = await _pump(tester, style: SWT.BORDER | SWT.READ_ONLY);

    await tester.tap(find.byIcon(Icons.arrow_drop_up));
    await tester.pumpAndSettle();

    expect(_steps(sent), [SWT.ARROW_UP]);
  });

  testWidgets('a held button the spinner is disabled under stops repeating', (WidgetTester tester) async {
    // An id of its own: the client keeps the stamps of the spinners the other tests delivered.
    final sent = await _pump(tester, id: 43);

    final gesture = await tester.startGesture(tester.getCenter(find.byIcon(Icons.arrow_drop_up)));
    await tester.pump();
    await deliverChange(_spinner(enabled: false, seq: 11, id: 43), keys: [WireKey.k_Control_enabled], base: 10);
    await tester.pump();
    await tester.pump(const Duration(seconds: 1));

    expect(tester.takeException(), isNull);
    expect(_steps(sent), [SWT.ARROW_UP]);
    await gesture.up();
  });

  // Win32 and GTK stop repeating while the pointer is off the held button, and go on once it is back.
  testWidgets('a held button repeats only while the pointer is on it', (WidgetTester tester) async {
    final sent = await _pump(tester);
    final up = tester.getCenter(find.byIcon(Icons.arrow_drop_up));

    final gesture = await tester.startGesture(up);
    await tester.pump();
    await gesture.moveTo(up - const Offset(80, 0));
    await tester.pump(const Duration(seconds: 1));
    expect(_steps(sent), [SWT.ARROW_UP], reason: 'off the button, nothing repeats');

    await gesture.moveTo(up);
    await tester.pump(const Duration(milliseconds: 300));
    expect(_steps(sent).length, greaterThan(1), reason: 'back on it, the repeat goes on');
    await gesture.up();
  });

  // The buttons are what an accessibility tool, and the E2E suite, find and press by role.
  testWidgets('a semantics tap on the up button steps once', (WidgetTester tester) async {
    final handle = tester.ensureSemantics();
    final sent = await _pump(tester, id: 44);

    final buttons = find.semantics.byFlag(SemanticsFlag.isButton);
    expect(buttons, findsAtLeast(2));
    tester.semantics.tap(buttons.first);
    await tester.pumpAndSettle();

    expect(_steps(sent), [SWT.ARROW_UP]);
    handle.dispose();
  });
}
