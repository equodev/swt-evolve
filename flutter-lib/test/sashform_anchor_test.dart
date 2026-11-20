// A SashForm with children is tagged itself, so a test can read its weights.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, int x) => VLabel()
  ..swt = 'Label'
  ..id = id
  ..style = SWT.NONE
  ..text = 'pane $id'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 195, 300);

VSashForm _sashForm() => VSashForm()
  ..swt = 'SashForm'
  ..id = 42
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    _label(1, 0),
    VSash()
      ..swt = 'Sash'
      ..id = 3
      ..style = SWT.VERTICAL
      ..visible = true
      ..enabled = true
      ..bounds = _rect(195, 0, 10, 300),
    _label(2, 205),
  ];

void main() {
  setUpAll(() => TestAnchors.enabled = true);
  tearDownAll(() => TestAnchors.enabled = false);

  testWidgets('a SashForm with panes and a sash is tagged once, around its children', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 400,
          height: 300,
          child: SashFormSwt(value: _sashForm()),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    expect(TestAnchors.nodes(['SashForm/']).where((n) => n['id'] == 'SashForm/42'), hasLength(1));
    final inside = TestAnchors.nodes([], within: 'SashForm/42').map((n) => n['id']);
    expect(inside, containsAll(['Label/1', 'Sash/3', 'Label/2']));
  });
}
