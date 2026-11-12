import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show RenderProxyBox, ScrollDirection;
import '../gen/point.dart';
import '../gen/scrollbar.dart';
import '../gen/scrolledcomposite.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_settings/scrolledcomposite_theme_settings.dart';
import 'scrollbar_evolve.dart' show sendScrollBarSelectionEvent;
import 'utils/widget_utils.dart';
import 'utils/layered_layout_builder.dart';
import 'utils/quiet_animations.dart';
import 'utils/steady_scrollbar.dart';

class ScrolledCompositeImpl<
  T extends ScrolledCompositeSwt,
  V extends VScrolledComposite
>
    extends CompositeImpl<T, V> {
  /// A click focuses no ScrolledComposite natively, empty or not: the focus belongs to its content.
  @override
  void takeFocusOnPress() {}

  ScrollController? _horizontalController;
  ScrollController? _verticalController;

  bool _userScrolled = false;
  int? _reportedH;
  int? _reportedV;

  /// The number of `setOrigin`/`showControl` requests already acted on. Java counts only those, never a
  /// scroll by the user, so a different number is a request and the same number is not.
  int _takenRequest = 0;

  /// The origin Java last sent, which a request moves the view to. Kept apart from `state.origin`, which
  /// shows where the view is: Java does not send an origin again when it is asked for once more.
  VPoint? _requestedOrigin;

  /// Until the user scrolls, the view follows the selection of the scroll bars (zero on open, or what
  /// `setOrigin` set), so a reveal on open cannot leave it elsewhere. After that it keeps its own offset.
  void _followSelectionUntilScrolled() {
    if (_userScrolled) return;
    final h = state.horizontalBar?.selection ?? 0;
    final v = state.verticalBar?.selection ?? 0;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || _userScrolled) return;
      _jumpTo(_horizontalController, h);
      _jumpTo(_verticalController, v);
    });
  }

  /// Where the view stops for [offset]: at its end when the offset lies past it.
  double _stop(ScrollController controller, int offset) =>
      offset.toDouble().clamp(0.0, controller.position.maxScrollExtent);

  void _jumpTo(ScrollController? controller, int offset) {
    if (controller == null || !controller.hasClients) return;
    final target = _stop(controller, offset);
    if (controller.offset != target) controller.jumpTo(target);
  }

  /// A request moves the view to the origin it came with, once; what Java holds is not reported back, and
  /// afterwards the view keeps its own offset as it does after a scroll by the user.
  @override
  void setValue(V value) {
    if (lastChange?.touches(WireKey.k_ScrolledComposite_origin) ?? true) _requestedOrigin = state.origin;
    super.setValue(value);
    if (!mounted) return;
    _showOrigin();
    final request = state.originRequest ?? 0;
    if (request == _takenRequest) return;
    _takenRequest = request;
    final origin = _requestedOrigin;
    if (origin == null) return;
    final x = origin.x;
    final y = origin.y;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      _moveTo(_horizontalController, x, vertical: false);
      _moveTo(_verticalController, y, vertical: true);
    });
  }

  /// Puts where the view is into `state.origin`, as `getOrigin` answers it: the live state shows the
  /// origin the user scrolled to. Java is not told through it; it learns a scroll as the selection of a
  /// scroll bar.
  void _showOrigin() {
    final h = _horizontalController;
    final v = _verticalController;
    final hasH = h != null && h.hasClients;
    final hasV = v != null && v.hasClients;
    if (!hasH && !hasV) return;
    final x = hasH ? h.offset.round() : 0;
    final y = hasV ? v.offset.round() : 0;
    final shown = state.origin;
    if (shown != null && shown.x == x && shown.y == y) return;
    state.origin = VPoint()
      ..x = x
      ..y = y;
  }

  /// The view stops at its end, which can come before the origin asked for: Java's client area leaves
  /// room for the bars the client draws over the content. Where it stops is not a scroll by the user.
  void _moveTo(ScrollController? controller, int offset, {required bool vertical}) {
    if (controller == null || !controller.hasClients) return;
    _userScrolled = true;
    // Recorded before the jump, whose scroll notification is dispatched while it runs.
    final shown = _stop(controller, offset).round();
    if (vertical) {
      _reportedV = shown;
    } else {
      _reportedH = shown;
    }
    _jumpTo(controller, offset);
  }

  /// A scroll of this widget's own views, not of a scrollable inside the content.
  bool _isOwn(ScrollNotification n) {
    final vertical = hasStyle(state.style, SWT.V_SCROLL);
    return n.metrics.axis == Axis.vertical
        ? n.depth == 0
        : n.depth == (vertical ? 1 : 0);
  }

  bool _onScroll(ScrollNotification n) {
    if (!_isOwn(n)) return false;
    if ((n is UserScrollNotification && n.direction != ScrollDirection.idle) ||
        (n is ScrollStartNotification && n.dragDetails != null)) {
      _userScrolled = true;
    }
    if (n is ScrollUpdateNotification || n is ScrollEndNotification) {
      _showOrigin();
      if (_userScrolled) _reportOffset(n.metrics);
    }
    return false;
  }

  /// Tells Java where the view is, as a selection of the matching scroll bar, so `getOrigin` follows it.
  void _reportOffset(ScrollMetrics metrics) {
    final vertical = metrics.axis == Axis.vertical;
    final bar = vertical ? state.verticalBar : state.horizontalBar;
    if (bar == null) return;
    final offset = metrics.pixels.clamp(0.0, metrics.maxScrollExtent).round();
    final reported = vertical ? _reportedV : _reportedH;
    if (offset == (reported ?? bar.selection ?? 0)) return;
    if (vertical) {
      _reportedV = offset;
    } else {
      _reportedH = offset;
    }
    sendBarSelection(bar, offset);
  }

  @protected
  void sendBarSelection(VScrollBar bar, int selection) =>
      sendScrollBarSelectionEvent(bar, selection);

  @override
  void initState() {
    super.initState();
    _horizontalController = _NoRevealScrollController();
    _verticalController = _NoRevealScrollController();
    _takenRequest = state.originRequest ?? 0;
    _requestedOrigin = state.origin;
  }

  @override
  void dispose() {
    _horizontalController?.dispose();
    _verticalController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(
      context,
    ).extension<ScrolledCompositeThemeExtension>()!;
    final enabled = state.enabled ?? true;

    final hasHScroll = hasStyle(state.style, SWT.H_SCROLL);
    final hasVScroll = hasStyle(state.style, SWT.V_SCROLL);

    final backgroundColor = getScrolledCompositeBackgroundColor(
      state,
      widgetTheme,
      isEnabled: enabled,
    );

    final minContentSize = getScrolledCompositeMinContentSize(
      state,
      widgetTheme,
    );
    final alwaysShowScrollBars = shouldAlwaysShowScrollBars(state);
    final expandHorizontal = shouldExpandHorizontal(state);
    final expandVertical = shouldExpandVertical(state);

    if (state.content == null) {
      return wrap(_buildEmptyContainer(widgetTheme, minContentSize, backgroundColor));
    }

    Widget contentWidget = mapWidgetFromValue(state.content!);

    contentWidget = _NoReveal(child: contentWidget);

    contentWidget = _applyContentConstraints(
      contentWidget,
      minContentSize,
      expandHorizontal,
      expandVertical,
    );

    _followSelectionUntilScrolled();

    return wrap(
      NotificationListener<ScrollNotification>(
        onNotification: _onScroll,
        child: _ThemedScrolledComposite(
          horizontalController: _horizontalController!,
          verticalController: _verticalController!,
          hasHScroll: hasHScroll,
          hasVScroll: hasVScroll,
          expandHorizontal: expandHorizontal,
          expandVertical: expandVertical,
          minContentSize: minContentSize,
          alwaysShowScrollBars: alwaysShowScrollBars,
          widgetTheme: widgetTheme,
          backgroundColor: backgroundColor,
          child: contentWidget,
        ),
      ),
    );
  }

  Widget _buildEmptyContainer(
    ScrolledCompositeThemeExtension widgetTheme,
    Size minContentSize,
    Color backgroundColor,
  ) {
    return QuietAnimatedContainer(
      duration: widgetTheme.animationDuration,
      width: minContentSize.width > 0 ? minContentSize.width : null,
      height: minContentSize.height > 0 ? minContentSize.height : null,
      color: backgroundColor,
    );
  }

  Widget _applyContentConstraints(
    Widget content,
    Size minContentSize,
    bool expandHorizontal,
    bool expandVertical,
  ) {
    if (minContentSize.width > 0 || minContentSize.height > 0) {
      return ConstrainedBox(
        constraints: BoxConstraints(
          minWidth: minContentSize.width,
          minHeight: minContentSize.height,
        ),
        child: content,
      );
    }
    return content;
  }
}

