// A `dragStart` veto must apply to the drag it answered and no other. It used to live in a single
// process-wide flag, so one control's veto leaked into whatever drag ran next.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/dnd_session.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'support/channels.dart' as channels;
import 'package:swtflutter/src/gen/widgets.dart' show WireType;

void _dragStartResult(String swt, int id, bool doit) {
  final actionId = '$swt/$id/DragDetect/dragStartResult';
  final body = TaggedValues.encode({'doit': doit});
  final frame = channels.frame(actionId, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  testWidgets('a veto applies to the drag that was vetoed', (tester) async {
    DragStartVeto.begin(WireType.tree, 101);
    _dragStartResult('Tree', 101, false);
    await tester.pump();
    expect(DragStartVeto.isVetoed, isTrue);
  });

  testWidgets('a drag that Java allows is not vetoed', (tester) async {
    DragStartVeto.begin(WireType.tree, 102);
    _dragStartResult('Tree', 102, true);
    await tester.pump();
    expect(DragStartVeto.isVetoed, isFalse);
  });

  testWidgets("one control's veto does not reject another control's drag",
      (tester) async {
    DragStartVeto.begin(WireType.tree, 103);
    _dragStartResult('Tree', 103, false);
    await tester.pump();
    expect(DragStartVeto.isVetoed, isTrue);

    // A second, unrelated drag starts on a different control, whose listener allows it.
    DragStartVeto.begin(WireType.table, 104);
    _dragStartResult('Table', 104, true);
    await tester.pump();
    expect(DragStartVeto.isVetoed, isFalse,
        reason: "the Tree's veto must not survive into the Table's drag");
  });

  testWidgets('a veto arriving after its own drag ended cannot reject the next one',
      (tester) async {
    // Java is slow: the answer for the first drag turns up only once the second is under way.
    DragStartVeto.begin(WireType.tree, 105);
    DragStartVeto.begin(WireType.table, 106);
    _dragStartResult('Tree', 105, false);
    await tester.pump();

    expect(DragStartVeto.isVetoed, isFalse,
        reason: 'the late veto belongs to a drag that is over, not to the live one');

    // Drain the live drag's fail-open timer; Java never answered for Table/106.
    await tester.pump(const Duration(milliseconds: 400));
    expect(DragStartVeto.isVetoed, isFalse, reason: 'failing open allows the drop');
  });
}
