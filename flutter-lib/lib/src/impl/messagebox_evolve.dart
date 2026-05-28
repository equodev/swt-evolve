import 'package:flutter/material.dart';
import '../comm/comm.dart';
import '../gen/messagebox.dart';
import '../gen/swt.dart';
import '../theme/theme_extensions/button_theme_extension.dart';
import '../theme/theme_extensions/message_box_theme_extension.dart';

void showMessageBoxDialog(
  BuildContext context,
  VMessageBox value,
  int dialogId,
) {
  showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (ctx) => _MessageBoxDialog(
      style: value.style ?? 0,
      title: value.text ?? '',
      message: value.message ?? '',
      dialogId: dialogId,
    ),
  );
}

class _MessageBoxDialog extends StatelessWidget {
  final int style;
  final String title;
  final String message;
  final int dialogId;

  const _MessageBoxDialog({
    required this.style,
    required this.title,
    required this.message,
    required this.dialogId,
  });

  @override
  Widget build(BuildContext context) {
    final msgTheme = Theme.of(context).extension<MessageBoxThemeExtension>()!;
    final btnTheme = Theme.of(context).extension<ButtonThemeExtension>()!;

    final buttons = _buildButtons(context, style, dialogId, msgTheme, btnTheme);

    return Dialog(
      backgroundColor: msgTheme.backgroundColor,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(msgTheme.borderRadius),
      ),
      child: ConstrainedBox(
        constraints: BoxConstraints(minWidth: msgTheme.minWidth, maxWidth: msgTheme.maxWidth),
        child: Padding(
          padding: msgTheme.padding,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  _buildIcon(style, msgTheme),
                  SizedBox(width: msgTheme.iconTitleSpacing),
                  Expanded(
                    child: Text(
                      title,
                      style: msgTheme.titleStyle ?? const TextStyle(),
                    ),
                  ),
                ],
              ),
              if (message.isNotEmpty) ...[
                SizedBox(height: msgTheme.titleMessageSpacing),
                Padding(
                  padding: EdgeInsets.only(left: msgTheme.messageIndent),
                  child: Text(
                    message,
                    style: msgTheme.messageStyle ?? const TextStyle(),
                  ),
                ),
              ],
              SizedBox(height: msgTheme.contentButtonsSpacing),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: _intersperse(buttons, SizedBox(width: msgTheme.buttonSpacing)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

Widget _buildIcon(int style, MessageBoxThemeExtension theme) {
  const iconMask =
      SWT.ICON_ERROR | SWT.ICON_WARNING | SWT.ICON_INFORMATION | SWT.ICON_QUESTION | SWT.ICON_WORKING;
  switch (style & iconMask) {
    case SWT.ICON_ERROR:
      return Icon(Icons.error, color: theme.iconErrorColor);
    case SWT.ICON_WARNING:
      return Icon(Icons.warning, color: theme.iconWarningColor);
    case SWT.ICON_QUESTION:
      return Icon(Icons.help, color: theme.iconQuestionColor);
    case SWT.ICON_WORKING:
      return Icon(Icons.hourglass_empty, color: theme.iconInfoColor);
    case SWT.ICON_INFORMATION:
    default:
      return Icon(Icons.info, color: theme.iconInfoColor);
  }
}

List<Widget> _buildButtons(
  BuildContext context,
  int style,
  int dialogId,
  MessageBoxThemeExtension msgTheme,
  ButtonThemeExtension btnTheme,
) {
  primary(String label, int swtId) =>
      _MessageBoxButton(label: label, swtId: swtId, dialogId: dialogId, isPrimary: true, btnTheme: btnTheme);
  secondary(String label, int swtId) =>
      _MessageBoxButton(label: label, swtId: swtId, dialogId: dialogId, isPrimary: false, btnTheme: btnTheme);

  const buttonMask = SWT.OK | SWT.CANCEL | SWT.YES | SWT.NO | SWT.ABORT | SWT.RETRY | SWT.IGNORE;
  return switch (style & buttonMask) {
    SWT.OK => [primary('OK', SWT.OK)],
    (SWT.OK | SWT.CANCEL) => [secondary('Cancel', SWT.CANCEL), primary('OK', SWT.OK)],
    (SWT.YES | SWT.NO) => [secondary('No', SWT.NO), primary('Yes', SWT.YES)],
    (SWT.YES | SWT.NO | SWT.CANCEL) => [
        secondary('Cancel', SWT.CANCEL),
        secondary('No', SWT.NO),
        primary('Yes', SWT.YES),
      ],
    (SWT.RETRY | SWT.CANCEL) => [secondary('Cancel', SWT.CANCEL), primary('Retry', SWT.RETRY)],
    (SWT.ABORT | SWT.RETRY | SWT.IGNORE) => [
        secondary('Abort', SWT.ABORT),
        secondary('Retry', SWT.RETRY),
        primary('Ignore', SWT.IGNORE),
      ],
    _ => [primary('OK', SWT.OK)],
  };
}

List<Widget> _intersperse(List<Widget> items, Widget separator) {
  final result = <Widget>[];
  for (var i = 0; i < items.length; i++) {
    if (i > 0) result.add(separator);
    result.add(items[i]);
  }
  return result;
}

class _MessageBoxButton extends StatefulWidget {
  final String label;
  final int swtId;
  final int dialogId;
  final bool isPrimary;
  final ButtonThemeExtension btnTheme;

  const _MessageBoxButton({
    required this.label,
    required this.swtId,
    required this.dialogId,
    required this.isPrimary,
    required this.btnTheme,
  });

  @override
  State<_MessageBoxButton> createState() => _MessageBoxButtonState();
}

class _MessageBoxButtonState extends State<_MessageBoxButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final theme = widget.btnTheme;

    final bgColor = _isHovering
        ? (widget.isPrimary ? theme.pushButtonHoverColor : theme.secondaryButtonHoverColor)
        : (widget.isPrimary ? theme.pushButtonColor : theme.secondaryButtonColor);

    final textColor = widget.isPrimary ? theme.pushButtonTextColor : theme.secondaryButtonTextColor;

    final borderColor = widget.isPrimary ? theme.pushButtonBorderColor : theme.secondaryButtonBorderColor;

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovering = true),
      onExit: (_) => setState(() => _isHovering = false),
      child: Material(
        color: bgColor,
        borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
        child: InkWell(
          onTap: () {
            Navigator.pop(context);
            EquoCommService.sendPayload('MessageBox/${widget.dialogId}/close', '${widget.swtId}');
          },
          borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
          splashColor: theme.splashColor,
          highlightColor: theme.highlightColor,
          child: AnimatedContainer(
            duration: theme.buttonPressDelay,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
              border: Border.all(
                color: borderColor,
                width: theme.pushButtonBorderWidth,
              ),
            ),
            padding: theme.pushButtonPadding,
            child: Text(
              widget.label,
              style: (theme.pushButtonFontStyle ?? const TextStyle()).copyWith(color: textColor),
            ),
          ),
        ),
      ),
    );
  }
}
