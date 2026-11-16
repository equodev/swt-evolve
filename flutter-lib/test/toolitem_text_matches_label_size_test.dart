// A ToolItem's text, with or without an image, draws at the same size as a Label's text in the same
// parent when the app set no font: the bar is part of the same UI and uses the same text size.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/cached_text.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// A 1x1 transparent PNG.
const List<int> _pngBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

const String _labelText = 'Scale:';
const String _textOnly = 'Design';
const String _withImage = 'Analysis';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _parent() => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 800, 100)
  ..children = [
    VLabel()
      ..id = 2
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..text = _labelText
      ..bounds = _rect(0, 0, 100, 40),
    VToolBar()
      ..id = 3
      ..style = SWT.HORIZONTAL | SWT.FLAT | SWT.WRAP | SWT.RIGHT
      ..enabled = true
      ..visible = true
      ..bounds = _rect(100, 0, 700, 100)
      ..items = [
        VToolItem()
          ..id = 4
          ..style = SWT.PUSH
          ..enabled = true
          ..text = _textOnly,
        VToolItem()
          ..id = 5
          ..style = SWT.CHECK
          ..enabled = true
          ..text = _withImage
          ..image = (VImage()
            ..imageData = (VImageData()
              ..width = 16
              ..height = 16
              ..data = _pngBytes)),
      ],
  ];

/// The size [text] reaches the screen at: its style's size times any scale above it.
double _drawnFontSize(WidgetTester tester, String text) {
  final finder = find.text(text);
  final box = tester.renderObject<RenderBox>(finder);
  final fontSize = switch (box) {
    RenderCachedText() => box.style.fontSize!,
    RenderParagraph() => box.text.style!.fontSize!,
    _ => throw StateError('$text is drawn by ${box.runtimeType}'),
  };
  return fontSize * tester.getRect(finder).height / box.size.height;
}

void main() {
  tearDown(resetConfigFlags);

  testWidgets('ToolItem text draws at the size of a Label in the same parent', (tester) async {
    setConfigFlags(ConfigFlags()..use_swt_fonts = false);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 800, height: 100, child: CompositeSwt<VComposite>(value: _parent())),
      ),
    ));
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    for (var i = 0; i < 4; i++) {
      await tester.pump(const Duration(milliseconds: 50));
    }

    final label = _drawnFontSize(tester, _labelText);
    expect(label, closeTo(14.0, 0.1), reason: 'the Label\'s text in the standard type scale');
    expect({
      'text only': _drawnFontSize(tester, _textOnly),
      'image + text': _drawnFontSize(tester, _withImage),
    }, {
      'text only': closeTo(label, 0.1),
      'image + text': closeTo(label, 0.1),
    }, reason: 'ToolItem text against the Label\'s $label');
  });
}
