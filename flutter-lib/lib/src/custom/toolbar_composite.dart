import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/canvas.dart';

import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/label.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widgets.dart';
import '../impl/composite_evolve.dart';
import '../nolayout.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';

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
  final Map<int, GlobalKey> _childKeys = {};
  final GlobalKey _stackKey = GlobalKey();
  List<double> _dividerLocalXs = [];

  @override
  Widget buildComposite() {
    final children = state.children;

    if (children == null || children.isEmpty) {
      return wrap(const SizedBox.shrink());
    }

    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor = widget.backgroundColor ?? widgetTheme!.toolbarBackgroundColor;
      final visibleChildren = children.where((child) => child.visible != false).toList();

    if (widget.useBoundsLayout) {
      final boundsHeight = state.bounds?.height;
      final toolbarHeight = (boundsHeight != null && boundsHeight > 0)
          ? boundsHeight.toDouble()
          : double.infinity;
      final toolItemTheme = Theme.of(context).extension<ToolItemThemeExtension>();
      final keywordTextLower = toolItemTheme!.segmentKeywordText.toLowerCase();
      final keywordLeftOffset = widgetTheme!.keywordLeftOffset;
      final dividerColor = widgetTheme!.borderColor;
      final dividerThickness = widgetTheme!.separatorThickness;
      final dividerVerticalPadding = widgetTheme!.dividerVerticalPadding;

      final positionedContent = <Widget>[];

      final nonSepChildren = visibleChildren
          .where((c) => c is! VLabel)
          .where((c) => !(c is VCanvas && _isToolbarSeparatorCanvas(c)))
          .toList()
        ..sort((a, b) => (a.bounds?.x ?? 0).compareTo(b.bounds?.x ?? 0));

      WidgetsBinding.instance.addPostFrameCallback((_) {
        final stackBox = _stackKey.currentContext?.findRenderObject() as RenderBox?;
        if (stackBox == null) return;
        final stackGlobalX = stackBox.localToGlobal(Offset.zero).dx;

        final newDividers = <double>[];
        for (var i = 0; i < nonSepChildren.length; i++) {
          final box = _childKeys[i]?.currentContext?.findRenderObject() as RenderBox?;
          if (box != null) {
            final posEnd = box.localToGlobal(Offset(box.size.width, 0));
            final child = nonSepChildren[i];

            if ((child is VToolBar) && i + 1 < nonSepChildren.length) {
              final boxNext = _childKeys[i + 1]?.currentContext?.findRenderObject() as RenderBox?;
              if (boxNext != null) {
                final endGlobal = posEnd.dx;
                final startNextGlobal = boxNext.localToGlobal(Offset.zero).dx;
                if (!endGlobal.isNaN && !startNextGlobal.isNaN) {
                  newDividers.add((endGlobal + startNextGlobal) / 2 - stackGlobalX);
                }
              }
            }
          }
        }

        if (!listEquals(_dividerLocalXs, newDividers)) {
          setState(() => _dividerLocalXs = newDividers);
        }
      });

      for (var i = 0; i < nonSepChildren.length; i++) {
        final child = nonSepChildren[i];
        final key = _childKeys.putIfAbsent(i, () => GlobalKey());
        final baseLeft = (child.bounds?.x ?? 0).toDouble();
        final isKeyword = child is VToolBar &&
            child.items?.any((item) => item.text?.trim().toLowerCase() == keywordTextLower) == true;
        final left = isKeyword ? baseLeft - keywordLeftOffset : baseLeft;
        final builtChild = buildMapWidgetFromValue(child);
        positionedContent.add(
          Positioned(
            key: key,
            left: left,
            top: 0,
            bottom: 0,
            child: builtChild,
          ),
        );
      }

      for (final dividerX in _dividerLocalXs) {
        positionedContent.add(
          Positioned(
            left: dividerX - dividerThickness / 2,
            top: dividerVerticalPadding,
            bottom: dividerVerticalPadding,
            width: dividerThickness,
            child: ColoredBox(color: dividerColor),
          ),
        );
      }

      return Container(
        height: toolbarHeight,
        decoration: BoxDecoration(color: backgroundColor),
        child: Stack(
          key: _stackKey,
          clipBehavior: Clip.none,
          children: positionedContent,
        ),
      );
    }

    final widgets = visibleChildren.map((child) => buildMapWidgetFromValue(child)).toList();
    return Container(
      decoration: BoxDecoration(color: backgroundColor),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Row(
          mainAxisSize: MainAxisSize.max,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            ...widgets,
            Expanded(
              child: Container(color: backgroundColor),
            ),
          ],
        ),
      ),
    );
  }

  static bool _isToolbarSeparatorCanvas(VCanvas child) {
    final b = child.bounds;
    if (b == null) return false;
    final w = b.width;
    final h = b.height;
    return w > 0 && w <= 24 && h >= 20;
  }

  Widget buildMapWidgetFromValue(VControl child) {
    if (child is VComposite && (child.swt == "Composite")) {
      return ToolbarComposite(value: child, useBoundsLayout: widget.useBoundsLayout, backgroundColor: widget.backgroundColor);
    } else if (child is VCanvas) {
      if (_isToolbarSeparatorCanvas(child)) {
        final theme = Theme.of(context).extension<ToolBarThemeExtension>()!;
        return VerticalDivider(width: theme.separatorWidth, thickness: theme.separatorThickness, color: theme.borderColor);
      }
      return ToolbarComposite(value: child, useBoundsLayout: widget.useBoundsLayout, backgroundColor: widget.backgroundColor);
    }
    return mapWidgetFromValue(child);
  }
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

    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor = widgetTheme?.toolbarBackgroundColor ?? Colors.white;
    final visibleChildren = children.where((child) => child.visible == true).toList();

    return ColoredBox(
      color: backgroundColor,
      child: NoLayout(children: visibleChildren, composite: state),
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
      child: Stack(
        children: [
          Column(mainAxisSize: MainAxisSize.max, children: widgets),
          ...dividers,
        ],
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
