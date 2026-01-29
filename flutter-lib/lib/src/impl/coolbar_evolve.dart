import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/coolitem.dart';
import '../gen/coolbar.dart';
import '../gen/swt.dart';
import '../impl/composite_evolve.dart';
import '../theme/theme_extensions/coolbar_theme_extension.dart';
import 'utils/widget_utils.dart';

class CoolBarImpl<T extends CoolBarSwt, V extends VCoolBar>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<CoolBarThemeExtension>()!;
    final style = state.style;
    final isVertical = hasStyle(style, SWT.VERTICAL);
    final hasValidBounds = hasBounds(state.bounds);

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        onFocusChange: _onFocusChange,
        child: Builder(builder: (context) {
          return super.wrap(
            _CoolBarContainer(
              theme: theme,
              isVertical: isVertical,
              hasValidBounds: hasValidBounds,
              crossAxisSize: _getCrossAxisSize(theme, isVertical),
              backgroundColor: _getBackgroundColor(context, theme),
              borderColor: _getBorderColor(theme),
              child: _buildContent(theme, isVertical, hasValidBounds),
            ),
          );
        }),
      ),
    );
  }

  Widget _buildContent(CoolBarThemeExtension theme, bool isVertical, bool hasValidBounds) {
    final coolItems = _getCoolItems(theme);
    final wrapIndices = state.wrapIndices ?? [];

    if (wrapIndices.isNotEmpty) {
      return _WrappedCoolBarContent(
        items: coolItems,
        wrapIndices: wrapIndices,
        isVertical: isVertical,
      );
    }

    return _SimpleCoolBarContent(
      items: coolItems,
      isVertical: isVertical,
      hasValidBounds: hasValidBounds,
    );
  }

  List<Widget> _getCoolItems(CoolBarThemeExtension theme) {
    if (state.items == null) return [];

    final items = state.items!;
    final itemOrder = state.itemOrder;
    final itemSizes = state.itemSizes;

    final orderedItems = (itemOrder != null && itemOrder.isNotEmpty)
        ? itemOrder
            .where((i) => i >= 0 && i < items.length)
            .map((i) => items[i])
            .toList()
        : items;

    return orderedItems.asMap().entries.map((entry) {
      final index = entry.key;
      final coolItem = entry.value;

      if (itemSizes != null && index < itemSizes.length) {
        coolItem.preferredSize ??= itemSizes[index];
      }

      return CoolItemSwt(value: coolItem);
    }).toList();
  }

  double? _getCrossAxisSize(CoolBarThemeExtension theme, bool isVertical) {
    final bounds = state.bounds;
    if (hasBounds(bounds)) {
      return isVertical
          ? bounds!.width.toDouble()
          : bounds!.height.toDouble();
    }
    return null;
  }

  Color _getBackgroundColor(BuildContext context, CoolBarThemeExtension theme) {
    return getBackgroundColor(
      background: state.background,
      defaultColor: theme.backgroundColor,
    ) ?? theme.backgroundColor;
  }

  Color _getBorderColor(CoolBarThemeExtension theme) {
    return theme.borderColor;
  }

  void _onFocusChange(bool hasFocus) {
    if (hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }
}

class _CoolBarContainer extends StatelessWidget {
  final CoolBarThemeExtension theme;
  final bool isVertical;
  final bool hasValidBounds;
  final double? crossAxisSize;
  final Color backgroundColor;
  final Color borderColor;
  final Widget child;

  const _CoolBarContainer({
    required this.theme,
    required this.isVertical,
    required this.hasValidBounds,
    required this.crossAxisSize,
    required this.backgroundColor,
    required this.borderColor,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: theme.animationDuration,
      width: isVertical ? crossAxisSize : (hasValidBounds ? double.infinity : null),
      height: isVertical ? (hasValidBounds ? double.infinity : null) : crossAxisSize,
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(theme.borderRadius),
        border: Border.all(
          color: borderColor,
          width: theme.borderWidth,
        ),
      ),
      padding: theme.padding,
      child: child,
    );
  }
}

class _SimpleCoolBarContent extends StatelessWidget {
  final List<Widget> items;
  final bool isVertical;
  final bool hasValidBounds;

  const _SimpleCoolBarContent({
    required this.items,
    required this.isVertical,
    required this.hasValidBounds,
  });

  @override
  Widget build(BuildContext context) {
    final mainAxisSize = hasValidBounds ? MainAxisSize.max : MainAxisSize.min;

    if (isVertical) {
      return Column(
        mainAxisSize: mainAxisSize,
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.start,
        children: items,
      );
    }

    return Row(
      mainAxisSize: mainAxisSize,
      crossAxisAlignment: CrossAxisAlignment.center,
      mainAxisAlignment: MainAxisAlignment.start,
      children: items,
    );
  }
}

class _WrappedCoolBarContent extends StatelessWidget {
  final List<Widget> items;
  final List<int> wrapIndices;
  final bool isVertical;

  const _WrappedCoolBarContent({
    required this.items,
    required this.wrapIndices,
    required this.isVertical,
  });

  @override
  Widget build(BuildContext context) {
    final wrappedItems = _buildWrappedItems();

    return SingleChildScrollView(
      scrollDirection: isVertical ? Axis.horizontal : Axis.vertical,
      child: isVertical
          ? Row(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: wrappedItems
                  .map((group) => Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: group,
                      ))
                  .toList(),
            )
          : Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: wrappedItems
                  .map((group) => Row(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: group,
                      ))
                  .toList(),
            ),
    );
  }

  List<List<Widget>> _buildWrappedItems() {
    final List<List<Widget>> result = [];
    int currentIndex = 0;

    for (final endIndex in wrapIndices) {
      if (endIndex > currentIndex && endIndex <= items.length) {
        result.add(items.sublist(currentIndex, endIndex));
        currentIndex = endIndex;
      }
    }

    if (currentIndex < items.length) {
      result.add(items.sublist(currentIndex));
    }

    return result;
  }
}
