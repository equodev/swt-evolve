// Java sizes a bar to its widest header: the text plus what SWT's ExpandItem reserves around it,
// TEXT_INSET on each side, the chevron and, with an image, the image and one more TEXT_INSET. The
// header has to lay its text out inside exactly that, or a bar packed to its headers cuts its titles.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/swt.dart';

const _textInset = 6.0;
const _chevron = 24.0;

VExpandBar _bar(String title) => VExpandBar()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..spacing = 0
  ..items = [
    VExpandItem()
      ..id = 10
      ..text = title
      ..headerHeight = 24,
  ];

Future<void> _pump(WidgetTester tester, String title, double width) async {
  await tester.binding.setSurfaceSize(const Size(600, 400));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: width, child: ExpandBarSwt(value: _bar(title))),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

Rect _header(WidgetTester tester, String title) => tester.getRect(
  find
      .ancestor(of: find.text(title), matching: find.byType(GestureDetector))
      .first,
);

void main() {
  testWidgets('the text sits TEXT_INSET in, and the chevron and its inset end the row', (tester) async {
    await _pump(tester, 'Options', 400);
    final header = _header(tester, 'Options');
    final text = tester.getRect(find.text('Options'));
    expect(text.left - header.left, _textInset);
    expect(header.right - text.right, _textInset + _chevron);
  });

  testWidgets('a bar as wide as SWT reserves for a title shows all of it', (tester) async {
    await _pump(tester, 'Options', 400);
    final textWidth = tester
        .renderObject<RenderParagraph>(find.text('Options'))
        .getMaxIntrinsicWidth(double.infinity);

    await _pump(tester, 'Options', 2 * _textInset + _chevron + textWidth.ceilToDouble());
    final paragraph = tester.renderObject<RenderParagraph>(find.text('Options'));
    expect(paragraph.size.width, greaterThanOrEqualTo(textWidth));
    expect(paragraph.didExceedMaxLines, isFalse);
  });
}
