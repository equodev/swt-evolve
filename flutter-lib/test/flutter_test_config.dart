// Every test starts against a fresh client.
//
// A widget's state now lives in the registry rather than in the State that renders it, and it
// deliberately outlives that State - a hidden subtree has to keep receiving. In one process running
// many tests, that means state registered under `Label/9` by one test would still be there when the
// next test mounts its own `Label/9`, and the second test would render the first one's widget.
//
// So the registry is emptied between tests, which is what a new client is. The delivery gate goes
// with it: what a client last saw is only meaningful about the values it saw them on.

import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';

Future<void> testExecutable(FutureOr<void> Function() testMain) async {
  setUp(() {
    VRegistry.instance.clear();
    deliveryGate.reset();
  });
  await testMain();
}
