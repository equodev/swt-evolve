import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';

/// Answers a `MenuDetect` the way Java does. The client withholds the menu until this arrives, so
/// a test that right-clicks has to deliver it or wait out the fail-open timer.
Future<void> ackMenuDetect(WidgetTester tester, String swt, int id,
    {bool doit = true}) async {
  final action = utf8.encode('$swt/$id/menu/verdict');
  final body = utf8.encode(json.encode({'doit': doit}));
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);

  EquoCommService.commForTesting.receiveBinary(frame);
  await tester.pump();
  await tester.pump();
}
