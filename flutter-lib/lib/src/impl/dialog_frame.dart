import 'dart:async';
import 'dart:ui' show FontFeature;

import 'package:flutter/gestures.dart' show kDoubleTapTimeout;
import 'package:flutter/material.dart';

import '../theme/theme_extensions/display_theme_extension.dart';
import '../theme/theme_settings/dialog_frame_theme_settings.dart';
import 'utils/test_anchors.dart';

import '../gen/shell.dart';

/// How Java presents a dialog Shell (`DialogPresentation` on the Java side): the numbers it sends
/// as `VShell.presentation`.
abstract final class DialogPresentation {
  static const none = 0;
  static const alert = 1;
  static const sheet = 2;
  static const page = 3;
  static const progress = 4;

  /// A card in the window's corner over whatever is shown, the window not dimmed.
  static bool cornerCard(int? p) => p == progress;

  static bool presented(int? p) => p != null && p != none;

  /// A page Java placed over the editor area: drawn there, under the tab strip, blocking nothing.
  static bool hostedPage(VShell s) => s.presentation == page && s.pageHost != null;

  /// Shown one at a time in its frame, the one opened last on top: a sheet, or a page with no editor
  /// area to go to, which is then a large sheet.
  static bool stacks(VShell s) => s.presentation == sheet || (s.presentation == page && s.pageHost == null);
}

/// A dialog below the top of the frame, named in the top one's breadcrumb.
@immutable
class FrameCrumb {
  final int shellId;
  final String title;

  const FrameCrumb(this.shellId, this.title);

  @override
  bool operator ==(Object other) => other is FrameCrumb && other.shellId == shellId && other.title == title;

  @override
  int get hashCode => Object.hash(shellId, title);

  @override
  String toString() => 'FrameCrumb($shellId, $title)';
}

/// A page shown as a tab over the editor area.
@immutable
class FramePage {
  final int shellId;
  final String title;

  /// The editor area, in display coordinates.
  final Rect host;

  /// The height of the strip Java left above the page: the strip is drawn that tall.
  final double strip;

  /// Whether the editor area under the page is maximized.
  final bool maximized;

  /// A wizard's step titles, and the current one's index; null for a page with no steps.
  final List<String>? steps;
  final int step;

  const FramePage(this.shellId, this.title, this.host,
      {required this.strip, this.maximized = false, this.steps, this.step = -1});
}

/// What the dialog frames show of each dialog shell, worked out from the shells the display draws,
/// in draw order (the last one on top).
///
/// A window has one frame, and so does each page over its editor area: the dialogs a page opens are
/// drawn over the page, the others over the window. In each frame only the stacking shell opened last
/// (a sheet) is shown, the others stay mounted but hidden, and the top one's header lists them as a
/// breadcrumb, led by the page it is over. An alert is never hidden and never in a breadcrumb: it
/// overlays the frame it asks about.
///
/// Of the pages, the [selectedPage] is shown and the others wait under their tabs; all of them wait
/// while [pagesAside], the editors shown instead.
@immutable
class DialogFrame {
  /// Shells mounted but not shown.
  final Set<int> hidden;

  /// The top of each frame, by the page it is over (0 for the window), to the breadcrumb it shows.
  final Map<int, List<FrameCrumb>> tops;

  /// The pages over the editor area, in the order they opened.
  final List<FramePage> pages;

  /// The page a dialog is drawn over, by the dialog's id.
  final Map<int, int> owners;

  const DialogFrame._(this.hidden, this.tops, this.pages, this.owners);

  static const empty = DialogFrame._({}, {}, [], {});

