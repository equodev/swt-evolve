// Java waits for an answer to every copyArea into an Image. One the client cannot read pixels for
// is answered with none, so Java leaves the Image as it was instead of waiting out its budget.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'support/gc_frames.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('a copyArea into an Image the client cannot read is answered with no pixels', () async {
    const id = 852001;
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    final drawer = GCDrawer.embedded(VGC.empty()..id = id);
    addTearDown(drawer.dispose);
    EquoCommService.commForTesting.clearSentFrames();

    // An Image the client was never told about: nothing to size the copy by.
    receive('GC/$id/b', gcOp('copyAreaImageintint', [VImage()..id = 852999, 0, 0]));
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }

    final answers = EquoCommService.commForTesting.sentFrames
        .map(EquoCommBase.decodeFrame)
        .where((f) => f.$1 == 'GC/$id/copyAreaImageintintResponse')
        .toList();
    expect(answers, hasLength(1));
    expect(answers.single.$2, isEmpty);
  });
}
