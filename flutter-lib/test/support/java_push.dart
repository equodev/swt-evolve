import 'package:flutter_test/flutter_test.dart';

/// The full-state push Java sends: [edit] changes the state of the widget [widget] finds, which
/// then takes it through `setValue` as an update from Java does, and a frame is pumped.
Future<void> javaPushes(WidgetTester tester, Finder widget, void Function(dynamic state) edit) async {
  // ignore: avoid_dynamic_calls
  final dynamic widgetState = tester.state(widget);
  // ignore: avoid_dynamic_calls
  edit(widgetState.state);
  // ignore: avoid_dynamic_calls
  widgetState.setValue(widgetState.state);
  await tester.pump();
}

/// [javaPushes] of a state whose text is [text], as after a setText or a Modify Java acknowledged.
Future<void> javaPushesText(WidgetTester tester, Finder widget, String text) =>
    // ignore: avoid_dynamic_calls
    javaPushes(tester, widget, (state) => state.text = text);
