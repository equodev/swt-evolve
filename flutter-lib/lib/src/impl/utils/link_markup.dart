import 'text_utils.dart';

/// A stretch of the text of a Link. [href] is set for an anchor: its `href` attribute, or its label
/// when it has none. [mnemonicIndex] is the offset in [text] of a mnemonic character, or -1.
class LinkPart {
  final String text;
  final String? href;
  final int mnemonicIndex;

  const LinkPart(this.text, [this.href, this.mnemonicIndex = -1]);

  @override
  bool operator ==(Object other) =>
      other is LinkPart &&
      other.text == text &&
      other.href == href &&
      other.mnemonicIndex == mnemonicIndex;

  @override
  int get hashCode => Object.hash(text, href, mnemonicIndex);

  @override
  String toString() => href == null ? 'LinkPart($text)' : 'LinkPart($text -> $href)';
}

/// The parts a Link shows for [markup], read with the state machine of SWT's `Link.parse`: only
/// `<a>` and `<a href="...">` are anchors, so a `>` inside an href is not the end of the tag and any
/// other tag is plain text. The `&` of a mnemonic is dropped and `&&` shows one `&`.
List<LinkPart> parseLinkMarkup(String markup) {
  final parts = <LinkPart>[];
  final length = markup.length;
  var index = 0, state = 0;
  var start = 0, tagStart = 0, linkStart = 0, endtagStart = 0, refStart = 0;
  String? href;

  /// Adds the text of [from]..[to], and answers where in [parts] it landed, or -1 for no text.
  int addText(int from, int to) {
    final drawn = parseMnemonic(markup.substring(from, to));
    if (drawn.text.isEmpty) return -1;
    parts.add(LinkPart(drawn.text, null, drawn.index));
    return parts.length - 1;
  }

  while (index < length) {
    final c = markup[index].toLowerCase();
    switch (state) {
      case 0:
        if (c == '<') {
          tagStart = index;
          state++;
        }
      case 1:
        if (c == 'a') state++;
      case 2:
        if (c == 'h') {
          state = 7;
        } else if (c == '>') {
          linkStart = index + 1;
          state++;
        } else if (!_isWhitespace(c)) {
          state = 13;
        }
      case 3:
        if (c == '<') {
          endtagStart = index;
          state++;
        }
      case 4:
        state = c == '/' ? state + 1 : 3;
      case 5:
        state = c == 'a' ? state + 1 : 3;
      case 6:
        if (c == '>') {
          addText(start, tagStart);
          final label = parseMnemonic(markup.substring(linkStart, endtagStart));
          parts.add(LinkPart(label.text, href ?? label.text, label.index));
          href = null;
          start = tagStart = linkStart = endtagStart = refStart = index + 1;
          state = 0;
        } else {
          state = 3;
        }
      case 7:
        state = c == 'r' ? state + 1 : 0;
      case 8:
        state = c == 'e' ? state + 1 : 0;
      case 9:
        state = c == 'f' ? state + 1 : 0;
      case 10:
        state = c == '=' ? state + 1 : 0;
      case 11:
        if (c == '"') {
          state++;
          refStart = index + 1;
        } else {
          state = 0;
        }
      case 12:
        if (c == '"') {
          href = markup.substring(refStart, index);
          state = 2;
        }
      case 13:
        if (_isWhitespace(c)) {
          state = 0;
        } else if (c == '=') {
          state++;
        }
      case 14:
        state = c == '"' ? state + 1 : 0;
      case 15:
        if (c == '"') state = 2;
      default:
        state = 0;
    }
    index++;
  }
  if (start < length) {
    final leading = addText(start, tagStart);
    final trailing = addText(tagStart > linkStart ? tagStart : linkStart, length);
    // Of these two ranges `Link.parse` keeps the trailing mnemonic and only falls back to the
    // leading one, so a mnemonic in both leaves the leading part unmarked.
    if (leading >= 0 && trailing >= 0 && parts[trailing].mnemonicIndex >= 0) {
      parts[leading] = LinkPart(parts[leading].text);
    }
  }
  return parts;
}

/// Java's `Character.isWhitespace`.
bool _isWhitespace(String c) {
  final code = c.codeUnitAt(0);
  return (code >= 9 && code <= 13) ||
      (code >= 28 && code <= 32) ||
      code == 0x1680 ||
      (code >= 0x2000 && code <= 0x200a && code != 0x2007) ||
      code == 0x2028 ||
      code == 0x2029 ||
      code == 0x205f ||
      code == 0x3000;
}
