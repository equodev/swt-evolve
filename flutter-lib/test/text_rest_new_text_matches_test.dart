// A Text at rest lays out a new text by itself, without laying out the field around it: its size
// and baseline come from its strut, not its text. Whatever the new text is, the field has to look
// exactly as one mounted with that text from the start; a new font changes the strut, so the field
// is laid out around it again. A field draws its text in a layer of its own once the text has
// changed, so the first new text puts it there and the field is laid out around it that once.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VText _text(String text, {int seq = 10, VFont? font}) => VText()
  ..id = 7
  ..seq = seq
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..text = text
  ..font = font
  ..bounds = (VRectangle()
    ..width = 160
    ..height = 24);

final _shot = GlobalKey();

Future<void> _mount(WidgetTester tester, VText value) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(key: _shot, child: TextSwt<VText>(value: value)),
    ),
  ));
  await tester.pumpAndSettle();
}

Future<Uint8List> _pixels(WidgetTester tester) async {
  expect(find.byType(EditableText), findsNothing, reason: 'drawn at rest');
  final boundary = _shot.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  return (await tester.runAsync(() async {
    final image = await boundary.toImage();
    return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
  }))!;
}

void main() {
  setUpAll(() async {
    final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
    await inter.load();
  });

  const texts = ['Other', 'A value far too long to fit in the field it is shown in', 'gjpqy', '漢字テキスト', 'x 🙂 y', ' '];
  for (final next in texts) {
    testWidgets('a new text at rest looks as it does mounted with it: "$next"', (tester) async {
      await _mount(tester, _text(next));
      final expected = await _pixels(tester);

      await _mount(tester, _text('Value'));
      await deliverChange(_text(next, seq: 11), keys: [WireKey.k_Text_text], base: 10);
      await tester.pumpAndSettle();
      expect(await _pixels(tester), orderedEquals(expected));
    });
  }

  testWidgets('a field draws its text in a layer of its own only once the text has changed', (tester) async {
    await _mount(tester, _text('Value'));
    int layers() => find
        .ancestor(
            of: find.byWidgetPredicate((w) => w.runtimeType.toString() == '_TextAtRest'),
            matching: find.byType(RepaintBoundary))
        .evaluate()
        .length;
    final before = layers();

    await deliverChange(_text('Other', seq: 11), keys: [WireKey.k_Text_text], base: 10);
    await tester.pumpAndSettle();
    expect(layers(), before + 1);
  });

  testWidgets('a new text at rest lays out nothing around it', (tester) async {
    await _mount(tester, _text('Value'));
    // The first change puts the text in a layer of its own; the ones after it are what is measured.
    await deliverChange(_text('First', seq: 11), keys: [WireKey.k_Text_text], base: 10);
    await tester.pumpAndSettle();
    final marked = <String>[];
    final print = debugPrint;
    debugPrint = (String? message, {int? wrapWidth}) {
      final match = RegExp(r'markNeedsLayout\(\) called for (\w+)').firstMatch(message ?? '');
      if (match != null) marked.add(match.group(1)!);
    };
    debugPrintMarkNeedsLayoutStacks = true;
    addTearDown(() {
      debugPrintMarkNeedsLayoutStacks = false;
      debugPrint = print;
    });
    await deliverChange(_text('Other', seq: 12), keys: [WireKey.k_Text_text], base: 11);
    await tester.pump();
    debugPrintMarkNeedsLayoutStacks = false;
    debugPrint = print;
    expect(marked, isNot(contains('_RenderDecoration')));
  });

  testWidgets('a new font at rest lays the field out around it again', (tester) async {
    final font = VFont()
      ..fontData = [
        VFontData()
          ..name = 'Inter'
          ..height = 20
          ..style = SWT.NORMAL
      ];
    await _mount(tester, _text('Value', font: font));
    final expected = await _pixels(tester);

    await _mount(tester, _text('Value'));
    await deliverChange(_text('Value', seq: 11, font: font), keys: [WireKey.k_Control_font], base: 10);
    await tester.pumpAndSettle();
    expect(await _pixels(tester), orderedEquals(expected));
  });
}
