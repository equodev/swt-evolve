export 'package:swtflutter/testing/gc_frames.dart';

import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/testing/deliver.dart';
import 'package:swtflutter/testing/gc_frames.dart';

/// What Java sends for an owner-drawn row [itemId] it paints: one painting, then the GC's dispose.
Future<void> paintRow(int itemId) async {
  receive('GC/$itemId/b', GcFrame().whole(VGC.empty()).opNamed('fillRectangleintintintint', [0, 0, 20, 10]).bytes);
  await deliverFrame('GC/$itemId/gcDispose', {'fullRepaint': true});
}
