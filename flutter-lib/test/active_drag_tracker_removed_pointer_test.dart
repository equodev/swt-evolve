// A pointer removed while its button is held never sends an up. Hover suppression has to end with it,
// or every control after it gets no MouseEnter or MouseExit.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/dnd_session.dart';

void main() {
  testWidgets('a pointer removed while pressed stops suppressing hover', (tester) async {
    ActiveDragTracker.ensureInitialized();
    await tester.pumpWidget(const SizedBox());
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(10, 10));
    await mouse.down(const Offset(10, 10));
    expect(ActiveDragTracker.isSuppressingHover, isTrue);

    await mouse.removePointer();

    expect(ActiveDragTracker.isSuppressingHover, isFalse);
  });
}
