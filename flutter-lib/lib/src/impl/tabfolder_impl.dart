import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/widgets.dart';

import '../swt/tabfolder.dart';
import '../swt/tabitem.dart';

import '../impl/composite_impl.dart';
import 'icons_map.dart';

class TabFolderImpl<T extends TabFolderSwt, V extends TabFolderValue>
    extends CompositeImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
  }

  @override
  void extraSetState() {
    super.extraSetState();
  }

  @override
  Widget build(BuildContext context) {
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    return Column(
      children: [
        CustomTabBar(
          tabs: tabItems,
          initialSelectedIndex: _selectedIndex,
          useDarkTheme: useDarkTheme,
          onTabChanged: (index) {
            setState(() {
              _selectedIndex = index;
            });
          },
        ),
        Expanded(
          child: IndexedStack(
            index: _selectedIndex,
            children: tabBodies,
          ),
        ),
      ],
    );
  }

  List<TabItem> getTabItems() {
    if (state.children == null) {
      return [];
    }
    return state.children!
        .whereType<TabItemValue>()
        .map((tabItem) => getWidgetForTabItem(tabItem))
        .toList();
  }

  TabItem getWidgetForTabItem(TabItemValue tabItem) {
    // Create the TabItem instance using the TabItemSwt to render
    final tabItemWidget = TabItemSwt(value: tabItem);

    return TabItem(
      label: tabItem.text ?? "",
      image: tabItem.image,
      showCloseButton: true,
      customContent: tabItemWidget,
    );
  }

  List<Widget> getTabBodies() {
    if (state.children == null) {
      return <Widget>[];
    }
    return state.children!
        .whereType<TabItemValue>()
        .map((e) => tabBody(e))
        .toList();
  }

  Widget tabBody(TabItemValue e) {
    if (e.children != null && e.children!.isNotEmpty) {
      return mapWidgetFromValue(e.children!.first);
    }
    return Container();
  }
}

class CustomTabBar extends StatefulWidget {
  final List<TabItem> tabs;
  final int initialSelectedIndex;
  final Function(int) onTabChanged;
  final VoidCallback? onCloseTab;
  final bool useDarkTheme;

  const CustomTabBar({
    Key? key,
    required this.tabs,
    this.initialSelectedIndex = 0,
    required this.onTabChanged,
    this.onCloseTab,
    this.useDarkTheme = false,
  }) : super(key: key);

  @override
  State<CustomTabBar> createState() => _CustomTabBarState();
}

class _CustomTabBarState extends State<CustomTabBar> {
  late int _selectedTabIndex;

  @override
  void initState() {
    super.initState();
    _selectedTabIndex = widget.initialSelectedIndex;
  }

