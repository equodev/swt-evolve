import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// Regression for the GC recording/replay use-after-dispose that blanked a whole GC-drawn region
/// (a Group's icons, a canvas, etc. rendering empty).
///
/// At the end of a draw cycle the outgoing cycle's image clones are released. If a shape keeps
/// pointing at its now-disposed `ui.Image`, a lingering `ScenePainter` repaint draws that disposed
/// image — which throws and blanks the entire painted region. The fix nulls a released shape's
/// `image` reference (so the existing `image == null` guard skips it) and recurses into
/// `TransformShape` children. Both behaviors are RED before the gcdrawer fix and GREEN after.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  ImageShape rasterShape(ui.Image img) => ImageShape.raster(
        img,
        const Rect.fromLTWH(0, 0, 2, 2),
        const Rect.fromLTWH(0, 0, 2, 2),
      );

  test('a released raster shape has its image reference nulled', () async {
    final img = await createTestImage(width: 2, height: 2);
    final shape = rasterShape(img);
    expect(shape.image, isNotNull);

    disposeShapeImages([shape]);

    expect(shape.image, isNull,
        reason: 'a released image must be nulled so a stale repaint hits the image == null '
            'guard instead of drawing a disposed ui.Image');
  });

  test('images nested in a TransformShape are released too', () async {
    final img = await createTestImage(width: 2, height: 2);
    final inner = rasterShape(img);
    final wrapped = TransformShape(Matrix4.identity().storage, [inner]);

    disposeShapeImages([wrapped]);

    expect(inner.image, isNull,
        reason: 'disposeShapeImages must recurse into TransformShape children, not skip them');
  });
}
