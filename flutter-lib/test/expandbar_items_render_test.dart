// Every item of an ExpandBar shows its header, and an expanded item shows its client control, whether or not
// Java has given the bar bounds. The bar is exactly as tall as Java says: what its bounds leave out is scrolled.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VButton _button(int id, String text) => VButton()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text
  ..bounds = _rect(5, 5, 100, 30);

VExpandItem _item(
  int id,
  String text, {
  bool expanded = false,
  int height = 80,
  int clientId = 0,
}) {
  return VExpandItem()
    ..id = id
    ..text = text
    ..expanded = expanded
    ..height = height
    ..control = (VComposite()
      ..id = clientId
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(1, 24, 150, height - 1)
      ..children = [_button(clientId + 1, '$text button')]);
}

VExpandBar _bar({VRectangle? bounds}) => VExpandBar()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..bounds = bounds
  ..items = [
    _item(10, 'Define Scope', height: 59, clientId: 100),
    _item(11, 'Simple Search', expanded: true, height: 400, clientId: 110),
    _item(12, 'Favorites', height: 210, clientId: 120),
    _item(13, 'Advanced Search', expanded: true, height: 151, clientId: 130),
  ];

Future<void> _pump(WidgetTester tester, VExpandBar value) async {
  await tester.binding.setSurfaceSize(const Size(400, 1000));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 161, child: ExpandBarSwt(value: value)),
      ),
    ),
  );
  await tester.pump();
}

void main() {
  for (final withBounds in [false, true]) {
    testWidgets(
      'every header is drawn, ${withBounds ? 'with' : 'without'} bounds from Java',
      (tester) async {
        await _pump(
          tester,
          _bar(bounds: withBounds ? _rect(0, 0, 161, 700) : null),
        );

        for (final title in [
          'Define Scope',
          'Simple Search',
          'Favorites',
          'Advanced Search',
        ]) {
          expect(find.text(title), findsOneWidget, reason: title);
        }
        expect(find.text('Simple Search button').hitTestable(), findsOneWidget);
        expect(
          find.text('Advanced Search button').hitTestable(),
          findsOneWidget,
        );
      },
    );
  }

  testWidgets(
    'a bar without bounds is as tall as its headers and open bodies',
    (tester) async {
      await _pump(tester, _bar());

      final height = tester.getSize(find.byType(ExpandBarSwt)).height;
      expect(height, greaterThan(400 + 151), reason: 'two open bodies');
    },
  );

  testWidgets(
    'a bar bounded to one header high shows that header and scrolls the rest',
    (tester) async {
      await _pump(tester, _bar(bounds: _rect(0, 0, 161, 44)));

      expect(tester.getSize(find.byType(ExpandBarSwt)).height, 44);
      expect(find.text('Define Scope').hitTestable(), findsOneWidget);
      expect(find.text('Favorites').hitTestable(), findsNothing);
      expect(find.text('Simple Search button').hitTestable(), findsNothing);
    },
  );
}
