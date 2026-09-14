// SWT.MouseMove must carry the buttons held during the move in its stateMask.
// draw2d and GEF both decide drag-vs-hover from that field alone:
// SWTEventDispatcher.dispatchMouseMoved reaches handleMouseDragged, and
// DomainEventDispatcher.dispatchMouseMoved reaches EditDomain.mouseDrag, only
// when (stateMask & BUTTON_MASK) != 0. A move sent with stateMask 0 is
// delivered as a hover, so a diagram tool never sees a drag at all -- no
// marquee rubber band, no connection routing preview, no drop-validity cursor.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingLabelSwt extends LabelSwt<VLabel> {
  final List<int?> moves;
  const _RecordingLabelSwt({required VLabel value, required this.moves})
      : super(value: value);

  @override
  void sendMouseMoveMouseMove(VLabel val, VEvent? payload) =>
      moves.add(payload?.stateMask);
}

VLabel _label() => VLabel()
  ..id = 1
  ..enabled = true
  ..text = 'target'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

Future<List<int?>> _setUp(WidgetTester tester, List<int?> moves) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 60,
      child: _RecordingLabelSwt(value: _label(), moves: moves),
    ),
  ));
  await tester.pump();
  return moves;
}

void main() {
  testWidgets('a move with the left button held sets SWT.BUTTON1', (tester) async {
    final moves = await _setUp(tester, <int?>[]);
    final at = tester.getCenter(find.text('target'));

    final gesture = await tester.createGesture(buttons: kPrimaryMouseButton);
    await gesture.down(at);
    await gesture.moveTo(at + const Offset(40, 20));
    await tester.pump();
    await gesture.up();

    expect(moves, isNotEmpty, reason: 'the drag move never reached the widget');
    expect(moves.first! & SWT.BUTTON_MASK, isNot(0),
        reason: 'draw2d/GEF only take their drag branch when a button bit is set');
    expect(moves.first! & SWT.BUTTON1, SWT.BUTTON1);
  });

  testWidgets('a move with the right button held sets BUTTON3, not BUTTON2',
      (tester) async {
    // SWT numbers buttons by position, so the right button is BUTTON3 while
    // Flutter calls it "secondary" -- an easy pairing to invert.
    final moves = await _setUp(tester, <int?>[]);
    final at = tester.getCenter(find.text('target'));

    final gesture = await tester.createGesture(buttons: kSecondaryMouseButton);
    await gesture.down(at);
    await gesture.moveTo(at + const Offset(40, 20));
    await tester.pump();
    await gesture.up();

    expect(moves, isNotEmpty);
    expect(moves.first! & SWT.BUTTON3, SWT.BUTTON3);
    expect(moves.first! & SWT.BUTTON2, 0);
  });

  testWidgets('a hover with no button held carries no button bits',
      (tester) async {
    final moves = await _setUp(tester, <int?>[]);
    final at = tester.getCenter(find.text('target'));

    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: at);
    await gesture.moveTo(at + const Offset(30, 10));
    await tester.pump();

    expect(moves, isNotEmpty, reason: 'the hover move never reached the widget');
    expect(moves.first! & SWT.BUTTON_MASK, 0,
        reason: 'a hover must stay a hover -- a button bit here would make '
            'every mouse move start a drag');
  });
}
