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
import 'dart:ui';

class ToolItemImpl<T extends ToolItemSwt, V extends VToolItem>
    extends ItemImpl<T, V> {
  bool _isHovered = false;

  /// Helper method to build an image widget from VImage using ImageUtils
  /// Uses FutureBuilder to support async AssetsManager replacement
  Widget _buildImageWidget(VImage? image, bool enabled) {
    return FutureBuilder<Widget?>(
      future: ImageUtils.buildVImageAsync(
        image,
        enabled: enabled,
        constraints: const BoxConstraints(
          minWidth: AppSizes.toolbarMinSize,
          minHeight: AppSizes.toolbarMinSize,
        ),
        useBinaryImage: true,
        renderAsIcon: true,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return snapshot.data ?? const SizedBox.shrink();
        }
        // Show placeholder while loading
        return const SizedBox(
          width: AppSizes.toolbarMinSize,
          height: AppSizes.toolbarMinSize,
          child: Center(
            child: SizedBox(
              width: 12,
              height: 12,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        );
      },
    );
  }

  /// Helper method to get the appropriate image based on state and hover
  VImage? _getImageForState(bool enabled) {
    // Priority order: disabled > hot > normal
    if (!enabled && state.disabledImage != null) {
      return state.disabledImage;
    }
    // HotImage should show when item is enabled AND hovered
    if (enabled && _isHovered && state.hotImage != null) {
      return state.hotImage;
    }
    // Default to normal image
    return state.image;
  }

  /// Helper method to create a reusable toolbar button wrapper
  Widget _buildToolbarButton({
    required bool enabled,
    required Widget child,
    VoidCallback? onTap,
    Color? backgroundColor,
    BoxConstraints? constraints,
    String? tooltip,
    bool isDropdown = false,
  }) {
    return Material(
      color: backgroundColor ?? Colors.transparent,
      borderRadius: BorderRadius.circular(AppSizes.borderRadius),
      child: InkWell(
          onTap: enabled ? (onTap ?? onPressed) : null,
          onHover: (h) => setState(() => _isHovered = h),
        hoverColor: AppColors.getHoverColor(),
        borderRadius: BorderRadius.circular(AppSizes.borderRadius),
        child: ConstrainedBox(
          constraints: constraints ?? AppConstraints.toolbarConstraints,
          child: Tooltip(
            message: tooltip ?? state.toolTipText ?? '',
            preferBelow: false,
            verticalOffset: 0,
            margin: const EdgeInsets.symmetric(horizontal: 20),
            waitDuration: const Duration(seconds: 1),
            child: isDropdown
                ? Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                child,
                GestureDetector(
                  onTap: enabled ? openMenu : null,
                  child: Icon(
                    Icons.arrow_drop_down,
                    size: 12,
                    color: AppColors.getColor(enabled),
                  ),
                ),
              ],
            )
                : child,
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    var text = state.text;

    var enabled = state.enabled ?? false;
    var bits = SWT.PUSH | SWT.CHECK | SWT.RADIO | SWT.SEPARATOR | SWT.DROP_DOWN;

    return Container(
      child: switch (state.style & bits) {
        SWT.CHECK => () {
          final image = _getImageForState(enabled);
          final isChecked = state.selection ?? false;

          Widget child;
          if (image != null) {
            child = _buildImageWidget(image, enabled);
          } else {
            child = Icon(
              isChecked ? Icons.check_box : Icons.check_box_outline_blank,
              size: AppSizes.toolbarIconLarge,
              color: AppColors.getColor(enabled),
            );
          }

          return _buildToolbarButton(
            enabled: enabled,
            backgroundColor:
            isChecked ? AppColors.getHoverColor() : Colors.transparent,
            onTap: () {
              onPressed();
              setState(() => state.selection = !isChecked);
            },
            child: child,
          );
        }(),
        SWT.RADIO => () {
          final image = _getImageForState(enabled);
          final isSelected = state.selection ?? false;

          Widget child;
          if (image != null) {
            child = _buildImageWidget(image, enabled);
          } else {
            child = Icon(
              isSelected
                  ? Icons.radio_button_checked
                  : Icons.radio_button_unchecked,
              size: AppSizes.toolbarIconLarge,
              color: AppColors.getColor(enabled),
            );
          }

          return _buildToolbarButton(
            enabled: enabled,
            onTap: () {
              onPressed();
              setState(() => state.selection = !isSelected);
            },
            child: child,
          );
        }(),
        SWT.DROP_DOWN => () {
          final image = _getImageForState(enabled);

          if (image != null) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: _buildImageWidget(image, enabled),
              isDropdown: true,
            );
          } else if (text != null && text.isNotEmpty) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: Center(
                  child: Text(
                    text!,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: AppSizes.toolbarTextSize,
                      color: AppColors.getColor(enabled),
                    ),
                  ),
                ),
              ),
              isDropdown: true,
            );
          }
        }(),
        SWT.PUSH => () {
          final image = _getImageForState(enabled);

          if (image != null) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: _buildImageWidget(image, enabled),
            );
          } else if (text != null && text.isNotEmpty) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: Center(
                  child: Text(
                    text!,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: AppSizes.toolbarTextSize,
                      color: AppColors.getColor(enabled),
                    ),
                  ),
                ),
              ),
            );
          }
        }(),
        SWT.SEPARATOR => () {
          return const VerticalDivider(
            width: AppSizes.separatorWidth,
            thickness: AppSizes.separatorThickness,
            indent: AppSizes.separatorIndent,
            endIndent: AppSizes.separatorIndent,
          );
        }(),
        _ => () {
          final image = _getImageForState(enabled);

          if (image != null) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: _buildImageWidget(image, enabled),
            );
          } else if (text != null && text.isNotEmpty) {
            return _buildToolbarButton(
              enabled: enabled,
              tooltip: state.toolTipText ?? text ?? '',
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: Center(
                  child: Text(
                    text,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: AppSizes.toolbarTextSize,
                      color: AppColors.getColor(enabled),
                    ),
                  ),
                ),
              ),
            );
          }
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