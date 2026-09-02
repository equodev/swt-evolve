// SWT contract: a StyledText runs its ST.VerifyKey listeners before acting on a key, and a vetoed
// key leaves the editor untouched. JFace's ContentAssistant is built on that -- while its proposal
// popup is up it vetoes the arrow keys, drives the popup selection with them, and expects the
// editor caret to stay put so the next typed character still filters at the same prefix.
//
// The client applies caret navigation optimistically, so a vetoed arrow moved the caret anyway and
// the character typed after it was inserted a line below (Eclipse IDE, content assist). A
// text-changing key survives that because Java drops the Modify a veto produced and re-pushes; a
// caret-only key produces no Modify and no differing text, so nothing corrected it.
//
// Java flags a vetoable editor with `{swt}/{id}/verifyKey/vetoable`; the client applies caret
// navigation immediately and *undoes* it when `{swt}/{id}/verifyKey/verdict` rejects the key, in
// order, one answer per forwarded key. It is applied rather than withheld because a JDT editor
// keeps an ST.VerifyKey listener attached for its whole life, not only while a popup is up --
// withholding would have cost every arrow key a round trip.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it --
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/styledtext.dart';

/// SWT.ARROW_DOWN — the keyCode a verdict names so it can only resolve the key it belongs to.
const int _kArrowDown = 16777218;

const double _kWidth = 240;
const double _kHeight = 60;

