// A web Browser's setText document gets its URL from the app server asynchronously, so a later
// setText or setUrl can replace it before it has one.
import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/browser_inline_documents.dart';

const _endpoint = 'http://app/inline-document';

void main() {
  late List<Completer<String?>> requests;
  late List<String> released;
  late InlineDocuments documents;

  setUp(() {
    requests = [];
    released = [];
    documents = InlineDocuments((html, baseUrl) {
      final request = Completer<String?>();
      requests.add(request);
      return request.future;
    }, released.add, _endpoint);
  });

  test('a setText replaced by another while its URL is made is never shown', () async {
    final first = documents.make('<p>first</p>', null);
    final second = documents.make('<p>second</p>', null);

    requests[0].complete('$_endpoint/a');
    expect(await first, isFalse);
    expect(released, ['$_endpoint/a']);
    expect(documents.pending, isTrue, reason: 'the second setText still has no URL');

    requests[1].complete('$_endpoint/b');
    expect(await second, isTrue);
    expect(documents.pending, isFalse);
    expect(documents.current, '$_endpoint/b');
  });

  test('a setUrl while a setText URL is made supersedes the setText', () async {
    final text = documents.make('<p>loading</p>', null);
    expect(documents.pending, isTrue);

    documents.cancel();
    expect(documents.pending, isFalse);

    requests[0].complete('$_endpoint/a');
    expect(await text, isFalse);
    expect(released, ['$_endpoint/a']);
    expect(documents.current, isNull);
  });

  test('a replaced document stays served until its successor has a URL', () async {
    final first = documents.make('<p>first</p>', null);
    requests[0].complete('$_endpoint/a');
    await first;

    final second = documents.make('<p>second</p>', null);
    expect(documents.current, '$_endpoint/a');
    expect(released, isEmpty);

    requests[1].complete('$_endpoint/b');
    await second;
    expect(released, ['$_endpoint/a']);
  });

  test('a setUrl keeps the current document for Back', () async {
    final text = documents.make('<p>loading</p>', null);
    requests[0].complete('$_endpoint/a');
    await text;

    documents.cancel();
    expect(documents.current, '$_endpoint/a');
    expect(released, isEmpty);
  });

  test('every document the server serves reports as a setText document', () async {
    expect(documents.isDocument('$_endpoint/an-earlier-one'), isTrue);
    expect(documents.isDocument('http://app/page'), isFalse);

    final text = documents.make('<p>loading</p>', null);
    requests[0].complete('blob:http://app/1');
    await text;
    expect(documents.isDocument('blob:http://app/1'), isTrue);
  });

  test('dispose releases the current document and the one being made', () async {
    final first = documents.make('<p>first</p>', null);
    requests[0].complete('$_endpoint/a');
    await first;
    final second = documents.make('<p>second</p>', null);

    documents.dispose();
    requests[1].complete('$_endpoint/b');
    expect(await second, isFalse);
    expect(released, ['$_endpoint/a', '$_endpoint/b']);
  });
}
