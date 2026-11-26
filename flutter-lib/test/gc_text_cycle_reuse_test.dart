// Text drawn cycle after cycle reuses what the previous cycle built, and draws what it drew.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show appScaleNotifier;
import 'support/channels.dart' as channels;

const _delimiter = 2, _tab = 4, _transparent = 1, _mnemonic = 8;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 942001;

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  // A font as the wire decodes it: a new object every cycle, equal to the last one.
  VFont font() => VFont()
    ..fontData = [
      VFontData()
        ..name = 'Liberation Sans'
        ..height = 11
        ..style = 0
    ];

  Future<List<TextShape>> cycle(GCDrawer drawer, int id, List<(String, int)> texts) async {
    drawer.state.font = font();
    for (final (text, flags) in texts) {
      drawer.onDrawTextStringintintint(VGCDrawTextStringintintint()
        ..string = text
        ..x = 3
        ..y = 4
        ..flags = flags | _transparent);
    }
    EquoCommService.commForTesting
        .receiveBinary(channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    return drawer.shapes.whereType<TextShape>().toList();
  }

  GCDrawer drawer(int id) {
    final d = GCDrawer.embedded(VGC.empty()..id = id);
    addTearDown(d.dispose);
    return d;
  }

  test('an equal font decoded again draws in the same style object and the same layout', () async {
    final id = nextId++;
    final d = drawer(id);
    final first = await cycle(d, id, [('int x = 1;', 0)]);
    final second = await cycle(d, id, [('int x = 1;', 0)]);
    expect(first.single.style, equals(second.single.style));
    expect(identical(first.single.style, second.single.style), isTrue);
    expect(identical(first.single.painter, second.single.painter), isTrue);
  });

  test('a zoom changed between cycles reaches the text of the next', () async {
    final id = nextId++;
    final d = drawer(id);
    final before = appScaleNotifier.value;
    addTearDown(() => appScaleNotifier.value = before);
    appScaleNotifier.value = 1;
    final plain = await cycle(d, id, [('int x = 1;', 0)]);
    appScaleNotifier.value = 2;
    final zoomed = await cycle(d, id, [('int x = 1;', 0)]);
    expect(zoomed.single.style.fontSize, plain.single.style.fontSize! / 2);
  });

  test('strings switching font and colour draw in the style each draws in alone', () async {
    VFont bold() => VFont()
      ..fontData = [
        VFontData()
          ..name = 'Liberation Sans'
          ..height = 11
          ..style = 1
      ];
    final id = nextId++;
    final d = drawer(id);
    final tokens = <TextShape>[];
    for (var i = 0; i < 6; i++) {
      d.state.font = i.isEven ? font() : bold();
      d.state.foreground = VColor.fromArgb(i % 3 == 0 ? 0xFF102030 : 0xFF803010);
      d.onDrawTextStringintintint(VGCDrawTextStringintintint()
        ..string = 'token$i'
        ..flags = _transparent);
    }
    EquoCommService.commForTesting
        .receiveBinary(channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    tokens.addAll(d.shapes.whereType<TextShape>());
    expect(tokens, hasLength(6));
    for (var i = 0; i < 6; i++) {
      final alone = drawer(nextId);
      final aloneId = nextId++;
      alone.state.foreground = VColor.fromArgb(i % 3 == 0 ? 0xFF102030 : 0xFF803010);
      alone.state.font = i.isEven ? font() : bold();
      alone.onDrawTextStringintintint(VGCDrawTextStringintintint()
        ..string = 'token$i'
        ..flags = _transparent);
      EquoCommService.commForTesting
          .receiveBinary(channels.frame('GC/$aloneId/gcDispose', TaggedValues.encode({'fullRepaint': true})));
      await settle();
      expect(alone.shapes.whereType<TextShape>().single.style, tokens[i].style, reason: 'token $i');
    }
  });

  // What SWT's drawText flags make of line breaks and tabs, written out in full.
  String expected(String text, int flags) {
    final delimiter = (flags & _delimiter) != 0 ? '\n' : ' ';
    return text
        .replaceAll('\r\n', delimiter)
        .replaceAll('\r', delimiter)
        .replaceAll('\n', delimiter)
        .replaceAll('\t', (flags & _tab) != 0 ? '        ' : ' ');
  }

  test('line breaks, tabs and mnemonics are drawn as the flags say', () async {
    final id = nextId++;
    final d = drawer(id);
    const texts = ['plain', 'a\tb', 'a\nb', 'a\r\nb', 'a\rb', '\t\r\n\n\r', '', 'tail\t'];
    for (final flags in [0, _delimiter, _tab, _delimiter | _tab]) {
      final shapes = await cycle(d, id, [for (final t in texts) (t, flags)]);
      expect(shapes.map((s) => s.text), [for (final t in texts) expected(t, flags)], reason: 'flags $flags');
    }
    final mnemonics = await cycle(d, id, [('&File\tCtrl+F', _mnemonic | _tab), ('a&&b', _mnemonic)]);
    expect(mnemonics.map((s) => s.text), ['File        Ctrl+F', 'a&b']);
  });
}
