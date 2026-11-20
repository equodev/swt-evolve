// Installs its own binding, so it cannot share an isolate with files that installed the testWidgets
// one: the annotation keeps it out of the test bundles (tool/test_bundles.dart).
@TestOn('vm')
library;

import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/evolve_binding.dart';

// Plain tests, not testWidgets: the binding under test has to be the one installed, which a
// testWidgets binding would take the place of. They share that one binding, so they run in order.
void main() {
  setUpAll(() => EvolveWidgetsBinding.ensureInitialized(followPlatformSemantics: false));

  test('with no assistive technology the engine asking again keeps the tree off; an explicit '
      'request still turns it on', () {
    final binding = SemanticsBinding.instance;
    expect(binding, isA<EvolveWidgetsBinding>());
    expect(binding.semanticsEnabled, isFalse);

    binding.platformDispatcher.onSemanticsEnabledChanged!();
    expect(binding.semanticsEnabled, isFalse);

    final handle = binding.ensureSemantics();
    expect(binding.semanticsEnabled, isTrue);
    handle.dispose();
    expect(binding.semanticsEnabled, isFalse);
  });

  test('an assistive technology the runner reports later turns the tree on, and off once it stops',
      () async {
    final binding = SemanticsBinding.instance;
    expect(binding.semanticsEnabled, isFalse);

    await _runnerReports(true);
    expect(binding.semanticsEnabled, isTrue);

    await _runnerReports(true);
    await _runnerReports(false);
    expect(binding.semanticsEnabled, isFalse);
  });
}

/// Delivers `assistiveTechnology` the way the engine delivers a runner's message.
Future<void> _runnerReports(bool listening) async {
  final message = const StandardMethodCodec()
      .encodeMethodCall(MethodCall('assistiveTechnology', listening));
  ServicesBinding.instance.channelBuffers
      .push(EvolveWidgetsBinding.accessibilityChannel.name, message, (_) {});
  await Future<void>.delayed(Duration.zero);
}
