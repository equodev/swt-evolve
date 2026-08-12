import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

VText _text({required int style, required int width, required String value}) => VText()
  ..id = 1
  ..style = style
  ..enabled = true
  ..editable = true
  ..text = value
  ..textLimit = 2
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = width
    ..height = 28);

Widget _wrap(VText value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(child: TextSwt<VText>(value: value)),
    );

RenderEditable _findEditable(WidgetTester tester) {
  RenderEditable? re;
  void visit(RenderObject ro) {
    if (ro is RenderEditable) re = ro;
    ro.visitChildren(visit);
  }

  visit(tester.binding.renderViewElement!.renderObject!);
  expect(re, isNotNull, reason: 'no RenderEditable found for the Text');
  return re!;
}

double _chrome(WidgetTester tester) {
  final editable = _findEditable(tester);
  final outerWidth = tester.getSize(find.byType(TextSwt<VText>)).width;
  return outerWidth - editable.size.width;
}

void main() {
  testWidgets(
    'a bordered single-line Text drops the unused label-notch reservation so the value has room',
    (tester) async {
      await tester.pumpWidget(
        _wrap(_text(style: SWT.BORDER | SWT.SINGLE, width: 27, value: '99')),
      );
      await tester.pumpAndSettle();

      expect(
        _chrome(tester),
        lessThanOrEqualTo(8.0),
        reason: 'chrome should be just the contentPadding once the gapPadding floor is dropped',
      );
    },
  );

  testWidgets(
    'a search Text keeps the default border reservation (filters unaffected)',
    (tester) async {
      await tester.pumpWidget(
        _wrap(_text(style: SWT.BORDER | SWT.SINGLE | SWT.SEARCH, width: 200, value: '99')),
      );
      await tester.pumpAndSettle();

      expect(
        _chrome(tester),
        greaterThan(8.0),
        reason: 'a search field must keep its border gap (the notch that frames its icons)',
      );
    },
  );
}
