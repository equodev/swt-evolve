// The Browser builds its same-server URLs (/local-file/, /proxy, /equo-browser-function) from the
// shell's base path so they still reach the app server when the shell is mounted under a prefix.
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/browser_app_base.dart';

void main() {
  group('appBasePath', () {
    test('a shell served at the origin has the root as its base', () {
      expect(appBasePath('http://localhost:8080/'), '/');
      expect(appBasePath('http://localhost:8080/index.html'), '/');
    });

    test(
      'a shell mounted under a prefix keeps the prefix, slash-terminated',
      () {
        expect(appBasePath('http://host/s/abc123/'), '/s/abc123/');
        expect(appBasePath('http://host/s/abc123/index.html'), '/s/abc123/');
        expect(appBasePath('http://host/app'), '/');
      },
    );
  });

  group('underAppBase', () {
    test('places a root-relative path below the base', () {
      expect(underAppBase('/', '/local-file/t/'), '/local-file/t/');
      expect(underAppBase('/s/x/', '/local-file/t/'), '/s/x/local-file/t/');
      expect(underAppBase('/s/x/', 'proxy?url=u'), '/s/x/proxy?url=u');
    });
  });

  group('prefixLocalFilePaths', () {
    const html =
        '<link href="/local-file/t/a.css"><img src="/local-file/u/b.png">';

    test('leaves a root-served document untouched', () {
      expect(prefixLocalFilePaths(html, '/'), html);
    });

    test('moves every /local-file/ reference under the prefix', () {
      expect(
        prefixLocalFilePaths(html, '/s/x/'),
        '<link href="/s/x/local-file/t/a.css"><img src="/s/x/local-file/u/b.png">',
      );
    });
  });
}
