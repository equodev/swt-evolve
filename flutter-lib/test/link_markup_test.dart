// What a Link shows for its markup is what SWT's Link.parse reads: only <a> and <a href="..."> are
// anchors, an href may hold '>' and '&', and every other tag is text.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/link_markup.dart';

String _shown(String markup) =>
    parseLinkMarkup(markup).map((part) => part.text).join();

void main() {
  test('an anchor shows its label and carries its href', () {
    expect(parseLinkMarkup('Hello <a href="x">big</a> world'), const [
      LinkPart('Hello '),
      LinkPart('big', 'x'),
      LinkPart(' world'),
    ]);
  });

  test('an anchor without an href is its own target', () {
    expect(parseLinkMarkup('<a>Go</a>'), const [LinkPart('Go', 'Go')]);
  });

  test('tags are read in any case and with other attributes beside the href', () {
    expect(parseLinkMarkup('<A HREF="x">Up</A>'), const [LinkPart('Up', 'x')]);
    expect(parseLinkMarkup('<a target="t" href="x">Go</a>'), const [LinkPart('Go', 'x')]);
    expect(parseLinkMarkup('<a href="x" target="t">Go</a>'), const [LinkPart('Go', 'x')]);
  });

  test('a > or an & inside an href belongs to the href', () {
    expect(parseLinkMarkup('<a href="a>b">Go</a>'), const [LinkPart('Go', 'a>b')]);
    expect(parseLinkMarkup('<a href="?a=1&b=2">Go</a>'), const [LinkPart('Go', '?a=1&b=2')]);
  });

  test('several anchors keep their order', () {
    expect(parseLinkMarkup('<a>one</a> <a href="y">two</a>'), const [
      LinkPart('one', 'one'),
      LinkPart(' '),
      LinkPart('two', 'y'),
    ]);
  });

  test('an anchor that is not closed shows its text and links nothing', () {
    expect(parseLinkMarkup('<a>foo'), const [LinkPart('foo')]);
    expect(_shown('before <a href="x">after'), 'before after');
    expect(_shown('<a>foo</a> tail <a>bar'), 'foo tail bar');
  });

  test('a tag that is not an anchor is text', () {
    expect(parseLinkMarkup('<b>x</b>'), const [LinkPart('<b>x</b>')]);
    expect(_shown('1 < 2 > 0'), '1 < 2 > 0');
    expect(_shown('<'), '<');
    expect(_shown('<a'), '<a');
  });

  test('a mnemonic marker is not shown and && is one &', () {
    expect(_shown('&Save'), 'Save');
    expect(_shown('Save&'), 'Save');
    expect(_shown('a&&b'), 'a&b');
    expect(_shown('&Go <a>&now</a>'), 'Go now');
    expect(_shown('a&<a>b</a>'), 'ab');
  });

  test('a part records where its mnemonic marker was', () {
    expect(parseLinkMarkup('&Go <a>n&ow</a>'), const [
      LinkPart('Go ', null, 0),
      LinkPart('now', 'now', 1),
    ]);
  });

  test('of the two trailing ranges only the later mnemonic is kept', () {
    expect(parseLinkMarkup('&one <a>t&wo'), const [
      LinkPart('one '),
      LinkPart('two', null, 1),
    ]);
    expect(parseLinkMarkup('&one <a>two'), const [
      LinkPart('one ', null, 0),
      LinkPart('two'),
    ]);
  });

  test('an anchor whose tag runs into its href is still an anchor', () {
    expect(parseLinkMarkup('<ahref="x">y</a>'), const [LinkPart('y', 'x')]);
  });

  test('an end tag with no anchor open is read as the start of an unclosed one', () {
    expect(_shown('stray </a> end'), 'stray  end');
  });

  test('nothing is something to show', () {
    expect(parseLinkMarkup(''), isEmpty);
    expect(_shown('x<a></a>y'), 'xy');
  });
}
