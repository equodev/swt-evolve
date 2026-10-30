import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'channels.dart' as channels;

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
  final body = TaggedValues.encode(<String, Object?>{});
  EquoCommService.commForTesting.receiveBinary(channels.frame('Menu/$menuId/shown', body));
}
