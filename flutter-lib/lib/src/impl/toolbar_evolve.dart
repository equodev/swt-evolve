import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/item.dart';
import '../gen/swt.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../styles.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';
import 'utils/widget_utils.dart';

class ToolBarImpl<T extends ToolBarSwt, V extends VToolBar>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final toolItems = getToolItems(context);
    final style = StyleBits(state.style);
    final isVertical = style.has(SWT.VERTICAL);
    final shouldWrap = style.has(SWT.WRAP);
    final hasBorder = style.has(SWT.BORDER);
    final isFlat = style.has(SWT.FLAT);
    final hasShadowOut = style.has(SWT.SHADOW_OUT);
    final isRightToLeft = style.has(SWT.RIGHT_TO_LEFT);

    final backgroundColor = getSwtBackgroundColor(context) ?? widgetTheme.toolbarBackgroundColor;

    return Builder(builder: (context) {
      final toolItemTheme = Theme.of(context).extension<ToolItemThemeExtension>();
      final iconSize = toolItemTheme?.defaultIconSize ?? 24.0;

      Widget bar;
      if (shouldWrap) {
        final limitedToolItems = toolItems.map((item) {
          return ConstrainedBox(
            constraints: BoxConstraints(
              maxWidth: isVertical ? iconSize : double.infinity,
              maxHeight: isVertical ? double.infinity : iconSize,
            ),
            child: item,
          );
        }).toList();

        bar = Wrap(
          direction: isVertical ? Axis.vertical : Axis.horizontal,
          textDirection: isRightToLeft ? TextDirection.rtl : TextDirection.ltr,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: limitedToolItems,
        );
      } else {
        bar = SingleChildScrollView(
          scrollDirection: isVertical ? Axis.vertical : Axis.horizontal,
          reverse: isRightToLeft && !isVertical,
          child: isVertical
              ? Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                )
              : Row(
                  mainAxisSize: MainAxisSize.min,
                  textDirection: isRightToLeft ? TextDirection.rtl : TextDirection.ltr,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: toolItems,
                ),
        );
      }

      BoxDecoration? decoration;
      if (hasBorder || hasShadowOut || isFlat) {
        decoration = BoxDecoration(
          color: backgroundColor,
          border: hasBorder ? Border.all(color: widgetTheme.borderColor, width: widgetTheme.borderWidth) : null,
          boxShadow: hasShadowOut
              ? [
                  BoxShadow(
                    color: widgetTheme.shadowColor.withOpacity(widgetTheme.shadowOpacity),
                    blurRadius: widgetTheme.shadowBlurRadius,
                    offset: widgetTheme.shadowOffset,
                  ),
                ]
              : null,
        );
      } else {
        decoration = BoxDecoration(
          color: backgroundColor,
        );
      }

      return super.wrap(
        Align(
          alignment: Alignment.centerLeft,
          child: Container(
            decoration: decoration,
          child: bar,
          ),
        ),
      );
    });
  }

  List<Widget> getToolItems(BuildContext context) {
    if (state.items == null) {
      return [];
    }

    final widgetTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final items = state.items!
        .whereType<VToolItem>()
        .where((toolItem) => (toolItem.image != null || toolItem.text != null))
        .toList();

    final toolItemTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    final keywordTextLower = toolItemTheme.segmentKeywordText.toLowerCase();
    final debugTextLower = toolItemTheme.segmentDebugText.toLowerCase();
    final specialDropdownTooltipText = toolItemTheme.specialDropdownTooltipText;
    final specialDropdownImageFilename = toolItemTheme.specialDropdownImageFilename;

    final hasKeyword = items.any((item) => item.text?.trim().toLowerCase() == keywordTextLower);
    final hasDebug = items.any((item) => item.text?.trim().toLowerCase() == debugTextLower);
    
    if (hasKeyword && hasDebug) {
      return _buildGroupedItems(items, context, keywordTextLower, debugTextLower, specialDropdownTooltipText, specialDropdownImageFilename);
    }

    return _buildGroupedItems(items, context, keywordTextLower, debugTextLower, specialDropdownTooltipText, specialDropdownImageFilename);
  }

  List<Widget> _buildGroupedItems(
    List<VToolItem> items,
    BuildContext context,
    String keywordTextLower,
    String debugTextLower,
    String specialDropdownTooltipText,
    String? specialDropdownImageFilename,
  ) {
    final result = <Widget>[];
    final processedIndices = <int>{};
    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>()!;
    final toolItemTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    
    for (int i = 0; i < items.length; i++) {
      if (processedIndices.contains(i)) continue;
      
      final item = items[i];
      final text = item.text?.trim().toLowerCase();
      final tooltipText = item.toolTipText?.trim();
      final imageFilename = item.image?.filename;

      final useSpecialDropdown = getConfigFlags().use_special_dropdown_button ?? true;

      print('useSpecialDropdown: $useSpecialDropdown');

      if (useSpecialDropdown) {
        final matchesByText = text == specialDropdownTooltipText.toLowerCase();
        final matchesByTooltip = tooltipText == specialDropdownTooltipText;
        final matchesByImage = specialDropdownImageFilename != null && imageFilename == specialDropdownImageFilename;

        if (matchesByText || matchesByTooltip || matchesByImage) {
          result.add(_buildSpecialDropdown(context, item));
          processedIndices.add(i);
          continue;
        }
      }
      
      if (text == keywordTextLower || text == debugTextLower) {
        VToolItem? otherItem;
        int? otherIndex;
        for (int j = i + 1; j < items.length; j++) {
          final otherText = items[j].text?.trim().toLowerCase();
          if ((text == keywordTextLower && otherText == debugTextLower) ||
              (text == debugTextLower && otherText == keywordTextLower)) {
            otherItem = items[j];
            otherIndex = j;
            break;
          }
        }
        
        if (otherItem != null && otherIndex != null) {
          final firstIsKeyword = text == keywordTextLower;
          final firstItem = firstIsKeyword ? item : otherItem;
          final secondItem = firstIsKeyword ? otherItem : item;
          
          result.add(_buildSegmentControl(context, firstItem, secondItem));
          processedIndices.add(i);
          processedIndices.add(otherIndex);
        } else {
          result.add(getWidgetForToolItem(item, toolbarTheme));
        }
      } else {
        result.add(getWidgetForToolItem(item, toolbarTheme));
      }
    }
    
    return result;
  }

  Widget _buildSegmentControl(BuildContext context, VToolItem keywordItem, VToolItem debugItem) {
    return _SegmentControlWidget(
      keywordItem: keywordItem,
      debugItem: debugItem,
    );
  }

  Widget _buildSpecialDropdown(BuildContext context, VToolItem toolItem) {
    return _SpecialDropdownWidget(
      toolItem: toolItem,
    );
  }


  Widget getWidgetForToolItem(VToolItem toolItem, ToolBarThemeExtension widgetTheme, {bool shouldLimitSize = false, double? maxSize}) {
    final itemWidget = ToolItemSwt(value: toolItem);

    Widget result = Padding(
      padding: widgetTheme.itemPadding,
      child: itemWidget,
    );

    if (shouldLimitSize && maxSize != null) {
      result = ConstrainedBox(
        constraints: BoxConstraints(
          maxWidth: maxSize,
          maxHeight: maxSize,
        ),
        child: result,
      );
    }

    return result;
  }
}

