// A hidden Canvas paints nothing, its children included. A custom combo (a Canvas hosting a Text,
// such as Nebula's CDateTime) hidden with setVisible(false) keeps its bounds, so a shown child
// would be drawn over whatever the layout puts there next.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';

const _childText = '<choose date>';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCanvas _canvas({required bool visible}) => VCanvas()
  ..swt = 'Canvas'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = visible
  ..bounds = _rect(0, 0, 300, 24)
  ..children = [
    VLabel()
      ..swt = 'Label'
      ..id = 2
      ..style = SWT.NONE
      ..visible = true
      ..text = _childText
      ..bounds = _rect(0, 0, 200, 24),
  ];

Widget _host(VCanvas value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 24,
        child: CanvasSwt<VCanvas>(value: value),
      ),
    );

void main() {
  testWidgets('a shown Canvas renders its children', (tester) async {
    await tester.pumpWidget(_host(_canvas(visible: true)));
    await tester.pump();
    expect(find.text(_childText), findsOneWidget);
  });

  testWidgets('a hidden Canvas renders none of its children', (tester) async {
    await tester.pumpWidget(_host(_canvas(visible: false)));
    await tester.pump();
    expect(find.text(_childText), findsNothing);
  });

  testWidgets('a Canvas hidden after being shown stops rendering its children', (tester) async {
    await tester.pumpWidget(_host(_canvas(visible: true)));
    await tester.pump();
    await deliverWhole(_canvas(visible: false)..seq = 10);
    await tester.pump();
    expect(find.text(_childText), findsNothing);
  });
}
