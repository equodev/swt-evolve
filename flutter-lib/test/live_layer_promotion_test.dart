// A control that keeps repainting gets a layer of its own. Promotion happens after a frame has
// painted, so the layer has to be painted the next time its parent paints, whatever its child
// composites.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/live_layer.dart';

class _Ticks extends CustomPainter {
  _Ticks(this.tick) : super(repaint: tick);

  final ValueNotifier<int> tick;

  @override
  void paint(Canvas canvas, Size size) => canvas.drawRect(Offset.zero & size, Paint()..color = Color(0xFF000000 + tick.value));

  @override
  bool shouldRepaint(_Ticks old) => false;
}

void main() {
  testWidgets('a control promoted to a layer while its child composites is painted with its parent', (tester) async {
    final tick = ValueNotifier(0);
    final sibling = ValueNotifier(0);
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        // Repaints the parent without repainting the control.
        CustomPaint(painter: _Ticks(sibling), size: const Size(40, 40)),
        LiveLayer(
          child: SizedBox(
            width: 40,
            height: 40,
            child: Stack(children: [
              // A child with a layer of its own makes the control composite before it is promoted.
              const RepaintBoundary(child: SizedBox.expand()),
              CustomPaint(painter: _Ticks(tick), size: const Size(40, 40)),
            ]),
          ),
        ),
      ]),
    ));

    // The third frame in a row promotes it, after that frame has painted.
    for (var i = 1; i <= 3; i++) {
      tick.value = i;
      await tester.pump();
    }
    sibling.value = 1;
    await tester.pump();

    expect(tester.takeException(), isNull);
    final layer = tester.renderObject(find.byType(LiveLayer));
    expect(layer.isRepaintBoundary, isTrue);
  });
}
