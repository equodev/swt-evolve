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
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    if (state.maximizedControl != null) {
      final maxChild = children.firstWhere(
        (child) => child.id == state.maximizedControl?.id,
        orElse: () => children.first,
      );
      return wrap(mapWidgetFromValue(maxChild));
    }

    final isVertical = state.style.has(SWT.VERTICAL);
    final widgetTheme = Theme.of(context).extension<SashThemeExtension>()!;
    final sashWidth = (state.sashWidth ?? widgetTheme.hitAreaSize).toDouble();

    List<int> weights = state.weights ?? List.filled(children.length, 1);

    if (weights.length != children.length) {
      weights = List.filled(children.length, 1);
    }

    return wrap(
      _SashFormLayout(
        children: children,
        weights: weights,
        isVertical: isVertical,
        sashWidth: sashWidth,
        sashTheme: widgetTheme,
        onWeightsChanged: (newWeights) {
          setState(() {
            state.weights = newWeights;
          });
        },
        onMouseEnter: () => widget.sendMouseTrackMouseEnter(state, null),
        onMouseExit: () => widget.sendMouseTrackMouseExit(state, null),
        onFocusIn: () => widget.sendFocusFocusIn(state, null),
        onFocusOut: () => widget.sendFocusFocusOut(state, null),
      ),
    );
  }
}

class _SashFormLayout extends StatefulWidget {
  final List<VControl> children;
  final List<int> weights;
  final bool isVertical;
  final double sashWidth;
  final SashThemeExtension sashTheme;
  final Function(List<int>) onWeightsChanged;
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
      final weightsChanged = !_listsEqual(currentWeightsRounded, widget.weights);
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
    final availableWidth = totalWidth - (widget.sashWidth * (widget.children.length - 1));

    final panelWidths = _calculatePanelSizes(availableWidth);

    List<Widget> stackChildren = [];
    double currentX = 0;

    for (int i = 0; i < widget.children.length; i++) {
      stackChildren.add(
        Positioned(
          left: currentX,
          top: 0,
          width: panelWidths[i],
          height: totalHeight,
          child: mapWidgetFromValue(widget.children[i]),
        ),
      );

      currentX += panelWidths[i];

      if (i < widget.children.length - 1) {
        stackChildren.add(_buildSash(i, false, currentX, totalHeight, constraints));
        currentX += widget.sashWidth;
      }
    }

    return Stack(
      clipBehavior: Clip.none,
      children: stackChildren,
    );
  }

  Widget _buildVerticalLayout(BoxConstraints constraints) {
    final totalWidth = constraints.maxWidth;
    final totalHeight = constraints.maxHeight;
    final availableHeight = totalHeight - (widget.sashWidth * (widget.children.length - 1));

    final panelHeights = _calculatePanelSizes(availableHeight);

    List<Widget> stackChildren = [];
    double currentY = 0;

    for (int i = 0; i < widget.children.length; i++) {
      stackChildren.add(
        Positioned(
          left: 0,
          top: currentY,
          width: totalWidth,
          height: panelHeights[i],
          child: mapWidgetFromValue(widget.children[i]),
        ),
      );

      currentY += panelHeights[i];

      if (i < widget.children.length - 1) {
        stackChildren.add(_buildSash(i, true, currentY, totalWidth, constraints));
        currentY += widget.sashWidth;
      }
    }

    return Stack(
      clipBehavior: Clip.none,
      children: stackChildren,
    );
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

  Widget _buildSash(int index, bool isVertical, double position, double crossAxisSize, BoxConstraints constraints) {
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
        onPanStart: (details) => _onSashDragStart(index, details, isVertical),
        onPanUpdate: (details) => _onSashDragUpdate(details, isVertical, constraints),
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

  void _onSashDragStart(int sashIndex, DragStartDetails details, bool isVertical) {
    print('SashForm: Drag start on sash $sashIndex');

    final totalSize = isVertical
        ? context.size?.height ?? 0
        : context.size?.width ?? 0;
    final totalSashSpace = widget.sashWidth * (widget.children.length - 1);
    final availableSpace = totalSize - totalSashSpace;

    if (availableSpace <= 0) return;

    final totalWeight = _panelSizes.reduce((a, b) => a + b);
    final currentGlobalPosition = isVertical ? details.globalPosition.dy : details.globalPosition.dx;

    double currentPixelPosition = 0;
    for (int i = 0; i < sashIndex; i++) {
      currentPixelPosition += (_panelSizes[i] / totalWeight) * availableSpace;
      currentPixelPosition += widget.sashWidth;
    }
    currentPixelPosition += (_panelSizes[sashIndex] / totalWeight) * availableSpace;

    setState(() {
      _draggingSashIndex = sashIndex;
      _isInLocalEditMode = true;
      _dragStartGlobalPosition = currentGlobalPosition - currentPixelPosition;
    });
  }

  void _onSashDragUpdate(DragUpdateDetails details, bool isVertical, BoxConstraints constraints) {
    if (_draggingSashIndex == null || !_isInLocalEditMode) return;

    if (_isDragPaused) return;

    final totalSize = isVertical ? constraints.maxHeight : constraints.maxWidth;
    final totalSashSpace = widget.sashWidth * (widget.children.length - 1);
    final availableSpace = totalSize - totalSashSpace;

    if (availableSpace <= 0) return;

    final totalWeight = _panelSizes.reduce((a, b) => a + b);

    final currentGlobalPosition = isVertical ? details.globalPosition.dy : details.globalPosition.dx;
    final targetPixelPosition = currentGlobalPosition - _dragStartGlobalPosition;

    double leftPixelPosition = 0;
    for (int i = 0; i < _draggingSashIndex!; i++) {
      leftPixelPosition += (_panelSizes[i] / totalWeight) * availableSpace;
      leftPixelPosition += widget.sashWidth;
    }

    final newLeftPixelSize = targetPixelPosition - leftPixelPosition;
    final newRightPixelSize = availableSpace - leftPixelPosition - newLeftPixelSize -
                              (widget.sashWidth * (widget.children.length - _draggingSashIndex! - 1));

    double otherPanelsSize = 0;
    for (int i = 0; i < widget.children.length; i++) {
      if (i != _draggingSashIndex! && i != _draggingSashIndex! + 1) {
        otherPanelsSize += _panelSizes[i];
      }
    }

    final weightPerPixel = totalWeight / availableSpace;
    final newLeftSize = (newLeftPixelSize * weightPerPixel).clamp(0.1, totalWeight - otherPanelsSize - 0.1);
    final newRightSize = (totalWeight - otherPanelsSize - newLeftSize).clamp(0.1, totalWeight - otherPanelsSize - 0.1);

    setState(() {
      if (newLeftSize > 0.1 && newRightSize > 0.1) {
        _panelSizes[_draggingSashIndex!] = newLeftSize;
        _panelSizes[_draggingSashIndex! + 1] = newRightSize;
      }
    });
  }

  void _onSashDragEnd() {
    setState(() {
      _draggingSashIndex = null;
      _isInLocalEditMode = false;
      _isDragPaused = false;
    });

    final newWeights = _panelSizes.map((f) => f.round()).toList();
    widget.onWeightsChanged(newWeights);
  }
}