  factory DialogFrame.of(List<VShell> dialogShells,
      {required bool modern, int? selectedPage, bool pagesAside = false}) {
    if (!modern) return empty;
    final pages = [
      for (final s in dialogShells)
        if (DialogPresentation.hostedPage(s))
          FramePage(s.id, s.text ?? '', Rect.fromLTWH(s.pageHost!.x.toDouble(), s.pageHost!.y.toDouble(),
              s.pageHost!.width.toDouble(), s.pageHost!.height.toDouble()),
              strip: (s.pageStrip ?? 0).toDouble(),
              maximized: s.pageMaximized ?? false,
              steps: s.pageSteps?.whereType<String>().toList(),
              step: s.pageStep ?? -1),
    ];
    final pageIds = {for (final p in pages) p.shellId};
    final shown = pagesAside || pages.isEmpty
        ? null
        : (pageIds.contains(selectedPage) ? selectedPage : pages.last.shellId);
    final hidden = <int>{for (final p in pages) if (p.shellId != shown) p.shellId};
    final owners = <int, int>{};
    final stacks = <int, List<VShell>>{};
    for (final s in dialogShells) {
      final owner = s.pageOwner?.id;
      final scope = owner != null && pageIds.contains(owner) ? owner : 0;
      if (scope != 0) {
        owners[s.id] = scope;
        if (scope != shown) hidden.add(s.id);
      }
      if (DialogPresentation.stacks(s)) (stacks[scope] ??= []).add(s);
    }
    final tops = <int, List<FrameCrumb>>{};
    stacks.forEach((scope, stack) {
      final top = stack.last;
      for (final s in stack) {
        if (s.id != top.id) hidden.add(s.id);
      }
      final page = scope == 0 ? null : pages.firstWhere((p) => p.shellId == scope);
      final chain = [
        if (page != null) FrameCrumb(page.shellId, page.title),
        for (final s in stack) FrameCrumb(s.id, s.text ?? ''),
      ];
      tops[top.id] = chain.length < 2 ? const [] : chain;
    });
    return DialogFrame._(hidden, tops, pages, owners);
  }

  bool isHidden(int id) => hidden.contains(id);

  /// The crumbs [id] shows, or none when it is not the top of a frame.
  List<FrameCrumb> crumbsOf(int id) => tops[id] ?? const [];

  /// The page [id] is drawn over, or null when it is drawn over the window.
  FramePage? ownerOf(int id) {
    final owner = owners[id];
    return owner == null ? null : pages.firstWhere((p) => p.shellId == owner);
  }

  FramePage? pageOf(int id) {
    for (final p in pages) {
      if (p.shellId == id) return p;
    }
    return null;
  }

  /// Whether a crumb walk to [target] still has a dialog to close.
  bool walksTo(int target) => tops.entries.any((e) => e.key != target && e.value.any((c) => c.shellId == target));
}

/// Bumped when a page's own record changes. The display draws the page strip from the pages' records,
/// and a change to one reaches only that shell, so the display listens here to redraw the strip.
final ValueNotifier<int> pageRecordsChanged = ValueNotifier<int>(0);

/// A crumb that was clicked, and the dialogs that were above it then.
@immutable
class PopTarget {
  final int target;
  final Set<int> above;

  const PopTarget(this.target, this.above);

  /// The walk to [crumb] of the top whose breadcrumb is [crumbs].
  factory PopTarget.to(int crumb, List<FrameCrumb> crumbs) =>
      PopTarget(crumb, {for (final c in crumbs.skipWhile((c) => c.shellId != crumb).skip(1)) c.shellId});
}

/// The crumb walk under way: every dialog that was above the crumb closes, the top first, each
/// through its own Cancel path. One that refuses to close stays the top, and a dialog it then opens
/// was not above the crumb, so it is left open and ends the walk.
final ValueNotifier<PopTarget?> dialogFramePopTarget = ValueNotifier<PopTarget?>(null);

/// Whether the top shell [topId], showing [crumbs], has to close itself for the walk [pop].
bool closesForPopTarget(int topId, List<FrameCrumb> crumbs, PopTarget? pop) {
  if (pop == null || pop.target == topId || !pop.above.contains(topId)) return false;
  return crumbs.any((c) => c.shellId == pop.target);
}

/// A dialog's header with dialog chrome: no title band, the title as the card's heading, one close
/// icon. As tall as the title bar it replaces, since Java's trim reserves that height. On the top of
/// a stacked frame the title becomes a breadcrumb, with Back in front.
class DialogFrameHeader extends StatelessWidget {
  final int shellId;
  final String title;
  final List<FrameCrumb> crumbs;

  /// Shown at the header's end, before its buttons: a progress card's elapsed time.
  final Widget? trailing;
  final bool hasClose, hasMax, isMaximized, isDraggable;
  final double height;
  final DisplayThemeExtension theme;
  final VoidCallback onClose, onMaximize;
  final VoidCallback? onDragStart, onDragEnd, onDragCancel;
  final void Function(Offset) onDrag;

