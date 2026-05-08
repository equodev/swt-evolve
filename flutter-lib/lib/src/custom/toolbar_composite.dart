import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/canvas.dart';

import '../gen/clabel.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/image.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../impl/decorations_evolve.dart';
import '../nolayout.dart';
import '../theme/theme_extensions/clabel_theme_extension.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';
import 'controls_item.dart';

class HorizontalMenuBar extends StatelessWidget {
  const HorizontalMenuBar({super.key});

  @override
  Widget build(BuildContext context) {
    final data = DecorationsMenuData.of(context);
    if (data == null || !data.isHorizontal || data.menuBar == null) {
      return const SizedBox.shrink();
    }
    return mapWidgetFromValue(data.menuBar!);
  }
}

class ToolbarComposite extends CompositeSwt<VComposite> {
  final bool useBoundsLayout;
  final Color? backgroundColor;

  const ToolbarComposite({
    super.key,
    required super.value,
    this.useBoundsLayout = false,
    this.backgroundColor,
  });

  @override
  State<StatefulWidget> createState() => MainToolbarCompositeImpl();
}

class MainToolbarCompositeImpl extends CompositeImpl<ToolbarComposite, VComposite> {
  @override
  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    // If all visible children are VCLabel chips, render as chip widget directly.
    // This keeps the ToolbarComposite element stable (no widget-type change) while
    // still supporting chip rendering for composites that contain only chip labels.
    final visibleKidsForChip = children.where((c) => c.visible != false).toList();
    if (visibleKidsForChip.isNotEmpty) {
      final labels = visibleKidsForChip.whereType<VCLabel>().toList();
      if (labels.length == visibleKidsForChip.length) {
        final chipLabels = labels.where((l) =>
            l.cursor?.cursorStyle == SWT.CURSOR_HAND &&
            l.image != null &&
            (l.toolTipText?.isNotEmpty == true ||
                (l.text?.isNotEmpty == true && l.text != '<none>'))).toList();
        if (chipLabels.isNotEmpty) {
          final addonImages = labels
              .where((l) => !chipLabels.contains(l) && l.image != null)
              .map((l) => l.image!)
              .toList();
          chipLabels.sort((a, b) => (a.bounds?.x ?? 0).compareTo(b.bounds?.x ?? 0));
          Widget chipWidget = mapWidgetFromValue(chipLabels.first);
          if (addonImages.isNotEmpty) {
            chipWidget = ChipAddonImages(addonImages: addonImages, child: chipWidget);
          }
          return chipWidget;
        }
      }
    }

    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor = widget.backgroundColor ?? widgetTheme!.toolbarBackgroundColor;
    final visibleChildren = children.where((child) => child.visible != false).toList();
    final isRootToolbar = widget.value.swt == "MainToolbar";

    final menuData = isRootToolbar ? DecorationsMenuData.of(context) : null;
    final hasHorizontalMenu = menuData?.isHorizontal == true && menuData?.menuBar != null;

