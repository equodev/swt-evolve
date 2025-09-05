import 'package:flutter/material.dart';
import '../swt/swt.dart';
import '../swt/label.dart';
import '../impl/control_impl.dart';
import 'widget_config.dart';
import 'package:swtflutter/src/styles.dart';
import 'separatorlabel.dart';

class LabelImpl<T extends LabelSwt<V>, V extends LabelValue>
    extends ControlImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final text = state.text ?? '';
    final enabled = state.enabled ?? true;

    Widget child;

    if (state.style.has(SWT.SEPARATOR)) {
      child = SeparatorLabel(
        direction:
            state.style.has(SWT.VERTICAL) ? Axis.vertical : Axis.horizontal,
        useDarkTheme: useDarkTheme,
      );
    } else {
      child = TextLabel(
        text: text,
        alignment: _getAlignment(),
        wrap: state.style.has(SWT.WRAP),
        useDarkTheme: useDarkTheme,
        vertical: state.style.has(SWT.VERTICAL),
      );
    }

    return MouseRegion(
      onEnter: (_) => handleMouseEnter(),
      onExit: (_) => handleMouseExit(),
      child: Focus(
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            handleFocusIn();
          } else {
            handleFocusOut();
          }
        },
        child: Opacity(
          opacity: enabled ? 1.0 : 0.5,
          child: child,
        ),
      ),
    );
  }

  TextAlign _getAlignment() {
    if (state.style.has(SWT.CENTER)) return TextAlign.center;
    if (state.style.has(SWT.RIGHT)) return TextAlign.right;
    return TextAlign.left;
  }

  void handleMouseEnter() {
    widget.sendMouseTrackMouseEnter(state, null);
  }

  void handleMouseExit() {
    widget.sendMouseTrackMouseExit(state, null);
  }

  void handleFocusIn() {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut() {
    widget.sendFocusFocusOut(state, null);
  }
}