  const DialogFrameHeader({
    super.key,
    required this.shellId,
    required this.title,
    required this.crumbs,
    this.trailing,
    required this.hasClose,
    required this.hasMax,
    required this.isMaximized,
    required this.isDraggable,
    required this.height,
    required this.theme,
    required this.onClose,
    required this.onMaximize,
    required this.onDrag,
    this.onDragStart,
    this.onDragEnd,
    this.onDragCancel,
  });

  @override
  Widget build(BuildContext context) {
    final base = theme.titleTextStyle ?? const TextStyle(fontSize: 13);
    final heading =
        base.copyWith(fontWeight: FontWeight.w600, fontSize: (base.fontSize ?? 13) + 1);
    final muted = base.copyWith(color: dialogMutedColor(context, base, 0.6));
    final iconColor = theme.closeButtonColor;
    final stacked = crumbs.length > 1;

    final children = <Widget>[
      if (stacked)
        _HeaderButton(
          key: ValueKey('dialogFrame.back.$shellId'),
          anchor: 'DialogFrame/back/$shellId',
          icon: Icons.arrow_back,
          color: iconColor,
          tooltip: 'Back',
          onTap: onClose,
        ),
      Expanded(
        child: stacked
            ? Row(children: [
                for (final c in crumbs.take(crumbs.length - 1)) ...[
                  Flexible(
                    child: _Crumb(
                      key: ValueKey('dialogFrame.crumb.${c.shellId}'),
                      anchor: 'DialogFrame/crumb/${c.shellId}',
                      label: c.title,
                      style: muted,
                      onTap: () => dialogFramePopTarget.value = PopTarget.to(c.shellId, crumbs),
                    ),
                  ),
                  Icon(Icons.chevron_right, size: 14, color: muted.color),
                ],
                Flexible(
                  flex: 2,
                  child: Text(title, style: heading, maxLines: 1, overflow: TextOverflow.ellipsis),
                ),
              ])
            : Text(title, style: heading, maxLines: 1, overflow: TextOverflow.ellipsis),
      ),
      if (trailing != null) Padding(padding: const EdgeInsets.symmetric(horizontal: 8), child: trailing),
      if (hasMax)
        _HeaderButton(
          anchor: 'DialogFrame/maximize/$shellId',
          icon: isMaximized ? Icons.close_fullscreen : Icons.crop_square,
          color: iconColor,
          onTap: onMaximize,
        ),
      if (hasClose)
        _HeaderButton(
          key: ValueKey('dialogFrame.close.$shellId'),
          anchor: 'DialogFrame/close/$shellId',
          icon: Icons.close,
          color: iconColor,
          tooltip: 'Close',
          onTap: onClose,
        ),
    ];

    // The header is trim, as a title bar is: Tab walks the shell's own controls, Escape is the keyboard's Back.
    return ExcludeFocus(
      child: GestureDetector(
        onPanStart: isDraggable ? (_) => onDragStart?.call() : null,
        onPanUpdate: isDraggable ? (d) => onDrag(d.delta) : null,
        onPanEnd: isDraggable ? (_) => onDragEnd?.call() : null,
        onPanCancel: isDraggable ? (onDragCancel ?? onDragEnd) : null,
        child: SizedBox(
          height: height,
          child: Padding(
            padding: EdgeInsets.only(left: stacked ? 6 : 16, right: 6),
            child: Row(children: children),
          ),
        ),
      ),
    );
  }
}

class _Crumb extends StatelessWidget {
  final String anchor, label;
  final TextStyle style;
  final VoidCallback onTap;

  const _Crumb({super.key, required this.anchor, required this.label, required this.style, required this.onTap});

  @override
  Widget build(BuildContext context) => tagSemantics(
        null,
        id: anchor,
        InkWell(
          borderRadius: BorderRadius.circular(4),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
            child: Text(label, style: style, maxLines: 1, overflow: TextOverflow.ellipsis),
          ),
        ),
      );
}

class _HeaderButton extends StatelessWidget {
  final String anchor;
  final IconData icon;
  final Color color;
  final String? tooltip;
  final VoidCallback onTap;

