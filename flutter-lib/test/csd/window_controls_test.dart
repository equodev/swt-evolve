import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/custom/csd/csd_state.dart';
import 'package:swtflutter/src/custom/csd/window_controls.dart';

void main() {
  // Records which window operation a control invoked.
  late List<String> calls;
  late WindowControlActions actions;

  setUp(() {
    csdMaximized.value = false;
    calls = [];
    actions = WindowControlActions(
      onMinimize: () => calls.add('min'),
      onMaxRestore: (max) => calls.add(max ? 'maximize' : 'restore'),
      onClose: () => calls.add('close'),
    );
  });

  Future<void> pump(WidgetTester tester, String os) async {
    await tester.pumpWidget(MaterialApp(
      home: Scaffold(
        body: Center(
          child: SizedBox(
            height: 32,
            child: WindowControls(osOverride: os, actions: actions),
          ),
        ),
      ),
    ));
  }

  for (final os in ['mac', 'windows', 'linux']) {
    group('$os controls', () {
      // mac traffic-lights have no Tooltip (just a Semantics label); win/linux use Tooltips.
      Finder ctl(String label) =>
          os == 'mac' ? find.bySemanticsLabel(label) : find.byTooltip(label);

      testWidgets('minimize / maximize / close fire the right action', (tester) async {
        final handle = os == 'mac' ? tester.ensureSemantics() : null;
        await pump(tester, os);

        await tester.tap(ctl('Minimize'));
        // maximize label differs by OS (Zoom on mac, Maximize elsewhere)
        await tester.tap(ctl(os == 'mac' ? 'Zoom' : 'Maximize'));
        await tester.tap(ctl('Close'));
        await tester.pump();

        expect(calls, ['min', 'maximize', 'close']);
        handle?.dispose();
      });

      testWidgets('reflects maximized state from csdMaximized', (tester) async {
        final handle = os == 'mac' ? tester.ensureSemantics() : null;
        csdMaximized.value = true;
        await pump(tester, os);
        // When maximized, the middle control offers Restore.
        expect(ctl('Restore'), findsOneWidget);
        handle?.dispose();
      });
    });
  }
}
