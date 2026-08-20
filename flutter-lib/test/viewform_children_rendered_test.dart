// ViewFormImpl.build() was a stub returning Text("ViewForm") — children were
// never rendered, leaving any dialog or view that uses ViewForm completely
// empty in web mode.
//
// The fix delegates to buildComposite(), which positions children via NoLayout
// using the bounds already set by ViewFormLayout on the Java side.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/viewform.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, String text) => VLabel()
  ..id = id
  ..style = SWT.NONE
  ..enabled = true
  ..text = text
  ..bounds = _rect(0, 20, 300, 20);

VViewForm _viewForm() {
  final content = _label(2, 'INSIDE');
  return VViewForm()
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(0, 0, 300, 200)
    ..children = [content]
    ..content = content;
}

void main() {
  testWidgets('ViewForm renders its children, not a stub placeholder',
      (WidgetTester tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: ViewFormSwt<VViewForm>(value: _viewForm()),
      ),
    ));
    await tester.pumpAndSettle();

    expect(
      find.text('INSIDE'),
      findsOneWidget,
      reason: 'ViewForm must render its child controls, not a stub placeholder',
    );
  });
}
