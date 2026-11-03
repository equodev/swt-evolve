import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';

import 'utils/mnemonic_text.dart';
import 'utils/text_utils.dart';
import 'widget_config.dart';

/// SWT's CTabFolder.minChars default.
const int ctabDefaultMinimumCharacters = 20;

/// ctabfolder_tab_scroll (default true) scrolls a tab strip that does not fit. False lays the tabs
/// out as SWT's CTabFolder.setItemSize does: they shrink towards minimumCharacters and the ones that
/// still do not fit go behind the chevron.
bool ctabTabScroll() => getConfigFlags().ctabfolder_tab_scroll != false;

/// The label SWT measures for a tab at its minimum size (CTabFolderRenderer.computeSize with
/// MINIMUM_SIZE), or null when the tab has no text at that size.
String? ctabMinimumLabel(String text, int minChars) {
  const ellipsis = '...';
  if (minChars == 0) return null;
  if (text.length <= minChars) return text;
  final end = minChars < ellipsis.length + 1 ? minChars : minChars - ellipsis.length;
  return text.substring(0, end) + (minChars > ellipsis.length + 1 ? ellipsis : '');
}

/// [text] shortened to [width] as CTabFolderRenderer.shortenText does: the longest prefix that fits
/// with "...", else its first character.
String ctabShortenText(String text, double width, double Function(String) measure) {
  const ellipsis = '...';
  if (measure(text) <= width) return text;
  final ellipsisWidth = measure(ellipsis);
  final clusters = text.characters.toList();
  for (var end = clusters.length - 1; end > 0; end--) {
    final prefix = clusters.take(end).join();
    if (measure(prefix) + ellipsisWidth <= width) return prefix + ellipsis;
  }
  return clusters.isEmpty ? '' : clusters.first;
}

/// Marks a tab label drawn shortened, which upstream's CTabItem.getToolTipText answers with the
/// whole text when the tab has no tooltip of its own.
const ctabShortenedLabel = #ctabShortenedLabel;

/// A tab label that a compressed strip can shrink: its minimum intrinsic width is the label SWT
/// measures at [minimumCharacters], and it draws its text shortened to the width it is given.
class ShrinkableTabLabel extends StatelessWidget {
  const ShrinkableTabLabel(this.text, {super.key, this.style, required this.minimumCharacters});

  /// The raw SWT text, `&` markers included.
  final String text;
  final TextStyle? style;
  final int minimumCharacters;

  @override
  Widget build(BuildContext context) {
    final plain = parseMnemonic(text).text;
    final effective = DefaultTextStyle.of(context).style.merge(style);
    final scaler = MediaQuery.textScalerOf(context);
    final direction = Directionality.of(context);
    Size measure(String value) {
      final painter = TextPainter(
        text: TextSpan(text: value, style: effective),
        textDirection: direction,
        textScaler: scaler,
        maxLines: 1,
      )..layout();
      final size = painter.size;
      painter.dispose();
      return size;
    }

    final full = measure(plain);
    final minLabel = ctabMinimumLabel(plain, minimumCharacters);
    final minWidth = minLabel == null ? 0.0 : measure(minLabel).width;
    return _LabelIntrinsics(
      minWidth: minWidth.ceilToDouble(),
      maxWidth: full.width.ceilToDouble(),
      height: full.height,
      child: LayoutBuilder(builder: (context, constraints) {
        if (full.width <= constraints.maxWidth + 0.5) return MnemonicText(text, style: style);
        return MetaData(
          metaData: ctabShortenedLabel,
          child: Text(
            ctabShortenText(plain, constraints.maxWidth, (value) => measure(value).width),
            style: style,
            maxLines: 1,
            softWrap: false,
            overflow: TextOverflow.clip,
          ),
        );
      }),
    );
  }
}

/// Answers intrinsic sizes for a child that cannot (a LayoutBuilder).
class _LabelIntrinsics extends SingleChildRenderObjectWidget {
  const _LabelIntrinsics({
    required this.minWidth,
    required this.maxWidth,
    required this.height,
    super.child,
  });

  final double minWidth;
  final double maxWidth;
  final double height;

  @override
  _RenderLabelIntrinsics createRenderObject(BuildContext context) =>
      _RenderLabelIntrinsics(minWidth, maxWidth, height);

  @override
  void updateRenderObject(BuildContext context, _RenderLabelIntrinsics renderObject) {
    renderObject
      ..minWidth = minWidth
      ..maxWidth = maxWidth
      ..height = height;
  }
}

class _RenderLabelIntrinsics extends RenderProxyBox {
  _RenderLabelIntrinsics(this._minWidth, this._maxWidth, this._height);

  double _minWidth;
  set minWidth(double value) {
    if (value == _minWidth) return;
    _minWidth = value;
    markNeedsLayout();
  }

