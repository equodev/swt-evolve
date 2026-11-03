// Covers what the ad-hoc per-event shapes lacked: verdicts matched to proposals by order, and a
// timed-out proposal that cannot be resolved twice by its own late verdict.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/veto_gate.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'support/channels.dart' as channels;
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, WireType;

void _deliver(String actionId, Object payload) {
  final body = TaggedValues.encode(payload);
  final frame = channels.frame(actionId, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  testWidgets('a gate stays disarmed until Java says a veto is possible', (tester) async {
    final gate = VetoGate.key()..attach(WireType.spinner, 7);
    addTearDown(gate.detach);

    expect(gate.armed, isFalse, reason: 'no listener on the Java side yet');
    _deliver('Spinner/7/key/vetoable', {'value': true});
    await tester.pump();
    expect(gate.armed, isTrue);
  });

  testWidgets('verdicts resolve proposals in order', (tester) async {
    final gate = VetoGate.key()..attach(WireType.text, 3);
    addTearDown(gate.detach);
    _deliver('Text/3/key/vetoable', {'value': true});
    await tester.pump();

    final seen = <String>[];
    gate.propose((doit) => seen.add('first:$doit'));
    gate.propose((doit) => seen.add('second:$doit'));
    gate.propose((doit) => seen.add('third:$doit'));

    _deliver('Text/3/key/verdict', {'doit': false});
    await tester.pump();
    expect(seen, ['first:false'], reason: 'the head resolves, nothing behind it');

    _deliver('Text/3/key/verdict', {'doit': true});
    await tester.pump();
    _deliver('Text/3/key/verdict', {'doit': false});
    await tester.pump();
    expect(seen, ['first:false', 'second:true', 'third:false']);
  });

  testWidgets('a DragStart verdict without its doit is a veto', (tester) async {
    // The DragStart event leaves a false `doit` out, so a refusal can arrive empty.
    final gate = VetoGate.dragStart()..attach(WireType.canvas, 9);
    addTearDown(gate.detach);

    final seen = <bool>[];
    gate.propose(seen.add);
    _deliver('Canvas/9/DragDetect/dragStartResult', <String, Object?>{});
    await tester.pump();
    expect(seen, [false]);
  });

  testWidgets('a key verdict without its doit allows', (tester) async {
    final gate = VetoGate.key()..attach(WireType.text, 13);
    addTearDown(gate.detach);
    _deliver('Text/13/key/vetoable', {'value': true});
    await tester.pump();

    final seen = <bool>[];
    gate.propose(seen.add);
    _deliver('Text/13/key/verdict', <String, Object?>{});
    await tester.pump();
    expect(seen, [true]);
  });

  testWidgets('a menu verdict without its doit allows', (tester) async {
    final gate = VetoGate.optimistic(WireChannel.menuVerdict)..attach(WireType.canvas, 14);
    addTearDown(gate.detach);

    final seen = <bool>[];
    gate.propose(seen.add);
    _deliver('Canvas/14/menu/verdict', <String, Object?>{});
    await tester.pump();
    expect(seen, [true]);
  });

  testWidgets('a proposal fails open when no verdict arrives', (tester) async {
    final gate = VetoGate.modify(timeout: const Duration(milliseconds: 400))
      ..attach(WireType.text, 4);
    addTearDown(gate.detach);
    _deliver('Text/4/modify/vetoable', {'value': true});
    await tester.pump();

    bool? verdict;
    gate.propose((doit) => verdict = doit);
    expect(verdict, isNull, reason: 'held while Java is still thinking');

    await tester.pump(const Duration(milliseconds: 400));
    expect(verdict, isTrue, reason: 'a lost verdict must not swallow the input');
  });

  testWidgets('a late verdict cannot resolve the proposal behind a timed-out one',
      (tester) async {
    final gate = VetoGate.modify(timeout: const Duration(milliseconds: 400))
      ..attach(WireType.text, 5);
    addTearDown(gate.detach);
    _deliver('Text/5/modify/vetoable', {'value': true});
    await tester.pump();

    final seen = <String>[];
    gate.propose((doit) => seen.add('first:$doit'));
    await tester.pump(const Duration(milliseconds: 400));
    expect(seen, ['first:true'], reason: 'failed open');

    gate.propose((doit) => seen.add('second:$doit'));
    // Java's answer for the FIRST proposal finally turns up.
    _deliver('Text/5/modify/verdict', {'doit': false});
    await tester.pump();
    expect(seen, ['first:true'],
        reason: 'the stale verdict is consumed by its own spent proposal, not by the second');

    _deliver('Text/5/modify/verdict', {'doit': true});
    await tester.pump();
    expect(seen, ['first:true', 'second:true']);
  });

  testWidgets('gates are scoped per control, so one control cannot veto another',
      (tester) async {
    // The bug DragStartVeto had: a single process-wide flag, set by any control's answer.
    final a = VetoGate.key()..attach(WireType.tree, 11);
    final b = VetoGate.key()..attach(WireType.table, 12);
    addTearDown(a.detach);
    addTearDown(b.detach);

    _deliver('Tree/11/key/vetoable', {'value': true});
    await tester.pump();
    expect(a.armed, isTrue);
    expect(b.armed, isFalse, reason: 'arming Tree must not arm Table');

    _deliver('Table/12/key/vetoable', {'value': true});
    await tester.pump();
    final seen = <String>[];
    a.propose((doit) => seen.add('tree:$doit'));
    b.propose((doit) => seen.add('table:$doit'));

    _deliver('Tree/11/key/verdict', {'doit': false});
    await tester.pump();
    expect(seen, ['tree:false'], reason: "Tree's veto must not reach Table");

    _deliver('Table/12/key/verdict', {'doit': true});
    await tester.pump();
    expect(seen, ['tree:false', 'table:true']);
  });

  testWidgets('a verdict that arrives before its proposal is kept, not dropped',
      (tester) async {
    // The Display forwards the keystroke to Java from its own top-level handler, before the
    // focused widget's text pipeline has produced the edit to withhold — so Java's answer can
    // legitimately beat the proposal it belongs to.
    final gate = VetoGate.key()..attach(WireType.spinner, 11);
    addTearDown(gate.detach);
    _deliver('Spinner/11/key/vetoable', {'value': true});
    await tester.pump();

    _deliver('Spinner/11/key/verdict', {'doit': true});
    await tester.pump();

    bool? verdict;
    gate.propose((doit) => verdict = doit);
    expect(verdict, isTrue,
        reason: 'the early verdict resolves it at once, with no wait for the fail-open timer');
  });

  testWidgets('an early veto still vetoes', (tester) async {
    final gate = VetoGate.key()..attach(WireType.spinner, 12);
    addTearDown(gate.detach);
    _deliver('Spinner/12/key/vetoable', {'value': true});
    await tester.pump();
    _deliver('Spinner/12/key/verdict', {'doit': false});
    await tester.pump();

    bool? verdict;
    gate.propose((doit) => verdict = doit);
    expect(verdict, isFalse);
  });

  testWidgets('losing focus abandons a held proposal instead of committing it',
      (tester) async {
    final gate = VetoGate.key()..attach(WireType.spinner, 9);
    addTearDown(gate.detach);
    _deliver('Spinner/9/key/vetoable', {'value': true});
    await tester.pump();

    var ran = false;
    gate.propose((_) => ran = true);
    gate.armed = false; // focus moved away; Java re-arms on the next focus

    expect(ran, isFalse, reason: 'there is nothing left to commit the edit to');
    _deliver('Spinner/9/key/verdict', {'doit': true});
    await tester.pump();
    expect(ran, isFalse, reason: 'and a verdict arriving afterwards is inert');
  });
}
