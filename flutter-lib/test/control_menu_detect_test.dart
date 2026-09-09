// Right-click on a Canvas: two independent failures, one guard each.
//
// 1. SWT sends MenuDetect to the control under the pointer whenever the platform's
//    context-menu trigger fires, whether or not that control has a Menu attached, and
//    applications rely on it to build their menu lazily -- a diagram editor is a single
//    Canvas painting its whole scene through a GC, and its menu manager only populates
//    the menu once MenuDetect has run. MenuDetect was sent by Table, TableItem,
//    CTabFolder and TrayItem only; no Control/Composite/Canvas ever sent it.
//
// 2. Even with a Menu attached, the Canvas menu never opened: applyMenu's GestureDetector
//    used the default deferToChild, and a childless Canvas offers nothing hit-testable
//    below it (the GC overlay is an IgnorePointer over an empty box), so the detector
//    never joined the gesture arena.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

/// Same as the generated CanvasSwt, except sendEvent captures outgoing events
/// instead of handing them to EquoCommService (which needs a live transport that
/// does not exist in a widget test).
class _CapturingCanvasSwt extends CanvasSwt<VCanvas> {
  const _CapturingCanvasSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCanvas val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

class _CapturingCompositeSwt extends CompositeSwt<VComposite> {
  const _CapturingCompositeSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VComposite val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VMenu _menu() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = 101
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Add Shape',
  ];

/// The diagram-editor shape: a Canvas with no child widgets, painting its whole
/// scene itself.
VCanvas _figureCanvas({VMenu? menu}) => VCanvas()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..menu = menu
  ..bounds = _rect(0, 0, 400, 300);

Widget _host(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 400, height: 300, child: child),
    );

void main() {
  testWidgets('right-clicking a Canvas with no menu sends MenuDetect',
      (tester) async {
    final events = <String>[];
    final payloads = <VEvent?>[];

    await tester.pumpWidget(_host(_CapturingCanvasSwt(
      value: _figureCanvas(),
      onEvent: (ev, payload) {
        events.add(ev);
        payloads.add(payload);
      },
    )));
    await tester.pump();

    await tester.tapAt(const Offset(120, 80), buttons: kSecondaryButton);
    await tester.pump();

    expect(events, contains('MenuDetect/MenuDetect'),
        reason: 'SWT sends MenuDetect to the control under the pointer even '
            'when no Menu is attached yet -- that is when a lazily-built '
            'context menu gets populated');

    final detect = payloads[events.indexOf('MenuDetect/MenuDetect')];
    expect(detect?.x, 120);
    expect(detect?.y, 80);

    expect(events.indexOf('Mouse/MouseDown'),
        lessThan(events.indexOf('MenuDetect/MenuDetect')),
        reason: 'a tool that tracks the secondary press picks the item under the '
            'cursor on MouseDown, so a menu built before that would describe the '
            'previous selection');
  });

  testWidgets('right-clicking a Canvas that has a menu sends MenuDetect '
      'and opens the menu', (tester) async {
    final events = <String>[];

    await tester.pumpWidget(_host(_CapturingCanvasSwt(
      value: _figureCanvas(menu: _menu()),
      onEvent: (ev, _) => events.add(ev),
    )));
    await tester.pump();

    await tester.tapAt(const Offset(120, 80), buttons: kSecondaryButton);
    await tester.pump();
    await tester.pump();
    await ackMenuShown(tester, 100);

    expect(events, contains('MenuDetect/MenuDetect'),
        reason: 'MenuDetect precedes the menu being shown, so a listener that '
            'populates the menu has run by the time it opens');
    expect(find.text('Add Shape'), findsOneWidget,
        reason: 'a menu attached to a childless Canvas must open on right-click -- '
            'nothing below applyMenu is hit-testable, so the detector has to be '
            'hittable in its own right');
  });

  testWidgets('right-clicking a Composite that has children sends MenuDetect',
      (tester) async {
    final events = <String>[];

    final composite = VComposite()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(0, 0, 400, 300)
      ..children = [
        VCanvas()
          ..id = 2
          ..style = SWT.NONE
          ..enabled = true
          ..bounds = _rect(0, 0, 400, 100),
      ];

    await tester.pumpWidget(_host(_CapturingCompositeSwt(
      value: composite,
      onEvent: (ev, _) => events.add(ev),
    )));
    await tester.pump();

    // Below the child canvas, on the composite's own area.
    await tester.tapAt(const Offset(200, 200), buttons: kSecondaryButton);
    await tester.pump();

    expect(events, contains('MenuDetect/MenuDetect'),
        reason: 'the has-children Composite path bypasses ControlImpl.wrap(), '
            'so it needs the same MenuDetect delivery');
  });
}
