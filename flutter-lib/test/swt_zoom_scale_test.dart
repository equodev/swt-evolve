// A popup is placed at `anchorRect + localPosition`, where Material measures the anchor against
// the overlay and the position comes from the gesture. Both have to be read in the same space, so
// the zoom Transform has to sit above the overlay: with it below, a menu opened under the pointer
// lands at a fraction of the distance the pointer travelled from the anchor.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/swt_zoom_scale.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const _anchorKey = Key('anchor');
const _panelKey = Key('panel');

/// Opens a menu [tapOffset] from the anchor's top-left at [scale], and answers how far the panel
/// ended up from the anchor on screen.
Future<Offset> _menuGapAt(WidgetTester tester, double scale, Offset tapOffset) async {
  appScaleNotifier.value = scale;
  final controller = MenuController();
  await tester.pumpWidget(MaterialApp(
    builder: (context, child) => SwtZoomScale(child: child!),
    home: Scaffold(
      body: Align(
        alignment: Alignment.topLeft,
        child: MenuAnchor(
          controller: controller,
          menuChildren: const [SizedBox(key: _panelKey, width: 60, height: 30)],
          child: const SizedBox(key: _anchorKey, width: 40, height: 20),
        ),
      ),
    ),
  ));
  controller.open(position: tapOffset);
  await tester.pumpAndSettle();
  return tester.getTopLeft(find.byKey(_panelKey)) -
      tester.getTopLeft(find.byKey(_anchorKey));
}

void main() {
  tearDown(() => appScaleNotifier.value = 1.0);

  testWidgets('a zoomed app opens a menu the same distance from the anchor',
      (tester) async {
    const tap = Offset(12, 8);
    // Measured as a ratio against the unzoomed run rather than against a literal, so the panel's
    // own padding -- which is scaled too -- cancels out.
    final plain = await _menuGapAt(tester, 1.0, tap);
    final zoomed = await _menuGapAt(tester, 2.0, tap);

    expect(zoomed.dx, closeTo(plain.dx * 2, 0.01));
    expect(zoomed.dy, closeTo(plain.dy * 2, 0.01));
  });

  testWidgets('a zoomed app lays out in the space it is drawn into',
      (tester) async {
    appScaleNotifier.value = 2.0;
    late Size body;
    await tester.pumpWidget(MaterialApp(
      builder: (context, child) => SwtZoomScale(child: child!),
      home: Scaffold(
        body: LayoutBuilder(builder: (context, constraints) {
          body = constraints.biggest;
          return const SizedBox.expand();
        }),
      ),
    ));

    final view = tester.view.physicalSize / tester.view.devicePixelRatio;
    expect(body.width, closeTo(view.width / 2, 0.01));
    expect(body.height, closeTo(view.height / 2, 0.01));
  });

  testWidgets('applying a zoom keeps every widget State alive', (tester) async {
    final key = GlobalKey<_ProbeState>();
    Widget app() => MaterialApp(
          builder: (context, child) => SwtZoomScale(child: child!),
          home: Scaffold(body: _Probe(key: key)),
        );

    await tester.pumpWidget(app());
    final state = key.currentState;
    expect(state, isNotNull);

    // The zoom arrives with the config flags, well after the first frame. A tree that changed
    // depth here would lose every State below it -- and with it each widget's subscription.
    appScaleNotifier.value = 2.0;
    await tester.pumpWidget(app());
    expect(key.currentState, same(state));
  });
}

class _Probe extends StatefulWidget {
  const _Probe({super.key});

  @override
  State<_Probe> createState() => _ProbeState();
}

class _ProbeState extends State<_Probe> {
  @override
  Widget build(BuildContext context) => const SizedBox.shrink();
}