  const _HeaderButton({super.key, required this.anchor, required this.icon, required this.color, required this.onTap, this.tooltip});

  @override
  Widget build(BuildContext context) => tagSemantics(
        null,
        id: anchor,
        InkWell(
          borderRadius: BorderRadius.circular(6),
          onTap: onTap,
          child: SizedBox(width: 26, height: 26, child: Icon(icon, size: 15, color: color)),
        ),
      );
}

/// The editor area while pages are over it: the page's ground, and a tab strip naming every page,
/// led by Editors, which puts the pages aside.
class PageBackdrop extends StatelessWidget {
  final List<FramePage> pages;
  final int? shown;
  final double stripHeight;
  final void Function(int id) onSelect, onClose, onMaximize;
  final VoidCallback onEditors;

  const PageBackdrop({
    super.key,
    required this.pages,
    required this.shown,
    required this.stripHeight,
    required this.onSelect,
    required this.onClose,
    required this.onEditors,
    required this.onMaximize,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final base = theme.titleTextStyle ?? const TextStyle(fontSize: 13);
    final accent = Theme.of(context).colorScheme.primary;
    final muted = base.copyWith(color: dialogMutedColor(context, base, 0.6), fontWeight: FontWeight.w400);
    Widget tab({required Key key, required String anchor, required Widget icon, required String label,
        required bool on, required VoidCallback onTap, VoidCallback? onClose, String? closeAnchor, Key? closeKey,
        VoidCallback? onDoubleTap}) {
      return tagSemantics(
        null,
        id: anchor,
        InkWell(
          key: key,
          // A double click is told apart by hand: a double-tap recognizer would hold every single click
          // back until it knows no second one is coming.
          onTap: () {
            final now = DateTime.now();
            final double = onDoubleTap != null && _lastTabTap != null &&
                now.difference(_lastTabTap!) < kDoubleTapTimeout && _lastTabTapKey == key;
            _lastTabTap = double ? null : now;
            _lastTabTapKey = key;
            if (double) {
              onDoubleTap();
            } else {
              onTap();
            }
          },
          child: Container(
            height: stripHeight,
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(
              border: Border(bottom: BorderSide(color: on ? accent : Colors.transparent, width: 2)),
            ),
            child: Row(mainAxisSize: MainAxisSize.min, children: [
              icon,
              const SizedBox(width: 6),
              Text(label, style: on ? base.copyWith(fontWeight: FontWeight.w600) : muted),
              if (onClose != null) ...[
                const SizedBox(width: 6),
                tagSemantics(
                  null,
                  id: closeAnchor!,
                  InkWell(
                    key: closeKey,
                    borderRadius: BorderRadius.circular(4),
                    onTap: onClose,
                    child: Icon(Icons.close, size: 13, color: muted.color),
                  ),
                ),
              ],
            ]),
          ),
        ),
      );
    }

    final shownPage = pages.where((p) => p.shellId == shown).firstOrNull;
    return Material(
      color: theme.dialogBackgroundColor,
      child: Align(
        alignment: Alignment.topLeft,
        child: Container(
          height: stripHeight,
          decoration: BoxDecoration(border: Border(bottom: BorderSide(color: theme.dialogBorderColor))),
          child: Row(children: [
            Expanded(child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(children: [
              tab(
                key: const ValueKey('dialogFrame.editors'),
                anchor: 'DialogFrame/editors',
                icon: Icon(Icons.description_outlined, size: 14, color: muted.color),
                label: 'Editors',
                on: shown == null,
                onTap: onEditors,
              ),
              for (final p in pages)
                tab(
                  key: ValueKey('dialogFrame.pageTab.${p.shellId}'),
                  anchor: 'DialogFrame/pageTab/${p.shellId}',
                  icon: Icon(Icons.web_asset_outlined, size: 14, color: p.shellId == shown ? accent : muted.color),
                  label: p.title,
                  on: p.shellId == shown,
                  onTap: () => onSelect(p.shellId),
                  onClose: () => onClose(p.shellId),
                  closeAnchor: 'DialogFrame/pageClose/${p.shellId}',
                  closeKey: ValueKey('dialogFrame.pageClose.${p.shellId}'),
                  // As an editor tab does: a double click maximizes or restores the editor area.
                  onDoubleTap: () => onMaximize(p.shellId),
                ),
            ]),
          )),
            if (shownPage?.steps != null)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: PageSteps(steps: shownPage!.steps!, current: shownPage.step),
              ),
            if (shownPage != null)
              tagSemantics(
                null,
                id: 'DialogFrame/maximize',
                InkWell(
                  key: const ValueKey('dialogFrame.maximize'),
                  borderRadius: BorderRadius.circular(6),
                  onTap: () => onMaximize(shownPage.shellId),
                  child: SizedBox(
                    width: 30,
                    height: stripHeight,
                    child: Icon(shownPage.maximized ? Icons.close_fullscreen : Icons.open_in_full,
                        size: 14, color: muted.color),
                  ),
                ),
              ),
          ]),
        ),
      ),
    );
  }
}

