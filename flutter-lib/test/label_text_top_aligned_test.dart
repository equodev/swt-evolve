import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';

// Every native draws a Label's text from the top of its bounds, whatever the
// style; only CLabel centres it vertically.
const double _boundsHeight = 120;

VLabel _label(int style, String text) => VLabel()
  ..id = 1
  ..style = style
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = _boundsHeight.toInt());

Widget _wrap(VLabel value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: LabelSwt<VLabel>(value: value),
        ),
      ),
    );

Future<double> _textTopInLabel(WidgetTester tester, VLabel value) async {
  freshClient();
  await tester.pumpWidget(_wrap(value));
  await tester.pumpAndSettle();
  final label = tester.getTopLeft(find.byType(LabelSwt<VLabel>));
  final text = tester.getTopLeft(find.byWidgetPredicate((w) => w is Text).first);
  return text.dy - label.dy;
}

void main() {
  const cases = <String, (int, String)>{
    // A CTabItem's control: the folder gives it the whole page.
    'single line, SWT.NONE': (SWT.NONE, 'Content of tab 1'),
    'single line, SWT.CENTER': (SWT.CENTER, 'There is no active editor.'),
    // The workbench's empty-page message: a wrapping Label filling the view.
    'SWT.WRAP': (SWT.LEFT | SWT.TOP | SWT.WRAP, 'There is no active editor that provides an outline.'),
    'multi-line text': (SWT.LEFT, 'First line\nSecond line'),
  };

  testWidgets('a tall image-only label keeps its image vertically centred', (tester) async {
    const size = 16.0;
    final value = VLabel()
      ..id = 1
      ..style = SWT.NONE
      ..image = (VImage()
        ..svgContent = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">'
            '<rect width="16" height="16" fill="#FF00FF"/></svg>'
        ..imageData = (VImageData()
          ..width = size.toInt()
          ..height = size.toInt()))
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = _boundsHeight.toInt());
    freshClient();
    await tester.pumpWidget(_wrap(value));
    await tester.pumpAndSettle();
    final label = tester.getRect(find.byType(LabelSwt<VLabel>));
    final image = tester.getRect(find
        .descendant(
          of: find.byType(LabelSwt<VLabel>),
          matching: find.byWidgetPredicate((w) => w is SizedBox && w.width == size && w.height == size),
        )
        .first);
    expect(image.center.dy - label.top, closeTo(_boundsHeight / 2, 4),
        reason: 'an image-only label centres its image vertically, as every native does');
  });

  for (final entry in cases.entries) {
    testWidgets('a tall label draws its text from the top: ${entry.key}', (tester) async {
      final (style, text) = entry.value;
      final top = await _textTopInLabel(tester, _label(style, text));
      expect(top, lessThan(12),
          reason: 'the text starts ${top}px below the top of a ${_boundsHeight}px label; '
              'native draws it right under the top edge, not centred in the bounds');
    });
  }
}
