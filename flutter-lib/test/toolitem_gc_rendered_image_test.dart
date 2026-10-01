import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// An Image an application painted with a GC is rendered on this side and stays here: Java sends it
/// as a `remoteRef` with no pixel data. A ToolItem showing such an image has to draw that render,
/// and an image that resolves to nothing must not leave the item on its loading indicator.

VImage _gcRendered(int ref) => VImage()
  ..remoteRef = ref
  ..width = 16
  ..height = 16;

VToolBar _toolBar(VToolItem item) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

VToolItem _item(VImage image) => VToolItem()
  ..id = 2
  ..style = SWT.PUSH
  ..enabled = true
  ..width = 24
  ..image = image;

Future<void> _pump(WidgetTester tester, VToolItem item) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 40,
      child: ToolBarSwt(value: _toolBar(item)),
    ),
  ));
  await tester.pump();
  await tester.pump();
}

Future<ui.Image> _render(WidgetTester tester) async =>
    (await tester.runAsync(() => createTestImage(width: 16, height: 16)))!;

void main() {
  setUp(ImageUtils.clearCache);

  testWidgets('draws an image that was rendered here from a GC', (tester) async {
    ImageUtils.registerRemoteImage(41, await _render(tester));

    await _pump(tester, _item(_gcRendered(41)));

    expect(find.byType(RawImage), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgets('draws the render once it arrives after the item was built', (tester) async {
    await _pump(tester, _item(_gcRendered(42)));

    ImageUtils.registerRemoteImage(42, await _render(tester));
    await tester.pump();
    await tester.pump();

    expect(find.byType(RawImage), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgets('an image that resolves to nothing leaves no loading indicator', (tester) async {
    await _pump(
      tester,
      _item(VImage()
        ..width = 16
        ..height = 16),
    );

    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgets('two different renders do not share one resolved icon', (tester) async {
    final a = await _render(tester);
    final b = await _render(tester);
    ImageUtils.registerRemoteImage(43, a);
    ImageUtils.registerRemoteImage(44, b);

    ui.Image shown() => tester.widget<RawImage>(find.byType(RawImage)).image!;

    await _pump(tester, _item(_gcRendered(43)));
    expect(shown().isCloneOf(a), isTrue);

    await _pump(tester, _item(_gcRendered(44)));
    expect(shown().isCloneOf(b), isTrue);
  });
}
