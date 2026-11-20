// TestAnchors locates an ExpandBar's items where they are drawn, and a control inside an expanded
// item as painting.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/test_anchors.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VExpandItem _item(int id, String text, {required bool expanded, required int clientId}) => VExpandItem()
  ..swt = 'ExpandItem'
  ..id = id
  ..text = text
  ..expanded = expanded
  ..height = 60
  ..control = (VComposite()
    ..id = clientId
    ..style = SWT.NONE
    ..enabled = true
    ..bounds = _rect(1, 24, 150, 59)
    ..children = [VButton()
      ..swt = 'Button'
      ..id = clientId + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = '$text button'
      ..bounds = _rect(5, 5, 100, 30)]);

void main() {
  setUp(() => TestAnchors.enabled = true);
  tearDown(() => TestAnchors.enabled = false);

  testWidgets('items are located at their headers; an open item\'s button paints', (tester) async {
    await tester.binding.setSurfaceSize(const Size(400, 600));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 161,
          child: ExpandBarSwt(
            value: VExpandBar()
              ..id = 1
              ..style = SWT.NONE
              ..enabled = true
              ..bounds = _rect(0, 0, 161, 300)
              ..items = [
                _item(10, 'Expand 0', expanded: true, clientId: 100),
                _item(11, 'Expand 1', expanded: false, clientId: 110),
              ],
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final first = TestAnchors.locate('ExpandItem/10')!;
    final second = TestAnchors.locate('ExpandItem/11')!;
    final title0 = tester.getRect(find.text('Expand 0'));
    final title1 = tester.getRect(find.text('Expand 1'));
    expect((first['y'] as double) <= title0.top, isTrue, reason: 'item 0 starts at its header: $first, $title0');
    expect((second['y'] as double) <= title1.top && (second['y'] as double) > title0.bottom, isTrue,
        reason: 'item 1 starts at its own header: $second, $title1');

    final button = TestAnchors.locate('Button/101');
    expect(button, isNotNull);
    expect(button!['paints'], isTrue, reason: 'the open item\'s button: $button');
  });
}
