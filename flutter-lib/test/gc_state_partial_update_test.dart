// A drawing library sets one attribute between primitives — draw2d's SWTGraphics re-clips and
// re-colours per figure — so Java sends a GC's state as what changed once the batch carrying it has
// described the GC whole. The drawer therefore has to merge such a frame into the state it holds:
// taking it as a whole description would null every property the change did not name, and the ops
// that follow would be drawn in the wrong colour, with the wrong clip, or not at all.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

import 'support/gc_frames.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const id = 851001;

  /// The whole GC, as the first state push of a paint describes it.
  void deliverWhole(VGC state) => receive('GC/$id/b', GcFrame().whole(state).bytes);

  /// A change naming [keys], as every state push after the first sends one.
  void deliverChange(VGC state, Set<int> keys) => receive('GC/$id/b', GcFrame().change(state, keys).bytes);

  VGC whole() => VGC.empty()
    ..id = id
    ..style = 33554432
    ..alpha = 255
    ..fillRule = 1
    ..lineStyle = 1
    ..lineCap = 1
    ..lineJoin = 1
    ..lineWidth = 3
    ..background = colourOf(192, 192, 192)
    ..foreground = colourOf(0, 0, 0)
    ..clipping = rectOf(20, 20, 96, 54);

  GCDrawer drawer() {
    final state = VGC.empty()..id = id;
    final d = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(d.dispose);
    return d;
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  test('a whole state push replaces what the drawer holds', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();

    expect(d.state.lineWidth, 3);
    expect(d.state.background?.blue, 192);
    expect(d.state.clipping?.width, 96);
  });

  test('a change applies on top of the state held, leaving the rest alone', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();

    deliverChange(VGC.empty()..foreground = colourOf(128, 128, 128), {WireKey.k_GC_foreground});
    await settle();

    expect(d.state.foreground?.red, 128,
        reason: 'the property the change named has to take effect');
    // The regression: taken as a whole description, this frame would null everything else and
    // every op drawn after it would use the wrong colour, clip and line width.
    expect(d.state.background?.blue, 192);
    expect(d.state.lineWidth, 3);
    expect(d.state.clipping?.width, 96);
    expect(d.state.alpha, 255);
    expect(d.state.fillRule, 1);
  });

  test('a change naming a colour and a number merges both, each colour one ARGB word', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();

    deliverChange(
        VGC.empty()
          ..foreground = VColor.fromArgb(0xFF123456)
          ..lineWidth = 2,
        {WireKey.k_GC_foreground, WireKey.k_GC_lineWidth});
    await settle();

    expect(d.state.foreground?.alpha, 0xFF);
    expect(d.state.foreground?.red, 0x12);
    expect(d.state.foreground?.green, 0x34);
    expect(d.state.foreground?.blue, 0x56);
    expect(d.state.lineWidth, 2);
    expect(d.state.background?.blue, 192);
    expect(d.state.clipping?.width, 96);
  });

  test('successive changes accumulate, the way a paint applies them one op at a time', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();

    deliverChange(VGC.empty()..clipping = rectOf(200, 100, 7, 7), {WireKey.k_GC_clipping});
    await settle();
    deliverChange(VGC.empty()..background = colourOf(0, 0, 0), {WireKey.k_GC_background});
    await settle();
    deliverChange(VGC.empty()..lineWidth = 1, {WireKey.k_GC_lineWidth});
    await settle();

    expect(d.state.clipping?.x, 200);
    expect(d.state.background?.red, 0);
    expect(d.state.lineWidth, 1);
    expect(d.state.foreground?.red, 0, reason: 'never named by any change, so never touched');
  });

  test('a later whole push still replaces, so a new paint starts clean', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();
    deliverChange(VGC.empty()..lineWidth = 9, {WireKey.k_GC_lineWidth});
    await settle();

    deliverWhole(whole()..lineWidth = 1);
    await settle();

    expect(d.state.lineWidth, 1,
        reason: 'a whole description is the state, not something to merge into the old one');
  });

  test('the drawer reads the merged state when it turns an op into a shape', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();
    deliverChange(VGC.empty()..background = colourOf(10, 20, 30), {WireKey.k_GC_background});
    await settle();

    expect(d.fillColor.red, 10);
    expect(d.fillColor.green, 20);
    expect(d.fillColor.blue, 30);
    expect(d.strokeColor.red, 0, reason: 'foreground was not in the change');
  });

  // Guards the assumption the merge rests on: VColor/VRectangle arrive whole inside a change, so a
  // named property is replaced outright rather than merged field by field.
  test('a change carries each named value whole', () async {
    final d = drawer();
    deliverWhole(whole());
    await settle();
    deliverChange(VGC.empty()..clipping = rectOf(1, 2, 3, 4), {WireKey.k_GC_clipping});
    await settle();

    final VRectangle? c = d.state.clipping;
    expect(c, isNotNull);
    expect([c!.x, c.y, c.width, c.height], [1, 2, 3, 4]);
    final VColor? bg = d.state.background;
    expect(bg?.blue, 192, reason: 'untouched by a change that named only the clip');
  });
}
