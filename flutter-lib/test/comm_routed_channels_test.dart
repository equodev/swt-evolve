// A GC drawer answers every op that travels as tagged values with one router on its base channel rather
// than a handler per op channel. An entry has to reach it exactly as it reached a handler of its
// own: in order, held until the router is up, and never once the router has gone.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_api.dart' show ChannelRouter;
import 'package:swtflutter/src/comm/comm_ws.dart';

import 'support/batch.dart';

/// A router answering `drawLine` and `fillRect` into [seen].
ChannelRouter _router(List<String> seen) => (name) => switch (name) {
      'drawLine' => (p) => seen.add('line:${(p as Map)['x1']}'),
      'fillRect' => (p) => seen.add('rect:${(p as Map)['x']}'),
      _ => null,
    };

void main() {
  test('a routed entry reaches the router by name, in order', () async {
    final seen = <String>[];
    EquoCommService.onRouted('GC/301', _router(seen));
    receiveBatch([
      ('GC/301/drawLine', {'x1': 1}),
      ('GC/301/fillRect', {'x': 2}),
      ('GC/301/drawLine', {'x1': 3}),
    ]);
    await Future.delayed(Duration.zero);
    expect(seen, ['line:1', 'rect:2', 'line:3']);
  });

  test('an entry that arrived before the router is replayed when it registers', () async {
    receiveBatch([
      ('GC/302/drawLine', {'x1': 5}),
      ('GC/302/fillRect', {'x': 6}),
    ]);
    await Future.delayed(Duration.zero);
    final seen = <String>[];
    EquoCommService.onRouted('GC/302', _router(seen));
    await Future.delayed(Duration.zero);
    expect(seen, ['line:5', 'rect:6']);
  });

  test('a channel with a handler of its own does not go to the router', () async {
    final routed = <String>[];
    final own = <String>[];
    EquoCommService.onRouted('GC/303', _router(routed));
    EquoCommService.onRaw('GC/303/drawLine', (p) => own.add('own:${(p as Map)['x1']}'));
    receiveBatch([
      ('GC/303/drawLine', {'x1': 7}),
    ]);
    await Future.delayed(Duration.zero);
    expect(own, ['own:7']);
    expect(routed, isEmpty);
  });

  test('a name the router does not know is held, not lost', () async {
    final seen = <String>[];
    EquoCommService.onRouted('GC/304', _router(seen));
    receiveBatch([
      ('GC/304/drawOval', {'x': 1}),
    ]);
    await Future.delayed(Duration.zero);
    final late = <String>[];
    EquoCommService.onRaw('GC/304/drawOval', (p) => late.add('oval:${(p as Map)['x']}'));
    await Future.delayed(Duration.zero);
    expect(seen, isEmpty);
    expect(late, ['oval:1']);
  });

  test('a removed router hears nothing, and only its own token removes it', () async {
    final first = <String>[];
    final token = EquoCommService.onRouted('GC/305', _router(first));
    final second = <String>[];
    EquoCommService.onRouted('GC/305', _router(second));
    // The first drawer's teardown leaves the newer drawer's router in place.
    EquoCommService.remove('GC/305', token);
    receiveBatch([
      ('GC/305/drawLine', {'x1': 9}),
    ]);
    await Future.delayed(Duration.zero);
    expect(first, isEmpty);
    expect(second, ['line:9']);
  });
}