class _SegmentControlWidget extends StatefulWidget {
  final VToolItem keywordItem;
  final VToolItem debugItem;

  const _SegmentControlWidget({
    required this.keywordItem,
    required this.debugItem,
  });

  @override
  State<_SegmentControlWidget> createState() => _SegmentControlWidgetState();
}

class _SegmentControlWidgetState extends State<_SegmentControlWidget> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    
    return _buildSegmentControlContent(
      context: context,
      widgetTheme: widgetTheme,
      keywordItem: widget.keywordItem,
      debugItem: widget.debugItem,
    );
  }

  Widget _buildSegmentControlContent({
    required BuildContext context,
    required ToolItemThemeExtension widgetTheme,
    required VToolItem keywordItem,
    required VToolItem debugItem,
  }) {
    final keywordTextLower = widgetTheme.segmentKeywordText.toLowerCase();
    final debugTextLower = widgetTheme.segmentDebugText.toLowerCase();
    
    final keywordText = keywordItem.text?.trim().toLowerCase() == keywordTextLower 
        ? keywordItem 
        : debugItem;
    final debugText = debugItem.text?.trim().toLowerCase() == debugTextLower 
        ? debugItem 
        : keywordItem;
    
    final keywordSelected = keywordText.selection ?? false;
    final debugSelected = debugText.selection ?? false;
    final keywordEnabled = keywordText.enabled ?? true;
    final debugEnabled = debugText.enabled ?? true;
    
    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: widgetTheme.segmentSelectedTextColor,
      baseTextStyle: widgetTheme.fontStyle,
    );
    
    final keywordTextPainter = TextPainter(
      text: TextSpan(text: widgetTheme.segmentKeywordText, style: textStyle),
      textDirection: TextDirection.ltr,
    );
    keywordTextPainter.layout();
    
    final debugTextPainter = TextPainter(
      text: TextSpan(text: widgetTheme.segmentDebugText, style: textStyle),
      textDirection: TextDirection.ltr,
    );
    debugTextPainter.layout();
    
    final keywordWidth = keywordTextPainter.width + widgetTheme.segmentPadding.horizontal;
    final debugWidth = debugTextPainter.width + widgetTheme.segmentPadding.horizontal;
    final backgroundHeight = keywordTextPainter.height + widgetTheme.segmentPadding.vertical;
    
    final selectedWidth = keywordSelected ? keywordWidth : debugWidth;
    final leftPosition = keywordSelected ? 0.0 : keywordWidth;
    
    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final backgroundColor = toolbarTheme?.compositeBackgroundColor ?? Colors.white;

    return Container(
      color: backgroundColor,
      child: IntrinsicHeight(
        child: Container(
          padding: EdgeInsets.symmetric(vertical: widgetTheme.segmentPadding.vertical),
          decoration: BoxDecoration(
            color: widgetTheme.segmentUnselectedBackgroundColor,
            borderRadius: BorderRadius.circular(widgetTheme.segmentBorderRadius),
          ),
          child: Stack(
            children: [
              AnimatedPositioned(
                duration: widgetTheme.segmentAnimationDuration,
                curve: Curves.easeInOut,
                left: leftPosition,
                top: 0,
                bottom: 0,
                child: Container(
                  width: selectedWidth,
                  decoration: BoxDecoration(
                    color: widgetTheme.segmentInnerColor,
                    borderRadius: BorderRadius.circular(widgetTheme.segmentBorderRadius),
                  ),
                ),
              ),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _buildSegmentButtonWrapper(
                    context: context,
                    widgetTheme: widgetTheme,
                    toolItem: keywordText,
                    otherToolItem: debugText,
                    isSelected: keywordSelected,
                    enabled: keywordEnabled,
                    text: widgetTheme.segmentKeywordText,
                  ),
                  _buildSegmentButtonWrapper(
                    context: context,
                    widgetTheme: widgetTheme,
                    toolItem: debugText,
                    otherToolItem: keywordText,
                    isSelected: debugSelected,
                    enabled: debugEnabled,
                    text: widgetTheme.segmentDebugText,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSegmentButtonWrapper({
    required BuildContext context,
    required ToolItemThemeExtension widgetTheme,
    required VToolItem toolItem,
    required VToolItem otherToolItem,
    required bool isSelected,
    required bool enabled,
    required String text,
  }) {
    final textColor = isSelected 
        ? widgetTheme.segmentSelectedTextColor 
        : widgetTheme.segmentUnselectedTextColor;
    
    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: textColor,
      baseTextStyle: widgetTheme.fontStyle,
    );
    
    return GestureDetector(
      onTap: enabled ? () {
        ToolItemSwt(value: toolItem).sendSelectionSelection(toolItem, null);
      } : null,
      child: Container(
        padding: widgetTheme.segmentPadding,
        child: Center(
          child: AnimatedDefaultTextStyle(
            duration: widgetTheme.segmentAnimationDuration,
            style: textStyle,
            child: Text(
              text,
              textAlign: TextAlign.center,
            ),
          ),
        ),
      ),
    );
  }
}

class _SpecialDropdownWidget extends StatefulWidget {
  final VToolItem toolItem;

  const _SpecialDropdownWidget({
    required this.toolItem,
  });

  @override
  State<_SpecialDropdownWidget> createState() => _SpecialDropdownWidgetState();
}

class _SpecialDropdownWidgetState extends State<_SpecialDropdownWidget> {
  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    final enabled = widget.toolItem.enabled ?? true;
    final text = widgetTheme.specialDropdownText;
    
    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: widgetTheme.specialDropdownTextColor,
      baseTextStyle: widgetTheme.fontStyle,
    );
    
    Widget mainContentButton = Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: enabled ? () {
          final itemWidget = ToolItemSwt(value: widget.toolItem);
          itemWidget.sendSelectionSelection(widget.toolItem, null);
        } : null,
        splashColor: widgetTheme.specialDropdownTextColor.withOpacity(widgetTheme.splashOpacity),
        highlightColor: widgetTheme.specialDropdownTextColor.withOpacity(widgetTheme.highlightOpacity),
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(widgetTheme.borderRadius),
          bottomLeft: Radius.circular(widgetTheme.borderRadius),
        ),
        child: Container(
          padding: widgetTheme.specialDropdownPadding,
          decoration: BoxDecoration(
            color: widgetTheme.specialDropdownBackgroundColor,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(widgetTheme.borderRadius),
              bottomLeft: Radius.circular(widgetTheme.borderRadius),
            ),
          ),
          child: Padding(
            padding: widgetTheme.textPadding,
            child: Center(
              child: Text(
                text,
                style: textStyle,
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ),
      ),
    );

    Widget separator = Container(
      width: widgetTheme.separatorBarWidth,
      color: widgetTheme.specialDropdownSeparatorColor,
    );
    
    Widget dropdownArrow = Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: enabled ? () {
          final itemWidget = ToolItemSwt(value: widget.toolItem);
          itemWidget.sendSelectionOpenMenu(widget.toolItem, null);
        } : null,
        splashColor: widgetTheme.specialDropdownTextColor.withOpacity(widgetTheme.splashOpacity),
        highlightColor: widgetTheme.specialDropdownTextColor.withOpacity(widgetTheme.highlightOpacity),
        borderRadius: BorderRadius.only(
          topRight: Radius.circular(widgetTheme.borderRadius),
          bottomRight: Radius.circular(widgetTheme.borderRadius),
        ),
        child: Container(
          padding: widgetTheme.specialDropdownPadding,
          decoration: BoxDecoration(
            color: widgetTheme.specialDropdownBackgroundColor,
            borderRadius: BorderRadius.only(
              topRight: Radius.circular(widgetTheme.borderRadius),
              bottomRight: Radius.circular(widgetTheme.borderRadius),
            ),
          ),
          child: MouseRegion(
            cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
            child: Icon(
              Icons.arrow_drop_down,
              size: widgetTheme.dropdownArrowSize,
              color: widgetTheme.specialDropdownArrowColor,
            ),
          ),
        ),
      ),
    );
    
    return IntrinsicWidth(
      child: IntrinsicHeight(
        child: Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            mainContentButton,
            separator,
            dropdownArrow,
          ],
        ),
      ),
    );
  }
}
