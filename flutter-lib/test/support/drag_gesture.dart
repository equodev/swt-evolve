import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

/// Drags from [start] to [end] in steps, pumping between them: a Draggable has to cross its touch
/// slop, and a DragTarget needs a frame to update its hover state.
Future<void> dragInSteps(WidgetTester tester, Offset start, Offset end, {int steps = 6}) async {
  final gesture = await tester.startGesture(start);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 1; i <= steps; i++) {
    await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  await gesture.up();
}
