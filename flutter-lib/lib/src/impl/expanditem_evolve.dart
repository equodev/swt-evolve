import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/expanditem.dart';
import '../gen/widgets.dart';
import '../impl/item_evolve.dart';
import 'color_utils.dart';

class ExpandItemImpl<T extends ExpandItemSwt, V extends VExpandItem>
    extends ItemImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    final textColor = getForeground();
    final backgroundColor = getBackground();

    Widget? content;
    if (state.control != null) {
      content = mapWidgetFromValue(state.control!);
    }

    return Material(
      color: Colors.transparent,
      child: Container(
        width: double.infinity,
        height: (state.height != null && state.height! > 0)
            ? state.height!.toDouble()
            : null,
        decoration: BoxDecoration(
          color: backgroundColor,
          border: Border.all(
            color: getBorderColor(),
            width: 1,
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            if (state.text != null)
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                child: Text(
                  state.text!,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.normal,
                    color: textColor,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            if (content != null)
              Expanded(
                child: Material(
                  color: backgroundColor,
                  child: Container(
                    width: double.infinity,
                    padding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                    alignment: Alignment.topLeft,
                    child: content,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
