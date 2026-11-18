// afterNextChange answers at the next painted frame and at no other time: a test waiting on it
// re-reads its condition exactly when the client may show something new.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/test_harness.dart';

void main() {
  testWidgets('afterNextChange answers after the next frame, not before', (tester) async {
    final counter = ValueNotifier<int>(0);
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: ValueListenableBuilder<int>(valueListenable: counter, builder: (_, v, __) => Text('$v')),
    ));

    var answers = 0;
    afterNextChange(() => answers++);
    await tester.pump(const Duration(seconds: 1));
    expect(answers, 0, reason: 'nothing changed, so nothing was painted and nothing answered');

    counter.value = 1;
    expect(answers, 0, reason: 'a change is scheduled, not painted yet');
    await tester.pump();
    expect(answers, 1);

    counter.value = 2;
    await tester.pump();
    expect(answers, 1, reason: 'one answer per call');
  });
}
