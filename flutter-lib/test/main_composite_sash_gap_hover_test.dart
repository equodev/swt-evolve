// The e4 workbench sets the divider's resize cursor itself: SashLayout's MouseMove listener on the
// sash container sets SIZEWE while the pointer is within 5px of the band between two part stacks,
// and its MouseExit listener clears it. MainComposite paints each part stack inset by
// panelChildGap, so the divider the user sees is the band plus one gap on each side. The
// container has to hear the hover over all of it, or the resize cursor only shows on the band.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/cursor.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/control_evolve.dart' show ControlImpl;
import 'package:swtflutter/src/impl/utils/pending_move.dart';

class _CapturingMainComposite extends MainComposite {
  const _CapturingMainComposite({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VComposite val, int channel, VEvent? payload) =>
      onEvent(wireChannelNames[channel]!, payload);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _part({required int id, required VRectangle bounds}) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..text = 'part $id'
  ..bounds = bounds;

// SashLayout.tileSubNodes for two part stacks in a 400x300 container, sashWidth 4.
const double _sashWidth = 4;
const double _panelChildGap = 5;
const double _bandLeft = 198;
const double _bandRight = _bandLeft + _sashWidth;
const double _y = 150;
const int _mouse = 1;

VComposite _sashContainer(int id, {VCursor? cursor}) => VComposite()
  ..swt = 'MainComposite'
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..cursor = cursor
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _part(id: id + 1, bounds: _rect(0, 0, _bandLeft.toInt(), 300)),
    _part(id: id + 2, bounds: _rect(_bandRight.toInt(), 0, 198, 300)),
  ];

Future<List<(String, VEvent?)>> _mount(WidgetTester tester, VComposite value) async {
  final events = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: _CapturingMainComposite(
        value: value,
        onEvent: (ev, payload) => events.add((ev, payload)),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  events.clear();
  return events;
}

Future<TestGesture> _mouseAt(WidgetTester tester, Offset at) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse, pointer: _mouse);
  await gesture.addPointer(location: at);
  addTearDown(gesture.removePointer);
  await tester.pumpAndSettle();
  return gesture;
}

Iterable<VEvent?> _moves(List<(String, VEvent?)> events) =>
    events.where((e) => e.$1.contains('MouseMove')).map((e) => e.$2);