DateTime? _lastTabTap;
Key? _lastTabTapKey;

/// A wizard's steps at the end of the page strip: done, current (named) and to come.
class PageSteps extends StatelessWidget {
  final List<String> steps;
  final int current;

  const PageSteps({super.key, required this.steps, required this.current});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final base = theme.titleTextStyle ?? const TextStyle(fontSize: 13);
    final accent = Theme.of(context).colorScheme.primary;
    final muted = dialogMutedColor(context, base, 0.5);
    final children = <Widget>[];
    for (var i = 0; i < steps.length; i++) {
      if (i > 0) children.add(Container(width: 16, height: 1.5, color: muted.withValues(alpha: 0.4)));
      final done = i < current, on = i == current;
      children.add(Tooltip(
        key: ValueKey('dialogFrame.step.$i'),
        message: steps[i],
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Container(
            width: 18,
            height: 18,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: done ? accent : null,
              border: Border.all(color: done || on ? accent : muted, width: 1.5),
            ),
            child: done
                ? Icon(Icons.check, size: 11, color: dialogStepDoneColor(context))
                : Text('${i + 1}', style: base.copyWith(fontSize: 10.5, color: on ? accent : muted, fontWeight: FontWeight.w600)),
          ),
          if (on) ...[
            const SizedBox(width: 6),
            Text(steps[i], style: base.copyWith(fontWeight: FontWeight.w600)),
          ],
        ]),
      ));
    }
    return Row(mainAxisSize: MainAxisSize.min, children: children);
  }
}

/// A page's heading, drawn over the title area its dialog lays out: the title, and the message on a
/// strip coloured by its kind (DialogPageParts' MESSAGE_* numbers).
class PageHeading extends StatelessWidget {
  final String? title;
  final String? message;
  final int messageType;

  const PageHeading({super.key, this.title, this.message, this.messageType = 0});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final scheme = Theme.of(context).colorScheme;
    final base = theme.titleTextStyle ?? const TextStyle(fontSize: 13);
    final tint = dialogMessageColor(context, messageType, base);
    final (IconData icon, String kind) = switch (messageType) {
      3 => (Icons.error, 'error'),
      2 => (Icons.warning_amber_rounded, 'warning'),
      1 => (Icons.info, 'information'),
      _ => (Icons.info_outline, 'plain'),
    };
    final hasMessage = message != null && message!.isNotEmpty;
    return ColoredBox(
      color: theme.dialogBackgroundColor,
      child: ClipRect(
        child: OverflowBox(
        alignment: Alignment.topLeft,
        maxHeight: double.infinity,
        child: Padding(
        padding: const EdgeInsets.fromLTRB(18, 8, 18, 4),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
          Text(title ?? '', maxLines: 1, overflow: TextOverflow.ellipsis,
              style: base.copyWith(fontSize: (base.fontSize ?? 13) + 6, fontWeight: FontWeight.w600)),
          if (hasMessage) ...[
            const SizedBox(height: 6),
            Container(
              key: ValueKey('dialogFrame.message.$kind'),
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              decoration: BoxDecoration(color: tint.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(7)),
              child: Row(children: [
                Icon(icon, size: 15, color: tint),
                const SizedBox(width: 8),
                Flexible(child: Text(message!, maxLines: 1, overflow: TextOverflow.ellipsis, style: base)),
              ]),
            ),
          ],
        ]),
      ))),
    );
  }
}

