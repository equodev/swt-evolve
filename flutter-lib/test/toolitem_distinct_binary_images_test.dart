// Two ToolItems whose images differ must each paint their own image.
//
// An image's cache identity has to cover its whole content. Encoded formats open with a header
// that is shared by every image of the same size and format -- a GIF's screen descriptor and
// global palette, a PNG's signature and IHDR -- so two different 16x16 icons can agree on their
// length and on their first hundred bytes. These are the "back" and "next" arrows of the SWT
// graphics example exactly as Java sends them: 925 bytes each, identical up to byte 781.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

const String _sharedHead =
    '47494638396110001000f7ff00fefdeffdfbe6fdf7d9fefae6fcf2cafef7d9fef7dafdf6d9fdf6da'
    'fce9a8fcedb9fdf2c9fdf2cafef6d9fbe08cfce499fbe499fce8a8fce8a9fdedb9fbdd83fce08cfce399'
    'bd8416ae7512b77d14b77e14b57b14b57c14bc8216b98015bb8216ab7011ab7111a96e11af7412ad7212'
    'a76c10a66b10ffffff';
const int _paletteEnd = 781;

const String _backTail =
    '21f90401000027002c000000001000100000087a004f081c48b0a0c18308055eb890906007001d1a0afc'
    '0000c007891e060418e0a1a3470f0433084020a080000307101468d04083400e0c182c58203326019a0c'
    '36081c314181829e3d7d4e188a612009091112208dc05482531204435880f0004408ab21b21a14e1a082'
    '0889024b50280156a0091365d3960d08003b';

const String _nextTail =
    '21f90401000027002c000000001000100000087a004f081c48b0a0c18308095eb890b06007001d1a0efc'
    '0000c007831e326af43020c0000f043534685000c10103020a08402020c3c00d0c182c201073814c9b0c'
    '3808c430a1a7829f131404153a422009094823288d20210153120443480d0182ea03081642481451c181'
    '0889274a502801f684091365d34a0c08003b';

Uint8List _hex(String hex) => Uint8List.fromList([
      for (var i = 0; i < hex.length; i += 2) int.parse(hex.substring(i, i + 2), radix: 16),
    ]);

Uint8List _gif(String tail) {
  final head = _hex(_sharedHead);
  final bytes = Uint8List(_paletteEnd)..setRange(0, head.length, head);
  return Uint8List.fromList([...bytes, ..._hex(tail)]);
}

VImage _image(Uint8List bytes) => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = bytes);

VToolItem _item(int id, String text, Uint8List bytes) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text
  ..image = _image(bytes);

void main() {
  final back = _gif(_backTail);
  final next = _gif(_nextTail);

  test('the two arrows are distinct images of the same length and header', () {
    expect(back.length, 925);
    expect(next.length, back.length);
    expect(next.sublist(0, _paletteEnd), back.sublist(0, _paletteEnd));
    expect(next, isNot(back));
  });

  test('images that share a header but differ in content get different keys', () {
    expect(ImageUtils.stableImageKey(_image(next)),
        isNot(ImageUtils.stableImageKey(_image(back))));
  });

  testWidgets('each ToolItem paints its own image', (tester) async {
    ImageUtils.clearCache();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 60,
        child: ToolBarSwt(
          value: VToolBar()
            ..id = 1
            ..style = SWT.HORIZONTAL | SWT.FLAT
            ..enabled = true
            ..visible = true
            ..items = [_item(2, 'Back', back), _item(3, 'Next', next)]
            ..bounds = (VRectangle()
              ..x = 0
              ..y = 0
              ..width = 200
              ..height = 60),
        ),
      ),
    ));
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    for (var i = 0; i < 4; i++) {
      await tester.pump(const Duration(milliseconds: 50));
    }

    Uint8List paintedBytes(String label) {
      final item = find.ancestor(of: find.text(label), matching: find.byType(ToolItemSwt));
      final image = tester.widget<Image>(find.descendant(of: item, matching: find.byType(Image)));
      return (image.image as MemoryImage).bytes;
    }

    expect(paintedBytes('Back'), back);
    expect(paintedBytes('Next'), next);
  });
}