/// The application decides when the focused control is shown (`setShowFocusedControl`) and moves the
/// view with a request. Flutter reveals a widget in two ways, and neither scrolls the view: the focus
/// traversal through these positions, and a widget asking to be shown on screen (a focused field's
/// caret) through [_NoReveal].
class _NoRevealScrollController extends ScrollController {
  @override
  ScrollPosition createScrollPosition(
    ScrollPhysics physics,
    ScrollContext context,
    ScrollPosition? oldPosition,
  ) => _NoRevealScrollPosition(
    physics: physics,
    context: context,
    initialPixels: initialScrollOffset,
    keepScrollOffset: keepScrollOffset,
    oldPosition: oldPosition,
    debugLabel: debugLabel,
  );
}

class _NoRevealScrollPosition extends ScrollPositionWithSingleContext {
  _NoRevealScrollPosition({
    required super.physics,
    required super.context,
    super.initialPixels,
    super.keepScrollOffset,
    super.oldPosition,
    super.debugLabel,
  });

  @override
  Future<void> ensureVisible(
    RenderObject object, {
    double alignment = 0.0,
    Duration duration = Duration.zero,
    Curve curve = Curves.ease,
    ScrollPositionAlignmentPolicy alignmentPolicy = ScrollPositionAlignmentPolicy.explicit,
    RenderObject? targetRenderObject,
  }) => Future<void>.value();
}

