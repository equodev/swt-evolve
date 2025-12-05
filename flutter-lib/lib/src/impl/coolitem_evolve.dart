import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../gen/swt.dart';
import '../gen/coolitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/item_evolve.dart';
import 'color_utils.dart';

class CoolItemImpl<T extends CoolItemSwt, V extends VCoolItem>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final control = state.control;
    final text = state.text;
    final preferredSize = state.preferredSize;
    final minimumSize = state.minimumSize;
    final controlBounds = control?.bounds;
    final width = controlBounds?.width?.toDouble() ?? preferredSize?.x?.toDouble();

    return _buildCoolItemWrapper(
      child: _buildCoolItemContent(control, text),
      width: width,
    );
  }

  Widget _buildCoolItemContent(VControl? control, String? text) {
    if (control != null) {
      return mapWidgetFromValue(control);
    } else if (text != null && text.isNotEmpty) {
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
        child: Text(
          text,
          style: TextStyle(
            fontSize: AppSizes.toolbarTextSize,
            color: getForeground(),
          ),
        ),
      );
    } else {
      return const SizedBox.shrink();
    }
  }

  Widget _buildCoolItemWrapper({
    required Widget child,
    double? width,
  }) {
    final alignedChild = Align(
      alignment: Alignment.center,
      child: child,
    );

    if (width != null && width > 0) {
      return SizedBox(
        width: width,
        child: alignedChild,
      );
    }

    return alignedChild;
  }

  void onPressed() {
    widget.sendSelectionSelection(state, null);
  }

  void onDoubleClick() {
    widget.sendSelectionDefaultSelection(state, null);
  }
}