    if (widget.useBoundsLayout) {
      final boundsHeight = state.bounds?.height;
      final toolbarHeight = (boundsHeight != null && boundsHeight > 0)
          ? boundsHeight.toDouble()
          : 70.0;
      final toolItemTheme = Theme.of(context).extension<ToolItemThemeExtension>();
      final keywordTextLower = toolItemTheme!.segmentKeywordText.toLowerCase();
      final keywordLeftOffset = widgetTheme!.keywordLeftOffset;
      final dividerColor = widgetTheme!.borderColor;
      final dividerThickness = widgetTheme!.separatorThickness;
      final dividerVerticalPadding = widgetTheme!.dividerVerticalPadding;

      final nonSepChildren = visibleChildren
          .where((c) => c is! VLabel)
          .where((c) => (c.bounds?.width ?? 0) > 0 || (c.bounds?.height ?? 0) > 0)
          .toList()
        ..sort((a, b) => (a.bounds?.x ?? 0).compareTo(b.bounds?.x ?? 0));

      final computedDividers = <double>[];
      for (var i = 0; i < nonSepChildren.length - 1; i++) {
        final curr = nonSepChildren[i];
        final next = nonSepChildren[i + 1];
        if (curr is VToolBar) {
          final currEnd =
              ((curr.bounds?.x ?? 0) + (curr.bounds?.width ?? 0)).toDouble();
          final nextStart = (next.bounds?.x ?? 0).toDouble();
          if (nextStart > currEnd) {
            computedDividers.add((currEnd + nextStart) / 2.0);
          }
        }
      }

      final builtChildren = [
        for (final child in nonSepChildren)
          (child: child, widget: buildMapWidgetFromValue(child)),
      ];

      final toolbarRow = Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (isRootToolbar) const VerticalMenuButton(atStart: true),
          Expanded(
            child: LayoutBuilder(
              builder: (_, box) {
                final availW = box.maxWidth;
                final effectiveH =
                    box.maxHeight.isFinite ? box.maxHeight : toolbarHeight;

                double currentRightNatural = 0.0;
                final reflowItems = <({
                  double effectiveLeft,
                  double effectiveW,
                  Widget widget,
                })>[];

                for (final entry in builtChildren) {
                  final rawLeft = (entry.child.bounds?.x ?? 0).toDouble();
                  final javaChildW =
                      (entry.child.bounds?.width ?? 0).toDouble();
                  final isKeyword = isRootToolbar &&
                      _containsKeyword(entry.child, keywordTextLower);
                  final isVCLabelItem = _containsVCLabel(entry.child);

                  final effectiveLeft = (isKeyword || isVCLabelItem)
                      ? currentRightNatural
                      : math.max(rawLeft, currentRightNatural);

                  final naturalW = _estimateChildNaturalWidth(entry.child, toolItemTheme!);
                  final double effectiveW;
                  if (isVCLabelItem && entry.child is VCLabel) {
                    final vcl = entry.child as VCLabel;
                    effectiveW = _isVCLabelChip(vcl) ? _measureChipW(vcl) : javaChildW;
                  } else if (isVCLabelItem && entry.child is VComposite) {
                    effectiveW = _getVCLabelCompositeW(entry.child as VComposite);
                  } else if (isVCLabelItem) {
                    effectiveW = javaChildW;
                  } else {
                    effectiveW = math.max(javaChildW, naturalW);
                  }
                  reflowItems.add((effectiveLeft: effectiveLeft, effectiveW: effectiveW, widget: entry.widget));
                  currentRightNatural = math.max(currentRightNatural, effectiveLeft + effectiveW);
                }

                final totalRight = currentRightNatural;
                final finalScaleX = (totalRight > availW && totalRight > 0)
                    ? availW / totalRight
                    : 1.0;

                final stackChildren = <Widget>[];

                for (final item in reflowItems) {
                  stackChildren.add(Positioned(
                    left: item.effectiveLeft * finalScaleX,
                    top: 0,
                    bottom: 0,
                    width: item.effectiveW * finalScaleX,
                    child: item.widget,
                  ));
                }

                for (final dividerX in computedDividers) {
                  stackChildren.add(Positioned(
                    left: dividerX * finalScaleX - dividerThickness / 2,
                    top: dividerVerticalPadding,
                    bottom: dividerVerticalPadding,
                    width: dividerThickness,
                    child: ColoredBox(color: dividerColor),
                  ));
                }

                return SizedBox(
                  width: availW,
                  height: effectiveH,
                  child: Stack(
                    clipBehavior: Clip.hardEdge,
                    children: stackChildren,
                  ),
                );
              },
            ),
          ),
          if (isRootToolbar) ToolbarOptionalControlsRow(useBoundsLayout: true),
          if (isRootToolbar) const VerticalMenuButton(atStart: false),
        ],
      );

      return Container(
        height: toolbarHeight,
        decoration: BoxDecoration(color: backgroundColor),
        child: ToolbarAreaMarker(
          active: true,
          child: hasHorizontalMenu
              ? Column(
                  children: [
                    const HorizontalMenuBar(),
                    Expanded(child: toolbarRow),
                  ],
                )
              : toolbarRow,
        ),
      );
    }

    final widgets = visibleChildren.map((child) => buildMapWidgetFromValue(child)).toList();
    final nonBoundsRow = Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        if (isRootToolbar) const VerticalMenuButton(atStart: true),
        Expanded(
          child: ClipRect(
            child: Align(
              alignment: Alignment.centerLeft,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: widgets,
              ),
            ),
          ),
        ),
        if (isRootToolbar) ToolbarOptionalControlsRow(useBoundsLayout: false),
        if (isRootToolbar) const VerticalMenuButton(atStart: false),
      ],
    );

    return Container(
      decoration: BoxDecoration(color: backgroundColor),
      child: ToolbarAreaMarker(
        active: true,
        child: hasHorizontalMenu
            ? Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const HorizontalMenuBar(),
                  nonBoundsRow,
                ],
              )
            : nonBoundsRow,
      ),
    );
  }

  bool _isVCLabelChip(VCLabel vcl) =>
      vcl.cursor?.cursorStyle == SWT.CURSOR_HAND &&
      vcl.image != null &&
      ((vcl.text?.isNotEmpty == true && vcl.text != '<none>') ||
          vcl.toolTipText?.isNotEmpty == true);

  double _measureChipW(VCLabel vcl) {
    final text = (vcl.text == null || vcl.text == '<none>') ? '' : vcl.text!;
    final clabelTheme = Theme.of(context).extension<CLabelThemeExtension>()!;
    final margins = (vcl.leftMargin ?? 0) + (vcl.rightMargin ?? 0);
    double textW = 0;
    if (text.isNotEmpty && clabelTheme.primaryTextStyle != null) {
      textW = (TextPainter(text: TextSpan(text: text, style: clabelTheme.primaryTextStyle), textDirection: TextDirection.ltr)..layout()).width;
    }
    return math.max(
      margins + clabelTheme.iconSize + (text.isNotEmpty ? clabelTheme.iconTextSpacing + textW : 0.0),
      (vcl.bounds?.width ?? 0).toDouble(),
    );
  }

  double _getVCLabelCompositeW(VComposite composite) {
    final vcLabels = (composite.children ?? []).whereType<VCLabel>().toList();
    if (vcLabels.isEmpty) return (composite.bounds?.width ?? 0).toDouble();
    return vcLabels.fold(0.0, (total, vcl) =>
        total + (_isVCLabelChip(vcl) ? _measureChipW(vcl) : (vcl.bounds?.width ?? 0).toDouble()));
  }

  static bool _containsKeyword(VControl child, String keywordTextLower) {
    if (child is VToolBar) {
      return child.items?.any(
            (item) => item.text?.trim().toLowerCase() == keywordTextLower,
          ) ==
          true;
    }
    if (child is VComposite) {
      return child.children?.any(
            (c) => _containsKeyword(c, keywordTextLower),
          ) ==
          true;
    }
    return false;
  }

  static bool _containsVCLabel(VControl child) {
    if (child is VCLabel) return true;
    if (child is VComposite) {
      return child.children?.any(_containsVCLabel) == true;
    }
    return false;
  }

  static double _estimateChildNaturalWidth(
      VControl child, ToolItemThemeExtension itemTheme) {
    if (child is VToolBar) {
      final items = child.items;
      if (items == null || items.isEmpty) {
        return itemTheme.defaultIconSize + itemTheme.buttonPadding.horizontal;
      }
      return items.fold(0.0, (acc, item) {
        if ((item.style & SWT.SEPARATOR) != 0 && item.image == null) return acc + itemTheme.separatorWidth;
        if ((item.style & SWT.DROP_DOWN) != 0) {
          return acc +
              itemTheme.defaultIconSize +
              itemTheme.buttonPadding.horizontal +
              itemTheme.separatorBarWidth +
              itemTheme.dropdownArrowSize;
        }
        return acc + itemTheme.defaultIconSize + itemTheme.buttonPadding.horizontal;
      });
    }
    if (child is VComposite) {
      final javaW = (child.bounds?.width ?? 0).toDouble();
      final children = child.children;
      if (children == null || children.isEmpty) return javaW;
      final childrenSum = children.fold(
          0.0, (acc, c) => acc + _estimateChildNaturalWidth(c, itemTheme));
      return math.max(javaW, childrenSum);
    }
    return (child.bounds?.width ?? 0).toDouble();
  }

  Widget buildMapWidgetFromValue(VControl child) {
    if (child is VComposite && child.swt == "Composite") {
      // Always wrap in a keyed ToolbarComposite so Flutter never changes the
      // element type at this position (which would destroy canvas GC state).
      // Chip rendering is handled inside ToolbarComposite.buildComposite().
      return ToolbarComposite(
        key: ValueKey(child.id),
        value: child,
        useBoundsLayout: widget.useBoundsLayout,
        backgroundColor: widget.backgroundColor,
      );
    } else if (child is VCanvas && child is! VCLabel) {
      return ToolbarComposite(
        key: ValueKey(child.id),
        value: child as VComposite,
        useBoundsLayout: widget.useBoundsLayout,
        backgroundColor: widget.backgroundColor,
      );
    }
    return mapWidgetFromValue(child);
  }
}

