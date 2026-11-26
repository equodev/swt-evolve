// A resizable shell resizes from a border just outside its frame, and every point inside the frame
// belongs to its controls. Natively a SWT.RESIZE shell adds no trim (its client area is its whole
// bounds) and the window manager's resize zone lies on and past the window's edge.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/pointer.dart';

import 'support/sent_events.dart';

// DisplaySwt caches the last state it saw per display id, so each test uses its own id.
int _base(int n) => 8700 + n * 10;

const _shellW = 420;
const _shellH = 240;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// A trim-less shell of [style] whose Composite fills it, with a push button against each edge of
/// the frame. With [bounds], it opens over a main shell, as a secondary shell of that size does.
VDisplay _shell(int base, int style, VRectangle? bounds) => VDisplay()
  ..swt = 'Display'
  ..id = base
  ..activeShellId = base + 1
  ..mainShellId = bounds == null ? null : base + 9
  ..shells = [
    if (bounds != null)
      VShell()
        ..swt = 'Shell'
        ..id = base + 9
        ..style = SWT.SHELL_TRIM
        ..visible = true
        ..bounds = bounds,
    VShell()
      ..swt = 'Shell'
      ..id = base + 1
      ..style = style
      ..visible = true
      ..bounds = bounds ?? _rect(100, 100, _shellW, _shellH)
      ..children = [
        VComposite()
          ..swt = 'Composite'
          ..id = base + 2
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = _rect(0, 0, _shellW, _shellH)
          ..children = [
            _button(base + 3, 'left', _rect(0, 100, 60, 24)),
            _button(base + 4, 'right', _rect(_shellW - 60, 100, 60, 24)),
            _button(base + 5, 'top', _rect(180, 0, 60, 24)),
            _button(base + 6, 'bottom', _rect(180, _shellH - 24, 60, 24)),
          ],
      ],
  ];

VButton _button(int id, String text, VRectangle bounds) => VButton()
  ..swt = 'Button'
  ..id = id
  ..style = SWT.PUSH
  ..text = text
  ..enabled = true
  ..visible = true
  ..bounds = bounds;

/// The shell under test: a main shell, when there is one, comes first.
Finder get _frame => find.byWidgetPredicate((w) => w is ShellSwt).last;

/// The shell's frame in whole pixels: a drag summed from several moves leaves float noise.
Rect _frameRect(WidgetTester tester) {
  final r = tester.getRect(_frame);
  return Rect.fromLTRB(r.left.roundToDouble(), r.top.roundToDouble(), r.right.roundToDouble(),
      r.bottom.roundToDouble());
}

Future<Rect> _pump(WidgetTester tester, int base,
    {int style = SWT.ON_TOP | SWT.RESIZE, VRectangle? bounds}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(value: _shell(base, style, bounds)),
  ));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
  return _frameRect(tester);
}

Future<void> _drag(WidgetTester tester, Offset from, Offset by) async {
  final gesture = await tester.startGesture(from, kind: PointerDeviceKind.mouse);
  await tester.pump();
  for (var i = 1; i <= 6; i++) {
    await gesture.moveTo(from + by * (i / 6));
    await tester.pump();
  }
  await gesture.up();
  await tester.pumpAndSettle();
}

Future<void> _click(WidgetTester tester, Offset at) async {
  final gesture = await tester.startGesture(at, kind: PointerDeviceKind.mouse);
  await tester.pump();
  await gesture.up();
  await tester.pumpAndSettle();
}

List<String> _events(String prefix, String name) =>
    sentEvents().map((e) => e.$1).where((c) => c.startsWith(prefix) && c.contains(name)).toList();

