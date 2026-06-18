import 'package:flutter/material.dart';
import '../gen/swt.dart';
import '../gen/tooltip.dart';
import '../gen/widget.dart';
import '../styles.dart';
import '../theme/theme_extensions/tooltip_theme_extension.dart';
import 'widget_config.dart';
import 'utils/pointer.dart';

class ToolTipImpl<T extends ToolTipSwt, V extends VToolTip>
    extends WidgetSwtState<T, V> {
  bool _animVisible = false;
  bool _rendered = false;
  bool _dismissed = false;
  bool _prevEffectiveVisible = false;

  TooltipThemeExtension get _theme =>
      Theme.of(context).extension<TooltipThemeExtension>()!;

  @override
  void initState() {
    super.initState();
    if (state.visible ?? false) {
      _rendered = true;
      _prevEffectiveVisible = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_dismissed) setState(() => _animVisible = true);
      });
    }
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final visible = state.visible ?? false;
    if (!visible) _dismissed = false;
    final effectiveVisible = visible && !_dismissed;

    if (effectiveVisible && !_prevEffectiveVisible) {
      _rendered = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_dismissed) setState(() => _animVisible = true);
      });
    } else if (!effectiveVisible && _prevEffectiveVisible) {
      setState(() => _animVisible = false);
    }
    _prevEffectiveVisible = effectiveVisible;
  }

  void _onTap() {
    _dismissed = true;
    setState(() => _animVisible = false);
    widget.sendSelectionSelection(state, null);
  }

  @override
  Widget build(BuildContext context) {
    if (!_rendered) return const SizedBox.shrink();
    final theme = _theme;
    final x = (state.location?.x ?? 0).toDouble();
    final y = (state.location?.y ?? 0).toDouble();

    return Stack(children: [
      Positioned(
        left: x,
        top: y,
        child: AnimatedOpacity(
          opacity: _animVisible ? 1.0 : 0.0,
          duration: _animVisible ? theme.fadeInDuration : theme.fadeOutDuration,
          curve: _animVisible ? Curves.easeOut : Curves.easeIn,
          onEnd: () {
            if (!_animVisible && mounted) setState(() => _rendered = false);
          },
          child: AnimatedSlide(
            offset: _animVisible ? Offset.zero : Offset(0, theme.slideOffsetY),
            duration: _animVisible ? theme.fadeInDuration : theme.fadeOutDuration,
            curve: _animVisible ? Curves.easeOut : Curves.easeIn,
            child: IgnorePointer(
              ignoring: !_animVisible,
              child: Material(
                color: Colors.transparent,
                child: pointerInterceptor(_TooltipContent(
                  message: state.message,
                  text: state.text,
                  style: state.style,
                  onTap: _onTap,
                )),
              ),
            ),
          ),
        ),
      ),
    ]);
  }
}

class _TooltipContent extends StatelessWidget {
  final String? message;
  final String? text;
  final int style;
  final VoidCallback onTap;

  const _TooltipContent({
    this.message,
    this.text,
    required this.style,
    required this.onTap,
  });

  IconData? _getIcon() {
    if (style.has(SWT.ICON_INFORMATION)) return Icons.info_outline;
    if (style.has(SWT.ICON_WARNING)) return Icons.warning_amber_outlined;
    if (style.has(SWT.ICON_ERROR)) return Icons.error_outline;
    return null;
  }

  Color _iconColor(TooltipThemeExtension theme) {
    if (style.has(SWT.ICON_INFORMATION)) return theme.informationIconColor;
    if (style.has(SWT.ICON_WARNING)) return theme.warningIconColor;
    if (style.has(SWT.ICON_ERROR)) return theme.errorIconColor;
    return theme.textColor;
  }

  Color _backgroundColor(TooltipThemeExtension theme) {
    if (style.has(SWT.ICON_INFORMATION)) return theme.informationBackgroundColor;
    if (style.has(SWT.ICON_WARNING)) return theme.warningBackgroundColor;
    if (style.has(SWT.ICON_ERROR)) return theme.errorBackgroundColor;
    return theme.backgroundColor;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<TooltipThemeExtension>()!;
    final isBalloon = style.has(SWT.BALLOON);
    final icon = _getIcon();
    final displayTitle = text ?? '';
    final displayMessage = message ?? '';

    return GestureDetector(
      onTap: onTap,
      child: MouseRegion(
        cursor: SystemMouseCursors.click,
        child: Container(
          constraints: BoxConstraints(
            minWidth: theme.minWidth,
            maxWidth: theme.maxWidth,
            minHeight: theme.minHeight,
          ),
          padding: theme.padding,
          decoration: BoxDecoration(
            color: _backgroundColor(theme),
            border: Border.all(color: theme.borderColor, width: theme.borderWidth),
            borderRadius: BorderRadius.circular(
              isBalloon ? theme.balloonBorderRadius : theme.borderRadius,
            ),
            boxShadow: [
              BoxShadow(
                color: theme.shadowColor,
                blurRadius: theme.shadowBlurRadius,
                offset: Offset(0, theme.shadowOffsetY),
              ),
            ],
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (icon != null) ...[
                Icon(icon, color: _iconColor(theme), size: theme.iconSize),
                SizedBox(width: theme.iconSpacing),
              ],
              Flexible(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (displayTitle.isNotEmpty)
                      Text(displayTitle, style: theme.titleTextStyle),
                    if (displayTitle.isNotEmpty && displayMessage.isNotEmpty)
                      SizedBox(height: theme.titleMessageSpacing),
                    if (displayMessage.isNotEmpty)
                      Text(
                        displayMessage,
                        style: theme.messageTextStyle,
                        maxLines: theme.messageMaxLines,
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
