// Browser tests: the nearest config wins, and test/flutter_test_config.dart loads VM-only code.
import 'dart:async';

Future<void> testExecutable(FutureOr<void> Function() testMain) async => testMain();