  double _maxWidth;
  set maxWidth(double value) {
    if (value == _maxWidth) return;
    _maxWidth = value;
    markNeedsLayout();
  }

  double _height;
  set height(double value) {
    if (value == _height) return;
    _height = value;
    markNeedsLayout();
  }

  @override
  double computeMinIntrinsicWidth(double height) => _minWidth;

  @override
  double computeMaxIntrinsicWidth(double height) => _maxWidth;

  @override
  double computeMinIntrinsicHeight(double width) => _height;

  @override
  double computeMaxIntrinsicHeight(double width) => _height;

  @override
  Size computeDryLayout(BoxConstraints constraints) =>
      constraints.constrain(Size(_maxWidth, _height));
}

/// A tab strip laid out as SWT's CTabFolder.setItemSize and setItemLocation do. Each tab is sized
/// between its minimum intrinsic width (labels at minimumCharacters) and its maximum one (full
/// labels); tabs that do not fit even at their minimum are parked past the strip's end, unpainted.
class CompressedTabRow extends MultiChildRenderObjectWidget {
  const CompressedTabRow({
    super.key,
    required this.selectedIndex,
    required this.onOverflowChanged,
    required this.onTabsMoved,
    this.overflowReserve = 0,
    super.children,
  });

  final int selectedIndex;

  /// Width the header takes from this row only because the tabs overflow (the chevron). Overflow is
  /// decided as if that width were free, as SWT reserves the chevron only once it shows.
  final double overflowReserve;

  /// Called after layout when the tabs stop or start fitting at their minimum widths, which is when
  /// SWT shows the chevron.
  final ValueChanged<bool> onOverflowChanged;

  /// Called during layout when a tab moved, resized or was parked, which a relayout without a
  /// rebuild can do (an icon that loads late). It must not mark anything dirty.
  final VoidCallback onTabsMoved;

  @override
  RenderCompressedTabRow createRenderObject(BuildContext context) =>
      RenderCompressedTabRow(selectedIndex, onOverflowChanged, onTabsMoved, overflowReserve);

  @override
  void updateRenderObject(BuildContext context, RenderCompressedTabRow renderObject) {
    renderObject
      ..selectedIndex = selectedIndex
      ..overflowReserve = overflowReserve
      ..onOverflowChanged = onOverflowChanged
      ..onTabsMoved = onTabsMoved;
  }
}

class _TabSlot extends ContainerBoxParentData<RenderBox> {
  bool showing = true;
}

