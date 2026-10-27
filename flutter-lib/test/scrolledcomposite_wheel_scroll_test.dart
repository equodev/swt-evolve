// A scroll with the mouse wheel or a trackpad counts as the user's: a rebuild must keep the offset, and the
// offset is reported to Java as the selection of the matching scroll bar (what `getOrigin` reads).

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrollbar.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/scrolledcomposite_evolve.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VScrollBar _bar(int id, {int selection = 0}) => VScrollBar()
  ..id = id
  ..selection = selection
  ..maximum = 500
  ..thumb = 150;

VScrolledComposite _sc({int verticalSelection = 0}) => VScrolledComposite()
  ..id = 1
  ..style = SWT.H_SCROLL | SWT.V_SCROLL
  ..enabled = true
  ..bounds = _rect(0, 0, 150, 150)
  ..horizontalBar = _bar(10)
  ..verticalBar = _bar(11, selection: verticalSelection)
  ..content = (VComposite()
    ..id = 2
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(0, 0, 500, 500)
    ..children = [
      VComposite()
        ..id = 3
        ..style = SWT.NONE
        ..enabled = true
        ..bounds = _rect(0, 0, 10, 10),
    ]);

class _CapturingImpl
    extends ScrolledCompositeImpl<_CapturingSc, VScrolledComposite> {
  final sent = <(int, int)>[];

  @override
  void sendBarSelection(VScrollBar bar, int selection) =>
      sent.add((bar.id!, selection));
}

class _CapturingSc extends ScrolledCompositeSwt<VScrolledComposite> {
  const _CapturingSc({required super.value});

  @override
  State createState() => _CapturingImpl();
}

class _Harness {
  _Harness(this.tester, this.rebuild, this.impl);

  final WidgetTester tester;
  final VoidCallback rebuild;
  final _CapturingImpl impl;

  ScrollPosition get vertical => tester
      .stateList<ScrollableState>(find.byType(Scrollable))
      .map((s) => s.position)
      .firstWhere((p) => p.axis == Axis.vertical);

  Future<void> wheel(double dy) async {
    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(const Offset(75, 75)));
    await tester.sendEventToBinding(pointer.scroll(Offset(0, dy)));
    await tester.pump();
  }
}

Future<_Harness> _pump(WidgetTester tester, VScrolledComposite value) async {
  late StateSetter setter;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 150,
      height: 150,
      child: StatefulBuilder(builder: (context, setState) {
        setter = setState;
        return _CapturingSc(value: value);
      }),
    ),
  ));
  await tester.pump();
  final impl = tester.state<_CapturingImpl>(find.byType(_CapturingSc));
  return _Harness(tester, () => setter(() {}), impl);
}

void main() {
  testWidgets('an offset reached with the mouse wheel survives a rebuild',
      (tester) async {
    final h = await _pump(tester, _sc());

    await h.wheel(120);
    expect(h.vertical.pixels, greaterThan(0), reason: 'sanity: the wheel scrolled');
    final scrolled = h.vertical.pixels;

    h.rebuild();
    await tester.pump();

    expect(h.vertical.pixels, scrolled);
  });

  testWidgets('a wheel scroll is reported as the selection of the vertical bar',
      (tester) async {
    final h = await _pump(tester, _sc());

    await h.wheel(120);

    expect(h.impl.sent, isNotEmpty);
    expect(h.impl.sent.last, (11, h.vertical.pixels.round()));
    expect(h.impl.sent.every((e) => e.$1 == 11), isTrue,
        reason: 'only the vertical bar moved');
  });

  testWidgets('a scroll is not reported again once Java holds that offset',
      (tester) async {
    final h = await _pump(tester, _sc());

    await h.wheel(120);
    final count = h.impl.sent.length;
    h.rebuild();
    await tester.pump();

    expect(h.impl.sent.length, count);
  });

  testWidgets('before any user scroll the view follows the selection of the bar',
      (tester) async {
    final h = await _pump(tester, _sc(verticalSelection: 80));

    expect(h.vertical.pixels, 80);
    expect(h.impl.sent, isEmpty, reason: 'a programmatic offset is not a user scroll');
  });
}
