import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/material.dart' show InputDecorator, InputDecoration, OutlineInputBorder, Color;
import 'package:flutter/widgets.dart';

import '../swt/group.dart';
import '../swt/widget.dart';

import '../impl/composite_impl.dart';
import '../widgets.dart';

class GroupImpl<T extends GroupSwt, V extends GroupValue>
    extends CompositeImpl<T, V> {
  @override
  Widget build(BuildContext context) {
    // var children = <Widget>[
    //   Text(state.text ?? ""),
    //   Divider(),
    // ];
    // if (state.children != null) {
    //   var groupChildren = state.children!.toDart;
    //   children.add(mapLayout(state, state.layout, groupChildren));
    // }
    // return Column(
    //   mainAxisSize: MainAxisSize.min,
    //   children: children,
    // );
    Widget? child;
    if (state.children != null) {
      var groupChildren = state.children!;
      child = mapLayout(state, state.layout, groupChildren);
    }
    final theme = FluentTheme.of(context);
    return InputDecorator(
      decoration: InputDecoration(
        labelText: state.text ?? "",
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(7.0),
        ),
      ),
      child: child,
    );
  }
}
