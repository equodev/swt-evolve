import 'package:fluent_ui/fluent_ui.dart';
import 'package:swtflutter/src/swt/menuitem.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/menu.dart';
import '../swt/widget.dart';

class MenuImpl<T extends MenuSwt, V extends MenuValue>
    extends WidgetSwtState<T, V> {
  @override
  Widget build(BuildContext context) {
    List<MenuFlyoutItemBase> menuItems =
        getItems(state.children?.cast<MenuItemValue>());
    return MenuFlyout(items: menuItems);
  }

  List<MenuFlyoutItemBase> getItems(List<MenuItemValue>? items) {
    if (items == null) {
      return [];
    }

    List<MenuFlyoutItemBase> menuItems = [];
    for (MenuItemValue menuItem in items) {
      // Sub Menu item
      if (menuItem.menu != null) {
        menuItems.add(MenuFlyoutSubItem(
            text: Text(menuItem.text ?? ""),
            items: (_) =>
                getItems(menuItem.menu?.children?.cast<MenuItemValue>())));
      } else {
        // Menu item
        menuItems.add(MenuFlyoutItem(
          leading: const Icon(FluentIcons.share),
          text: Text(menuItem.text ?? ""),
          onPressed: () {
            MenuItemSwt mItem =
                mapWidgetFromValue(menuItem) as MenuItemSwt<MenuItemValue>;
            mItem.sendSelectionSelection(mItem.value, menuItem.id);
          }, //Flyout.of(context).close,
        ));
      }
    }

    return menuItems;
  }
}
