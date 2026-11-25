// flutter test --platform chrome test/web/
@TestOn('browser')
library;

import 'dart:js_interop';
import 'dart:ui_web' as ui_web;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/impl/utils/press_replay.dart';
import 'package:web/web.dart' as web;

class _CapturingButtonSwt extends ButtonSwt<VButton> {
  const _CapturingButtonSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VButton val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!);
}

VButton _button(int style) => VButton()
  ..id = 1
  ..style = style
  ..enabled = true
  ..visible = true
  ..text = 'Push'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 90
    ..height = 26);

Future<List<String>> _pumpButton(WidgetTester tester, {int style = SWT.PUSH}) async {
  final sent = <String>[];
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: _CapturingButtonSwt(value: _button(style), onEvent: sent.add),
      ),
    ),
  );
  await tester.pumpAndSettle();
  return sent;
}

class _CapturingTabFolderSwt extends TabFolderSwt<VTabFolder> {
  const _CapturingTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VTabFolder val, int channel, VEvent? payload) =>
      onEvent(wireChannelNames[channel]!);
}

VTabFolder _folder() {
  final one = VTabItem()
    ..id = 11
    ..text = 'One';
  return VTabFolder()
    ..id = 10
    ..style = SWT.TOP
    ..enabled = true
    ..visible = true
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 120)
    ..items = [
      one,
      VTabItem()
        ..id = 12
        ..text = 'Two',
    ]
    ..selection = [one];
}

web.Element _tappableNode() {
  final nodes = web.document.querySelectorAll('flt-semantics[flt-tappable]');
  expect(nodes.length, 1, reason: 'sanity: the button is the one tappable semantics node');
  return nodes.item(0)! as web.Element;
}

void _dispatch(web.Element target, String type, double x, double y, int buttons) {
  final init =
      {
            'bubbles': true,
            'cancelable': true,
            'composed': true,
            'clientX': x,
            'clientY': y,
            'button': 0,
            'buttons': buttons,
            'pointerId': 1,
            'pointerType': 'mouse',
            'isPrimary': true,
          }.jsify()
          as web.PointerEventInit;
  target.dispatchEvent(web.PointerEvent(type, init));
}

List<String> _mouseAndSelection(List<String> sent, {bool withDoubleClick = false}) => sent
    .where((e) =>
        e == 'Mouse/MouseDown' ||
        e == 'Mouse/MouseUp' ||
        e == 'Selection/Selection' ||
        (withDoubleClick && e == 'Mouse/MouseDoubleClick'))
    .toList();

void _click(web.Element target, double x, double y) => target.dispatchEvent(
  web.MouseEvent(
    'click',
    {'bubbles': true, 'clientX': x, 'clientY': y}.jsify() as web.MouseEventInit,
  ),
);

