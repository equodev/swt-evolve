import 'package:flutter/material.dart';
import 'package:flutter/gestures.dart';
import '../gen/control.dart';
import '../gen/event.dart';
import '../gen/sashform.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../styles.dart';
import '../theme/theme_extensions/sash_theme_extension.dart';
import 'color_utils.dart';

class SashFormImpl<T extends SashFormSwt, V extends VSashForm>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final allChildren = state.children;

    if (allChildren == null || allChildren.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    if (state.maximizedControl != null) {
      final maxChild = allChildren.firstWhere(
        (child) => child.id == state.maximizedControl?.id,
        orElse: () => allChildren.first,
      );
      return wrap(mapWidgetFromValue(maxChild));
    }

    final isVertical = state.style.has(SWT.VERTICAL);
    final widgetTheme = Theme.of(context).extension<SashThemeExtension>()!;
    final sashWidth = (state.sashWidth ?? widgetTheme.hitAreaSize).toDouble();

    List<int> allWeights = state.weights ?? List.filled(allChildren.length, 1);

    if (allWeights.length != allChildren.length) {
      allWeights = List.filled(allChildren.length, 1);
    }

    // Mirror DartSashFormLayout.getControls(true) on the Java side: a pane
    // that has been set invisible (e.g. an expand/collapse toggle) must not
    // reserve any sash-form space, so the remaining visible panes take it over.
    final visibleIndices = [
      for (var i = 0; i < allChildren.length; i++)
        if (allChildren[i].visible != false) i,
    ];

    if (visibleIndices.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    final children = [for (final i in visibleIndices) allChildren[i]];
    final weights = [for (final i in visibleIndices) allWeights[i]];

    return wrap(
      _SashFormLayout(
        children: children,
        weights: weights,
        isVertical: isVertical,
        sashWidth: sashWidth,
        sashTheme: widgetTheme,
        onWeightsChanged: (newWeights) {
          final mergedWeights =
              _mergeVisibleWeights(allWeights, visibleIndices, newWeights);
          setState(() {
            state.weights = mergedWeights;
          });
          // Report the dragged weights back to Java: DartSashForm applies
          // them via setWeights, so the next Java-side layout (e.g. a window
          // resize) keeps the user's drag instead of recomputing the panels
          // from the stale weights. setWeights validates against
          // the FULL children list, so the merged array is what must go over.
          _sendWeights(mergedWeights);
        },
        // Live sync while the sash moves, so Java re-lays-out the pane
        // CONTENT during the drag (not only at drop) — the stale content is
        // the visible symptom of that. state.weights is left alone mid-drag;
        // the drag-end onWeightsChanged commit stays authoritative.
        onWeightsDragged: (newWeights) => _sendWeights(
            _mergeVisibleWeights(allWeights, visibleIndices, newWeights)),
        onMouseEnter: () => widget.sendMouseTrackMouseEnter(state, null),
        onMouseExit: () => widget.sendMouseTrackMouseExit(state, null),
        onFocusIn: () => widget.sendFocusFocusIn(state, null),
        onFocusOut: () => widget.sendFocusFocusOut(state, null),
      ),
    );
  }

  void _sendWeights(List<int> weights) {
    final event = VEvent()..text = weights.join(',');
    widget.sendEvent(state, 'Selection/Selection', event);
  }

  /// Folds the dragged weights of the VISIBLE panes back into the full
  /// weights array Java's setWeights expects. Everything is first rescaled to
  /// a ~1000 total so a hidden pane keeps its relative share for when it is
  /// shown again, without the small-total rounding that loses drags.
  static List<int> _mergeVisibleWeights(
    List<int> allWeights,
    List<int> visibleIndices,
    List<int> visibleWeights,
  ) {
    if (visibleIndices.length == allWeights.length) {
      return visibleWeights; // nothing hidden — the common case
    }
    final totalAll = allWeights.fold<int>(0, (a, b) => a + b);
    if (totalAll <= 0) return visibleWeights;
    final base = [
      for (final w in allWeights) ((w / totalAll) * 1000).round(),
    ];
    final visiblePortion =
        visibleIndices.fold<int>(0, (a, i) => a + base[i]);
    final newTotal = visibleWeights.fold<int>(0, (a, b) => a + b);
    if (newTotal <= 0) return base;
    final merged = List<int>.of(base);
    for (var i = 0; i < visibleIndices.length; i++) {
      merged[visibleIndices[i]] =
          ((visibleWeights[i] / newTotal) * visiblePortion).round();
    }
    return merged;
  }
}

