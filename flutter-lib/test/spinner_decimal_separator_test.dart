// The text of a Spinner with digits shows its decimal places with the separator of the user's locale, as every
// native spinner does, and getText() on the Java side answers the same text. Java sends the separator it uses, and
// this side writes it into the field and reads it back out of what is typed.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// The generated SpinnerSwt, except sendEvent captures outgoing events instead of handing them to
/// EquoCommService, which needs a live transport.
class _CapturingSpinnerSwt extends SpinnerSwt<VSpinner> {
  const _CapturingSpinnerSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSpinner val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

VSpinner _spinner({int digits = 2, String? separator, int selection = 123}) =>
    VSpinner()
      ..id = 31
      ..style = SWT.BORDER
      ..enabled = true
      ..minimum = 0
      ..maximum = 100000
      ..selection = selection
      ..increment = 1
      ..digits = digits
      ..decimalSeparator = separator
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 160
        ..height = 32);

Future<void> _pump(WidgetTester tester, VSpinner value,
    {void Function(String ev, VEvent? payload)? onEvent}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: _CapturingSpinnerSwt(value: value, onEvent: onEvent ?? (_, __) {}),
    ),
  ));
  await tester.pumpAndSettle();
}

String _shown(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).controller.text;

void main() {
  testWidgets('the field writes its decimals with the separator Java sent',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(separator: ','));

    expect(_shown(tester), '1,23');
  });

  testWidgets('without a separator the field uses a point',
      (WidgetTester tester) async {
    await _pump(tester, _spinner());

    expect(_shown(tester), '1.23');
  });

  testWidgets('a separator that is not a point is written once, for every place',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(separator: '\u066B', selection: 5));

    expect(_shown(tester), '0\u066B05');
  });

  testWidgets('a text typed with the separator is committed as that many places',
      (WidgetTester tester) async {
    final committed = <int>[];
    await _pump(tester, _spinner(separator: ','), onEvent: (ev, payload) {
      if (ev == 'Selection/DefaultSelection') committed.add(payload!.index!);
    });

    await tester.enterText(find.byType(EditableText), '2,5');
    await tester.testTextInput.receiveAction(TextInputAction.done);
    await tester.pump();

    expect(committed, [250]);
    expect(_shown(tester), '2,50');
  });

  testWidgets('a point is not accepted when the separator is another character',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(separator: ','));

    await tester.enterText(find.byType(EditableText), '1.5');
    await tester.pump();

    expect(_shown(tester), isNot(contains('.')));
  });

  testWidgets('changing the digits rewrites the text of the same value',
      (WidgetTester tester) async {
    await _pump(tester, _spinner(digits: 0, separator: ','));
    expect(_shown(tester), '123');

    // What an update from Java does: the state takes the new digits and the widget rebuilds.
    // ignore: avoid_dynamic_calls
    final dynamic widgetState = tester.state(find.byType(_CapturingSpinnerSwt));
    // ignore: avoid_dynamic_calls
    widgetState.state.digits = 2;
    // ignore: avoid_dynamic_calls
    widgetState.setValue(widgetState.state);
    await tester.pump();

    expect(_shown(tester), '1,23');
  });
}
