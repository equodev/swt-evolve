// No native focuses an AnimatedProgress or a ScrolledComposite on a click: a progress bar takes no
// focus, and a ScrolledComposite's belongs to its content.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/animatedprogress.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scrolledcomposite.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _r() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 200
  ..height = 100;

class _AP extends AnimatedProgressSwt<VAnimatedProgress> {
  const _AP({required super.value, required this.out});
  final List<String> out;
  @override
  void sendEvent(VAnimatedProgress v, String ev, VEvent? p) {
    if (ev.startsWith('Focus/')) out.add(ev);
  }
}

class _SC extends ScrolledCompositeSwt<VScrolledComposite> {
  const _SC({required super.value, required this.out});
  final List<String> out;
  @override
  void sendEvent(VScrolledComposite v, String ev, VEvent? p) {
    if (ev.startsWith('Focus/')) out.add(ev);
  }
}

Future<List<String>> _click(
  WidgetTester t,
  Widget Function(List<String>) w,
) async {
  final out = <String>[];
  await t.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 100, child: w(out)),
      ),
    ),
  );
  await t.pump();
  await t.tapAt(const Offset(100, 50));
  await t.pump(const Duration(milliseconds: 500));
  return out;
}

void main() {
  testWidgets('a click focuses no AnimatedProgress', (t) async {
    final sent = await _click(
      t,
      (o) => _AP(
        out: o,
        value: VAnimatedProgress()
          ..swt = 'AnimatedProgress'
          ..id = 1
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = _r(),
      ),
    );
    expect(sent, isEmpty);
  });

  testWidgets('a click focuses no empty ScrolledComposite', (t) async {
    final sent = await _click(
      t,
      (o) => _SC(
        out: o,
        value: VScrolledComposite()
          ..swt = 'ScrolledComposite'
          ..id = 2
          ..style = SWT.H_SCROLL | SWT.V_SCROLL
          ..enabled = true
          ..visible = true
          ..bounds = _r()
          ..children = [],
      ),
    );
    expect(sent, isEmpty);
  });
}
