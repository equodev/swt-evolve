// A control set to an SWT Region is clipped to it. The region is what an application uses to draw
// an outline — the Eclipse workbench frames its drag-and-drop feedback with a shell the size of the
// whole window whose region is a few thin bars — so a control that ignores it paints over
// everything the region exists to exclude, and the workbench's feedback becomes an opaque sheet.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/region.dart';
import 'package:swtflutter/src/impl/utils/region_clip.dart';

VRegion _region(List<int> rects) => VRegion()..rects = rects;

Future<void> _pump(WidgetTester tester, Widget child) => tester.pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: Center(child: SizedBox(width: 200, height: 100, child: child)),
      ),
    );

void main() {
  testWidgets('a control with no region is left alone', (tester) async {
    await _pump(tester, RegionClip.maybe(null, const SizedBox()));

    expect(find.byType(RegionClip), findsNothing);
    expect(find.byType(ClipPath), findsNothing);
  });

  testWidgets('a control with a region is clipped to it', (tester) async {
    await _pump(tester, RegionClip.maybe(_region([0, 0, 10, 10]), const SizedBox()));

    expect(find.byType(ClipPath), findsOneWidget);
  });

  testWidgets('the clip is the region, not its bounding box', (tester) async {
    // The shape that matters: a frame. Its bounds cover the whole area, so a clip taken from the
    // bounds would leave the middle painted — which is the entire hole.
    final frame = _region([
      0, 0, 100, 2, // top bar
      0, 98, 100, 2, // bottom bar
      0, 0, 2, 100, // left bar
      98, 0, 2, 100, // right bar
    ]);
    await _pump(tester, RegionClip(rects: frame.rects!, child: const SizedBox()));

    final clipper = tester.widget<ClipPath>(find.byType(ClipPath)).clipper!;
    final path = clipper.getClip(const Size(100, 100));

    expect(path.contains(const Offset(1, 50)), isTrue, reason: 'the left bar is inside the region');
    expect(path.contains(const Offset(50, 1)), isTrue, reason: 'the top bar is inside the region');
    expect(path.contains(const Offset(50, 50)), isFalse,
        reason: 'the middle was subtracted, and clipping to the bounds would fill it back in');
  });

  testWidgets('a region covering nothing hides the control', (tester) async {
    await _pump(tester, RegionClip(rects: const [], child: const SizedBox()));

    final clipper = tester.widget<ClipPath>(find.byType(ClipPath)).clipper!;
    final path = clipper.getClip(const Size(100, 100));

    expect(path.contains(const Offset(50, 50)), isFalse);
  });

  testWidgets('a trailing partial rectangle is ignored rather than throwing', (tester) async {
    await _pump(tester, RegionClip(rects: const [0, 0, 10, 10, 20, 20], child: const SizedBox()));

    final clipper = tester.widget<ClipPath>(find.byType(ClipPath)).clipper!;
    final path = clipper.getClip(const Size(100, 100));

    expect(path.contains(const Offset(5, 5)), isTrue);
    expect(path.contains(const Offset(21, 21)), isFalse);
  });

  testWidgets('the clip is recomputed when the region changes', (tester) async {
    await _pump(tester, RegionClip(rects: const [0, 0, 10, 10], child: const SizedBox()));
    final first = tester.widget<ClipPath>(find.byType(ClipPath)).clipper!;

    await _pump(tester, RegionClip(rects: const [0, 0, 50, 50], child: const SizedBox()));
    final second = tester.widget<ClipPath>(find.byType(ClipPath)).clipper!;

    // The workbench moves its feedback by rebuilding the region on every drag update, so a clipper
    // that reported itself unchanged would freeze the feedback where it first appeared.
    expect(second.shouldReclip(first), isTrue);
    expect(second.shouldReclip(second), isFalse);
  });
}
