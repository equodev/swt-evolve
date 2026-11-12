// A child whose resize is expensive keeps its size while the space it is given keeps changing, and
// takes the new one once the space has stayed put.

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/held_size.dart';

void main() {
  late List<Size> laidOut;

  Widget at(double width, double height) => Directionality(
        textDirection: TextDirection.ltr,
        child: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: width,
            height: height,
            child: HeldSize(
              child: LayoutBuilder(builder: (context, c) {
                laidOut.add(c.biggest);
                return const SizedBox.expand();
              }),
            ),
          ),
        ),
      );

  setUp(() => laidOut = []);

  testWidgets('the first size is taken at once', (tester) async {
    await tester.pumpWidget(at(300, 200));

    expect(laidOut.last, const Size(300, 200));
  });

  testWidgets('a size that keeps changing is held, and the last one taken once it stays', (tester) async {
    await tester.pumpWidget(at(300, 200));
    for (var h = 210.0; h <= 260; h += 10) {
      await tester.pumpWidget(at(300, h));
      await tester.pump(const Duration(milliseconds: 16));
      expect(laidOut.last, const Size(300, 200), reason: 'still moving at $h');
    }
    expect(find.byType(ClipRect), findsOneWidget, reason: 'what overflows the space it has is not drawn');

    await tester.pump(const Duration(milliseconds: 120));

    expect(laidOut.last, const Size(300, 260));
  });

  testWidgets('going back to the held size cancels the change', (tester) async {
    await tester.pumpWidget(at(300, 200));
    await tester.pumpWidget(at(300, 240));
    await tester.pumpWidget(at(300, 200));
    await tester.pump(const Duration(milliseconds: 200));

    expect(laidOut.last, const Size(300, 200));
  });
}
