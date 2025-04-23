import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/tabfolder.dart';
import '../swt/tabitem.dart';
import '../swt/widget.dart';

import '../impl/composite_impl.dart';

class TabFolderImpl<T extends TabFolderSwt, V extends TabFolderValue>
    extends CompositeImpl<T, V> {
  late List<Tab> tabs;

  @override
  void initState() {
    super.initState();
    tabs = tabList();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    tabs = tabList();
  }

  @override
  Widget build(BuildContext context) {
    return TabView(
        closeButtonVisibility: CloseButtonVisibilityMode.never,
        tabWidthBehavior: TabWidthBehavior.sizeToContent,
        showScrollButtons: true,
        currentIndex: state.selectionIndex ?? 0,
        onChanged: (index) {
          setState(() => state.selectionIndex = index);
          widget.sendSelectionSelection(state, index);
        },
        tabs: tabs);
  }

  List<Tab> tabList() {
    if (state.children == null) {
      return <Tab>[];
    }
    return state.children!
        .map((e) => e as TabItemValue)
        .map((e) => Tab(text: Text(e.text ?? ""), body: tabBody(e)))
        .toList();
  }

  Widget tabBody(TabItemValue e) {
    if (e.children != null) {
      return mapWidgetFromValue(e.children!.first);
    }
    return Container();
  }
}
