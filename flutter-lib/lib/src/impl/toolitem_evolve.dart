import 'dart:convert';
import 'dart:typed_data' show Uint8List;
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../gen/swt.dart';
import '../gen/toolitem.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../impl/item_evolve.dart';
import './utils/image_utils.dart';
import './utils/widget_utils.dart';
import '../theme/theme_extensions/toolitem_theme_extension.dart';
import '../theme/theme_extensions/toolbar_theme_extension.dart';
import 'dart:ui';

class ToolItemImpl<T extends ToolItemSwt, V extends VToolItem>
    extends ItemImpl<T, V> {
  bool _isHovered = false;
  Widget? _cachedImageWidget;
  VImage? _cachedImage;
  double? _cachedIconSize;
  Color? _cachedIconColor;
  bool? _cachedEnabled;

  double _calculateIconSize(BoxConstraints? constraints, double defaultSize) {
    if (constraints == null) return defaultSize;

    if (constraints.minWidth > 0 &&
        constraints.maxWidth.isFinite &&
        constraints.minWidth == constraints.maxWidth) {
      return constraints.minWidth;
    }

    if (constraints.minWidth > 0 && constraints.minHeight > 0) {
      return constraints.minWidth < constraints.minHeight
          ? constraints.minWidth
          : constraints.minHeight;
    }

    if (constraints.maxWidth.isFinite && constraints.maxHeight.isFinite) {
      return constraints.maxWidth < constraints.maxHeight
          ? constraints.maxWidth
          : constraints.maxHeight;
    }

    return defaultSize;
  }

  Widget _buildImageWidget(
    VImage? image,
    bool enabled,
    BoxConstraints? constraints,
    double defaultIconSize,
    Color iconColor,
    ToolItemThemeExtension widgetTheme,
  ) {
    final iconSize = _calculateIconSize(constraints, defaultIconSize);

    final imageChanged = _cachedImage != image;
    final sizeChanged = _cachedIconSize != iconSize;
    final enabledChanged = _cachedEnabled != enabled;
    final colorChanged = !enabled && _cachedIconColor != iconColor;

    if (imageChanged || sizeChanged || enabledChanged || colorChanged) {
      _cachedImage = image;
      _cachedIconSize = iconSize;
      _cachedIconColor = iconColor;
      _cachedEnabled = enabled;
      _cachedImageWidget = null;
    }

    if (_cachedImageWidget != null) {
      return _cachedImageWidget!;
    }

    final imageKey =
        image?.filename ?? image?.imageData?.hashCode.toString() ?? 'no-image';
    final futureKey = enabled
        ? '${imageKey}_${iconSize}_$enabled'
        : '${imageKey}_${iconSize}_${iconColor.value}_$enabled';

    return FutureBuilder<Widget?>(
      key: ValueKey(futureKey),
      future: ImageUtils.buildVImageAsync(
        image,
        size: iconSize,
        color: enabled ? null : iconColor,
        enabled: enabled,
        constraints: constraints,
        useBinaryImage: true,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done &&
            snapshot.hasData) {
          final widget = snapshot.data ?? const SizedBox.shrink();
          _cachedImageWidget = widget;
          return widget;
        }
        return SizedBox(
          width: iconSize,
          height: iconSize,
          child: Center(
            child: SizedBox(
              width: iconSize * widgetTheme.loadingIndicatorSizeFactor,
              height: iconSize * widgetTheme.loadingIndicatorSizeFactor,
              child: CircularProgressIndicator(
                strokeWidth: widgetTheme.loadingIndicatorStrokeWidth,
              ),
            ),
          ),
        );
      },
    );
  }

  VImage? _getImageForState(bool enabled) {
    if (!enabled && state.disabledImage != null) {
      return state.disabledImage;
    }
    if (enabled && _isHovered && state.hotImage != null) {
      return state.hotImage;
    }
    return state.image;
  }

  Widget _buildChildContent({
    required VImage? image,
    required bool enabled,
    required BoxConstraints? constraints,
    required double defaultIconSize,
    required Color iconColor,
    required ToolItemThemeExtension widgetTheme,
    required String? text,
    required TextStyle textStyle,
  }) {
    final hasImage = image != null;
    final hasText = text != null && text.isNotEmpty;
    if (hasImage && hasText) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _buildImageWidget(image, enabled, constraints, defaultIconSize, iconColor, widgetTheme),
          const SizedBox(width: 4),
          Text(text, textAlign: TextAlign.center, style: textStyle),
        ],
      );
    } else if (hasImage) {
      return _buildImageWidget(image, enabled, constraints, defaultIconSize, iconColor, widgetTheme);
    } else if (hasText) {
      return Padding(
        padding: widgetTheme.textPadding,
        child: Center(child: Text(text, textAlign: TextAlign.center, style: textStyle)),
      );
    } else {
      return SizedBox(width: widgetTheme.emptyButtonSize, height: widgetTheme.emptyButtonSize);
    }
  }

  Widget _buildClickableButton({
    required VoidCallback? onTap,
    required bool enabled,
    required ToolItemThemeExtension widgetTheme,
    required Widget child,
    required Color hoverBackgroundColor,
    bool useMouseRegion = true,
  }) {
    Widget button = Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: enabled ? onTap : null,
        splashColor: widgetTheme.hoverColor.withOpacity(
          widgetTheme.splashOpacity,
        ),
        highlightColor: widgetTheme.hoverColor.withOpacity(
          widgetTheme.highlightOpacity,
        ),
        borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
        child: Container(
          padding: widgetTheme.buttonPadding,
          decoration: BoxDecoration(
            color: hoverBackgroundColor,
            borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          ),
          child: child,
        ),
      ),
    );

    if (useMouseRegion) {
      button = MouseRegion(
        onEnter: enabled ? (_) => setState(() => _isHovered = true) : null,
        onExit: enabled ? (_) => setState(() => _isHovered = false) : null,
        child: button,
      );
    }

    return button;
  }

  Widget _buildToolbarButton({
    required BuildContext context,
    required ToolItemThemeExtension widgetTheme,
    required bool enabled,
    required Widget child,
    VoidCallback? onTap,
    Color? backgroundColor,
    BoxConstraints? constraints,
    String? tooltip,
    bool isDropdown = false,
  }) {
    final textColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: enabled
          ? widgetTheme.enabledColor
          : widgetTheme.disabledColor,
    );

    final toolbarTheme = Theme.of(context).extension<ToolBarThemeExtension>();
    final defaultBackgroundColor =
        toolbarTheme?.toolbarBackgroundColor ?? Colors.white;
    final bgColor = getBackgroundColor(
      background: null,
      defaultColor: backgroundColor ?? Colors.transparent,
    );

    Widget hoverableContent;

    if (isDropdown) {
      Widget mainContentButton = Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: enabled
              ? () {
                  setState(() => _isHovered = false);
                  if (onTap != null) {
                    onTap();
                  } else {
                    onPressed();
                  }
                }
              : null,
          splashColor: widgetTheme.hoverColor.withOpacity(
            widgetTheme.splashOpacity,
          ),
          highlightColor: widgetTheme.hoverColor.withOpacity(
            widgetTheme.highlightOpacity,
          ),
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          child: child,
        ),
      );

      Widget dropdownArrow = Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: enabled
              ? () {
                  setState(() => _isHovered = false);
                  openMenu();
                }
              : null,
          splashColor: widgetTheme.hoverColor.withOpacity(
            widgetTheme.splashOpacity,
          ),
          highlightColor: widgetTheme.hoverColor.withOpacity(
            widgetTheme.highlightOpacity,
          ),
          borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
          child: MouseRegion(
            cursor: enabled
                ? SystemMouseCursors.click
                : SystemMouseCursors.basic,
            child: Icon(
              Icons.arrow_drop_down,
              size: widgetTheme.dropdownArrowSize,
              color: textColor,
            ),
          ),
        ),
      );

      Widget separator = Container(
        width: widgetTheme.separatorBarWidth,
        margin: widgetTheme.separatorBarMargin,
        color: _isHovered && enabled
            ? textColor.withOpacity(widgetTheme.separatorOpacity)
            : Colors.transparent,
      );

      hoverableContent = IntrinsicWidth(
        child: IntrinsicHeight(
          child: MouseRegion(
            onEnter: enabled ? (_) => setState(() => _isHovered = true) : null,
            onExit: enabled ? (_) => setState(() => _isHovered = false) : null,
            child: Container(
              padding: widgetTheme.buttonPadding,
              decoration: BoxDecoration(
                color: enabled && _isHovered
                    ? widgetTheme.hoverColor
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(widgetTheme.borderRadius),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [mainContentButton, separator, dropdownArrow],
              ),
            ),
          ),
        ),
      );
    } else {
      hoverableContent = IntrinsicWidth(
        child: IntrinsicHeight(
          child: _buildClickableButton(
            onTap: enabled
                ? () {
                    setState(() => _isHovered = false);
                    if (onTap != null) {
                      onTap();
                    } else {
                      onPressed();
                    }
                  }
                : null,
            enabled: enabled,
            widgetTheme: widgetTheme,
            hoverBackgroundColor: enabled && _isHovered
                ? widgetTheme.hoverColor
                : Colors.transparent,
            child: child,
          ),
        ),
      );
    }

    hoverableContent = Tooltip(
      message: tooltip ?? state.toolTipText ?? '',
      preferBelow: widgetTheme.tooltipPreferBelow,
      verticalOffset: widgetTheme.tooltipVerticalOffset,
      margin: widgetTheme.tooltipMargin,
      waitDuration: widgetTheme.tooltipWaitDuration,
      child: hoverableContent,
    );

    return Container(
      constraints: constraints,
      color: bgColor,
      child: hoverableContent,
    );
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<ToolItemThemeExtension>()!;
    var text = state.text;
    var enabled = state.enabled ?? false;
    var bits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;

    BoxConstraints? constraints;
    if (state.width != null && state.width! > 0) {
      constraints = BoxConstraints(
        minWidth: state.width!.toDouble(),
        maxWidth: state.width!.toDouble(),
      );
    }

    final textColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: enabled
          ? widgetTheme.enabledColor
          : widgetTheme.disabledColor,
    );

    final textStyle = getTextStyle(
      context: context,
      font: null,
      textColor: textColor,
      baseTextStyle: widgetTheme.fontStyle,
    );

    return Container(
      constraints: constraints,
      child: switch (state.style & bits) {
        SWT.CHECK => () {
          final image = _getImageForState(enabled);
          final isChecked = state.selection ?? false;

          Widget iconOrImage;
          if (image != null) {
            iconOrImage = _buildImageWidget(
              image,
              enabled,
              constraints,
              widgetTheme.defaultIconSize,
              textColor,
              widgetTheme,
            );
          } else {
            iconOrImage = Icon(
              isChecked ? Icons.check_box : Icons.check_box_outline_blank,
              size: widgetTheme.iconSize,
              color: textColor,
            );
          }

          final Widget child;
          if (text != null && text.isNotEmpty) {
            child = Row(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                iconOrImage,
                const SizedBox(width: 4),
                Text(text, textAlign: TextAlign.center, style: textStyle),
              ],
            );
          } else {
            child = iconOrImage;
          }

          final toolbarTheme = Theme.of(
            context,
          ).extension<ToolBarThemeExtension>();
          final defaultBackgroundColor =
              toolbarTheme?.compositeBackgroundColor ?? Colors.white;

          return _buildToolbarButton(
            context: context,
            widgetTheme: widgetTheme,
            enabled: enabled,
            backgroundColor: defaultBackgroundColor,
            constraints: constraints,
            onTap: () {
              setState(() {
                state.selection = !isChecked;
                _isHovered = false;
              });
              onPressed();
            },
            child: child,
          );
        }(),
        SWT.RADIO => () {
          final image = _getImageForState(enabled);
          final isSelected = state.selection ?? false;

          Widget iconOrImage;
          if (image != null) {
            iconOrImage = _buildImageWidget(
              image,
              enabled,
              constraints,
              widgetTheme.defaultIconSize,
              textColor,
              widgetTheme,
            );
          } else {
            iconOrImage = Icon(
              isSelected
                  ? Icons.radio_button_checked
                  : Icons.radio_button_unchecked,
              size: widgetTheme.iconSize,
              color: textColor,
            );
          }

          final Widget child;
          if (text != null && text.isNotEmpty) {
            child = Row(
              mainAxisSize: MainAxisSize.min,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                iconOrImage,
                const SizedBox(width: 4),
                Text(text, textAlign: TextAlign.center, style: textStyle),
              ],
            );
          } else {
            child = iconOrImage;
          }

          return _buildToolbarButton(
            context: context,
            widgetTheme: widgetTheme,
            enabled: enabled,
            constraints: constraints,
            onTap: () {
              setState(() {
                state.selection = !isSelected;
                _isHovered = false;
              });
              onPressed();
            },
            child: child,
          );
        }(),
        SWT.DROP_DOWN => () {
          final image = _getImageForState(enabled);
          return _buildToolbarButton(
            context: context,
            widgetTheme: widgetTheme,
            enabled: enabled,
            tooltip: state.toolTipText ?? text ?? '',
            constraints: constraints,
            child: _buildChildContent(
              image: image,
              enabled: enabled,
              constraints: constraints,
              defaultIconSize: widgetTheme.defaultIconSize,
              iconColor: textColor,
              widgetTheme: widgetTheme,
              text: text,
              textStyle: textStyle,
            ),
            isDropdown: true,
          );
        }(),
        SWT.PUSH => () {
          final image = _getImageForState(enabled);
          return _buildToolbarButton(
            context: context,
            widgetTheme: widgetTheme,
            enabled: enabled,
            tooltip: state.toolTipText ?? text ?? '',
            constraints: constraints,
            child: _buildChildContent(
              image: image,
              enabled: enabled,
              constraints: constraints,
              defaultIconSize: widgetTheme.defaultIconSize,
              iconColor: textColor,
              widgetTheme: widgetTheme,
              text: text,
              textStyle: textStyle,
            ),
          );
        }(),
        SWT.SEPARATOR => () {
          return VerticalDivider(
            width: widgetTheme.separatorWidth,
            thickness: widgetTheme.separatorThickness,
            indent: widgetTheme.separatorIndent,
            endIndent: widgetTheme.separatorIndent,
            color: widgetTheme.separatorColor,
          );
        }(),
        _ => () {
          final image = _getImageForState(enabled);
          return _buildToolbarButton(
            context: context,
            widgetTheme: widgetTheme,
            enabled: enabled,
            tooltip: state.toolTipText ?? text ?? '',
            constraints: constraints,
            child: _buildChildContent(
              image: image,
              enabled: enabled,
              constraints: constraints,
              defaultIconSize: widgetTheme.defaultIconSize,
              iconColor: textColor,
              widgetTheme: widgetTheme,
              text: text,
              textStyle: textStyle,
            ),
          );
        }(),
      },
    );
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }

  void openMenu() {
    widget.sendSelectionOpenMenu(state, null);
  }
}
