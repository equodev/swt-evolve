import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/control.dart';
import '../gen/coolitem.dart';
import '../gen/widgets.dart';
import '../impl/item_evolve.dart';
import '../theme/theme_extensions/coolitem_theme_extension.dart';

class CoolItemImpl<T extends CoolItemSwt, V extends VCoolItem>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<CoolItemThemeExtension>()!;
    final control = state.control;
    final text = state.text;
    final width = _getWidth();
    final constraints = _getConstraints(width);

    return _CoolItemContainer(
      constraints: constraints,
      onPressed: _onPressed,
      onDoubleClick: _onDoubleClick,
      child: _buildContent(context, theme, control, text),
    );
  }

  Widget _buildContent(
    BuildContext context,
    CoolItemThemeExtension theme,
    VControl? control,
    String? text,
  ) {
    if (control != null) {
      return mapWidgetFromValue(control);
    }

    if (text != null && text.isNotEmpty) {
      return Padding(
        padding: theme.contentPadding,
        child: Text(
          text,
          style: theme.textStyle,
        ),
      );
    }

    return const SizedBox.shrink();
  }

  double? _getWidth() {
    final controlBounds = state.control?.bounds;
    final preferredSize = state.preferredSize;

    return controlBounds?.width?.toDouble() ?? preferredSize?.x?.toDouble();
  }

  BoxConstraints? _getConstraints(double? width) {
    if (width != null && width > 0) {
      return BoxConstraints(
        minWidth: width,
        maxWidth: width,
      );
    }
    return null;
  }

  void _onPressed() {
    widget.sendSelectionSelection(state, null);
  }

  void _onDoubleClick() {
    widget.sendSelectionDefaultSelection(state, null);
  }
}

class _CoolItemContainer extends StatelessWidget {
  final BoxConstraints? constraints;
  final VoidCallback onPressed;
  final VoidCallback onDoubleClick;
  final Widget child;

  const _CoolItemContainer({
    required this.constraints,
    required this.onPressed,
    required this.onDoubleClick,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    Widget content = child;

    if (constraints != null) {
      content = ConstrainedBox(
        constraints: constraints!,
        child: content,
      );
    }

    return GestureDetector(
      onTap: onPressed,
      onDoubleTap: onDoubleClick,
      child: content,
    );
  }
}
