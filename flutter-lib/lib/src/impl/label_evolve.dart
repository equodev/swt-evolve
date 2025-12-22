import 'package:flutter/material.dart';
import '../gen/label.dart';
import '../gen/swt.dart';
import '../impl/control_evolve.dart';
import '../impl/color_utils.dart';
import '../impl/widget_config.dart';
import './utils/image_utils.dart';
import './utils/font_utils.dart';
import '../theme/label_theme_extension.dart';

class LabelImpl<T extends LabelSwt, V extends VLabel>
    extends ControlImpl<T, V> {

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final widgetTheme = Theme.of(context).extension<LabelThemeExtension>()!;
    
    bool hasStyle(int style) => (state.style & style) != 0;
    final enabled = state.enabled ?? true;
    
    // Handle separator style
    if (hasStyle(SWT.SEPARATOR)) {
      return _buildSeparator(context, widgetTheme, colorScheme, hasStyle);
    }
    
    // Handle regular label
    return _buildLabel(context, widgetTheme, colorScheme, textTheme, hasStyle, enabled);
  }

  Widget _buildSeparator(BuildContext context, LabelThemeExtension widgetTheme, 
      ColorScheme colorScheme, bool Function(int) hasStyle) {
    
    final isVertical = hasStyle(SWT.VERTICAL);
    final separatorColor = widgetTheme.borderColor ?? colorScheme.outline;
    final thickness = widgetTheme.borderWidth;
    
    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : BoxConstraints(
            minHeight: widgetTheme.minHeight,
            minWidth: widgetTheme.minWidth,
          );
    
    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
        child: wrap(
          Container(
            constraints: constraints,
            child: isVertical
                ? Container(
                    width: thickness,
                    color: separatorColor,
                  )
                : Container(
                    height: thickness,
                    color: separatorColor,
                  ),
          ),
        ),
      ),
    );
  }

  Widget _buildLabel(BuildContext context, LabelThemeExtension widgetTheme,
      ColorScheme colorScheme, TextTheme textTheme, bool Function(int) hasStyle, bool enabled) {
    
    final text = state.text ?? '';
    final hasWrap = hasStyle(SWT.WRAP);
    final isVertical = hasStyle(SWT.VERTICAL);
    
    // Determine text alignment
    TextAlign textAlign = widgetTheme.textAlign;
    if (hasStyle(SWT.CENTER)) {
      textAlign = TextAlign.center;
    } else if (hasStyle(SWT.RIGHT)) {
      textAlign = TextAlign.right;
    } else if (hasStyle(SWT.LEFT)) {
      textAlign = TextAlign.left;
    }
    
    final useSwtColors = getConfigFlags().use_swt_colors ?? false;
    final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;
    
    final backgroundColor = useSwtColors && state.background != null
        ? colorFromVColor(state.background, defaultColor: widgetTheme.backgroundColor)
        : widgetTheme.backgroundColor;
    
    final textColor = enabled 
        ? (useSwtColors && state.foreground != null
            ? colorFromVColor(state.foreground, defaultColor: widgetTheme.primaryTextColor)
            : widgetTheme.primaryTextColor)
        : widgetTheme.disabledTextColor;
    
    TextStyle textStyle = enabled
        ? widgetTheme.primaryTextStyle
        : widgetTheme.disabledTextStyle;
    textStyle = textStyle.copyWith(color: textColor);
    
    if (useSwtFonts && state.font != null) {
      textStyle = FontUtils.textStyleFromVFont(state.font, context, color: textColor) ?? textStyle;
    }

    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;
    
    final shouldWrap = hasWrap || (hasBounds && text.isNotEmpty);
    
    Widget textWidget = Text(
      text,
      textAlign: textAlign,
      softWrap: shouldWrap,
      overflow: shouldWrap ? TextOverflow.visible : TextOverflow.ellipsis,
      maxLines: shouldWrap ? null : 1,
      style: textStyle,
    );

    if (isVertical) {
      textWidget = RotatedBox(
        quarterTurns: 3,
        child: textWidget,
      );
    }


    Widget child = hasBounds
        ? ConstrainedBox(
            constraints: BoxConstraints(
              maxWidth: state.bounds!.width.toDouble(),
              maxHeight: state.bounds!.height.toDouble(),
            ),
            child: textWidget,
          )
        : textWidget;

    final constraints = hasBounds
        ? BoxConstraints(
            minWidth: state.bounds!.width.toDouble(),
            maxWidth: state.bounds!.width.toDouble(),
            minHeight: state.bounds!.height.toDouble(),
            maxHeight: state.bounds!.height.toDouble(),
          )
        : BoxConstraints(
            minHeight: widgetTheme.minHeight,
            minWidth: widgetTheme.minWidth,
            maxWidth: widgetTheme.maxWidth,
          );

    // Handle image
    if (state.image != null) {
      final imageWidget = ImageUtils.buildVImage(
        state.image,
        width: widgetTheme.iconSize,
        height: widgetTheme.iconSize,
        enabled: enabled,
        constraints: null,
        useBinaryImage: true,
        renderAsIcon: false,
      );
      
      if (imageWidget != null) {
        child = Row(
          mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
          mainAxisAlignment: _getMainAxisAlignment(textAlign, widgetTheme),
          crossAxisAlignment: widgetTheme.crossAxisAlignment,
          children: [
            imageWidget,
            SizedBox(width: widgetTheme.iconTextSpacing),
            Flexible(child: textWidget),
          ],
        );
      }
    }

    return MouseRegion(
      onEnter: (_) => widget.sendMouseTrackMouseEnter(state, null),
      onExit: (_) => widget.sendMouseTrackMouseExit(state, null),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            widget.sendFocusFocusIn(state, null);
          } else {
            widget.sendFocusFocusOut(state, null);
          }
        },
        child: Opacity(
          opacity: enabled ? 1.0 : 0.6,
          child: wrap(
            Container(
              constraints: constraints,
              padding: widgetTheme.padding,
              margin: widgetTheme.margin,
              decoration: backgroundColor != null ? BoxDecoration(
                color: backgroundColor,
                borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
                border: widgetTheme.borderColor != null 
                    ? Border.all(
                        color: widgetTheme.borderColor!,
                        width: widgetTheme.borderWidth,
                      )
                    : null,
              ) : null,
              child: hasBounds
                  ? Align(
                      alignment: _getAlignment(textAlign),
                      child: child,
                    )
                  : child,
            ),
          ),
        ),
      ),
    );
  }

  MainAxisAlignment _getMainAxisAlignment(TextAlign textAlign, LabelThemeExtension widgetTheme) {
    switch (textAlign) {
      case TextAlign.center:
        return MainAxisAlignment.center;
      case TextAlign.right:
      case TextAlign.end:
        return MainAxisAlignment.end;
      case TextAlign.left:
      case TextAlign.start:
      default:
        return widgetTheme.mainAxisAlignment;
    }
  }

  Alignment _getAlignment(TextAlign textAlign) {
    switch (textAlign) {
      case TextAlign.center:
        return Alignment.center;
      case TextAlign.right:
      case TextAlign.end:
        return Alignment.centerRight;
      case TextAlign.left:
      case TextAlign.start:
      default:
        return Alignment.centerLeft;
    }
  }
}