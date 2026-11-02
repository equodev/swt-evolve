// SWT contract: a VerifyListener runs BEFORE a keystroke's character is displayed — a vetoed
// keystroke is never rendered. The async bridge applies edits optimistically, so a vetoed
// character was rendered first and only reverted a round-trip later (and the revert push could
// be swallowed by the stale-echo guard, leaving the vetoed text on screen).
//
// Java flags a Verify-hooked field with `{swt}/{id}/modify/vetoable`; the field then holds each
// edit un-rendered, forwards it as a Modify proposal, and applies or drops it on the
// `{swt}/{id}/modify/verdict` answer. A `doit:false` verdict on an already-applied edit (no
// vetoable flag — e.g. the embedded backend) instead clears the echo guard so Java's corrective
// push is applied rather than mistaken for a stale echo.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel;

import 'delivery/support/deliver.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'support/channels.dart' as channels;
import 'package:swtflutter/src/impl/utils/force_live.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => ForceLive(child: EvolveApp(
      theme: ThemeMode.light,
      contentWidget: child,
    ));

VText _value(String text) => VText()
  ..swt = 'Text'
  ..id = 1
  ..style = 0 // no READ_ONLY -> editable
  ..enabled = true
  ..editable = true
  ..text = text
  ..bounds = _bounds(200, 30);

Widget _appWith(VText v) => _app(SizedBox(
      width: 200,
      height: 30,
      child: TextSwt<VText>(value: v),
    ));

/// Delivers an inbound frame exactly as the transport would (2-byte name length,
/// name, tagged-values body) so the widget's channel subscriptions receive it.
void _deliver(String actionId, Object payload) {
  final body = TaggedValues.encode(payload);
  final frame = channels.frame(actionId, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  testWidgets('a vetoable field never renders an edit until Java allows it',
      (tester) async {
    await tester.pumpWidget(_appWith(_value('1234')));

    // Java: this field has a VerifyListener — edits need a verdict.
    _deliver('Text/1/modify/vetoable', {'value': true});
    await tester.pump();

    final field = find.byType(EditableText);
    await tester.enterText(field, '12347');
    await tester.pump();

    expect(find.text('12347'), findsNothing,
        reason: 'SWT parity: the edit must not render before the Verify verdict');
    expect(find.text('1234'), findsOneWidget);

    // Java's VerifyListener vetoed (e.g. 12347 > 9999): the edit is dropped.
    _deliver('Text/1/modify/verdict', {'doit': false});
    await tester.pump();

    expect(find.text('12347'), findsNothing,
        reason: 'a vetoed keystroke is never displayed');
    expect(find.text('1234'), findsOneWidget);

    // An allowed edit still goes through on doit:true.
    await tester.enterText(field, '1239');
    await tester.pump();
    expect(find.text('1239'), findsNothing,
        reason: 'still gated until the verdict');

    _deliver('Text/1/modify/verdict', {'doit': true});
    await tester.pump();
    expect(find.text('1239'), findsOneWidget,
        reason: 'an accepted edit is applied on the verdict');
  });

  testWidgets(
      'a doit:false verdict on an applied edit lets the corrective push through',
      (tester) async {
    await tester.pumpWidget(_appWith(_value('1234')));

    // No vetoable flag (embedded backend): the edit applies optimistically.
    final field = find.byType(EditableText);
    await tester.enterText(field, '12347');
    await tester.pump();
    expect(find.text('12347'), findsOneWidget,
        reason: 'sanity: ungated edit applies locally');

    // Java rejected the Modify; its corrective push carries the pre-edit text,
    // which the stale-echo guard would otherwise swallow (the focus baseline).
    _deliver('Text/1/modify/verdict', {'doit': false});
    await tester.pump();
    await deliverWhole(_value('1234')..seq = 2);
    await tester.pump();

    expect(find.text('1234'), findsOneWidget,
        reason: "Java's rejection push must revert the field, not be treated "
            'as a stale echo');
    expect(find.text('12347'), findsNothing);
  });

  group('keystrokes typed before their verdicts arrive', () {
    late List<String> modifies;
    late Finder field;

    Future<void> pumpVetoable(WidgetTester tester) async {
      modifies = [];
      await tester.pumpWidget(_app(SizedBox(
        width: 200,
        height: 30,
        child: _RecordingTextSwt(value: _value(''), modifies: modifies),
      )));
      _deliver('Text/1/modify/vetoable', {'value': true});
      await tester.pump();
      field = find.byType(EditableText);
    }

    String shown(WidgetTester tester) => tester.widget<EditableText>(field).controller.text;

    // Each keystroke edits what the field shows, as the platform's text input does.
    Future<void> typeEach(WidgetTester tester, String chars) async {
      for (final char in chars.split('')) {
        await tester.enterText(field, shown(tester) + char);
        await tester.pump();
      }
    }

    Future<void> answer(WidgetTester tester, List<bool> verdicts) async {
      for (final doit in verdicts) {
        _deliver('Text/1/modify/verdict', {'doit': doit});
        await tester.pump();
      }
    }

    testWidgets('all survive', (tester) async {
      await pumpVetoable(tester);
      await typeEach(tester, 'abc');
      await answer(tester, [true, true, true]);

      expect(shown(tester), 'abc', reason: 'every allowed keystroke of a burst is kept');
      expect(modifies, ['a', 'ab', 'abc'],
          reason: 'Java decides each keystroke on the text it already allowed');
    });

    testWidgets('lose only the one Java vetoes', (tester) async {
      await pumpVetoable(tester);
      await typeEach(tester, 'a b');
      // Java vetoes the space and allows the rest.
      await answer(tester, [true, false, true]);

      expect(shown(tester), 'ab');
      expect(modifies, ['a', 'a ', 'ab'],
          reason: 'the keystroke after a veto is proposed on the text without the vetoed one');
    });

    testWidgets('never reach Java on top of a vetoed one', (tester) async {
      await pumpVetoable(tester);
      await typeEach(tester, '1a2');
      // A listener that lets through what has no typed character allows all but the 'a'.
      await answer(tester, [true, false, true]);

      expect(modifies.skip(2), everyElement(isNot(contains('a'))),
          reason: 'Java must not decide on, or commit, a text carrying the keystroke it vetoed');
      expect(shown(tester), '12');
    });
  });
}

/// Records the text of every Modify the field sends to Java.
class _RecordingTextSwt extends TextSwt<VText> {
  final List<String> modifies;
  const _RecordingTextSwt({required super.value, required this.modifies});

  @override
  void sendEvent(VText val, int channel, VEvent? payload) {
    if (channel == WireChannel.modifyModify) modifies.add(payload?.text ?? '');
    super.sendEvent(val, channel, payload);
  }
}
