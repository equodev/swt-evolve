@Tags(['bench'])
library;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

const _count = 1000;
const _rounds = 6;

VRectangle _cell(int i) => VRectangle()
  ..x = (i % 40) * 30
  ..y = (i ~/ 40) * 24
  ..width = 28
  ..height = 22;

/// What one frame of [_count] widgets each changing one property costs, by phase, after warming up.
Future<void> _measure(WidgetTester tester, String name, List<VControl> Function(int round) children,
    List<int> changed) async {
  freshClient();
  final root = VComposite()
    ..id = 1
    ..style = SWT.NONE
    ..seq = 1
    ..bounds = (VRectangle()
      ..width = 1200
      ..height = 800)
    ..children = children(0);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 1200, height: 800, child: CompositeSwt<VComposite>(value: root)),
  ));
  await tester.pumpAndSettle();
  final totals = <String, double>{};
  var rebuiltTotal = 0;
  for (var round = 1; round <= _rounds; round++) {
    for (final child in children(round)) {
      await deliverChange(child, keys: changed, base: child.seq - 1);
    }
    var rebuilt = 0;
    final kinds = <String, int>{};
    debugOnRebuildDirtyWidget = (e, __) {
      rebuilt++;
      if (round == _rounds) {
        final kind = e.widget.runtimeType.toString().split('<').first;
        kinds[kind] = (kinds[kind] ?? 0) + 1;
      }
    };
    if (round == _rounds) {
      tester.binding.addPostFrameCallback((_) {
        final top = kinds.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
        // ignore: avoid_print
        print('[frame-phases] $name rebuilt per widget: '
            '${top.map((e) => '${e.key} ${(e.value / _count).toStringAsFixed(1)}').join(', ')}');
      });
    }
    FlutterTimeline.debugCollectionEnabled = true;
    FlutterTimeline.debugReset();
    await tester.pump();
    final timings = FlutterTimeline.debugCollect();
    FlutterTimeline.debugCollectionEnabled = false;
    debugOnRebuildDirtyWidget = null;
    if (round == 1) continue;
    rebuiltTotal += rebuilt;
    for (final t in timings.timedBlocks) {
      totals[t.name] = (totals[t.name] ?? 0) + t.duration;
    }
  }
  final n = _rounds - 1;
  final phases = totals.entries.where((e) => e.value / n >= 50).map((e) => '${e.key} ${(e.value / n).round()}').join(', ');
  // ignore: avoid_print
  print('[frame-phases] $name: ${rebuiltTotal ~/ n} elements rebuilt; us per frame: $phases');
}

void main() {
  testWidgets('frame phases per property change', (tester) async {
    await _measure(
        tester,
        'button.enabled',
        (round) => [
              for (var i = 0; i < _count; i++)
                VButton()
                  ..id = 1000 + i
                  ..style = SWT.PUSH
                  ..seq = 10 + round
                  ..bounds = _cell(i)
                  ..text = 'B$i'
                  ..enabled = round.isEven,
            ],
        [WireKey.k_Control_enabled]);
    await _measure(
        tester,
        'button.toolTipText',
        (round) => [
              for (var i = 0; i < _count; i++)
                VButton()
                  ..id = 3000 + i
                  ..style = SWT.PUSH
                  ..seq = 10 + round
                  ..bounds = _cell(i)
                  ..text = 'B$i'
                  ..toolTipText = 'tip $round',
            ],
        [WireKey.k_Control_toolTipText]);
    await _measure(
        tester,
        'label.text',
        (round) => [
              for (var i = 0; i < _count; i++)
                VLabel()
                  ..id = 5000 + i
                  ..style = SWT.NONE
                  ..seq = 10 + round
                  ..bounds = _cell(i)
                  ..text = 'L$i r$round',
            ],
        [WireKey.k_Label_text]);
  });
}
