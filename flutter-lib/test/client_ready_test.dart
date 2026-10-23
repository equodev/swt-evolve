// A change that moves the scale reaches the ClientReady report from both the scale's listener and
// the config's; Java must be told the new area once, and a reconnect must announce a fresh client.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/client_ready.dart';

void main() {
  late List<(String, Map<String, Object>)> sent;
  late ClientReadyReporter reporter;

  void report(int width, int height) => reporter.report('Display', 1,
      width: width, height: height, displayWidth: 1920, displayHeight: 1080, zoom: 150);

  setUp(() {
    sent = [];
    reporter = ClientReadyReporter((channel, payload) async =>
        sent.add((channel, payload as Map<String, Object>)));
  });

  test('the first report announces a fresh client on the window\'s channel', () {
    report(1280, 800);

    expect(sent, hasLength(1));
    expect(sent.single.$1, 'Display/1/ClientReady');
    expect(sent.single.$2['isFirst'], isTrue);
    expect(sent.single.$2['width'], 1280);
  });

  test('a report equal to the last one is not sent again', () {
    report(1280, 800);
    report(1500, 750);
    report(1500, 750);

    expect(sent.map((s) => s.$2['width']), [1280, 1500]);
    expect(sent.last.$2['isFirst'], isFalse);
  });

  test('after a restart the same area is reported again, as a fresh client', () {
    report(1280, 800);
    reporter.restart();
    report(1280, 800);

    expect(sent, hasLength(2));
    expect(sent.last.$2['isFirst'], isTrue);
  });
}
