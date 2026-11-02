import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'channels.dart' as channels;

/// Answers a `MenuDetect` the way Java does. The client withholds the menu until this arrives, so
/// a test that right-clicks has to deliver it or wait out the fail-open timer.
Future<void> ackMenuDetect(WidgetTester tester, String swt, int id,
    {bool doit = true}) async {
  final body = TaggedValues.encode({'doit': doit});
  final frame = channels.frame('$swt/$id/menu/verdict', body);

  EquoCommService.commForTesting.receiveBinary(frame);
  await tester.pump();
  await tester.pump();
}
