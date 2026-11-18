// An icon the pack lacks is looked up once. A GC cycle that draws it commits only when every image
// it draws is resolved, so asking the server again on every draw holds an owner-drawn row back.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/assets_manager.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    AssetsManager.resetForTesting();
    applyConfigFlags(ConfigFlags());
  });

  // Not setMockMessageHandler: clearing it would also drop the asset handler flutter_test installs,
  // and every later asset load of the isolate would wait forever.
  tearDown(() => TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.allMessagesHandler = null);

  test('a name the pack lacks is asked for once, however often it is drawn', () async {
    final asked = <String>[];
    _answerAssetsWithThePage(asked);

    expect(await AssetsManager.loadReplacement('not_in_the_pack_obj'), isNull);
    final first = asked.length;
    expect(first, greaterThan(0));

    for (var i = 0; i < 5; i++) {
      expect(await AssetsManager.loadReplacement('not_in_the_pack_obj'), isNull);
    }
    expect(asked.length, first, reason: 'later draws are answered without asking again: $asked');
  });

  test('draws that ask for a missing name at once share one lookup', () async {
    final asked = <String>[];
    _answerAssetsWithThePage(asked);

    final results = await Future.wait(
      List.generate(3, (_) => AssetsManager.loadReplacement('also_not_in_the_pack_obj')),
    );
    expect(results, everyElement(isNull));
    expect(asked.toSet().length, asked.length, reason: 'each format asked once: $asked');
  });

  test('CDT folders are in the pack, like the plain folder', () async {
    for (final name in ['fldr_obj', 'cfolder_obj', 'sroot_obj']) {
      expect(await AssetsManager.loadReplacement(name), isA<String>(), reason: name);
    }
    expect(await AssetsManager.loadReplacement('not_in_the_pack_obj'), isNull);
  });
}

/// Answers every asset request as the web serves an unknown path: with the page, not a 404.
void _answerAssetsWithThePage(List<String> asked) {
  final page = ByteData.sublistView(Uint8List.fromList(utf8.encode('<!DOCTYPE html><html></html>')));
  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.allMessagesHandler = (channel, handler, message) {
    if (channel != 'flutter/assets') return handler?.call(message);
    asked.add(utf8.decode(message!.buffer.asUint8List(message.offsetInBytes, message.lengthInBytes)));
    return Future.value(page);
  };
}