class ChipAddonImages extends InheritedWidget {
  final List<VImage> addonImages;

  const ChipAddonImages({
    super.key,
    required this.addonImages,
    required super.child,
  });

  static List<VImage>? of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ChipAddonImages>()?.addonImages;

  @override
  bool updateShouldNotify(ChipAddonImages old) => addonImages != old.addonImages;
}

class SideBarComposite extends CompositeSwt<VComposite> {
  const SideBarComposite({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => SideBarCompositeImpl();
}

class StatusBarComposite extends CompositeSwt<VComposite> {
  const StatusBarComposite({super.key, required super.value});

  @override
  State<StatefulWidget> createState() => StatusBarCompositeImpl();
}

class StatusBarCompositeImpl extends CompositeImpl<StatusBarComposite, VComposite> {
  @override
  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final backgroundColor = widgetTheme.compositeBackgroundColor;

    final visibleChildren = children.where((child) => child.visible != false).toList()
      ..sort((a, b) => (a.bounds?.x ?? 0).compareTo(b.bounds?.x ?? 0));

    final positionedItems = visibleChildren.map((child) {
      return Positioned(
        left: (child.bounds?.x ?? 0).toDouble(),
        top: (child.bounds?.y ?? 0).toDouble(),
        width: child.bounds?.width.toDouble(),
        height: child.bounds?.height.toDouble(),
        child: mapWidgetFromValue(child),
      );
    }).toList();

    double contentHeight = 0;
    for (final child in visibleChildren) {
      final bottom = (child.bounds?.y ?? 0) + (child.bounds?.height ?? 0);
      if (bottom > contentHeight) contentHeight = bottom.toDouble();
    }

    final barHeight = (state.bounds?.height ?? 0) > 0
        ? state.bounds!.height.toDouble()
        : 30.0;
    if (contentHeight <= 0) contentHeight = barHeight;
    final effectiveHeight = contentHeight > barHeight ? contentHeight : barHeight;

    return Container(
      width: double.infinity,
      height: effectiveHeight,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(
          top: BorderSide(
            color: widgetTheme.borderColor,
            width: widgetTheme.borderWidth,
          ),
        ),
      ),
      child: ToolbarAreaMarker(
        active: true,
        background: backgroundColor,
        child: Stack(clipBehavior: Clip.none, children: positionedItems),
      ),
    );
  }
}

