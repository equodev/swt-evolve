// SWT contract: a MenuDetectListener setting `e.doit = false` suppresses the context menu. The
// client used to open it before sending MenuDetect, so the veto arrived too late to mean anything.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/impl/utils/menu_detect_gate.dart';

void _verdict(String swt, int id, bool doit) {
  final actionId = '$swt/$id/menu/verdict';
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode({'doit': doit}));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

void main() {
  testWidgets('the menu does not open before Java has answered', (tester) async {
    var opened = false;
    MenuDetectGate.withhold('Table', 201, () => opened = true);
    await tester.pump();
    expect(opened, isFalse, reason: 'a listener could still veto it');

    _verdict('Table', 201, true);
    await tester.pump();
    expect(opened, isTrue);
  });

  testWidgets('a vetoed MenuDetect never opens the menu', (tester) async {
    var opened = false;
    MenuDetectGate.withhold('Table', 202, () => opened = true);
    _verdict('Table', 202, false);
    await tester.pump();
    expect(opened, isFalse);

    // And it stays shut — the veto is not a deferral.
    await tester.pump(const Duration(milliseconds: 400));
    expect(opened, isFalse);
  });

  testWidgets('the menu still opens if Java never answers', (tester) async {
    var opened = false;
    MenuDetectGate.withhold('Table', 203, () => opened = true);
    await tester.pump(const Duration(milliseconds: 400));
    expect(opened, isTrue,
        reason: 'failing open: a lost verdict must not make right-click dead');
  });

  testWidgets('a veto on one control does not suppress another control\'s menu',
      (tester) async {
    var treeOpened = false;
    var tableOpened = false;
    MenuDetectGate.withhold('Tree', 204, () => treeOpened = true);
    MenuDetectGate.withhold('Table', 205, () => tableOpened = true);

    _verdict('Tree', 204, false);
    _verdict('Table', 205, true);
    await tester.pump();

    expect(treeOpened, isFalse);
    expect(tableOpened, isTrue);
  });
}
