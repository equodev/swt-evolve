import 'package:flutter/material.dart';
import '../gen/scrolledcomposite.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/scrolledcomposite_theme_extension.dart';
import '../theme/theme_settings/scrolledcomposite_theme_settings.dart';
import 'utils/widget_utils.dart';

class ScrolledCompositeImpl<T extends ScrolledCompositeSwt,
    V extends VScrolledComposite> extends CompositeImpl<T, V> {
  ScrollController? _horizontalController;
  ScrollController? _verticalController;

  @override
  void initState() {
    super.initState();
    _horizontalController = ScrollController();
    _verticalController = ScrollController();
  }

  @override
  void dispose() {
    _horizontalController?.dispose();
    _verticalController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ScrolledCompositeThemeExtension>()!;
    final enabled = state.enabled ?? true;

    final hasHScroll = hasStyle(state.style, SWT.H_SCROLL);
    final hasVScroll = hasStyle(state.style, SWT.V_SCROLL);

    final backgroundColor = getScrolledCompositeBackgroundColor(
      state,
      widgetTheme,
      isEnabled: enabled,
    );

    final minContentSize = getScrolledCompositeMinContentSize(state, widgetTheme);
    final alwaysShowScrollBars = shouldAlwaysShowScrollBars(state);
    final expandHorizontal = shouldExpandHorizontal(state);
    final expandVertical = shouldExpandVertical(state);

    if (state.content == null) {
      return _buildEmptyContainer(widgetTheme, minContentSize, backgroundColor);
    }

    Widget contentWidget = mapWidgetFromValue(state.content!);

    contentWidget = _applyContentConstraints(
      contentWidget,
      minContentSize,
      expandHorizontal,
      expandVertical,
    );

    return wrap(
      _ThemedScrolledComposite(
        horizontalController: _horizontalController!,
        verticalController: _verticalController!,
        hasHScroll: hasHScroll,
        hasVScroll: hasVScroll,
        alwaysShowScrollBars: alwaysShowScrollBars,
        widgetTheme: widgetTheme,
        backgroundColor: backgroundColor,
        child: contentWidget,
      ),
    );
  }

  Widget _buildEmptyContainer(
    ScrolledCompositeThemeExtension widgetTheme,
    Size minContentSize,
    Color backgroundColor,
  ) {
    return AnimatedContainer(
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

class _ThemedScrolledComposite extends StatelessWidget {
  final ScrollController horizontalController;
  final ScrollController verticalController;
  final bool hasHScroll;
  final bool hasVScroll;
  final bool alwaysShowScrollBars;
  final ScrolledCompositeThemeExtension widgetTheme;
  final Color backgroundColor;
  final Widget child;

  const _ThemedScrolledComposite({
    required this.horizontalController,
    required this.verticalController,
    required this.hasHScroll,
    required this.hasVScroll,
    required this.alwaysShowScrollBars,
    required this.widgetTheme,
    required this.backgroundColor,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    Widget result = child;

    if (hasVScroll && hasHScroll) {
      result = _buildBothScrollbars(context, result);
    } else if (hasVScroll) {
      result = _buildVerticalScrollbar(context, result);
    } else if (hasHScroll) {
      result = _buildHorizontalScrollbar(context, result);
    }

    return AnimatedContainer(
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
  }

  Widget _buildBothScrollbars(BuildContext context, Widget content) {
    return _StyledScrollbar(
      controller: verticalController,
      direction: Axis.vertical,
      thumbVisibility: alwaysShowScrollBars,
      widgetTheme: widgetTheme,
      notificationPredicate: (notification) => notification.depth == 0,
      child: _StyledScrollbar(
        controller: horizontalController,
        direction: Axis.horizontal,
        thumbVisibility: alwaysShowScrollBars,
        widgetTheme: widgetTheme,
        notificationPredicate: (notification) => notification.depth == 1,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: verticalController,
            scrollDirection: Axis.vertical,
            child: SingleChildScrollView(
              controller: horizontalController,
              scrollDirection: Axis.horizontal,
              child: content,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildVerticalScrollbar(BuildContext context, Widget content) {
    return _StyledScrollbar(
      controller: verticalController,
      direction: Axis.vertical,
      thumbVisibility: alwaysShowScrollBars,
      widgetTheme: widgetTheme,
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
    return _StyledScrollbar(
      controller: horizontalController,
      direction: Axis.horizontal,
      thumbVisibility: alwaysShowScrollBars,
      widgetTheme: widgetTheme,
      child: ScrollConfiguration(
        behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
        child: SingleChildScrollView(
          controller: horizontalController,
          scrollDirection: Axis.horizontal,
          child: content,
        ),
      ),
    );
  }
}

class _StyledScrollbar extends StatelessWidget {
  final ScrollController controller;
  final Axis direction;
  final bool thumbVisibility;
  final ScrolledCompositeThemeExtension widgetTheme;
  final bool Function(ScrollNotification)? notificationPredicate;
  final Widget child;

  const _StyledScrollbar({
    required this.controller,
    required this.direction,
    required this.thumbVisibility,
    required this.widgetTheme,
    this.notificationPredicate,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return ScrollbarTheme(
      data: ScrollbarThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.hovered) ||
              states.contains(WidgetState.dragged)) {
            return widgetTheme.scrollbarThumbHoverColor;
          }
          return widgetTheme.scrollbarThumbColor;
        }),
        trackColor: WidgetStateProperty.all(widgetTheme.scrollbarTrackColor),
        thickness: WidgetStateProperty.all(widgetTheme.scrollbarThickness),
        radius: Radius.circular(widgetTheme.scrollbarRadius),
        minThumbLength: widgetTheme.scrollbarMinThumbLength,
      ),
      child: Scrollbar(
        controller: controller,
        thumbVisibility: thumbVisibility,
        notificationPredicate: notificationPredicate,
        child: child,
      ),
    );
  }
}