class SideBarCompositeImpl extends CompositeImpl<SideBarComposite, VComposite> {
  final Map<int, GlobalKey> _childKeys = {};
  final GlobalKey _containerKey = GlobalKey();
  List<double> _dividerLocalYs = [];

  @override
  Widget buildComposite() {
    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor = widgetTheme!.compositeBackgroundColor;

    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    final visibleChildren = children.where((child) => child.visible != false).toList();

    final boundsHeight = state.bounds?.height;
    final containerHeight = (boundsHeight != null && boundsHeight > 0)
        ? boundsHeight.toDouble()
        : double.infinity;

    final dividerColor = widgetTheme!.borderColor;
    final dividerThickness = widgetTheme!.separatorThickness;
    final borderWidth = widgetTheme!.borderWidth;

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final containerBox = _containerKey.currentContext?.findRenderObject() as RenderBox?;
      if (containerBox == null) return;
      final containerGlobalY = containerBox.localToGlobal(Offset.zero).dy;

      final newDividers = <double>[];
      for (var i = 0; i < visibleChildren.length - 1; i++) {
        final box = _childKeys[i]?.currentContext?.findRenderObject() as RenderBox?;
        final boxNext = _childKeys[i + 1]?.currentContext?.findRenderObject() as RenderBox?;
        if (box == null || boxNext == null) continue;
        final endGlobal = box.localToGlobal(Offset(0, box.size.height)).dy;
        final startNextGlobal = boxNext.localToGlobal(Offset.zero).dy;
        if (!endGlobal.isNaN && !startNextGlobal.isNaN) {
          newDividers.add((endGlobal + startNextGlobal) / 2 - containerGlobalY);
        }
      }

      if (!listEquals(_dividerLocalYs, newDividers)) {
        setState(() => _dividerLocalYs = newDividers);
      }
    });

    final widgets = <Widget>[];
    for (var i = 0; i < visibleChildren.length; i++) {
      final key = _childKeys.putIfAbsent(i, () => GlobalKey());
      widgets.add(KeyedSubtree(key: key, child: buildMapWidgetFromValue(visibleChildren[i])));
    }

    final dividers = [
      for (final dividerY in _dividerLocalYs)
        Positioned(
          left: 0,
          right: 0,
          top: dividerY - dividerThickness / 2,
          height: dividerThickness,
          child: ColoredBox(color: dividerColor),
        ),
    ];

    return Container(
      key: _containerKey,
      height: containerHeight,
      decoration: BoxDecoration(
        color: backgroundColor,
        border: Border(top: BorderSide(color: dividerColor, width: borderWidth)),
      ),
      child: ToolbarAreaMarker(
        active: true,
        child: Stack(
          children: [
            Column(mainAxisSize: MainAxisSize.max, children: widgets),
            ...dividers,
          ],
        ),
      ),
    );
  }

  Widget buildMapWidgetFromValue(VControl child) {
    if (child is VComposite && (child.swt == "Composite")) {
      return SideBarComposite(value: child);
    } else if (child is VCanvas) {
      final theme = Theme.of(context).extension<ToolBarThemeExtension>()!;
      return Padding(
        padding: EdgeInsets.symmetric(vertical: theme.dividerVerticalPadding),
        child: SideBarComposite(value: child),
      );
    }
    return mapWidgetFromValue(child);
  }
}

class ToolbarAreaMarker extends InheritedWidget {
  final bool active;
  final Color? background;

  const ToolbarAreaMarker({
    super.key,
    required this.active,
    this.background,
    required super.child,
  });

  static bool of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ToolbarAreaMarker>()?.active ??
          false;

  static Color? backgroundOf(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<ToolbarAreaMarker>()?.background;

  @override
  bool updateShouldNotify(ToolbarAreaMarker oldWidget) =>
      active != oldWidget.active || background != oldWidget.background;
}
