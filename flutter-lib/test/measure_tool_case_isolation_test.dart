// The measurement tool renders hundreds of cases in one process, and every case builds its own V*
// for the same widget without ever setting an id — so they all address one registry channel. That
// is the same collision flutter_test_config.dart resets between tests, and the tool has to do the
// same between cases or every case is measured rendering the first case's state.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';

import '../tool/measure.dart';
import '../tool/measure_button.dart' as button;

void main() {
  testWidgets('a case is measured on its own value, not the previous case\'s', (
    tester,
  ) async {
    final measurer = WidgetMeasurer();
    // Three cases to take two steps: measuring the last one ends phase 1, which writes the Java
    // sizing files into the source tree.
    for (final text in ['first', 'second', 'third']) {
      measurer.addTestCase(button.createCase(text, ('PUSH', SWT.PUSH), text));
    }

    await tester.pumpWidget(MeasurementApp(measurer: measurer));
    expect(find.text('first'), findsOneWidget);

    await tester.tap(find.byTooltip('Next'));
    await tester.pump();

    expect(find.text('second'), findsOneWidget);
    expect(find.text('first'), findsNothing);
  });
}
