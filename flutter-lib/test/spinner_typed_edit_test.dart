// A native spinner reports every edit of its text field as it happens (Cocoa's textDidChange), not only the
// Enter that commits it: Java reads the value out of the text, notifies Selection when it moved, and
// notifies Modify. The edit goes up with the text it left, the range of the previous text it replaced and
// how many characters it put there, so Java can ask its Verify listeners about exactly those.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/batch.dart';

/// The generated SpinnerSwt, except sendEvent captures outgoing events instead of handing them to
/// EquoCommService, which needs a live transport.
class _CapturingSpinnerSwt extends SpinnerSwt<VSpinner> {
  const _CapturingSpinnerSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSpinner val, int channel, VEvent? payload) =>
      onEvent(wireChannelNames[channel]!, payload);
}

VSpinner _spinner({int selection = 123}) => VSpinner()
  ..id = 41
  ..style = SWT.BORDER
  ..enabled = true
  ..minimum = 0
  ..maximum = 100000
  ..selection = selection
  ..increment = 1
  ..digits = 0
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 32);

Future<void> _pump(WidgetTester tester, List<(String, VEvent?)> sent) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: _CapturingSpinnerSwt(
        value: _spinner(),
        onEvent: (ev, payload) => sent.add((ev, payload)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

String _shown(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).controller.text;

List<VEvent> _edits(List<(String, VEvent?)> sent) =>
    [for (final (ev, payload) in sent) if (ev == 'Modify/Modify') payload!];

// What Java answers to an edit it was asked to verify.
void _verdict(bool doit) => receiveTagged('Spinner/41/modify/verdict', {'doit': doit});

void main() {
  testWidgets('typing over the text sends the edit, not a commit',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '60');
    await tester.pump();

    final edits = _edits(sent);
    expect(edits, hasLength(1));
    expect(edits.single.text, '60');
    expect(edits.single.start, 0);
    expect(edits.single.end, 3, reason: 'the three characters it replaced');
    expect(edits.single.count, 2);
    expect(sent.where((s) => s.$1.startsWith('Selection/')), isEmpty,
        reason: 'only Enter or an arrow commits; the value is read in Java');
  });

  testWidgets('a character typed at the end replaces nothing',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '1234');
    await tester.pump();

    final edit = _edits(sent).single;
    expect([edit.text, edit.start, edit.end, edit.count], ['1234', 3, 3, 1]);
  });

  testWidgets('a character typed in the middle is placed where it went',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '1923');
    await tester.pump();

    final edit = _edits(sent).single;
    expect([edit.text, edit.start, edit.end, edit.count], ['1923', 1, 1, 1]);
  });

  testWidgets('deleting every character is an edit too, with no text left',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '');
    await tester.pump();

    final edit = _edits(sent).single;
    expect([edit.text, edit.start, edit.end, edit.count], ['', 0, 3, 0]);
  });

  testWidgets('every keystroke is reported, each against the text before it',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '12');
    await tester.enterText(find.byType(EditableText), '125');
    await tester.pump();

    final edits = _edits(sent);
    expect(edits.map((e) => e.text), ['12', '125']);
    expect(edits[1].start, 2);
    expect(edits[1].end, 2);
    expect(edits[1].count, 1);
  });

  testWidgets('an arrow sends the new value as a Selection and no edit',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.tap(find.byIcon(Icons.arrow_drop_up));
    await tester.pump();

    expect(_edits(sent), isEmpty, reason: 'Java notifies Modify for an arrow');
    final selections = [
      for (final (ev, payload) in sent)
        if (ev == 'Selection/Selection') payload!.index
    ];
    expect(selections, [124]);
  });

  testWidgets('Enter after typing still commits the value',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '60');
    await tester.testTextInput.receiveAction(TextInputAction.done);
    await tester.pump();

    final commits = [
      for (final (ev, payload) in sent)
        if (ev == 'Selection/DefaultSelection') payload!.index
    ];
    expect(commits, [60]);
    expect(_edits(sent), hasLength(1));
  });

  testWidgets('an edit Java vetoed is taken back to the value Java holds',
      (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '60');
    await tester.pump();
    expect(_shown(tester), '60');

    _verdict(false);
    await tester.pump();

    expect(_shown(tester), '123');
  });

  testWidgets('an edit Java let through stays', (WidgetTester tester) async {
    final sent = <(String, VEvent?)>[];
    await _pump(tester, sent);

    await tester.enterText(find.byType(EditableText), '60');
    _verdict(true);
    await tester.pump();

    expect(_shown(tester), '60');
  });
}