/// The generated StyledTextSwt with sendEvent captured instead of handed to EquoCommService
/// (a widget test has no live transport). The State and its real key path are untouched.
class _CapturingStyledTextSwt extends StyledTextSwt<VStyledText> {
  const _CapturingStyledTextSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VStyledText val, String ev, VEvent? payload) => onEvent(ev, payload);
}

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body)
/// so the widget's channel subscriptions receive it.
void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  // Two lines of different lengths: an unheld Arrow Down off the longer one lands at a different
  // offset, which is what makes the following character land in the wrong place.
  const text = '        s.\n    }';

  VStyledText value() => VStyledText()
    ..swt = 'StyledText'
    ..id = 1
    ..style = 0
    ..enabled = true
    ..editable = true
    ..caretOffset = 0
    ..text = text
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = _kWidth.toInt()
      ..height = _kHeight.toInt());

  /// Enters the editor with the caret at the end of the first line -- where content assist sits
  /// after `s.` -- and reports that offset, as the Selection the click sent tells it.
  Future<int> clickIntoFirstLineEnd(
    WidgetTester tester,
    List<VEvent?> selections,
  ) async {
    final topLeft = tester.getTopLeft(find.byType(_CapturingStyledTextSwt));
    // Past the end of the first line: SWT clamps such a click to the nearest offset on that line.
    await tester.tapAt(topLeft + const Offset(_kWidth - 4, 6));
    await tester.pump();
    expect(selections, isNotEmpty,
        reason: 'sanity: the click must start an edit and report its caret');
    return selections.last!.start!;
  }

  Widget appWith(void Function(String, VEvent?) onEvent) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: _kWidth,
          height: _kHeight,
          child: _CapturingStyledTextSwt(value: value(), onEvent: onEvent),
        ),
      );

  Future<void> pressArrowDown() async {
    await simulateKeyDownEvent(LogicalKeyboardKey.arrowDown);
    await simulateKeyUpEvent(LogicalKeyboardKey.arrowDown);
  }

  testWidgets('a vetoed Arrow Down leaves the next character at the same offset',
      (tester) async {
    final selections = <VEvent?>[];
    final modifies = <VEvent?>[];
    Widget app() => appWith((ev, payload) {
          if (ev == 'Selection/Selection') selections.add(payload);
          if (ev == 'Modify/Modify') modifies.add(payload);
        });

    // initState doesn't run extraSetState, so a second push is needed to build the text shape
    // the click enters edit mode on (in production Java always sends this push).
    await tester.pumpWidget(app());
    await tester.pumpWidget(app());
    final caret = await clickIntoFirstLineEnd(tester, selections);

    // Java: content assist just attached its VerifyKeyListener.
    _receiveJson('StyledText/1/verifyKey/vetoable', {'value': true});
    await tester.pump();

    await pressArrowDown();
    await tester.pump();
    // The assistant consumed the arrow to move its proposal selection.
    _receiveJson('StyledText/1/verifyKey/verdict',
        {'doit': false, 'keyCode': _kArrowDown});
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyU, character: 'u');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyU);
    await tester.pump();

    expect(modifies, isNotEmpty, reason: 'the typed character must reach Java');
    expect(modifies.last!.text, 'u');
    expect(modifies.last!.start, caret,
        reason: 'a vetoed arrow must not have moved the caret the character is typed at');
  });

  testWidgets('a caret move survives a verdict that never arrives', (tester) async {
    final selections = <VEvent?>[];
    final modifies = <VEvent?>[];
    Widget app() => appWith((ev, payload) {
          if (ev == 'Selection/Selection') selections.add(payload);
          if (ev == 'Modify/Modify') modifies.add(payload);
        });

    await tester.pumpWidget(app());
    await tester.pumpWidget(app());
    final caret = await clickIntoFirstLineEnd(tester, selections);

    _receiveJson('StyledText/1/verifyKey/vetoable', {'value': true});
    await tester.pump();

    await pressArrowDown();
    // No verdict: the entry expires instead of undoing a move Java never rejected.
    await tester.pump(const Duration(milliseconds: 600));

    await simulateKeyDownEvent(LogicalKeyboardKey.keyU, character: 'u');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyU);
    await tester.pump();

    expect(modifies, isNotEmpty);
    expect(modifies.last!.start, isNot(caret),
        reason: 'a lost verdict must not roll the caret back');
  });

  // Eclipse consumes plenty of keystrokes before they reach the widget, so Java cannot answer every
  // key the client forwards -- measured live at 482 forwarded against 423 answered. A verdict that
  // names a different key than the one outstanding must never undo it.
  testWidgets('a verdict naming another key undoes nothing', (tester) async {
    final selections = <VEvent?>[];
    final modifies = <VEvent?>[];
    Widget app() => appWith((ev, payload) {
          if (ev == 'Selection/Selection') selections.add(payload);
          if (ev == 'Modify/Modify') modifies.add(payload);
        });

    await tester.pumpWidget(app());
    await tester.pumpWidget(app());
    final caret = await clickIntoFirstLineEnd(tester, selections);

    _receiveJson('StyledText/1/verifyKey/vetoable', {'value': true});
    await tester.pump();

    await pressArrowDown();
    await tester.pump();
    // A rejection belonging to some other key (SWT.HOME) — the streams are out of step.
    _receiveJson('StyledText/1/verifyKey/verdict',
        {'doit': false, 'keyCode': 16777223});
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyU, character: 'u');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyU);
    await tester.pump();

    expect(modifies, isNotEmpty);
    expect(modifies.last!.start, isNot(caret),
        reason: "a mismatched verdict must not roll back a move Java allowed");
  });

  testWidgets('an allowed Arrow Down still moves the caret', (tester) async {
    final selections = <VEvent?>[];
    final modifies = <VEvent?>[];
    Widget app() => appWith((ev, payload) {
          if (ev == 'Selection/Selection') selections.add(payload);
          if (ev == 'Modify/Modify') modifies.add(payload);
        });

    await tester.pumpWidget(app());
    await tester.pumpWidget(app());
    final caret = await clickIntoFirstLineEnd(tester, selections);

    _receiveJson('StyledText/1/verifyKey/vetoable', {'value': true});
    await tester.pump();

    await pressArrowDown();
    await tester.pump();
    _receiveJson('StyledText/1/verifyKey/verdict',
        {'doit': true, 'keyCode': _kArrowDown});
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyU, character: 'u');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyU);
    await tester.pump();
    _receiveJson('StyledText/1/verifyKey/verdict',
        {'doit': true, 'keyCode': _kArrowDown});
    await tester.pump();

    expect(modifies, isNotEmpty);
    expect(modifies.last!.start, isNot(caret),
        reason: 'an allowed arrow keeps the move it already applied');
  });

  testWidgets('an editor nobody can veto for navigates without waiting', (tester) async {
    final selections = <VEvent?>[];
    final modifies = <VEvent?>[];
    Widget app() => appWith((ev, payload) {
          if (ev == 'Selection/Selection') selections.add(payload);
          if (ev == 'Modify/Modify') modifies.add(payload);
        });

    await tester.pumpWidget(app());
    await tester.pumpWidget(app());
    final caret = await clickIntoFirstLineEnd(tester, selections);

    // No vetoable flag: ordinary typing must not pay a round trip.
    await pressArrowDown();
    await tester.pump();

    await simulateKeyDownEvent(LogicalKeyboardKey.keyU, character: 'u');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyU);
    await tester.pump();

    expect(modifies, isNotEmpty);
    expect(modifies.last!.start, isNot(caret),
        reason: 'an unarmed editor applies navigation immediately, as before');
  });
}
