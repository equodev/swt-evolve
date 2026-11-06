// An app built on this one adds channels and types to the wire, numbered right after the client's own.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/widget_key.dart';
import 'package:swtflutter/src/comm/wire_names.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames, wireTypeNames;
import 'package:swtflutter/testing/channels.dart' as channels;

int _after(Iterable<int> numbers) => numbers.fold(0, (a, b) => a > b ? a : b) + 1;

void main() {
  final firstChannel = _after(wireChannelNames.keys);
  final firstType = _after(wireTypeNames.keys);

  void extend() => WireNames.extend(
        firstChannel: firstChannel,
        channelNames: const ['added/one', 'added/two'],
        firstType: firstType,
        typeNames: const ['AddedType'],
      );

  test('added names are read and written by the numbers after the client\'s own', () {
    extend();

    expect(WireNames.channels[firstChannel + 1], 'added/two');
    expect(WidgetKey.typeOf('AddedType'), firstType);
    final frame = channels.frame('AddedType/7/added/one', Uint8List(0));
    expect(ChannelHeader.read(frame)?.$1, 'AddedType/7/added/one');
  });

  test('the same table added again changes nothing', () {
    extend();
    extend();

    expect(WireNames.channels.length, wireChannelNames.length + 2);
  });

  test('a table that does not start where the client\'s ends was not generated with it', () {
    expect(
      () => WireNames.extend(
          firstChannel: firstChannel + 5, channelNames: const ['late'], firstType: firstType + 5, typeNames: const []),
      throwsStateError,
    );
  });
}
