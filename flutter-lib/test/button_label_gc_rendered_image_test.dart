import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// An Image an application painted with a GC is rendered on this side and stays here: Java sends it
/// as a `remoteRef` with its size and no pixel data. A push button or a label showing such an image
/// (a colour picker's swatch) draws that render in its own colours, whether it landed before the
/// control was built or after.

VImage _gcRendered(int ref) => VImage()
  ..remoteRef = ref
  ..width = 16
  ..height = 16;

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 60
  ..height = 30;

VButton _button(VImage image, {bool enabled = true}) => VButton()
  ..id = 1
  ..style = SWT.PUSH
  ..enabled = enabled
  ..visible = true
  ..bounds = _bounds()
  ..image = image;

VLabel _label(VImage image) => VLabel()
  ..id = 2
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _bounds()
  ..image = image;

Future<void> _pump(WidgetTester tester, Widget control) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 60, height: 30, child: control),
  ));
  await tester.pump();
  await tester.pump();
}

Future<ui.Image> _render(WidgetTester tester) async =>
    (await tester.runAsync(() => createTestImage(width: 16, height: 16)))!;

/// The render the control shows, drawn untinted; null if it shows none or another image.
void _expectShows(WidgetTester tester, ui.Image render) {
  final images = tester.widgetList<RawImage>(find.byType(RawImage)).toList();
  expect(images, hasLength(1), reason: 'the control draws the GC render');
  expect(images.single.image!.isCloneOf(render), isTrue);
  expect(images.single.width, 16);
  expect(images.single.height, 16);
  expect(find.ancestor(of: find.byType(RawImage), matching: find.byType(ColorFiltered)), findsNothing,
      reason: 'a GC render keeps its own colours');
}

void main() {
  setUp(ImageUtils.clearCache);

  testWidgets('a push button draws an image painted with a GC', (tester) async {
    final render = await _render(tester);
    ImageUtils.registerRemoteImage(51, render);

    await _pump(tester, ButtonSwt(value: _button(_gcRendered(51))));

    _expectShows(tester, render);
  });

  testWidgets('a disabled push button draws its GC-painted image too', (tester) async {
    final render = await _render(tester);
    ImageUtils.registerRemoteImage(52, render);

    await _pump(tester, ButtonSwt(value: _button(_gcRendered(52), enabled: false)));

    expect(tester.widgetList<RawImage>(find.byType(RawImage)).single.image!.isCloneOf(render), isTrue);
  });

  testWidgets('a push button draws the render once it lands after the button was built', (tester) async {
    await _pump(tester, ButtonSwt(value: _button(_gcRendered(53))));
    expect(find.byType(RawImage), findsNothing);

    final render = await _render(tester);
    ImageUtils.registerRemoteImage(53, render);
    await tester.runAsync(() => Future<void>.delayed(Duration.zero));
    await tester.pump();
    await tester.pump();

    _expectShows(tester, render);
  });

  testWidgets('a label draws an image painted with a GC', (tester) async {
    final render = await _render(tester);
    ImageUtils.registerRemoteImage(54, render);

    await _pump(tester, LabelSwt(value: _label(_gcRendered(54))));

    _expectShows(tester, render);
  });

  testWidgets('a label draws the render once it lands after the label was built', (tester) async {
    await _pump(tester, LabelSwt(value: _label(_gcRendered(55))));

    final render = await _render(tester);
    ImageUtils.registerRemoteImage(55, render);
    await tester.runAsync(() => Future<void>.delayed(Duration.zero));
    await tester.pump();
    await tester.pump();

    _expectShows(tester, render);
  });
}