  @override
  Widget build(BuildContext context) {
    final darkTheme = ThemeData.dark().copyWith(
      primaryColor: Color(0xFF6366F1),
      scaffoldBackgroundColor: Color(0xFF2C2C2C),
    );

    return Theme(
      data: widget.useDarkTheme ? darkTheme : Theme.of(context),
      child: Builder(
          builder: (context) {
            final theme = Theme.of(context);
            final isDark = theme.brightness == Brightness.dark;
            final backgroundColor = isDark ? Color(0xFF1E1E1E) : Color(0xFFF2F2F2);
            final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);
            final iconColor = isDark ? Colors.grey.shade400 : Colors.grey.shade600;

            return Container(
              height: 28,
              decoration: BoxDecoration(
                color: backgroundColor,
                border: Border(
                  bottom: BorderSide(color: borderColor, width: 1),
                ),
              ),
              child: Row(
                children: [
                  ...widget.tabs
                      .asMap()
                      .entries
                      .map((entry) {
                    final int index = entry.key;
                    final TabItem tab = entry.value;

                    return _buildTab(
                      context: context,
                      isSelected: index == _selectedTabIndex,
                      tab: tab,
                      onTap: () {
                        setState(() {
                          _selectedTabIndex = index;
                        });
                        widget.onTabChanged(index);
                      },
                      onClose: tab.showCloseButton ? () {
                        if (tab.onClose != null) {
                          tab.onClose!();
                        } else if (widget.onCloseTab != null) {
                          widget.onCloseTab!();
                        }
                      } : null,
                    );
                  }).toList(),

                  Spacer(),

                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 4),
                    child: Row(
                      children: [
                        Icon(Icons.help_outline, size: 16, color: iconColor),
                        SizedBox(width: 8),
                        Icon(Icons.calendar_today, size: 16, color: iconColor),
                      ],
                    ),
                  ),
                  SizedBox(width: 4),
                ],
              ),
            );
          }
      ),
    );
  }

  Widget _buildTab({
    required BuildContext context,
    required bool isSelected,
    required TabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
  }) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final backgroundColor = isDark ? Color(0xFF1A1A1A) : Color(0xFFF2F2F2);
    final selectedColor = isDark ? Color(0xFF2D2D2D) : Colors.white;
    final borderColor = isDark ? Color(0xFF333333) : Color(0xFFDDDDDD);

    // Colores para texto e iconos
    final textColor = isDark ? Colors.grey.shade500 : Colors.grey.shade600;
    final selectedTextColor = isDark ? Colors.white : Colors.grey.shade900; // Blanco cuando seleccionado en tema oscuro

    // Colores para iconos - Blanco cuando seleccionado en tema oscuro
    final iconColor = isDark
        ? (isSelected ? Colors.white : Colors.grey.shade600)
        : (isSelected ? Colors.grey.shade900 : Colors.grey.shade500);

    final closeIconColor = isDark
        ? (isSelected ? Colors.white.withOpacity(0.9) : Colors.grey.shade600)
        : (isSelected ? Colors.grey.shade700 : Colors.grey.shade400);
    final activeTabIndicatorColor = isDark ? Color(0xFF6366F1) : theme.primaryColor;

    return InkWell(
      onTap: onTap,
      child: Container(
        height: 28,
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 0),
        decoration: BoxDecoration(
          color: isSelected ? selectedColor : backgroundColor,
          boxShadow: isSelected && isDark ? [
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 2,
              offset: Offset(0, 1),
            )
          ] : null,
          border: Border(
            top: isSelected
                ? BorderSide(color: activeTabIndicatorColor, width: 2)
                : BorderSide.none,
            right: BorderSide(color: borderColor, width: 1),
            left: isSelected
                ? BorderSide(color: borderColor, width: 1)
                : BorderSide.none,
            bottom: isSelected
                ? BorderSide(color: selectedColor, width: 1)
                : BorderSide(color: borderColor, width: 1),
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Usar el widget personalizado cuando está disponible, o construir uno básico
            tab.customContent != null
                ? DefaultTextStyle(
              style: TextStyle(
                fontSize: 12,
                color: isSelected ? selectedTextColor : textColor,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal, // Aquí puedes ajustar la negrita para tabs seleccionados
              ),
              child: tab.customContent!,
            )
                : Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (tab.image != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
                    child: !materialIconMap.containsKey(tab.image)
                        ? Image.file(
                      File(tab.image!),
                      width: 16,
                      height: 16,
                    )
                        : Icon(
                        getMaterialIconByName(tab.image!),
                        size: 16,
                        color: iconColor
                    ),
                  ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 2.0),
                  child: Text(
                    tab.label,
                    style: TextStyle(
                      fontSize: 12,
                      color: isSelected ? selectedTextColor : textColor,
                      fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal, // Aquí puedes ajustar la negrita para tabs seleccionados
                    ),
                  ),
                ),
              ],
            ),
            if (tab.showCloseButton) ...[
              const SizedBox(width: 6),
              MouseRegion(
                cursor: SystemMouseCursors.click,
                child: GestureDetector(
                  onTap: onClose,
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: 1.0),
                    child: Icon(
                        Icons.close,
                        size: 14,
                        color: closeIconColor
                    ),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class TabItem {
  final String label;
  final String? image;
  final bool showCloseButton;
  final VoidCallback? onClose;
  final bool alignRight;
  final Widget? customContent;  // Nuevo campo para permitir contenido personalizado

  TabItem({
    required this.label,
    this.image,
    this.showCloseButton = true,
    this.onClose,
    this.alignRight = false,
    this.customContent,  // Nuevo campo opcional
  });
}

class CustomTabView extends StatefulWidget {
  final List<TabItem> tabs;
  final List<Widget> children;
  final int initialIndex;
  final bool useDarkTheme;

  const CustomTabView({
    Key? key,
    required this.tabs,
    required this.children,
    this.initialIndex = 0,
    this.useDarkTheme = false,
  }) : super(key: key);

  @override
  State<CustomTabView> createState() => _CustomTabViewState();
}

class _CustomTabViewState extends State<CustomTabView> {
  late int _selectedIndex;
  late PageController _pageController;

  @override
  void initState() {
    super.initState();
    _selectedIndex = widget.initialIndex;
    _pageController = PageController(initialPage: widget.initialIndex);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        CustomTabBar(
          tabs: widget.tabs,
          initialSelectedIndex: _selectedIndex,
          useDarkTheme: widget.useDarkTheme,
          onTabChanged: (index) {
            setState(() {
              _selectedIndex = index;
              _pageController.animateToPage(
                index,
                duration: const Duration(milliseconds: 300),
                curve: Curves.easeInOut,
              );
            });
          },
        ),

        Expanded(
          child: PageView(
            controller: _pageController,
            onPageChanged: (index) {
              setState(() {
                _selectedIndex = index;
              });
            },
            children: widget.children,
          ),
        ),
      ],
    );
  }
}