class _SashFormLayout extends StatefulWidget {
  final List<VControl> children;
  final List<int> weights;
  final bool isVertical;
  final double sashWidth;
  final SashThemeExtension sashTheme;
  final Function(List<int>) onWeightsChanged;
  final Function(List<int>) onWeightsDragged;
  final VoidCallback onMouseEnter;
  final VoidCallback onMouseExit;
  final VoidCallback onFocusIn;
  final VoidCallback onFocusOut;

  const _SashFormLayout({
    Key? key,
    required this.children,
    required this.weights,
    required this.isVertical,
    required this.sashWidth,
    required this.sashTheme,
    required this.onWeightsChanged,
    required this.onWeightsDragged,
    required this.onMouseEnter,
    required this.onMouseExit,
    required this.onFocusIn,
    required this.onFocusOut,
  }) : super(key: key);

  @override
  _SashFormLayoutState createState() => _SashFormLayoutState();
}

class _SashFormLayoutState extends State<_SashFormLayout> {
  late List<double> _panelSizes;
  int? _draggingSashIndex;
  bool _isInLocalEditMode = false;
  bool _isDragPaused = false;
  double _dragStartGlobalPosition = 0.0;
  // Last weights live-synced to Java this drag, so mid-drag updates only fire
  // when the values move by a visible amount (>= _liveSyncMinDelta per mille).
  List<int>? _lastSentWeights;
  static const int _liveSyncMinDelta = 5;

  @override
  void initState() {
    super.initState();
    _initializePanelSizes();
  }

  @override
  void didUpdateWidget(_SashFormLayout oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (oldWidget.children.length != widget.children.length) {
      _initializePanelSizes();
    } else if (oldWidget.weights != widget.weights && !_isInLocalEditMode) {
      final currentWeightsRounded = _panelSizes.map((f) => f.round()).toList();
      final weightsChanged = !_listsEqual(
        currentWeightsRounded,
        widget.weights,
      );
      if (weightsChanged) {
        _initializePanelSizes();
      }
    }
  }

