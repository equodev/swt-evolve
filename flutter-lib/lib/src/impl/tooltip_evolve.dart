import 'package:flutter/material.dart';
import '../gen/swt.dart';
import '../gen/tooltip.dart';
import '../gen/widget.dart';
import '../styles.dart';
import 'color_utils.dart';
import 'widget_config.dart';

class ToolTipImpl<T extends ToolTipSwt, V extends VToolTip>
    extends WidgetSwtState<T, V> {
  OverlayEntry? _overlayEntry;
  bool _isVisible = false;

  @override
  void initState() {
    super.initState();
    _updateVisibility();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    _updateVisibility();
  }

  void _updateVisibility() {
    final shouldBeVisible = state.visible ?? false;

    if (shouldBeVisible && !_isVisible) {
      _showTooltip();
    } else if (!shouldBeVisible && _isVisible) {
      _hideTooltip();
    }
  }

  void _showTooltip() {
    if (_overlayEntry != null) {
      return; // Already showing
    }

    _isVisible = true;
    _overlayEntry = _createOverlayEntry();

    WidgetsBinding.instance.addPostFrameCallback((_) {
      final overlay = Overlay.of(context);
      overlay.insert(_overlayEntry!);

      // Auto-hide if autoHide is enabled
      if (state.autoHide ?? true) {
        Future.delayed(const Duration(seconds: 5), () {
          _hideTooltip();
        });
      }
    });
  }

  void _hideTooltip() {
    if (_overlayEntry == null) {
      return;
    }

    _overlayEntry?.remove();
    _overlayEntry = null;
    _isVisible = false;
  }

  OverlayEntry _createOverlayEntry() {
    return OverlayEntry(
      builder: (context) => Positioned(
        top: 100, // Default position, could be enhanced with bounds
        left: 100,
        child: Material(
          color: Colors.transparent,
          child: _TooltipContent(
            message: state.message,
            text: state.text,
            style: state.style,
            onDismiss: _handleDismiss,
          ),
        ),
      ),
    );
  }

  void _handleDismiss() {
    _hideTooltip();
    widget.sendSelectionSelection(state, null);
  }

  @override
  Widget build(BuildContext context) {
    // ToolTip is an overlay-based widget, so we return an empty container
    // The actual tooltip is rendered via OverlayEntry
    return const SizedBox.shrink();
  }

  @override
  void dispose() {
    _hideTooltip();
    super.dispose();
  }
}

class _TooltipContent extends StatelessWidget {
  final String? message;
  final String? text;
  final int style;
  final VoidCallback onDismiss;

  const _TooltipContent({
    this.message,
    this.text,
    required this.style,
    required this.onDismiss,
  });

  IconData? _getIcon() {
    if (style.has(SWT.ICON_INFORMATION)) {
      return Icons.info_outline;
    } else if (style.has(SWT.ICON_WARNING)) {
      return Icons.warning_amber_outlined;
    } else if (style.has(SWT.ICON_ERROR)) {
      return Icons.error_outline;
    }
    return null;
  }

  Color _getIconColor() {
    if (style.has(SWT.ICON_INFORMATION)) {
      return Colors.blue;
    } else if (style.has(SWT.ICON_WARNING)) {
      return Colors.orange;
    } else if (style.has(SWT.ICON_ERROR)) {
      return Colors.red;
    }
    return getForeground();
  }

  Color _getBackgroundColor() {
    if (style.has(SWT.ICON_INFORMATION)) {
      return getCurrentTheme()
          ? const Color(0xFF1E3A5F)
          : const Color(0xFFE3F2FD);
    } else if (style.has(SWT.ICON_WARNING)) {
      return getCurrentTheme()
          ? const Color(0xFF4A3F1F)
          : const Color(0xFFFFF3E0);
    } else if (style.has(SWT.ICON_ERROR)) {
      return getCurrentTheme()
          ? const Color(0xFF4A1F1F)
          : const Color(0xFFFFEBEE);
    }
    return getBackground();
  }

  @override
  Widget build(BuildContext context) {
    final displayMessage = message ?? '';
    final displayTitle = text ?? '';
    final backgroundColor = _getBackgroundColor();
    final foregroundColor = getForeground();
    final borderColor = getBorderColor();
    final icon = _getIcon();
    final iconColor = _getIconColor();
    final isBalloon = style.has(SWT.BALLOON);

    return GestureDetector(
      onTap: onDismiss,
      child: MouseRegion(
        cursor: SystemMouseCursors.click,
        child: Container(
          constraints: const BoxConstraints(
            minWidth: 100,
            maxWidth: 350,
            minHeight: 40,
          ),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: backgroundColor,
            border: Border.all(
              color: borderColor,
              width: 1,
            ),
            borderRadius: isBalloon
                ? BorderRadius.circular(12)
                : BorderRadius.circular(4),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.3),
                blurRadius: 10,
                offset: const Offset(0, 3),
              ),
            ],
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (icon != null) ...[
                Icon(
                  icon,
                  color: iconColor,
                  size: 24,
                ),
                const SizedBox(width: 12),
              ],
              Flexible(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (displayTitle.isNotEmpty)
                      Text(
                        displayTitle,
                        style: TextStyle(
                          color: foregroundColor,
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    if (displayTitle.isNotEmpty && displayMessage.isNotEmpty)
                      const SizedBox(height: 4),
                    if (displayMessage.isNotEmpty)
                      Text(
                        displayMessage,
                        style: TextStyle(
                          color: foregroundColor,
                          fontSize: 12,
                        ),
                        maxLines: 10,
                        overflow: TextOverflow.ellipsis,
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
