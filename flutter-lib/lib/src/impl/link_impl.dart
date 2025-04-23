import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/gestures.dart';

import '../impl/control_impl.dart';
import '../swt/link.dart';

class LinkImpl<T extends LinkSwt, V extends LinkValue>
    extends ControlImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final spans = _parseText(state.text ?? "", context);

    final rich = RichText(
      text: TextSpan(
        children: spans,
        style: const TextStyle(color: Colors.black),
      ),
    );
    return super.wrap(rich);
  }

  List<TextSpan> _parseText(String text, BuildContext context) {
    final regex = RegExp(r'(<a(?: href="([^"]*)")?>([^<]+)</a>|[^<]+)',
        caseSensitive: false);
    final matches = regex.allMatches(text);

    List<TextSpan> spans = [];
    for (final match in matches) {
      if (match.group(3) != null) {
        // Link with optional href
        final linkText = match.group(3)!;
        final url = match.group(2) ?? linkText;
        spans.add(
          TextSpan(
            text: linkText,
            style: TextStyle(
              color: Colors.blue,
              decoration: TextDecoration.underline,
            ),
            recognizer: TapGestureRecognizer()
              ..onTap = () {
                _linkPressed(context, url);
              },
          ),
        );
      } else {
        // Plain text
        spans.add(TextSpan(text: match.group(0)));
      }
    }
    return spans;
  }

  void _linkPressed(BuildContext context, String link) async {
    widget.sendSelectionSelection(state, link);
  }
}