  bool _listsEqual(List<int> a, List<int> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  void _initializePanelSizes() {
    _panelSizes = widget.weights.map((w) => w.toDouble()).toList();
    if (_panelSizes.isEmpty || _panelSizes.every((f) => f == 0)) {
      _panelSizes = List.filled(widget.children.length, 1.0);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) {
        widget.onMouseEnter();
        if (_draggingSashIndex != null && _isDragPaused) {
          setState(() {
            _isDragPaused = false;
          });
        }
      },
      onExit: (_) {
        widget.onMouseExit();
        if (_draggingSashIndex != null) {
          setState(() {
            _isDragPaused = true;
          });
        }
      },
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.onFocusIn();
          } else {
            widget.onFocusOut();
          }
        },
        child: LayoutBuilder(
          builder: (context, constraints) {
            return widget.isVertical
                ? _buildVerticalLayout(constraints)
                : _buildHorizontalLayout(constraints);
          },
        ),
      ),
    );
  }

  Widget _buildHorizontalLayout(BoxConstraints constraints) {
    final totalWidth = constraints.maxWidth;
    final totalHeight = constraints.maxHeight;
    final availableWidth =
        totalWidth - (widget.sashWidth * (widget.children.length - 1));

    final panelWidths = _calculatePanelSizes(availableWidth);

    List<Widget> stackChildren = [];
    double currentX = 0;

    for (int i = 0; i < widget.children.length; i++) {
      stackChildren.add(
        Positioned(
          // Stable key so Flutter preserves the panel's State across the
          // rebuilds a sash drag produces (same pattern as NoLayout).
          key: ValueKey(widget.children[i].id),
          left: currentX,
          top: 0,
          width: panelWidths[i],
          height: totalHeight,
          child: mapWidgetFromValue(widget.children[i]),
        ),
      );

      currentX += panelWidths[i];

      if (i < widget.children.length - 1) {
        stackChildren.add(
          _buildSash(i, false, currentX, totalHeight, constraints),
        );
        currentX += widget.sashWidth;
      }
    }

    return Stack(clipBehavior: Clip.none, children: stackChildren);
  }

  Widget _buildVerticalLayout(BoxConstraints constraints) {
    final totalWidth = constraints.maxWidth;
    final totalHeight = constraints.maxHeight;
    final availableHeight =
        totalHeight - (widget.sashWidth * (widget.children.length - 1));

    final panelHeights = _calculatePanelSizes(availableHeight);

    List<Widget> stackChildren = [];
    double currentY = 0;

    for (int i = 0; i < widget.children.length; i++) {
      stackChildren.add(
        Positioned(
          // Stable key so Flutter preserves the panel's State across the
          // rebuilds a sash drag produces (same pattern as NoLayout).
          key: ValueKey(widget.children[i].id),
          left: 0,
          top: currentY,
          width: totalWidth,
          height: panelHeights[i],
          child: mapWidgetFromValue(widget.children[i]),
        ),
      );

      currentY += panelHeights[i];

      if (i < widget.children.length - 1) {
        stackChildren.add(
          _buildSash(i, true, currentY, totalWidth, constraints),
        );
        currentY += widget.sashWidth;
      }
    }

    return Stack(clipBehavior: Clip.none, children: stackChildren);
  }

  List<double> _calculatePanelSizes(double availableSpace) {
    if (availableSpace <= 0) {
      return List.filled(widget.children.length, 0);
    }

    final totalWeight = _panelSizes.reduce((a, b) => a + b);

    return _panelSizes.map((size) {
      return (size / totalWeight) * availableSpace;
    }).toList();
  }

  Widget _buildSash(
    int index,
    bool isVertical,
    double position,
    double crossAxisSize,
    BoxConstraints constraints,
  ) {
    final theme = widget.sashTheme;
    final sashColor = theme.sashColor;
    final sashColorHover = theme.sashHoverColor;
    final isDragging = _draggingSashIndex == index;
    final hitAreaSize = theme.hitAreaSize;

    Widget sashWidget = MouseRegion(
      cursor: isVertical
          ? SystemMouseCursors.resizeRow
          : SystemMouseCursors.resizeColumn,
      onEnter: (_) {
        if (_draggingSashIndex != null && _draggingSashIndex != index) {
          setState(() {
            _isDragPaused = true;
          });
        } else if (_draggingSashIndex == index && _isDragPaused) {
          setState(() {
            _isDragPaused = false;
          });
        }
      },
      onExit: (_) {
        // Nothing to do on exit
      },
      child: GestureDetector(
        // Stable key so tests/tooling (flutter_driver) can target a divider.
        key: ValueKey('sashform-sash-$index'),
        onPanStart: (details) => _onSashDragStart(index, details, isVertical),
        onPanUpdate: (details) =>
            _onSashDragUpdate(details, isVertical, constraints),
        onPanEnd: (_) => _onSashDragEnd(),
        child: Container(
          width: isVertical ? crossAxisSize : hitAreaSize,
          height: isVertical ? hitAreaSize : crossAxisSize,
          color: isDragging ? sashColorHover : sashColor,
          child: Center(
            child: Container(
              width: isVertical ? 30 : hitAreaSize,
              height: isVertical ? hitAreaSize : 30,
              color: isDragging
                  ? sashColorHover.withOpacity(theme.sashCenterHoverOpacity)
                  : sashColor.withOpacity(theme.sashCenterOpacity),
            ),
          ),
        ),
      ),
    );

    if (isVertical) {
      return Positioned(
        left: 0,
        top: position - ((hitAreaSize - widget.sashWidth) / 2),
        width: crossAxisSize,
        height: hitAreaSize,
        child: sashWidget,
      );
    } else {
      return Positioned(
        left: position - ((hitAreaSize - widget.sashWidth) / 2),
        top: 0,
        width: hitAreaSize,
        height: crossAxisSize,
        child: sashWidget,
      );
    }
  }

  void _onSashDragStart(
    int sashIndex,
    DragStartDetails details,
    bool isVertical,
  ) {
    print('SashForm: Drag start on sash $sashIndex');

    final totalSize = isVertical
        ? context.size?.height ?? 0
        : context.size?.width ?? 0;
    final totalSashSpace = widget.sashWidth * (widget.children.length - 1);
    final availableSpace = totalSize - totalSashSpace;

    if (availableSpace <= 0) return;

    final totalWeight = _panelSizes.reduce((a, b) => a + b);
    final currentGlobalPosition = isVertical
        ? details.globalPosition.dy
        : details.globalPosition.dx;

    double currentPixelPosition = 0;
    for (int i = 0; i < sashIndex; i++) {
      currentPixelPosition += (_panelSizes[i] / totalWeight) * availableSpace;
      currentPixelPosition += widget.sashWidth;
    }
    currentPixelPosition +=
        (_panelSizes[sashIndex] / totalWeight) * availableSpace;

    setState(() {
      _draggingSashIndex = sashIndex;
      _isInLocalEditMode = true;
      _dragStartGlobalPosition = currentGlobalPosition - currentPixelPosition;
    });
    _lastSentWeights = null;
  }

  // Normalize to a 1000 scale before rounding: with small weight totals
  // (e.g. [1, 1]) plain rounding collapses a sub-half-weight drag back to
  // the original values and the drag is silently lost. Only the ratios
  // matter to SWT.
  List<int> _normalizedWeights() {
    final totalWeight = _panelSizes.reduce((a, b) => a + b);
    return _panelSizes
        .map((f) => ((f / totalWeight) * 1000).round())
        .toList();
  }

  void _onSashDragUpdate(
    DragUpdateDetails details,
    bool isVertical,
    BoxConstraints constraints,
  ) {
    if (_draggingSashIndex == null || !_isInLocalEditMode) return;

    if (_isDragPaused) return;

    final totalSize = isVertical ? constraints.maxHeight : constraints.maxWidth;
    final totalSashSpace = widget.sashWidth * (widget.children.length - 1);
    final availableSpace = totalSize - totalSashSpace;

    if (availableSpace <= 0) return;

    final totalWeight = _panelSizes.reduce((a, b) => a + b);

    final currentGlobalPosition = isVertical
        ? details.globalPosition.dy
        : details.globalPosition.dx;
    final targetPixelPosition =
        currentGlobalPosition - _dragStartGlobalPosition;

    double leftPixelPosition = 0;
    for (int i = 0; i < _draggingSashIndex!; i++) {
      leftPixelPosition += (_panelSizes[i] / totalWeight) * availableSpace;
      leftPixelPosition += widget.sashWidth;
    }

    final newLeftPixelSize = targetPixelPosition - leftPixelPosition;
    final newRightPixelSize =
        availableSpace -
        leftPixelPosition -
        newLeftPixelSize -
        (widget.sashWidth * (widget.children.length - _draggingSashIndex! - 1));

    double otherPanelsSize = 0;
    for (int i = 0; i < widget.children.length; i++) {
      if (i != _draggingSashIndex! && i != _draggingSashIndex! + 1) {
        otherPanelsSize += _panelSizes[i];
      }
    }

    final weightPerPixel = totalWeight / availableSpace;
    final newLeftSize = (newLeftPixelSize * weightPerPixel).clamp(
      0.1,
      totalWeight - otherPanelsSize - 0.1,
    );
    final newRightSize = (totalWeight - otherPanelsSize - newLeftSize).clamp(
      0.1,
      totalWeight - otherPanelsSize - 0.1,
    );

    setState(() {
      if (newLeftSize > 0.1 && newRightSize > 0.1) {
        _panelSizes[_draggingSashIndex!] = newLeftSize;
        _panelSizes[_draggingSashIndex! + 1] = newRightSize;
      }
    });

    // Live-sync to Java while the sash moves so the pane content reflows
    // during the drag (not only at drop) — throttled to visible movement.
    final live = _normalizedWeights();
    final last = _lastSentWeights;
    var moved = last == null || last.length != live.length;
    if (!moved) {
      for (int i = 0; i < live.length; i++) {
        if ((live[i] - last[i]).abs() >= _liveSyncMinDelta) {
          moved = true;
          break;
        }
      }
    }
    if (moved) {
      _lastSentWeights = live;
      widget.onWeightsDragged(live);
    }
  }

  void _onSashDragEnd() {
    setState(() {
      _draggingSashIndex = null;
      _isInLocalEditMode = false;
      _isDragPaused = false;
    });

    widget.onWeightsChanged(_normalizedWeights());
  }
}