void main() {
  testWidgets('hover on the band reaches the container', (tester) async {
    final events = await _mount(tester, _sashContainer(40));

    final gesture = await _mouseAt(tester, const Offset(100, _y));
    await gesture.moveTo(const Offset(_bandLeft + _sashWidth / 2, _y));
    await tester.pumpAndSettle();

    expect(_moves(events).map((e) => e?.x), contains((_bandLeft + _sashWidth / 2).round()));
  });

  testWidgets('hover in the painted gap beside the band reaches the container', (tester) async {
    final events = await _mount(tester, _sashContainer(10));
    const inGapRing = _bandLeft - _panelChildGap / 2;

    final gesture = await _mouseAt(tester, const Offset(_bandLeft + _sashWidth / 2, _y));
    await gesture.moveTo(const Offset(inGapRing, _y));
    await tester.pumpAndSettle();

    expect(_moves(events).map((e) => e?.x), contains(inGapRing.round()),
        reason: 'nothing of the part is painted there, so SashLayout must hear the move to set '
            'its resize cursor over the whole divider the user sees');
  });

  testWidgets('the move in the gap reaches the container before the exit onto the part',
      (tester) async {
    const id = 60;
    final sent = <(String, VEvent?)>[];
    EquoCommBase.sendObserverForTesting = (action, payload) {
      final parts = action.split('/');
      if (parts[1] == '$id' && parts[2].startsWith('Mouse')) sent.add((parts.skip(2).join('/'), payload as VEvent?));
    };
    final clock = ControlImpl.nowMicros;
    // A clock that stands still keeps every move after the first inside the throttle window, so the
    // one in the gap is held when the pointer crosses onto the part.
    ControlImpl.nowMicros = () => 1 << 40;
    addTearDown(() {
      EquoCommBase.sendObserverForTesting = null;
      ControlImpl.nowMicros = clock;
      PendingMove.reset();
    });
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: MainComposite(value: _sashContainer(id))),
    ));
    await tester.pumpAndSettle();
    const inGapRing = _bandLeft - _panelChildGap / 2;

    final gesture = await _mouseAt(tester, const Offset(_bandLeft + 1, _y));
    sent.clear();
    await gesture.moveTo(const Offset(_bandLeft + 2, _y));
    await gesture.moveTo(const Offset(inGapRing, _y));
    await gesture.moveTo(const Offset(100, _y));
    await tester.pumpAndSettle();

    expect(sent.map((e) => e.$1).last, 'MouseTrack/MouseExit',
        reason: 'SashLayout clears its cursor on MouseExit; a move after it sets the cursor again');
    expect(sent[sent.length - 2].$2?.x, inGapRing.round());
  });

  testWidgets('hover on the painted part is still left to the part', (tester) async {
    final events = await _mount(tester, _sashContainer(20));

    final gesture = await _mouseAt(tester, const Offset(_bandLeft + _sashWidth / 2, _y));
    await gesture.moveTo(const Offset(100, _y));
    await tester.pumpAndSettle();

    expect(_moves(events).map((e) => e?.x), isNot(contains(100)));
  });

  testWidgets('a drag from the band onto a part keeps the resize cursor', (tester) async {
    final events = await _mount(
        tester, _sashContainer(30, cursor: VCursor()..cursorStyle = SWT.CURSOR_SIZEWE));

    final gesture = await _mouseAt(tester, const Offset(_bandLeft + _sashWidth / 2, _y));
    await gesture.down(const Offset(_bandLeft + _sashWidth / 2, _y));
    await tester.pump();
    await gesture.moveTo(const Offset(150, _y));
    await tester.pumpAndSettle();

    expect(events.where((e) => e.$1.contains('MouseExit')), isEmpty,
        reason: 'the container holds the press, so the pointer has not left it for SWT; '
            'SashLayout clears its cursor on MouseExit');
    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mouse),
        SystemMouseCursors.resizeLeftRight);
    await gesture.up();
    await tester.pumpAndSettle();
  });

  testWidgets('a drag from the band onto a part with its own cursor keeps the resize cursor',
      (tester) async {
    final value = _sashContainer(50, cursor: VCursor()..cursorStyle = SWT.CURSOR_SIZEWE);
    value.children![0].cursor = VCursor()..cursorStyle = SWT.CURSOR_ARROW;
    await _mount(tester, value);

    final gesture = await _mouseAt(tester, const Offset(_bandLeft + _sashWidth / 2, _y));
    await gesture.down(const Offset(_bandLeft + _sashWidth / 2, _y));
    await tester.pump();
    await gesture.moveTo(const Offset(150, _y));
    await tester.pumpAndSettle();

    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mouse),
        SystemMouseCursors.resizeLeftRight,
        reason: 'the container holds the press, and the control holding the mouse keeps its '
            'cursor until the release, whatever the pointer passes over');
    await gesture.up();
    await gesture.moveTo(const Offset(140, _y));
    await tester.pumpAndSettle();

    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mouse),
        SystemMouseCursors.basic,
        reason: 'once released, the part under the pointer shows its own cursor again');
  });

  // Where a vertical and a horizontal sash meet, SashLayout sets SIZEALL. Flutter's move cursor is
  // the CSS `move`, which a Linux cursor theme such as Adwaita draws as the plain arrow; all-scroll
  // is the four-way arrow GTK's SWT shows (GDK_FLEUR), and the same IDC_SIZEALL on Windows.
  testWidgets('a container set to SIZEALL shows the four-way resize cursor', (tester) async {
    await _mount(tester, _sashContainer(70, cursor: VCursor()..cursorStyle = SWT.CURSOR_SIZEALL));

    await _mouseAt(tester, const Offset(_bandLeft + _sashWidth / 2, _y));

    expect(RendererBinding.instance.mouseTracker.debugDeviceActiveCursor(_mouse),
        SystemMouseCursors.allScroll);
  });
}
