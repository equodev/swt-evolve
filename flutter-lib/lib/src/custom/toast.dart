import 'package:flutter/material.dart';

import '../gen/swt.dart';
import '../gen/toolitem.dart';
import '../impl/widget_config.dart';
import '../theme/theme_extensions/toast_theme_extension.dart';
import 'main_toolbar_scope.dart';
import 'rich_tooltip.dart';

/// The notification popup: a transient card in the corner of the window, raised when a main-toolbar
/// item is clicked.
///
/// Everything the feature needs lives here. The widget that uses it only tells [maybeRaiseFor] that
/// an item was clicked, and this decides whether anything happens.
class EvolveNotification {
  EvolveNotification._();

  /// Raises a card for [item], or does nothing -- the flag is off, the item is not in the main
  /// toolbar, or there is no overlay to draw into. It only reports: whatever the click does
  /// otherwise still happens.
  static void maybeRaiseFor(BuildContext context, VToolItem item) {
    if (!notificationPopup || !MainToolbarScope.isInside(context)) return;
    final data = RichToolTipData.of(text: item.text, toolTipText: item.toolTipText);
    ToastHost.showNew(
      context,
      title: data?.title ?? item.text ?? 'ToolItem',
      message: _sampleMessage,
      severity: _severityFor(item),
    );
  }

  /// Stand-in wording, so the card reads like a notification rather than like a dump of what Evolve
  /// knows about the item.
  static const String _sampleMessage = 'This is a test notification.';

  /// Keyed to the item's style, so a click-around shows more than one colour and an item always
  /// raises the same one. It does not mean anything happened.
  static ToastSeverity _severityFor(VToolItem item) {
    const bits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;
    return switch (item.style & bits) {
      SWT.CHECK || SWT.RADIO => ToastSeverity.success,
      SWT.DROP_DOWN => ToastSeverity.warning,
      _ => ToastSeverity.info,
    };
  }
}

/// How a toast is coloured and which glyph it leads with.
enum ToastSeverity { info, success, warning, error }

@immutable
class ToastData {
  final int id;
  final String? title;
  final String? message;
  final ToastSeverity severity;

  /// Null means the theme's own [ToastThemeExtension.displayDuration]. [Duration.zero] means the
  /// toast stays until someone closes it -- for a message the user must actually see.
  final Duration? duration;

  const ToastData({
    required this.id,
    this.title,
    this.message,
    this.severity = ToastSeverity.info,
    this.duration,
  });

}

/// The stack of live toasts, rendered as one entry in the window's root overlay.
///
/// It is a single global rather than per-widget state because a toast outlives whatever raised it:
/// the toolbar item the user clicked may well be rebuilt, or gone, before the card fades out.
class ToastHost {
  ToastHost._();

  static final ValueNotifier<List<ToastData>> _toasts =
      ValueNotifier<List<ToastData>>(const []);
  static OverlayEntry? _entry;
  static int _nextId = 1;

  /// Raises a toast over the window [context] belongs to. A no-op when the feature is off or there
  /// is no overlay to draw into, so callers need no guard of their own.
  static void show(BuildContext context, ToastData toast) {
    if (!notificationPopup) return;
    final overlay = Overlay.maybeOf(context, rootOverlay: true);
    if (overlay == null) return;
    if (_entry == null) {
      _entry = OverlayEntry(builder: (_) => const _ToastLayer());
      overlay.insert(_entry!);
    }
    _toasts.value = [..._toasts.value, toast];
  }

  /// Convenience for a caller that has no id of its own to give.
  static void showNew(
    BuildContext context, {
    String? title,
    String? message,
    ToastSeverity severity = ToastSeverity.info,
    Duration? duration,
  }) =>
      show(
        context,
        ToastData(
          id: nextId(),
          title: title,
          message: message,
          severity: severity,
          duration: duration,
        ),
      );

  static int nextId() => _nextId++;

  static void dismiss(int id) {
    _toasts.value = _toasts.value.where((t) => t.id != id).toList();
    if (_toasts.value.isEmpty) _removeEntry();
  }

  /// Drops every live toast and the overlay entry with them. Called when the app tears the widget
  /// tree down, and by tests between cases.
  static void reset() {
    _toasts.value = const [];
    _removeEntry();
    _nextId = 1;
  }

  /// The overlay can already be gone when this runs -- the toolbar disposing takes the window's
  /// tree with it -- and removing an unmounted entry throws.
  static void _removeEntry() {
    final entry = _entry;
    _entry = null;
    if (entry != null && entry.mounted) entry.remove();
  }

  @visibleForTesting
  static List<ToastData> get active => _toasts.value;
}

