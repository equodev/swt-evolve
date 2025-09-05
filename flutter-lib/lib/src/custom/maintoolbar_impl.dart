import 'package:flutter/material.dart';

import '../gen/composite.dart';
import '../gen/toolitem.dart';
import '../gen/widgets.dart';

import 'dart:io';
import 'package:flutter_svg/flutter_svg.dart';
import '../impl/widget_config.dart';
import '../impl/composite_evolve.dart';
import '../impl/scrollable_evolve.dart';
import '../gen/toolbar.dart';

class MainToolbarSwt<V extends VComposite> extends CompositeSwt<V> {
  const MainToolbarSwt({super.key, required super.value});

  @override
  State createState() =>
      MainToolbarImpl<MainToolbarSwt<VComposite>, VComposite>();
}

class MainToolbarImpl<T extends MainToolbarSwt, V extends VComposite>
    extends CompositeImpl<T, V> {
  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor =
        useDarkTheme ? const Color(0xFF2A2A2A) : const Color(0xFFF5F5F5);
    final Color separatorColor =
        useDarkTheme ? const Color(0xFF3B3B3B) : const Color(0xFFDDDDDD);

    return super.wrap(
      Container(
        height: 40,
        color: backgroundColor,
        child: Row(
          children: [
            ElevatedButton(
              onPressed: () => print('Test button works!'),
              child: Text('Test'),
            ),
            const Spacer(),
            Container(
              height: 22,
              width: 1.5,
              margin: const EdgeInsets.symmetric(vertical: 9),
              color: separatorColor,
            ),
            Container(
              height: 22,
              width: 1.5,
              margin: const EdgeInsets.symmetric(horizontal: 2, vertical: 9),
              color: separatorColor,
            ),
          ],
        ),
      ),
    );
  }
}
