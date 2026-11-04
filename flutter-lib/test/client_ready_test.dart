// A change that moves the scale reaches the ClientReady report from both the scale's listener and
// the config's; Java must be told the new area once, and a reconnect must announce a fresh client.

import 'package:flutter/foundation.dart' show TargetPlatform;
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireType;
import 'package:swtflutter/src/impl/client_ready.dart';

void main() {
  late List<(int, int, Map<String, Object>)> sent;
  late ClientReadyReporter reporter;

  void report(int width, int height) => reporter.report(WireType.display, 1,
      width: width, height: height, displayWidth: 1920, displayHeight: 1080, zoom: 150);

  setUp(() {
    sent = [];
    reporter = ClientReadyReporter((type, id, payload) async =>
        sent.add((type, id, payload as Map<String, Object>)));
  });

  test('the first report announces a fresh client on the window\'s channel', () {
    report(1280, 800);

    expect(sent, hasLength(1));
    expect((sent.single.$1, sent.single.$2), (WireType.display, 1));
    expect(sent.single.$3['isFirst'], isTrue);
    expect(sent.single.$3['width'], 1280);
  });

  test('a report equal to the last one is not sent again', () {
    report(1280, 800);
    report(1500, 750);
    report(1500, 750);

    expect(sent.map((s) => s.$3['width']), [1280, 1500]);
    expect(sent.last.$3['isFirst'], isFalse);
  });

  test('a browser\'s report carries the locale it formats numbers in', () {
    reporter = ClientReadyReporter(
        (type, id, payload) async => sent.add((type, id, payload as Map<String, Object>)), () => 'de-AT');

    report(1280, 800);

    expect(sent.single.$3['locale'], 'de-AT');
  });

  test('a desktop client reports no locale, so Java keeps the JVM\'s', () {
    report(1280, 800);

    expect(sent.single.$3.containsKey('locale'), isFalse);
  });

  test('the client reports the OS it runs on', () {
    reporter.report(WireType.display, 1,
        width: 1, height: 1, displayWidth: 1, displayHeight: 1, zoom: 100, platform: TargetPlatform.macOS);

    expect(sent.single.$3['targetPlatform'], 'macOS');
  });

  test('the client reports the light/dark appearance of its system', () {
    reporter = ClientReadyReporter(
        (type, id, payload) async => sent.add((type, id, payload as Map<String, Object>)), null, () => 'dark');

    report(1280, 800);

    expect(sent.single.$3['appearance'], 'dark');
  });

  test('after a restart the same area is reported again, as a fresh client', () {
    report(1280, 800);
    reporter.restart();
    report(1280, 800);

    expect(sent, hasLength(2));
    expect(sent.last.$3['isFirst'], isTrue);
  });

  // The report is written in the comm's own writer, which also frames it: it must reach the wire whole.
  test('the report reaches the wire as the fields Java reads, in order', () {
    EquoCommBase.recordSentFrames = true;
    final comm = EquoCommService.commForTesting..clearSentFrames();
    try {
      ClientReadyReporter().report(WireType.display, 1,
          width: 1280, height: 800, displayWidth: 1920, displayHeight: 1080, zoom: 150);
      final frame = comm.sentFrames.single;
      final (channel, start) = ChannelHeader.read(frame)!;
      expect(channel, 'Display/1/ClientReady');
      final r = WidgetWireReader(frame)..pos = start;
      expect([r.zigzag(), r.int32(), r.zigzag(), r.int32(), r.zigzag(), r.boolean()], [1, 1280, 1, 800, 1, true]);
    } finally {
      EquoCommBase.recordSentFrames = false;
      comm.clearSentFrames();
    }
  });

  // Display.getTouchEnabled answers this, so a client that cannot tell must report no touch device
  // rather than leave the field out.
  test('the touch device the host has is part of the report', () {
    report(1280, 800);
    expect(sent.single.$3['touchEnabled'], isFalse);

    reporter.report(WireType.display, 1,
        width: 1280,
        height: 800,
        displayWidth: 1920,
        displayHeight: 1080,
        zoom: 150,
        touchEnabled: true);

    expect(sent.last.$3['touchEnabled'], isTrue);
  });
}