class RenderCompressedTabRow extends RenderBox
    with
        ContainerRenderObjectMixin<RenderBox, _TabSlot>,
        RenderBoxContainerDefaultsMixin<RenderBox, _TabSlot> {
  RenderCompressedTabRow(
      this._selectedIndex, this.onOverflowChanged, this.onTabsMoved, this._overflowReserve);

  int _selectedIndex;
  set selectedIndex(int value) {
    if (value == _selectedIndex) return;
    _selectedIndex = value;
    markNeedsLayout();
  }

  double _overflowReserve;
  set overflowReserve(double value) {
    if (value == _overflowReserve) return;
    _overflowReserve = value;
    markNeedsLayout();
  }

  ValueChanged<bool> onOverflowChanged;
  bool? _reportedOverflow;
  VoidCallback onTabsMoved;
  List<Rect> _reportedTabs = const [];

  @override
  void setupParentData(RenderBox child) {
    if (child.parentData is! _TabSlot) child.parentData = _TabSlot();
  }

  List<RenderBox> get _tabs {
    final tabs = <RenderBox>[];
    var child = firstChild;
    while (child != null) {
      tabs.add(child);
      child = childAfter(child);
    }
    return tabs;
  }

  @override
  double computeMinIntrinsicWidth(double height) => 0;

  @override
  double computeMaxIntrinsicWidth(double height) =>
      _tabs.fold(0.0, (sum, tab) => sum + tab.getMaxIntrinsicWidth(height));

  @override
  double computeMinIntrinsicHeight(double width) =>
      _tabs.fold(0.0, (h, tab) => math.max(h, tab.getMinIntrinsicHeight(double.infinity)));

  @override
  double computeMaxIntrinsicHeight(double width) =>
      _tabs.fold(0.0, (h, tab) => math.max(h, tab.getMaxIntrinsicHeight(double.infinity)));

  @override
  Size computeDryLayout(BoxConstraints constraints) => constraints.biggest.isFinite
      ? constraints.biggest
      : constraints.constrain(Size(computeMaxIntrinsicWidth(double.infinity), 0));

  @override
  void performLayout() {
    final tabs = _tabs;
    final count = tabs.length;
    final free = constraints.maxWidth + _overflowReserve;
    final intrinsicHeight = constraints.hasBoundedHeight ? constraints.maxHeight : double.infinity;
    final selected = _selectedIndex >= 0 && _selectedIndex < count ? _selectedIndex : 0;
    final priority = [if (count > 0) selected, for (var i = 0; i < count; i++) if (i != selected) i];

    final minWidths = List<double>.filled(count, 0);
    var minSum = 0.0;
    for (final i in priority) {
      minWidths[i] = tabs[i].getMinIntrinsicWidth(intrinsicHeight);
      minSum += minWidths[i];
      if (minSum > free) break;
    }
    final overflow = count > 0 && minSum > free;
    final area = overflow ? constraints.maxWidth : free;
    final List<double> widths;
    final showing = List<bool>.filled(count, true);
    if (overflow) {
      widths = minWidths;
      if (area < widths[selected]) widths[selected] = area < 0 ? 0 : area;
      var used = 0.0;
      for (var p = 0; p < priority.length; p++) {
        final i = priority[p];
        used += widths[i];
        showing[i] = p == 0 || (widths[i] > 0 && used <= area);
      }
    } else {
      final maxWidths = [for (final tab in tabs) tab.getMaxIntrinsicWidth(intrinsicHeight)];
      final maxSum = maxWidths.fold(0.0, (a, b) => a + b);
      if (maxSum <= area) {
        widths = maxWidths;
      } else {
        // Every tab gets its minimum plus an even share of what is left, capped at its preferred.
        var extra = ((area - minSum) / count).floorToDouble();
        while (true) {
          var large = 0;
          var total = 0.0;
          for (var i = 0; i < count; i++) {
            if (maxWidths[i] > minWidths[i] + extra) {
              total += minWidths[i] + extra;
              large++;
            } else {
              total += maxWidths[i];
            }
          }
          if (total >= area) {
            extra--;
            break;
          }
          if (large == 0 || area - total < large) break;
          extra++;
        }
        widths = [
          for (var i = 0; i < count; i++)
            maxWidths[i] < minWidths[i] + extra ? maxWidths[i] : minWidths[i] + extra,
        ];
      }
    }

    var x = 0.0;
    var parked = area.isFinite ? area : 0.0;
    var height = 0.0;
    for (var i = 0; i < count; i++) {
      final tab = tabs[i];
      final slot = tab.parentData! as _TabSlot;
      final width = showing[i] ? widths[i] : tab.getMinIntrinsicWidth(intrinsicHeight);
      tab.layout(
        BoxConstraints(minWidth: width, maxWidth: width, maxHeight: constraints.maxHeight),
        parentUsesSize: true,
      );
      slot.showing = showing[i];
      if (showing[i]) {
        slot.offset = Offset(x, 0);
        x += tab.size.width;
        if (tab.size.height > height) height = tab.size.height;
      } else {
        slot.offset = Offset(parked, 0);
        parked += tab.size.width;
      }
    }
    for (final tab in tabs) {
      final slot = tab.parentData! as _TabSlot;
      slot.offset = Offset(slot.offset.dx, (height - tab.size.height) / 2);
    }
    size = constraints.constrain(Size(x, height));
    _overrunsSize = x > size.width;

    final placed = [for (final tab in tabs) (tab.parentData! as _TabSlot).offset & tab.size];
    if (!listEquals(placed, _reportedTabs)) {
      _reportedTabs = placed;
      onTabsMoved();
    }

    if (overflow != _reportedOverflow) {
      _reportedOverflow = overflow;
      SchedulerBinding.instance.addPostFrameCallback((_) {
        if (attached) onOverflowChanged(overflow);
      });
    }
  }

  // Set for the frame the tabs stop overflowing: laid out for the chevron's width, still drawn.
  bool _overrunsSize = false;
  final LayerHandle<ClipRectLayer> _clipLayer = LayerHandle<ClipRectLayer>();

  @override
  void dispose() {
    _clipLayer.layer = null;
    super.dispose();
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    if (_overrunsSize) {
      _clipLayer.layer = context.pushClipRect(needsCompositing, offset, Offset.zero & size, _paintTabs,
          oldLayer: _clipLayer.layer);
    } else {
      _clipLayer.layer = null;
      _paintTabs(context, offset);
    }
  }

  void _paintTabs(PaintingContext context, Offset offset) {
    var child = firstChild;
    while (child != null) {
      final slot = child.parentData! as _TabSlot;
      if (slot.showing) context.paintChild(child, offset + slot.offset);
      child = slot.nextSibling;
    }
  }

  @override
  bool hitTestChildren(BoxHitTestResult result, {required Offset position}) {
    var child = lastChild;
    while (child != null) {
      final slot = child.parentData! as _TabSlot;
      if (slot.showing &&
          result.addWithPaintOffset(
            offset: slot.offset,
            position: position,
            hitTest: (result, transformed) => child!.hitTest(result, position: transformed),
          )) {
        return true;
      }
      child = slot.previousSibling;
    }
    return false;
  }
}
