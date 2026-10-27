import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';

/// While a Display forwards keys from the root, the per-control forwarders must stay silent, or
/// every keystroke reaches Java twice. A remounted Display disposes its old State only after the
/// new one has started forwarding, so that dispose must not switch the per-control path back on.
void main() {
  testWidgets('a remounted Display keeps the per-control key forwarders silent', (tester) async {
    late StateSetter rebuild;
    var wrapped = false;
    final display = DisplaySwt(
        key: const ValueKey(1), value: VDisplay()..swt = 'Display'..id = 1);

    await tester.pumpWidget(MaterialApp(
      home: StatefulBuilder(builder: (context, setState) {
        rebuild = setState;
        // A change of depth is what remounts it: Flutter cannot carry a State across one.
        return wrapped ? Column(children: [Expanded(child: display)]) : display;
      }),
    ));
    expect(displayLevelKeyForwardingActive, isTrue);

    rebuild(() => wrapped = true);
    await tester.pump();

    expect(displayLevelKeyForwardingActive, isTrue,
        reason: 'the new Display forwards every key; a per-control forwarder would send it again');

    await tester.pumpWidget(const SizedBox.shrink());
    expect(displayLevelKeyForwardingActive, isFalse,
        reason: 'with no Display mounted the per-control forwarders are the only path');
  });
}
