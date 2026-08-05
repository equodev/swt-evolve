import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

import '../comm/comm.dart';

const String _kEvolveUrlOpenChannel = 'swt.evolve.url.open';

class HtmlTooltip extends StatefulWidget {
  final Widget child;
  final String? html;
  final String? text;
  final double panelWidth;
  final double panelMaxHeight;

  const HtmlTooltip({
    super.key,
    required this.child,
    this.html,
    this.text,
    this.panelWidth = 520,
    this.panelMaxHeight = 320,
  });

  @override
  State<HtmlTooltip> createState() => _HtmlTooltipState();
}

class _HtmlTooltipState extends State<HtmlTooltip> {
  static const TextStyle _base = TextStyle(fontSize: 12, height: 1.4, color: Color(0xFF222222));

  final LayerLink _link = LayerLink();
  final List<TapGestureRecognizer> _recognizers = [];
  OverlayEntry? _entry;
  List<InlineSpan>? _spans;
  bool _overItem = false;
  bool _overPanel = false;

  Object? _menuGroupId;

  bool get _hasHtml => (widget.html != null && widget.html!.trim().isNotEmpty);

  @override
  void dispose() {
    _removePanel();
    _disposeRecognizers();
    super.dispose();
  }

  void _disposeRecognizers() {
    for (final r in _recognizers) {
      r.dispose();
    }
    _recognizers.clear();
    _spans = null;
  }

