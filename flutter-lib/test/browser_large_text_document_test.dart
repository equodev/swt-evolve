// WebView2 refuses a NavigateToString document over 2 MB and loads nothing, so a desktop Browser
// on Windows loads a larger setText document from a file instead.
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/browser_frame_params_stub.dart';

String _document(int chars) {
  final html = StringBuffer('<html><head></head><body><h1>Loading →</h1><!--');
  while (html.length < chars) {
    html.write('0123456789abcdef');
  }
  html.write('--></body></html>');
  return html.toString();
}

void main() {
  tearDown(() => debugDefaultTargetPlatformOverride = null);

  test('a setText document over the WebView2 limit is loaded from a file', () async {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    final html = _document(2300000);

    final url = await browserInlineDocumentUrl(html, 'https://example.com/help/');

    expect(url, isNotNull, reason: 'the document went to NavigateToString, which loads nothing');
    final uri = Uri.parse(url!);
    expect(uri.scheme, 'file');
    expect(url.startsWith('${browserInlineDocumentEndpoint()}/'), isTrue,
        reason: 'the file is not reported as a setText document');
    final bytes = File.fromUri(uri).readAsBytesSync();
    expect(bytes.sublist(0, 3), [0xEF, 0xBB, 0xBF], reason: 'without a BOM the page is not UTF-8');
    final written = utf8.decode(bytes.sublist(3));
    expect(written, contains('<head><base href="https://example.com/help/">'));
    expect(written, contains('<h1>Loading →</h1>'));

    browserRevokeInlineDocumentUrl(url);
    expect(File.fromUri(uri).existsSync(), isFalse);
  });

  test('a smaller setText document is still loaded from the string', () async {
    debugDefaultTargetPlatformOverride = TargetPlatform.windows;
    expect(await browserInlineDocumentUrl(_document(1000), null), isNull);
  });

  test('only WebView2 limits the size of a setText document', () async {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    expect(await browserInlineDocumentUrl(_document(2300000), null), isNull);
  });
}