class _ToastLayer extends StatelessWidget {
  const _ToastLayer();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ToastThemeExtension>()!;
    return ValueListenableBuilder<List<ToastData>>(
      valueListenable: ToastHost._toasts,
      builder: (context, toasts, _) {
        // Newest at the bottom, nearest the corner. Older ones beyond the cap are simply not drawn;
        // they still expire on their own timers.
        final visible = toasts.length > theme.maxVisible
            ? toasts.sublist(toasts.length - theme.maxVisible)
            : toasts;
        return Positioned(
          right: theme.margin.right,
          bottom: theme.margin.bottom,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              for (final toast in visible)
                Padding(
                  padding: EdgeInsets.only(top: theme.spacing),
                  child: _ToastCard(key: ValueKey(toast.id), toast: toast),
                ),
            ],
          ),
        );
      },
    );
  }
}

class _ToastCard extends StatefulWidget {
  final ToastData toast;

  const _ToastCard({super.key, required this.toast});

  @override
  State<_ToastCard> createState() => _ToastCardState();
}

class _ToastCardState extends State<_ToastCard> with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  bool _leaving = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, value: 0);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final theme = Theme.of(context).extension<ToastThemeExtension>()!;
    if (_controller.duration == null) {
      _controller.duration = theme.slideInDuration;
      _controller.reverseDuration = theme.fadeOutDuration;
      _controller.forward();
      final life = widget.toast.duration ?? theme.displayDuration;
      if (life > Duration.zero) {
        Future.delayed(life + theme.slideInDuration, _dismiss);
      }
    }
  }

  void _dismiss() {
    if (!mounted || _leaving) return;
    _leaving = true;
    _controller.reverse().whenComplete(() {
      if (mounted) ToastHost.dismiss(widget.toast.id);
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  IconData _glyph() {
    switch (widget.toast.severity) {
      case ToastSeverity.success:
        return Icons.check_circle_outline;
      case ToastSeverity.warning:
        return Icons.warning_amber_outlined;
      case ToastSeverity.error:
        return Icons.error_outline;
      case ToastSeverity.info:
        return Icons.info_outline;
    }
  }

  Color _accent(ToastThemeExtension theme) {
    switch (widget.toast.severity) {
      case ToastSeverity.success:
        return theme.successColor;
      case ToastSeverity.warning:
        return theme.warningColor;
      case ToastSeverity.error:
        return theme.errorColor;
      case ToastSeverity.info:
        return theme.infoColor;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ToastThemeExtension>()!;
    final accent = _accent(theme);
    final title = widget.toast.title;
    final message = widget.toast.message;

    final lines = <Widget>[];
    if (title != null && title.isNotEmpty) {
      lines.add(Text(
        title,
        style: theme.titleTextStyle,
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
      ));
    }
    if (message != null && message.isNotEmpty) {
      if (lines.isNotEmpty) lines.add(SizedBox(height: theme.titleMessageSpacing));
      lines.add(Text(
        message,
        style: theme.messageTextStyle,
        maxLines: theme.messageMaxLines,
        overflow: TextOverflow.ellipsis,
      ));
    }

    final curve = CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutCubic,
      reverseCurve: Curves.easeIn,
    );

    return FadeTransition(
      opacity: curve,
      child: SlideTransition(
        position: Tween<Offset>(
          begin: Offset(theme.slideOffsetX, 0),
          end: Offset.zero,
        ).animate(curve),
        child: Material(
          color: Colors.transparent,
          child: Container(
            width: theme.width,
            decoration: BoxDecoration(
              color: theme.backgroundColor,
              borderRadius: BorderRadius.circular(theme.borderRadius),
              border: Border.all(color: theme.borderColor, width: theme.borderWidth),
              boxShadow: [
                BoxShadow(
                  color: theme.shadowColor,
                  blurRadius: theme.shadowBlurRadius,
                  offset: Offset(0, theme.shadowOffsetY),
                ),
              ],
            ),
            clipBehavior: Clip.antiAlias,
            child: IntrinsicHeight(
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Container(width: theme.accentBarWidth, color: accent),
                  Expanded(
                    child: Padding(
                      padding: theme.padding,
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Icon(_glyph(), size: theme.iconSize, color: accent),
                          SizedBox(width: theme.iconSpacing),
                          Expanded(
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: lines,
                            ),
                          ),
                          SizedBox(width: theme.iconSpacing),
                          InkWell(
                            onTap: _dismiss,
                            child: Icon(
                              Icons.close,
                              size: theme.closeIconSize,
                              color: theme.closeIconColor,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
