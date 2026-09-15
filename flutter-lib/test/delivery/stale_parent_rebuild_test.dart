// A parent holds its own copy of every child. When the parent rebuilds, Flutter hands each child
// that copy through didUpdateWidget - and the copy is whatever Java last told the *parent*, which is
// older than anything the child has since applied on its own channel.
//
// The trigger needs no message from Java at all. A hover, a layout pass, a sash drag - anything that
// rebuilds the parent on the Flutter side alone - is enough to push a stale child value back over a
// fresh one. That is what made an expandable section collapse on its own after being expanded, and
// it is why Java grew a habit of dirtying ancestors so their channel would re-send the subtree and
// overwrite the stale copies with fresh ones.
//
// These tests describe the behaviour, not the guard: a child shows what it was last told, whatever
// its parent rebuilds around it. There is one value per widget now, so a parent's child list is a
// list of the actual children rather than of copies, and there is nothing stale left to hand down.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';

const int _parentId = 500;
const int _childId = 501;
const String _childChannel = 'Label/$_childId';

VLabel _child(String text, {required int seq}) => VLabel()
  ..id = _childId
  ..seq = seq
  ..style = SWT.NONE
  ..text = text;

VComposite _parent(List<VControl> children, {required int seq}) => VComposite()
  ..id = _parentId
  ..seq = seq
  ..style = SWT.NONE
  ..children = children;

/// Delivers a frame exactly as the transport would: 2-byte name length, name, JSON body.
void _receive(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

Map<String, dynamic> _childFrame(String text, int seq) =>
    {'swt': 'Label', 'id': _childId, '_s': seq, 'style': SWT.NONE, 'text': text};

void main() {
  setUp(() => deliveryGate.reset());

  testWidgets('a parent rebuilding in place does not undo what the child was told',
      (WidgetTester tester) async {
    // The parent's value, holding the child as Java last described it to the parent. This object is
    // what a Flutter-side rebuild hands back down - it never changes again in this test, because
    // nothing arrives on the parent's channel.
    final parentValue = _parent([_child('collapsed', seq: 10)], seq: 10);

    Widget host(Key key) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            key: key,
            width: 300,
            height: 120,
            child: CompositeSwt<VComposite>(value: parentValue),
          ),
        );

    await tester.pumpWidget(host(const ValueKey('same')));
    await tester.pumpAndSettle();
    expect(find.text('collapsed'), findsOneWidget);

    // Java tells the child directly, on its own channel. The parent hears nothing.
    _receive(_childChannel, _childFrame('expanded', 20));
    await tester.pumpAndSettle();
    expect(find.text('expanded'), findsOneWidget);

    // Now rebuild the parent with no new information whatsoever - the same value object, a new
    // widget identity, exactly what a hover or a layout pass produces.
    await tester.pumpWidget(host(const ValueKey('same')));
    await tester.pumpAndSettle();

    expect(find.text('expanded'), findsOneWidget,
        reason: 'the parent knows only what it was told before the child was updated; rebuilding it '
            'must not push that back over the child');
    expect(find.text('collapsed'), findsNothing,
        reason: 'this is the reverting expand/collapse: the section closes itself on the next hover');
  });

  testWidgets('a parent whose subtree is remounted does not undo what the child was told',
      (WidgetTester tester) async {
    // The same situation, reached the other way. Changing the widget identity above the subtree -
    // which is what adding or removing a wrapper does, and what several impls do when a flag flips -
    // deactivates every descendant element and builds them again from scratch.
    //
    // This is the path a seq guard in didUpdateWidget could never cover: a remount does not call it,
    // and initState has no earlier state to compare against. Nothing here is a better guard - the
    // child reads its state from where that state lives, so neither path has a copy to hand down.
    final parentValue = _parent([_child('collapsed', seq: 10)], seq: 10);

    Widget host(Key key) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            key: key,
            width: 300,
            height: 120,
            child: CompositeSwt<VComposite>(value: parentValue),
          ),
        );

    await tester.pumpWidget(host(const ValueKey('before')));
    await tester.pumpAndSettle();

    _receive(_childChannel, _childFrame('expanded', 20));
    await tester.pumpAndSettle();
    expect(find.text('expanded'), findsOneWidget);

    await tester.pumpWidget(host(const ValueKey('after')));
    await tester.pumpAndSettle();

    expect(find.text('expanded'), findsOneWidget,
        reason: 'a remount is still just the parent rebuilding; what the child was told directly is '
            'still newer than what the parent holds');
  });

  testWidgets('repeated parent rebuilds keep the child current', (WidgetTester tester) async {
    final parentValue = _parent([_child('collapsed', seq: 10)], seq: 10);

    Widget host(int build) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            key: const ValueKey('same'),
            width: 300,
            height: 120,
            child: CompositeSwt<VComposite>(value: parentValue),
          ),
        );

    await tester.pumpWidget(host(0));
    await tester.pumpAndSettle();
    _receive(_childChannel, _childFrame('expanded', 20));
    await tester.pumpAndSettle();

    for (int build = 1; build <= 5; build++) {
      await tester.pumpWidget(host(build));
      await tester.pumpAndSettle();
    }

    expect(find.text('expanded'), findsOneWidget,
        reason: 'a stale copy does not become fresher by being handed down repeatedly, and the '
            'child must not flicker back on any one of those rebuilds');
  });

  testWidgets('a genuinely newer parent update still reaches the child', (WidgetTester tester) async {
    // The other direction: refusing every value that arrives inside a parent would strand a subtree
    // Java has legitimately re-described - a newly opened dialog would render empty. What separates
    // the two cases is when Java wrote the copy, which is what the write stamp says.
    final stale = _parent([_child('collapsed', seq: 10)], seq: 10);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 120,
        child: CompositeSwt<VComposite>(value: stale),
      ),
    ));
    await tester.pumpAndSettle();
    _receive(_childChannel, _childFrame('expanded', 20));
    await tester.pumpAndSettle();
    expect(find.text('expanded'), findsOneWidget);

    // Java describes the parent again, carrying a child written after the child's own update - the
    // shape of a folded delivery, where a dirty child is sent inside the dirty ancestor above it
    // rather than on its own channel.
    _receive('Composite/$_parentId', {
      'swt': 'Composite',
      'id': _parentId,
      '_s': 30,
      'style': SWT.NONE,
      'children': [_childFrame('from the parent', 31)],
    });
    await tester.pumpAndSettle();

    expect(find.text('from the parent'), findsOneWidget,
        reason: 'the parent was written after the child was last told anything, so its copy of the '
            'child is the newest description there is');
  });

  testWidgets('an older child inside a newer parent is still refused', (WidgetTester tester) async {
    // The fold sends a whole ancestor when it carries a descendant, and the ancestor's own payload
    // is newer than the child's last update while the child copy inside it need not be. Only the
    // copy's own stamp decides.
    final stale = _parent([_child('collapsed', seq: 10)], seq: 10);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 120,
        child: CompositeSwt<VComposite>(value: stale),
      ),
    ));
    await tester.pumpAndSettle();
    _receive(_childChannel, _childFrame('expanded', 20));
    await tester.pumpAndSettle();

    _receive('Composite/$_parentId', {
      'swt': 'Composite',
      'id': _parentId,
      '_s': 30,
      'style': SWT.NONE,
      'children': [_childFrame('collapsed', 10)],
    });
    await tester.pumpAndSettle();

    expect(find.text('expanded'), findsOneWidget,
        reason: 'a parent can be re-described for its own reasons while carrying children it has '
            'not heard about since - taking those would undo what each of them was told');
  });
}