class _NoReveal extends SingleChildRenderObjectWidget {
  const _NoReveal({required super.child});

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderNoReveal();
}

/// Keeps a descendant's request to be shown on screen from reaching the views above it.
class _RenderNoReveal extends RenderProxyBox {
  @override
  void showOnScreen({
    RenderObject? descendant,
    Rect? rect,
    Duration duration = Duration.zero,
    Curve curve = Curves.ease,
  }) {}
}

class _ThemedScrolledComposite extends StatelessWidget {
  final ScrollController horizontalController;
  final ScrollController verticalController;
  final bool hasHScroll;
  final bool hasVScroll;
  final bool expandHorizontal;
  final bool expandVertical;
  final Size minContentSize;
  final bool alwaysShowScrollBars;
  final ScrolledCompositeThemeExtension widgetTheme;
  final Color backgroundColor;
  final Widget child;

  const _ThemedScrolledComposite({
    required this.horizontalController,
    required this.verticalController,
    required this.hasHScroll,
    required this.hasVScroll,
    required this.expandHorizontal,
    required this.expandVertical,
    required this.minContentSize,
    required this.alwaysShowScrollBars,
    required this.widgetTheme,
    required this.backgroundColor,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return LayeredLayoutBuilder(
      builder: (context, constraints) {
        final vpW = constraints.maxWidth;
        final vpH = constraints.maxHeight;

        // Honor SWT setExpand{Horizontal,Vertical}: on an expanded axis, fill the
        // viewport (bounded) instead of wrapping in a scroll view, which would
        // impose an infinite constraint. Only scroll when a larger minSize exceeds
        // the viewport.
        final bool vFill = hasVScroll &&
            expandVertical &&
            vpH.isFinite &&
            minContentSize.height <= vpH;
        final bool hFill = hasHScroll &&
            expandHorizontal &&
            vpW.isFinite &&
            minContentSize.width <= vpW;

        // Wrap in a scroll view whenever the style asks for it, even in "fill" mode:
        // the stretch below only guarantees a *minimum* size, so content that turns
        // out taller/wider than the viewport anyway (e.g. a declared minSize that
        // understates the real content) stays reachable instead of being clipped.
        final bool doVScroll = hasVScroll;
        final bool doHScroll = hasHScroll;

        Widget result = child;

        // Stretch the filled axis to the viewport so content fills it.
        if (vFill || hFill) {
          result = ConstrainedBox(
            constraints: BoxConstraints(
              minWidth: hFill ? vpW : 0,
              minHeight: vFill ? vpH : 0,
            ),
            child: result,
          );
        }

        if (doVScroll && doHScroll) {
          result = _buildBothScrollbars(context, result);
        } else if (doVScroll) {
          result = _buildVerticalScrollbar(context, result);
        } else if (doHScroll) {
          result = _buildHorizontalScrollbar(context, result);
        }

        return QuietAnimatedContainer(
          duration: widgetTheme.animationDuration,
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: widgetTheme.borderRadius > 0
                ? BorderRadius.circular(widgetTheme.borderRadius)
                : null,
            border: widgetTheme.borderWidth > 0
                ? Border.all(
                    color: widgetTheme.borderColor,
                    width: widgetTheme.borderWidth,
                  )
                : null,
          ),
          child: result,
        );
      },
    );
  }

  Widget _buildBothScrollbars(BuildContext context, Widget content) {
    return SteadyScrollbar(
      controller: verticalController,
      thumbVisibility: alwaysShowScrollBars,
      notificationPredicate: (notification) => notification.depth == 0,
      child: SteadyScrollbar(
        controller: horizontalController,
        thumbVisibility: alwaysShowScrollBars,
        notificationPredicate: (notification) => notification.depth == 1,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: verticalController,
            scrollDirection: Axis.vertical,
            child: SingleChildScrollView(
              controller: horizontalController,
              scrollDirection: Axis.horizontal,
              physics: const ClampingScrollPhysics(),
              child: content,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildVerticalScrollbar(BuildContext context, Widget content) {
    return SteadyScrollbar(
      controller: verticalController,
      thumbVisibility: alwaysShowScrollBars,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: verticalController,
          scrollDirection: Axis.vertical,
          child: content,
        ),
      ),
    );
  }

  Widget _buildHorizontalScrollbar(BuildContext context, Widget content) {
    return SteadyScrollbar(
      controller: horizontalController,
      thumbVisibility: alwaysShowScrollBars,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: horizontalController,
          scrollDirection: Axis.horizontal,
          physics: const ClampingScrollPhysics(),
          child: content,
        ),
      ),
    );
  }
}

