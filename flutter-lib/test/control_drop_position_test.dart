// A drop on a Control reports where it landed, and Java hands that point to the application's
// DropTargetListener as the event's x/y. The point is read off the drag in the view's pixels, and
// SWT works in its own units: the two coincide at 1.0 only, so the drop is checked at another scale.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, String text, VRectangle bounds) => VLabel()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..text = text
  ..bounds = bounds;

Widget _placed(VLabel label) {
  final b = label.bounds!;
  return Positioned(
    left: b.x.toDouble(),
    top: b.y.toDouble(),
    width: b.width.toDouble(),
    height: b.height.toDouble(),
    child: LabelSwt<VLabel>(value: label),
  );
}

/// The point the last drop told Java it landed at.
Offset? _lastDrop() {
  final drops = EquoCommService.commForTesting.sentFrames
      .map(EquoCommBase.decodeFrame)
      .where((f) => f.$1.endsWith('/Drop/drop'))
      .map((f) => jsonDecode(f.$2) as Map<String, dynamic>)
      .toList();
  if (drops.isEmpty) return null;
  return Offset((drops.last['x'] as num).toDouble(), (drops.last['y'] as num).toDouble());
}

void main() {
  for (final scale in [1.0, 1.5]) {
    testWidgets('a drop on a Control at scale $scale reports where it landed, in SWT units',
        (tester) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      EquoCommBase.recordSentFrames = true;
      addTearDown(() => EquoCommBase.recordSentFrames = false);

      final source = _label(1, 'source', _rect(20, 20, 100, 30))..dragSource = true;
      final target = _label(2, 'target', _rect(200, 150, 150, 60))..dropTargetId = 9;
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 400,
          height: 300,
          child: Stack(children: [_placed(source), _placed(target)]),
        ),
      ));
      await tester.pumpAndSettle();
      EquoCommService.commForTesting.clearSentFrames();

      final start = tester.getCenter(find.text('source'));
      final end = tester.getCenter(find.text('target'));
      final gesture = await tester.startGesture(start);
      await tester.pump(const Duration(milliseconds: 50));
      const steps = 6;
      for (var i = 1; i <= steps; i++) {
        await gesture.moveTo(Offset.lerp(start, end, i / steps)!);
        await tester.pump(const Duration(milliseconds: 20));
      }
      await gesture.up();
      await tester.pumpAndSettle();
      // Nothing answers the dragStart verdict here; let the fail-open timer run out.
      await tester.pump(const Duration(milliseconds: 400));

      final dropped = _lastDrop();
      expect(dropped, isNotNull, reason: 'the drop must reach Java');
      expect((dropped! - end / scale).distance, lessThan(1),
          reason: 'the point the pointer was released at, in the units Java works in');
    });
  }
}
