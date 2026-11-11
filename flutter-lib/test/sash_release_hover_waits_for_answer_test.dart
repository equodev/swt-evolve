// A sash released where the drag took it keeps the pointer: the control beside it, which the
// application lays out from the sash's Selection, may still cover that spot until Java's answer to
// the release arrives, and natively the pointer never leaves the sash.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/utils/hover_arbiter.dart';
import 'delivery/support/deliver.dart';

class _CapturingSashSwt extends SashSwt<VSash> {
  const _CapturingSashSwt({super.key, required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VSash val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

VSash _sash(int id, int x, int seq) => VSash()
  ..id = id
  ..seq = seq
  ..style = SWT.VERTICAL
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = x
    ..y = 0
    ..width = 10
    ..height = 260);

void main() {
  late List<String> crossings;

  /// The dragged sash, placed by its parent where Java last put it, and the control after it at
  /// [besideX], drawn on top of it as a later sibling.
  Widget app(int besideX, {required VSash dragged}) => EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 390,
          height: 260,
          child: Stack(children: [
            Positioned(
              left: dragged.bounds!.x!.toDouble(),
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                key: const ValueKey('dragged'),
                value: dragged,
                onEvent: (ev) {
                  if (ev.startsWith('MouseTrack/Mouse') && ev != 'MouseTrack/MouseHover') crossings.add('dragged $ev');
                },
              ),
            ),
            Positioned(
              left: besideX.toDouble(),
              top: 0,
              width: 20,
              height: 260,
              child: _CapturingSashSwt(
                key: const ValueKey('beside'),
                value: _sash(2, besideX, 10),
                onEvent: (ev) {
                  if (ev.startsWith('MouseTrack/Mouse') && ev != 'MouseTrack/MouseHover') crossings.add('beside $ev');
                },
              ),
            ),
          ]),
        ),
      );

  Future<TestGesture> dragTwelveRight(WidgetTester tester) async {
    crossings = [];
    await tester.pumpWidget(const SizedBox());
    freshClient();
    await tester.pumpWidget(app(205, dragged: _sash(1, 195, 10)));
    await tester.pumpAndSettle();
    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: const Offset(200, 130));
    await tester.pump();
    await gesture.down(const Offset(200, 130));
    await tester.pump();
    await gesture.moveBy(const Offset(6, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(6, 0));
    await tester.pump();
    crossings.clear();
    await gesture.up();
    await tester.pump();
    await tester.pump();
    return gesture;
  }

  testWidgets('the pointer stays on a released sash until Java answers the release', (tester) async {
    final gesture = await dragTwelveRight(tester);
    expect(crossings, isEmpty, reason: 'the control beside it is where Java last put it, over the pointer');

    // Java's answers: the move, the release, and the control beside laid out after the sash.
    await deliverWhole(_sash(1, 201, 11));
    await deliverWhole(_sash(1, 207, 12));
    await tester.pumpWidget(app(217, dragged: _sash(1, 207, 12)));
    await tester.pump();
    await tester.pump();
    expect(crossings, isEmpty, reason: 'natively the sash moves with the pointer, which never leaves it');

    await gesture.moveTo(const Offset(220, 130));
    await tester.pump();
    expect(crossings, ['dragged MouseTrack/MouseExit', 'beside MouseTrack/MouseEnter']);
    await gesture.removePointer();
  });

  testWidgets('a release Java never answers settles the hover on what the client shows', (tester) async {
    final gesture = await dragTwelveRight(tester);
    expect(crossings, isEmpty);

    await tester.pump(HoverExclusivityArbiter.settleHoldLimit);
    await tester.pump();
    expect(crossings, ['dragged MouseTrack/MouseExit', 'beside MouseTrack/MouseEnter']);
    await gesture.removePointer();
  });
}