  void _showPanel() {
    if (_entry != null || !_hasHtml || !mounted) return;
    _spans ??= _htmlToSpans(widget.html!, _base, _recognizers, (url) {
      if (url.startsWith('http://') || url.startsWith('https://')) {
        EquoCommService.sendPayload(_kEvolveUrlOpenChannel, <String, String>{'url': url});
      }
    });
    _entry = OverlayEntry(
      builder: (context) => Positioned(
        width: widget.panelWidth,
        child: CompositedTransformFollower(
          link: _link,
          showWhenUnlinked: false,
          targetAnchor: Alignment.topRight,
          followerAnchor: Alignment.topLeft,
          offset: const Offset(4, 0),
          child: MouseRegion(
            onEnter: (_) => _overPanel = true,
            onExit: (_) {
              _overPanel = false;
              _scheduleMaybeHide();
            },
            child: TapRegion(
              groupId: _menuGroupId,
              child: Material(
                elevation: 6,
                borderRadius: BorderRadius.circular(4),
                color: const Color(0xFFFFFFF0),
                clipBehavior: Clip.antiAlias,
                child: ConstrainedBox(
                  constraints: BoxConstraints(maxHeight: widget.panelMaxHeight),
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    child: Text.rich(TextSpan(children: _spans)),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
    Overlay.of(context, rootOverlay: false).insert(_entry!);
  }

  void _removePanel() {
    _entry?.remove();
    _entry = null;
  }

  void _scheduleMaybeHide() {
    Future.delayed(const Duration(milliseconds: 120), () {
      if (!mounted) return;
      if (!_overItem && !_overPanel) _removePanel();
    });
  }

  @override
  Widget build(BuildContext context) {
    _menuGroupId = MenuController.maybeOf(context);

    if (!_hasHtml) {
      final text = widget.text;
      if (text == null || text.isEmpty) return widget.child;
      return Tooltip(
        message: text,
        waitDuration: const Duration(milliseconds: 500),
        child: widget.child,
      );
    }

    return CompositedTransformTarget(
      link: _link,
      child: MouseRegion(
        onEnter: (_) {
          _overItem = true;
          _showPanel();
        },
        onExit: (_) {
          _overItem = false;
          _scheduleMaybeHide();
        },
        child: widget.child,
      ),
    );
  }
}

List<InlineSpan> _htmlToSpans(
  String html,
  TextStyle base,
  List<TapGestureRecognizer> recognizers,
  void Function(String url) onTapUrl,
) {
  final spans = <InlineSpan>[];
  final buffer = StringBuffer();
  bool bold = false;
  bool italic = false;
  bool mono = false;
  String? href;
  bool atLineStart = true;

  TextStyle currentStyle() {
    var s = base;
    if (bold) s = s.copyWith(fontWeight: FontWeight.w600);
    if (italic) s = s.copyWith(fontStyle: FontStyle.italic);
    if (mono) s = s.copyWith(fontFamily: 'monospace');
    if (href != null) s = s.copyWith(color: const Color(0xFF1565C0));
    return s;
  }

  void flush() {
    if (buffer.isEmpty) return;
    final text = buffer.toString();
    buffer.clear();
    if (href != null) {
      final url = href!;
      final rec = TapGestureRecognizer()..onTapDown = (_) => onTapUrl(url);
      recognizers.add(rec);
      spans.add(TextSpan(text: text, style: currentStyle(), recognizer: rec));
    } else {
      spans.add(TextSpan(text: text, style: currentStyle()));
    }
  }

  void newline() {
    flush();
    if (!atLineStart) {
      spans.add(const TextSpan(text: '\n'));
      atLineStart = true;
    }
  }

  void appendText(String s) {
    final collapsed = s.replaceAll(RegExp(r'\s+'), ' ');
    if (collapsed.isEmpty) return;
    if (atLineStart && collapsed == ' ') return;
    buffer.write(collapsed);
    atLineStart = false;
  }

  int i = 0;
  while (i < html.length) {
    final ch = html[i];
    if (ch == '<') {
      final end = html.indexOf('>', i);
      if (end == -1) break;
      final raw = html.substring(i + 1, end).trim();
      i = end + 1;
      final closing = raw.startsWith('/');
      final name = (closing ? raw.substring(1) : raw).split(RegExp(r'[\s>]')).first.toLowerCase();
      switch (name) {
        case 'b':
        case 'strong':
        case 'dt':
        case 'h1':
        case 'h2':
        case 'h3':
        case 'h4':
        case 'h5':
        case 'h6':
          if (!closing && (name == 'dt' || name.startsWith('h'))) newline();
          bold = !closing;
          if (closing && (name == 'dt' || name.startsWith('h'))) newline();
          break;
        case 'i':
        case 'em':
          italic = !closing;
          break;
        case 'code':
        case 'tt':
          mono = !closing;
          break;
        case 'a':
          flush();
          if (closing) {
            href = null;
          } else {
            final m = RegExp('href\\s*=\\s*"([^"]*)"').firstMatch(raw) ??
                RegExp("href\\s*=\\s*'([^']*)'").firstMatch(raw);
            href = m?.group(1);
          }
          break;
        case 'br':
          newline();
          break;
        case 'li':
          if (!closing) {
            newline();
            buffer.write('• ');
            atLineStart = false;
          }
          break;
        case 'p':
        case 'dd':
        case 'div':
        case 'tr':
          newline();
          break;
        default:
          break;
      }
    } else if (ch == '&') {
      final end = html.indexOf(';', i);
      if (end != -1 && end - i <= 8) {
        appendText(_decodeEntity(html.substring(i + 1, end)));
        i = end + 1;
      } else {
        appendText('&');
        i++;
      }
    } else {
      final next = html.indexOf(RegExp('[<&]'), i);
      final stop = next == -1 ? html.length : next;
      appendText(html.substring(i, stop));
      i = stop;
    }
  }
  flush();
  return spans;
}

String _decodeEntity(String name) {
  switch (name) {
    case 'nbsp':
      return ' ';
    case 'lt':
      return '<';
    case 'gt':
      return '>';
    case 'amp':
      return '&';
    case 'quot':
      return '"';
    case 'apos':
      return "'";
    default:
      if (name.startsWith('#')) {
        final code = int.tryParse(name.substring(1));
        if (code != null) return String.fromCharCode(code);
      }
      return '';
  }
}