void main() {
  setUpAll(() {
    // flutter_test maps the browser's pointer positions at a pixel ratio of 3 while the semantics
    // nodes sit at the browser's own: the browser's, as in the app, so both land on the button.
    ui_web.debugOverrideDevicePixelRatio(null);
    PressReplay.ensureInstalled();
    // As an assistive technology turns the engine's semantics tree on.
    (web.document.querySelector('flt-semantics-placeholder') as web.HTMLElement?)?.click();
  });

  // With the semantics tree on, the engine holds back a press on a tappable node and, when the
  // browser's click follows, sends a semantics tap in its place and drops the press.
  for (final (name, style) in [
    ('push', SWT.PUSH),
    ('check', SWT.CHECK),
    ('radio', SWT.RADIO),
    ('toggle', SWT.TOGGLE),
  ]) {
    testWidgets('a left click on a $name button reports MouseDown and MouseUp with semantics on', (
      tester,
    ) async {
      final sent = await _pumpButton(tester, style: style);
      final node = _tappableNode();
      final rect = node.getBoundingClientRect();
      final x = rect.left + rect.width / 2;
      final y = rect.top + rect.height / 2;

      _dispatch(node, 'pointerdown', x, y, 1);
      _dispatch(node, 'pointerup', x, y, 0);
      _click(node, x, y);
      await tester.pumpAndSettle();

      expect(_mouseAndSelection(sent), ['Mouse/MouseDown', 'Mouse/MouseUp', 'Selection/Selection']);
    });
  }

  // Held past the engine's 200 ms debounce, the press reaches the app and the click is dropped.
  testWidgets('a long left press on a push button reports its press once', (tester) async {
    final sent = await _pumpButton(tester);
    final node = _tappableNode();
    final rect = node.getBoundingClientRect();
    final x = rect.left + rect.width / 2;
    final y = rect.top + rect.height / 2;

    _dispatch(node, 'pointerdown', x, y, 1);
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 400)));
    _dispatch(node, 'pointerup', x, y, 0);
    _click(node, x, y);
    await tester.pumpAndSettle();

    expect(_mouseAndSelection(sent), ['Mouse/MouseDown', 'Mouse/MouseUp', 'Selection/Selection']);
  });

  // Each replayed press keeps the browser's time stamp, so only quick clicks chain.
  testWidgets('two left clicks on a push button far apart send no MouseDoubleClick', (tester) async {
    final sent = await _pumpButton(tester);
    final node = _tappableNode();
    final rect = node.getBoundingClientRect();
    final x = rect.left + rect.width / 2;
    final y = rect.top + rect.height / 2;

    for (var i = 0; i < 2; i++) {
      if (i > 0) await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 1500)));
      _dispatch(node, 'pointerdown', x, y, 1);
      _dispatch(node, 'pointerup', x, y, 0);
      _click(node, x, y);
      await tester.pumpAndSettle();
    }

    expect(_mouseAndSelection(sent, withDoubleClick: true), [
      ...['Mouse/MouseDown', 'Mouse/MouseUp', 'Selection/Selection'],
      ...['Mouse/MouseDown', 'Mouse/MouseUp', 'Selection/Selection'],
    ]);
  });

  testWidgets('a quick double click on a push button sends one MouseDoubleClick', (tester) async {
    final sent = await _pumpButton(tester);
    final node = _tappableNode();
    final rect = node.getBoundingClientRect();
    final x = rect.left + rect.width / 2;
    final y = rect.top + rect.height / 2;

    for (var i = 0; i < 2; i++) {
      _dispatch(node, 'pointerdown', x, y, 1);
      _dispatch(node, 'pointerup', x, y, 0);
      _click(node, x, y);
      await tester.pump();
    }
    await tester.pumpAndSettle();

    expect(_mouseAndSelection(sent, withDoubleClick: true).where((e) => e == 'Mouse/MouseDoubleClick'),
        hasLength(1));
  });

  // Every tappable node gets the replay, not only a button's: a native folder reports the press on
  // a tab too, and selects the tab on the press.
  testWidgets('a left click on a tab reports MouseDown and MouseUp with semantics on', (
    tester,
  ) async {
    final sent = <String>[];
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: 300,
            height: 120,
            child: _CapturingTabFolderSwt(value: _folder(), onEvent: sent.add),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    final found = web.document.querySelectorAll('flt-semantics[flt-tappable]');
    final tabs = [for (var i = 0; i < found.length; i++) found.item(i)! as web.Element]
      ..sort((a, b) => a.getBoundingClientRect().left.compareTo(b.getBoundingClientRect().left));
    expect(tabs, hasLength(2), reason: 'sanity: the two tabs are the tappable semantics nodes');
    final node = tabs[1];
    sent.clear();
    final rect = node.getBoundingClientRect();
    final x = rect.left + rect.width / 2;
    final y = rect.top + rect.height / 2;

    _dispatch(node, 'pointerdown', x, y, 1);
    _dispatch(node, 'pointerup', x, y, 0);
    _click(node, x, y);
    await tester.pumpAndSettle();

    expect(_mouseAndSelection(sent), ['Mouse/MouseDown', 'Selection/Selection', 'Mouse/MouseUp']);
  });

  // A screen reader activates the button with a click and no pointer: it selects, with no press.
  testWidgets('a click with no pointer selects a push button without a MouseDown', (tester) async {
    final sent = await _pumpButton(tester);
    // The engine drops a click within 50 ms of the last pointer up it delivered, a previous test's.
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 60)));
    _tappableNode().dispatchEvent(
      web.MouseEvent('click', {'bubbles': true}.jsify() as web.MouseEventInit),
    );
    await tester.pumpAndSettle();

    expect(_mouseAndSelection(sent), ['Selection/Selection']);
  });
}
