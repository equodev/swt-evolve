import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';

/// Delivers the `shown` ack that MenuHelper sends once SWT.Show has rebuilt the popup.
/// A context menu no longer opens on the click itself — it waits for this — so a test that
/// asserts the menu is visible has to stand in for Java and send it.
///
/// Three pumps: the microtask that dispatches the ack, the post-frame callback that opens the
/// menu, and the overlay's own first frame.
Future<void> ackMenuShown(WidgetTester tester, int menuId) async {
  deliverMenuShown(menuId);
  await tester.pump();
  await tester.pump();
  await tester.pumpAndSettle();
}

/// The ack alone, for a test that drives the frames after it itself.
void deliverMenuShown(int menuId) {
  final action = utf8.encode('Menu/$menuId/shown');
  final body = TaggedValues.encode(<String, Object?>{});
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}
