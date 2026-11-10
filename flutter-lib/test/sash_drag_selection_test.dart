// Dart-side half: moving the sash must fire Selection events.
//
// Native SWT fires Selection continuously while the sash is dragged (see
// SwtSash.mouseDragged on every platform; on macOS the style is forced to
// SWT.SMOOTH and the sash also moves itself). Apps — Snippet 54, SashForm,
// and resizable panel layouts in real apps — resize the neighboring composites
// from those events, so with no during-drag events nothing relayouts while the
// user drags.

import 'package:flutter/gestures.dart' show PointerDeviceKind, kSecondaryButton;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'delivery/support/deliver.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

/// Same as the generated SashSwt, except sendEvent captures outgoing events
/// instead of handing them to EquoCommService (which needs a live transport
/// that doesn't exist in a widget test). The State, the real GestureDetector,
/// and the real coordinate computation are untouched.
class _CapturingSashSwt extends SashSwt<VSash> {
  const _CapturingSashSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSash val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VSash _verticalSash() => VSash()
  ..id = 7
  ..style = SWT.VERTICAL
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 195
    ..y = 0
    ..width = 10
    ..height = 260);

void main() {
  testWidgets('dragging a sash fires Selection events while it moves',
      (WidgetTester tester) async {
    final selections = <VEvent>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      // Mimic the parent Composite's NoLayout: the child is placed at its
      // bounds offset inside a fixed-size area.
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                value: _verticalSash(),
                onEvent: (ev, payload) {
                  if (ev == 'Selection/Selection' && payload != null) {
                    selections.add(payload);
                  }
                },
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final center = tester.getCenter(find.byType(_CapturingSashSwt));
    final gesture = await tester.startGesture(center);
    // Two clearly-beyond-slop moves to the right, still mid-drag.
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();

    expect(selections, isNotEmpty,
        reason: 'native SWT fires Selection continuously during a sash drag '
            '(SwtSash.mouseDragged); without those events the app cannot '
            'resize the composites while the sash moves');
    expect(selections.last.x, greaterThan(195),
        reason: 'during-drag Selection must carry the new position');

    await gesture.up();
    await tester.pump();

    expect(selections, isNotEmpty,
        reason: 'releasing the sash must fire a final Selection');
    final last = selections.last;
    expect(last.x, greaterThan(195),
        reason: 'the final Selection must carry the dragged-to x, not the '
            'original position');
    expect(last.width, 10);
    expect(last.height, 260);
  });

  testWidgets('a zoomed sash moves as far as the pointer, in SWT units',
      (WidgetTester tester) async {
    // The drag is read off the pointer in the view's pixels while the sash's bounds are in SWT
    // units. At 1.0 they coincide; at any other scale a missed conversion moves the sash a
    // different distance than the pointer travelled.
    Future<int> movedAt(double scale) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      final selections = <VEvent>[];
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 390,
          height: 260,
          child: Stack(
            children: [
              Positioned(
                left: 195,
                top: 0,
                width: 10,
                height: 260,
                child: _CapturingSashSwt(
                  // A widget of its own per run: the previous one keeps the position it was dragged to.
                  value: _verticalSash()..id = 7 + (scale * 10).round(),
                  onEvent: (ev, payload) {
                    if (ev == 'Selection/Selection' && payload != null) selections.add(payload);
                  },
                ),
              ),
            ],
          ),
        ),
      ));
      await tester.pumpAndSettle();

      final gesture = await tester.startGesture(tester.getCenter(find.byType(_CapturingSashSwt)));
      await gesture.moveBy(const Offset(30, 0));
      await tester.pump();
      await gesture.moveBy(const Offset(30, 0));
      await tester.pump();
      await gesture.up();
      await tester.pump();
      return selections.last.x! - 195;
    }

    // The pointer travels 60 of the view's pixels: 60 SWT units at 1.0, 40 at 1.5.
    expect(await movedAt(1.0), closeTo(60, 1));
    expect(await movedAt(1.5), closeTo(40, 1));
  });

  testWidgets('the sash follows every move of the pressed pointer, from the first pixel',
      (WidgetTester tester) async {
    final selections = <VEvent>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                value: _verticalSash()..id = 40,
                onEvent: (ev, payload) {
                  if (ev == 'Selection/Selection' && payload != null) selections.add(payload);
                },
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final gesture = await tester.startGesture(tester.getCenter(find.byType(_CapturingSashSwt)));
    await tester.pump();
    for (var i = 0; i < 3; i++) {
      await gesture.moveBy(const Offset(4, 0));
      await tester.pump();
    }
    await gesture.up();
    await tester.pump();

    // The press reports where the sash is, each move where the pointer took it, and the release
    // where the last move left it: no move is held back as a gesture's slop.
    expect(selections.map((e) => e.x), [195, 199, 203, 207, 207]);
    expect(selections.map((e) => e.detail), everyElement(0));
  });

  testWidgets('each move of a drag reports its Selection ahead of its MouseMove',
      (WidgetTester tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                value: _verticalSash()..id = 42,
                onEvent: (ev, _) {
                  if (ev == 'Selection/Selection' || ev == 'MouseMove/MouseMove') sent.add(ev);
                },
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final gesture = await tester.startGesture(tester.getCenter(find.byType(_CapturingSashSwt)),
        kind: PointerDeviceKind.mouse);
    await tester.pump();
    sent.clear();
    await gesture.moveBy(const Offset(5, 0));
    await tester.pump();
    await gesture.up();

    expect(sent.take(2), ['Selection/Selection', 'MouseMove/MouseMove']);
  });

  testWidgets('the sash is drawn where the drag took it before Java confirms the move',
      (WidgetTester tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(value: _verticalSash()..id = 41, onEvent: (_, __) {}),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();
    final sash = find.byType(_CapturingSashSwt);
    final drawn = find.descendant(of: sash, matching: find.byType(Container));
    final before = tester.getTopLeft(drawn);

    final gesture = await tester.startGesture(tester.getCenter(sash));
    await tester.pump();
    await gesture.moveBy(const Offset(12, 0));
    await tester.pump();

    // The bounds are still the ones Java last sent: only the drag can have moved it, and the pointer
    // still lies on it at the offset it was pressed at.
    expect(tester.getTopLeft(drawn).dx - before.dx, 12);
    await gesture.up();
  });

  /// The release of a sash pressed at its middle and dragged 12 to the right, once Java has
  /// answered the drag with the sash at [answeredX].
  Future<int> releasedAt(WidgetTester tester, int id, int answeredX) async {
    final ups = <VEvent>[];
    Widget at(int x) => EvolveApp(
          key: ValueKey(id),
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 390,
            height: 260,
            child: Stack(
              children: [
                Positioned(
                  left: x.toDouble(),
                  top: 0,
                  width: 10,
                  height: 260,
                  child: _CapturingSashSwt(
                    value: _verticalSash()
                      ..id = id
                      ..seq = 10
                      ..bounds!.x = x,
                    onEvent: (ev, payload) {
                      if (ev == 'Mouse/MouseUp' && payload != null) ups.add(payload);
                    },
                  ),
                ),
              ],
            ),
          ),
        );
    await tester.pumpWidget(const SizedBox());
    freshClient();
    await tester.pumpWidget(at(195));
    await tester.pumpAndSettle();

    final gesture = await tester.startGesture(tester.getCenter(find.byType(_CapturingSashSwt)),
        kind: PointerDeviceKind.mouse);
    await tester.pump();
    await gesture.moveBy(const Offset(12, 0));
    await tester.pump();
    await deliverWhole(_verticalSash()
      ..id = id
      ..seq = 11
      ..bounds!.x = answeredX);
    await tester.pump();
    await gesture.up();
    await tester.pump();
    return ups.single.x!;
  }

  testWidgets('after the release the sash is drawn where the drag left it until Java answers it',
      (WidgetTester tester) async {
    VSash at(int x, int seq) => _verticalSash()
      ..id = 46
      ..seq = seq
      ..bounds!.x = x;
    await tester.pumpWidget(const SizedBox());
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        // The parent stays where it placed the sash first: what moves is what the sash draws on top.
        child: Stack(children: [
          Positioned(left: 195, top: 0, width: 10, height: 260,
              child: _CapturingSashSwt(value: at(195, 10), onEvent: (_, __) {})),
        ]),
      ),
    ));
    await tester.pumpAndSettle();
    final drawn = find.descendant(of: find.byType(_CapturingSashSwt), matching: find.byType(Container));
    double drift() => tester.getTopLeft(drawn).dx - 195;

    final gesture = await tester.startGesture(tester.getCenter(find.byType(_CapturingSashSwt)),
        kind: PointerDeviceKind.mouse);
    await tester.pump();
    await gesture.moveBy(const Offset(6, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(6, 0));
    await tester.pump();
    await gesture.up();
    await tester.pump();
    expect(drift(), 12, reason: 'nothing has answered the release yet');

    await deliverWhole(at(201, 11));
    await tester.pump();
    expect(drift(), 6, reason: 'an answer to the first move is Java catching up, not its answer to the release');

    await deliverWhole(at(207, 12));
    await tester.pump();
    expect(drift(), 0, reason: 'the release is answered: Java places the sash');
  });

  testWidgets('a press of another button after a drag is placed on the sash as usual',
      (WidgetTester tester) async {
    final ups = <VEvent>[];
    await tester.pumpWidget(const SizedBox());
    freshClient();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                value: _verticalSash()
                  ..id = 45
                  ..seq = 10,
                onEvent: (ev, payload) {
                  if (ev == 'Mouse/MouseUp' && payload != null) ups.add(payload);
                },
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();
    final centre = tester.getCenter(find.byType(_CapturingSashSwt));

    final drag = await tester.startGesture(centre, kind: PointerDeviceKind.mouse);
    await tester.pump();
    await drag.moveBy(const Offset(12, 0));
    await tester.pump();
    await drag.up();
    await tester.pump();
    ups.clear();
    // Java answers the release keeping the sash where it was, as a listener that vetoes it does.
    await deliverWhole(_verticalSash()
      ..id = 45
      ..seq = 11);
    await tester.pump();

    final secondary = await tester.startGesture(centre, kind: PointerDeviceKind.mouse, buttons: kSecondaryButton);
    await tester.pump();
    await secondary.up();
    await tester.pump();

    expect(ups.single.x, 5);
  });

  testWidgets('the release is reported on the sash where the drag last took it, whenever Java answers',
      (WidgetTester tester) async {
    // Pressed at the middle of its width, the pointer is still there when it lets go: the sash
    // moved with it. Java answering the last move late, or a listener keeping the sash short of
    // the pointer, changes nothing here: Java places the MouseUp on the sash it kept.
    expect(await releasedAt(tester, 43, 207), 5);
    expect(await releasedAt(tester, 44, 203), 5);
  });
}
