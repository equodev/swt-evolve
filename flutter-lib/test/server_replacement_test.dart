import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/server_replacement.dart';

void main() {
  const servedBy = '3f2b8c1e-6a4d-4e1f-9b7a-0c5d2e8f1a3b';
  const relaunched = 'a91c0d7e-2b3f-4c8a-8e6d-5f4b3a2c1d0e';

  test('a page served by a JVM that was replaced reloads into the new one', () {
    expect(isServedByAnotherInstance(servedBy, relaunched), isTrue);
    expect(isServedByAnotherInstance(servedBy, '$relaunched\n'), isTrue);
  });

  test('the server that served the page keeps it', () {
    expect(isServedByAnotherInstance(servedBy, servedBy), isFalse);
  });

  test('no server, or a host answering with something else, keeps the page', () {
    expect(isServedByAnotherInstance(servedBy, null), isFalse);
    expect(isServedByAnotherInstance(servedBy, ''), isFalse);
    expect(isServedByAnotherInstance(servedBy, '<!DOCTYPE html><html></html>'), isFalse);
  });

  test('a page that was not served with an instance never reloads', () {
    expect(isServedByAnotherInstance('', relaunched), isFalse);
    expect(isServedByAnotherInstance('{{EQUO_INSTANCE}}', relaunched), isFalse);
  });

  test('the origin is asked every second for a minute, then every ten seconds', () {
    expect(replacementPollDelay(0), const Duration(seconds: 1));
    expect(replacementPollDelay(59), const Duration(seconds: 1));
    expect(replacementPollDelay(60), const Duration(seconds: 10));
    expect(replacementPollDelay(10000), const Duration(seconds: 10));
  });
}
