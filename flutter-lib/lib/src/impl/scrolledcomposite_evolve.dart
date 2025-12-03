import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/scrolledcomposite.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';

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
    final content = state.content;
    final minWidth = state.minWidth?.toDouble() ?? 0.0;
    final minHeight = state.minHeight?.toDouble() ?? 0.0;

    // Get scroll flags from style field (H_SCROLL = 1 << 8, V_SCROLL = 1 << 9)
    const int H_SCROLL = 1 << 8;  // 256
    const int V_SCROLL = 1 << 9;  // 512
    final hasHScroll = (state.style & H_SCROLL) != 0;
    final hasVScroll = (state.style & V_SCROLL) != 0;

    // If no content, show empty container
    if (content == null) {
      return Container(
        width: minWidth > 0 ? minWidth : null,
        height: minHeight > 0 ? minHeight : null,
      );
    }

    Widget contentWidget = mapWidgetFromValue(content);

    // Apply minimum size constraints to content
    // This ensures the content respects minWidth/minHeight
    if (minWidth > 0 || minHeight > 0) {
      contentWidget = ConstrainedBox(
        constraints: BoxConstraints(
          minWidth: minWidth,
          minHeight: minHeight,
        ),
        child: contentWidget,
      );
    }

    // Apply scrolling based on flags from Java
    if (hasVScroll && hasHScroll) {
      // Both scrollbars - always visible when there's content to scroll
      return Scrollbar(
        controller: _verticalController,
        thumbVisibility: true,
        notificationPredicate: (notification) => notification.depth == 0,
        child: Scrollbar(
          controller: _horizontalController,
          thumbVisibility: true,
          notificationPredicate: (notification) => notification.depth == 1,
          child: ScrollConfiguration(
            // Disable automatic scrollbars to prevent duplication
            behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
            child: SingleChildScrollView(
              controller: _verticalController,
              scrollDirection: Axis.vertical,
              child: SingleChildScrollView(
                controller: _horizontalController,
                scrollDirection: Axis.horizontal,
                child: contentWidget,
              ),
            ),
          ),
        ),
      );
    } else if (hasVScroll) {
      // Only vertical scrollbar - always visible when there's content to scroll
      return Scrollbar(
        controller: _verticalController,
        thumbVisibility: true,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: _verticalController,
            scrollDirection: Axis.vertical,
            child: contentWidget,
          ),
        ),
      );
    } else if (hasHScroll) {
      // Only horizontal scrollbar - always visible when there's content to scroll
      return Scrollbar(
        controller: _horizontalController,
        thumbVisibility: true,
        child: ScrollConfiguration(
          behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
          child: SingleChildScrollView(
            controller: _horizontalController,
            scrollDirection: Axis.horizontal,
            child: contentWidget,
          ),
        ),
      );
    } else {
      // No scrollbars
      return contentWidget;
    }
  }
}
