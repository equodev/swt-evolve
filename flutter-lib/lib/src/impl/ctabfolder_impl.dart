import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/ctabitem.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/ctabfolder.dart';

import '../impl/composite_impl.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends CTabFolderValue>
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
        .map((e) => e as CTabItemValue)
        .map((e) => Tab(text: Text(e.text ?? ""), body: tabBody(e)))
        .toList();
  }

  Widget tabBody(CTabItemValue e) {
    if (e.children != null && e.children!.isNotEmpty) {
      return mapWidgetFromValue(e.children!.first);
    }
    return Container();
  }
}
