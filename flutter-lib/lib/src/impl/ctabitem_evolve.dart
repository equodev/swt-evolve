import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import '../gen/ctabitem.dart';
import '../gen/widget.dart';
import '../impl/item_evolve.dart';
import 'icons_map.dart';
import 'widget_config.dart';

class CTabItemImpl<T extends CTabItemSwt, V extends VCTabItem>
    extends ItemImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    return buildTabItemContent(context);
  }

  Widget buildTabItemContent(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final textColor = isDark ? Colors.grey.shade300 : Colors.grey.shade800;
    final iconColor = isDark ? Colors.grey.shade400 : Colors.grey.shade700;

    return Padding(
      padding: const EdgeInsets.only(right: 2.0),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // if (state.image != null)
          //   Padding(
          //     padding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
          //     child: !materialIconMap.containsKey(state.image)
          //         ? Image.file(
          //       File(state.image!),
          //       width: 16,
          //       height: 16,
          //     )
          //         : Icon(
          //       getMaterialIconByName(state.image!),
          //       size: 16,
          //       color: iconColor,
          //     ),
          //   ),
          Padding(
            padding: const EdgeInsets.only(bottom: 2.0),
            child: Text(
              state.text ?? "",
              style: TextStyle(
                fontSize: 12,
                color: textColor,
              ),
            ),
          ),
          // if (state.showClose == true) ...[
          //   const SizedBox(width: 6),
          //   MouseRegion(
          //     cursor: SystemMouseCursors.click,
          //     child: GestureDetector(
          //       onTap: () {
          //         // Implementar cierre de la pestaña
          //         onCloseRequest();
          //       },
          //       child: Padding(
          //         padding: const EdgeInsets.only(bottom: 1.0),
          //         child: Icon(
          //           Icons.close,
          //           size: 14,
          //           color: iconColor,
          //         ),
          //       ),
          //     ),
          //   ),
          // ],
        ],
      ),
    );
  }

  void onCloseRequest() {
  }

  void onTabSelected() {
  }
}