void main() {
  setUp(() => EquoCommBase.recordSentFrames = true);

  testWidgets('a drag just outside the top-right corner resizes from that corner', (tester) async {
    final base = _base(1);
    final before = await _pump(tester, base);

    await _drag(tester, before.topRight + const Offset(3, -3), const Offset(40, -30));

    final after = _frameRect(tester);
    expect(after.left, before.left);
    expect(after.bottom, before.bottom);
    expect(after.width, before.width + 40);
    expect(after.top, before.top - 30);
    expect(_events('Shell/${base + 1}/', 'SetBounds'), isNotEmpty);
  });

  testWidgets('a drag just outside each edge resizes from that edge', (tester) async {
    final base = _base(2);
    var r = await _pump(tester, base);

    await _drag(tester, r.centerLeft - const Offset(4, 0), const Offset(-25, 0));
    var after = _frameRect(tester);
    expect((after.left, after.right), (r.left - 25, r.right));
    r = after;

    await _drag(tester, r.centerRight + const Offset(4, 0), const Offset(25, 0));
    after = _frameRect(tester);
    expect((after.left, after.right), (r.left, r.right + 25));
    r = after;

    await _drag(tester, r.topCenter - const Offset(0, 4), const Offset(0, -20));
    after = _frameRect(tester);
    expect((after.top, after.bottom), (r.top - 20, r.bottom));
    r = after;

    await _drag(tester, r.bottomCenter + const Offset(0, 4), const Offset(0, 20));
    after = _frameRect(tester);
    expect((after.top, after.bottom), (r.top, r.bottom + 20));
  });

  for (final (offset, edge) in [(3, 'left'), (4, 'right'), (5, 'top'), (6, 'bottom')]) {
    testWidgets('a press on a button 2 px inside the $edge edge selects it', (tester) async {
      final base = _base(3 + offset);
      final frame = await _pump(tester, base);
      final at = switch (edge) {
        'left' => frame.centerLeft.translate(2, 12 - _shellH / 2 + 100),
        'right' => frame.centerRight.translate(-2, 12 - _shellH / 2 + 100),
        'top' => frame.topCenter.translate(0, 2),
        _ => frame.bottomCenter.translate(0, -2),
      };

      await _click(tester, at);

      expect(_events('Button/${base + offset}/', 'Selection'), isNotEmpty);
    });
  }

  // Natively only a maximized window loses its resize border; one merely as big as the screen keeps
  // it, so where the band would lie outside the viewport it lies just inside the frame.
  testWidgets('a shell filling the viewport resizes from just inside each edge', (tester) async {
    tester.view.physicalSize = const Size(800, 600);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final base = _base(21);
    var r = await _pump(tester, base, bounds: _rect(0, 0, 800, 600));
    expect(r.topLeft, Offset.zero);
    expect(r.right >= 800 && r.bottom >= 600, isTrue, reason: '$r must cover the viewport');

    await _drag(tester, r.centerLeft + const Offset(3, 40), const Offset(25, 0));
    var after = _frameRect(tester);
    expect((after.left, after.right), (r.left + 25, r.right));
    r = after;

    await _drag(tester, r.centerRight + const Offset(-3, 40), const Offset(-25, 0));
    after = _frameRect(tester);
    expect((after.left, after.right), (r.left, r.right - 25));
    r = after;

    await _drag(tester, r.topCenter + const Offset(40, 3), const Offset(0, 20));
    after = _frameRect(tester);
    expect((after.top, after.bottom), (r.top + 20, r.bottom));
    r = after;

    await _drag(tester, r.bottomCenter + const Offset(40, -3), const Offset(0, -20));
    after = _frameRect(tester);
    expect((after.top, after.bottom), (r.top, r.bottom - 20));
    expect(_events('Shell/${base + 1}/', 'SetBounds'), isNotEmpty);
  });

  // On web a Browser under the band would take the press without a DOM element of the shell's own
  // there; the frame's interceptor covers only the frame.
  testWidgets('each part of the resize border outside the frame has its own pointer interceptor',
      (tester) async {
    final frame = await _pump(tester, _base(22));
    const cursors = [
      SystemMouseCursors.resizeUpDown,
      SystemMouseCursors.resizeLeftRight,
      SystemMouseCursors.resizeUpLeftDownRight,
      SystemMouseCursors.resizeUpRightDownLeft,
    ];
    final handles = find.byWidgetPredicate((w) => w is MouseRegion && cursors.contains(w.cursor));
    expect(handles, findsNWidgets(8));

    for (final handle in handles.evaluate()) {
      final band = find.byElementPredicate((e) => e == handle);
      final rect = tester.getRect(band);
      expect(frame.contains(rect.topLeft) && frame.contains(rect.bottomRight - const Offset(1, 1)),
          isFalse, reason: '$rect lies outside the frame $frame');
      final covered = find
          .ancestor(of: band, matching: find.byType(PointerInterceptorScope))
          .evaluate()
          .any((s) => tester.getRect(find.byElementPredicate((e) => e == s)).contains(rect.center));
      expect(covered, isTrue, reason: 'the resize handle at $rect has no pointer interceptor');
    }
  });

  testWidgets('a shell without SWT.RESIZE takes no drag outside its frame', (tester) async {
    final before = await _pump(tester, _base(20), style: SWT.ON_TOP);

    await _drag(tester, before.topRight + const Offset(3, -3), const Offset(40, -30));

    expect(_frameRect(tester), before);
  });
}