/// While the pages are put aside: a chip in the editor area's corner that brings them back.
class PageReturnChip extends StatelessWidget {
  final Rect host;
  final List<FramePage> pages;
  final VoidCallback onReturn;

  const PageReturnChip({super.key, required this.host, required this.pages, required this.onReturn});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<DisplayThemeExtension>()!;
    final base = theme.titleTextStyle ?? const TextStyle(fontSize: 13);
    final label = pages.length == 1 ? pages.single.title : '${pages.last.title} and ${pages.length - 1} more';
    return Positioned(
      right: 0,
      bottom: 0,
      left: 0,
      top: 0,
      child: IgnorePointer(
        ignoring: false,
        child: Stack(children: [
          Positioned(
            left: host.right - 16,
            top: host.bottom - 16,
            child: FractionalTranslation(
              translation: const Offset(-1, -1),
              child: tagSemantics(
                null,
                id: 'DialogFrame/returnToPage',
                Material(
                  key: const ValueKey('dialogFrame.returnToPage'),
                  color: theme.dialogBackgroundColor,
                  elevation: 6,
                  borderRadius: BorderRadius.circular(999),
                  child: InkWell(
                    borderRadius: BorderRadius.circular(999),
                    onTap: onReturn,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                      child: Row(mainAxisSize: MainAxisSize.min, children: [
                        Icon(Icons.web_asset_outlined, size: 15, color: Theme.of(context).colorScheme.primary),
                        const SizedBox(width: 8),
                        Text(label, style: base.copyWith(fontWeight: FontWeight.w600)),
                      ]),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ]),
      ),
    );
  }
}

/// How long an operation runs before its progress card shows: a quick one never flashes a card.
const Duration progressCardDelay = Duration(milliseconds: 600);

/// The margin between a corner card and the window's edges.
const double cornerCardMargin = 16;

/// The time an operation has run, as `m:ss`, counted from when its card was mounted.
class ElapsedTime extends StatefulWidget {
  final TextStyle? style;

  const ElapsedTime({super.key, this.style});

  @override
  State<ElapsedTime> createState() => _ElapsedTimeState();
}

class _ElapsedTimeState extends State<ElapsedTime> {
  int _seconds = 0;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted) setState(() => _seconds++);
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final seconds = _seconds;
    final text = '${seconds ~/ 60}:${(seconds % 60).toString().padLeft(2, '0')}';
    return Text(text, style: widget.style?.copyWith(fontFeatures: const [FontFeature.tabularFigures()]));
  }
}

/// Fades and lifts a presented dialog in when it appears, and again when it comes back to the top of
/// its frame. The child is kept across both, so the dialog's widgets keep their state.
class DialogAppear extends StatefulWidget {
  final bool shown;
  final Widget child;

  /// How long the dialog stays invisible before it fades in.
  final Duration delay;

  const DialogAppear({super.key, required this.shown, required this.child, this.delay = Duration.zero});

  @override
  State<DialogAppear> createState() => _DialogAppearState();
}

class _DialogAppearState extends State<DialogAppear> {
  bool _in = false;

  @override
  void initState() {
    super.initState();
    _schedule();
  }

  @override
  void didUpdateWidget(DialogAppear oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!widget.shown) {
      _in = false;
    } else if (!oldWidget.shown) {
      _schedule();
    }
  }

  Timer? _delayed;

  void _schedule() {
    if (!widget.shown) return;
    void appear() {
      if (mounted && widget.shown && !_in) setState(() => _in = true);
    }
    if (widget.delay > Duration.zero) {
      _delayed?.cancel();
      _delayed = Timer(widget.delay, appear);
    } else {
      WidgetsBinding.instance.addPostFrameCallback((_) => appear());
    }
  }

  @override
  void dispose() {
    _delayed?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final still = MediaQuery.maybeDisableAnimationsOf(context) ?? false;
    final on = (still && widget.delay == Duration.zero) || _in;
    const duration = Duration(milliseconds: 160);
    return AnimatedOpacity(
      opacity: on ? 1 : 0,
      duration: still ? Duration.zero : duration,
      curve: Curves.easeOut,
      child: AnimatedSlide(
        offset: on ? Offset.zero : const Offset(0, 0.015),
        duration: still ? Duration.zero : duration,
        curve: Curves.easeOut,
        child: widget.child,
      ),
    );
  }
}
