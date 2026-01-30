import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/main.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../custom/toolbar_composite.dart';
import 'composite_evolve.dart';
import 'icons_map.dart';
import 'widget_config.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import 'utils/widget_utils.dart';
import 'utils/image_utils.dart';
import 'color_utils.dart';

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {
  late int _selectedIndex;
  bool _hoveringTopBar = false;
  bool _scrollbarVisible = false;
  Timer? _scrollbarHideTimer;
  final GlobalKey _tabBarKey = GlobalKey();
  late final ScrollController _horizontalScrollController;
  bool _isMinimizeHovered = false;
  bool _isMaximizeHovered = false;

  @override
  void initState() {
    super.initState();
    _selectedIndex = state.selection ?? 0;
    _horizontalScrollController = ScrollController();
  }

  @override
  void dispose() {
    _scrollbarHideTimer?.cancel();
    _horizontalScrollController.dispose();
    super.dispose();
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final itemCount = state.items?.length ?? 0;

    // If Java sent a valid selection, use it
    if (state.selection != null && state.selection! >= 0 && state.selection! < itemCount) {
      if (_selectedIndex != state.selection!) {
        _selectedIndex = state.selection!;
      }
    }
    // If state.selection is null (means 0 in Java due to skipDefaultValues) or out of range,
    // ensure _selectedIndex is valid
    else if (itemCount > 0) {
      if (_selectedIndex >= itemCount) {
        // _selectedIndex is out of range after tab removal, adjust it
        _selectedIndex = itemCount - 1;
      } else if (state.selection == null && _selectedIndex != 0) {
        // state.selection=null means Java has selection=0, update if different
        _selectedIndex = 0;
      }
    }

    if (state.minimized != null || state.maximized != null) {
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<CTabFolderThemeExtension>()!;
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    final useSimpleStyle = false;
    final isSingle = state.single ?? false;
    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;
    final isMinimized = state.minimized ?? false;
    final isMaximized = state.maximized ?? false;

    final double? tabHeight = (state.tabHeight != null && state.tabHeight != SWT.DEFAULT)
        ? state.tabHeight!.toDouble()
        : null;

    final constraints = getConstraintsFromBounds(state.bounds);

    Widget column = Column(
      children: [
        if (!isTabBottom)
          buildTabBar(context, widgetTheme, tabItems, tabHeight, useSimpleStyle, isSingle),
        if (!isMinimized)
          Expanded(
            child: IndexedStack(
              index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
              children: tabBodies,
            ),
          ),
        if (isTabBottom)
          buildTabBar(context, widgetTheme, tabItems, tabHeight, useSimpleStyle, isSingle),
      ],
    );

    if (constraints != null) {
      return ConstrainedBox(
        constraints: constraints,
        child: column,
      );
    }

    return column;
  }

  Widget buildTabBar(BuildContext context, CTabFolderThemeExtension widgetTheme,
      List<CTabItem> tabItems, double? height,
      bool useSimpleStyle, bool isSingle) {
    List<CTabItem> visibleTabs = isSingle
        ? (_selectedIndex < tabItems.length ? [tabItems[_selectedIndex]] : [])
        : tabItems;

    final topRightComposite = getTopRightComposite();
    if (useSimpleStyle) {
      return buildSimpleTabBar(context, widgetTheme, visibleTabs, height);
    } else {
      return buildAdvancedTabBar(context, widgetTheme, visibleTabs, height, topRightComposite);
    }
  }

  Widget buildSimpleTabBar(BuildContext context, CTabFolderThemeExtension widgetTheme,
      List<CTabItem> tabs, double? height) {
    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;
    Widget tabBarContent = Row(
      children: [
        Expanded(
          child: _buildHorizontalScrollableTabs(
            widgetTheme: widgetTheme,
            child: Row(
              children: tabs.asMap().entries.map((entry) {
                final int index = entry.key;
                final CTabItem tab = entry.value;

                return Expanded(
                  child: _buildSimpleTab(
                    context: context,
                    widgetTheme: widgetTheme,
                    isSelected: index == _selectedIndex,
                    tab: tab,
                    onTap: () => _handleTabSelection(index),
                    onClose:
                        tab.showCloseButton ? () => _handleTabClose(index) : null,
                    isTabBottom: isTabBottom,
                  ),
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );

    return _buildTabBarContainer(
      widgetTheme: widgetTheme,
      height: height,
      child: tabBarContent,
      isTabBottom: isTabBottom,
    );
  }

  Widget buildAdvancedTabBar(BuildContext context, CTabFolderThemeExtension widgetTheme,
      List<CTabItem> tabs, double? height, VComposite? topRightComposite) {
    final isTabBottom = state.tabPosition == SWT.BOTTOM ?? false;
    final showMinimizeButton = state.minimizeVisible ?? true;
    final showMaximizeButton = state.maximizeVisible ?? true;
    final isMinimized = state.minimized ?? false;
    final isMaximized = state.maximized ?? false;
    final topRightAlignment = state.topRightAlignment ?? SWT.RIGHT;

    Widget tabBarContent = Row(
      key: _tabBarKey,
      children: [
        Expanded(
          child: _buildHorizontalScrollableTabs(
            widgetTheme: widgetTheme,
            child: Row(
              children: tabs.asMap().entries.map((entry) {
                final int index = entry.key;
                final CTabItem tab = entry.value;

                return Expanded(
                  child: _buildAdvancedTab(
                    context: context,
                    widgetTheme: widgetTheme,
                    isSelected: index == _selectedIndex,
                    tab: tab,
                    onTap: () => _handleTabSelection(index),
                    onClose:
                        tab.showCloseButton ? () => _handleTabClose(index) : null,
                    isTabBottom: isTabBottom,
                  ),
                );
              }).toList(),
            ),
          ),
        ),
      ],
    );

    final topRightControls = _buildTopRightControls(
      context: context,
      widgetTheme: widgetTheme,
      topRightComposite: topRightComposite,
      showMinimizeButton: showMinimizeButton,
      showMaximizeButton: showMaximizeButton,
      isMinimized: isMinimized,
      isMaximized: isMaximized,
      alignment: topRightAlignment,
    );

    return MouseRegion(
      onEnter: (_) => setState(() => _hoveringTopBar = true),
      onExit: (_) => setState(() => _hoveringTopBar = false),
      child: _buildTabBarContainer(
        widgetTheme: widgetTheme,
        height: height,
        child: Stack(
          children: [
            tabBarContent,
            Positioned(
              right: 0,
              top: 0,
              bottom: 0,
              child: topRightControls,
            ),
          ],
        ),
        isTabBottom: isTabBottom,
      ),
    );
  }

  Widget _buildSimpleTab({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
  }) {
    final enabled = state.enabled ?? false;

    // Determine colors based on enabled state
    final textColor = !enabled
        ? widgetTheme.tabDisabledTextColor
        : (isSelected
            ? getForegroundColor(
                foreground: state.selectionForeground,
                defaultColor: widgetTheme.tabSelectedTextColor,
              )
            : widgetTheme.tabTextColor.withOpacity(widgetTheme.tabUnselectedTextOpacity));
    final backgroundColor = !enabled
        ? widgetTheme.tabDisabledBackgroundColor
        : (isSelected
            ? getBackgroundColor(
                background: state.selectionBackground,
                defaultColor: widgetTheme.tabSelectedBackgroundColor,
              ) ?? widgetTheme.tabSelectedBackgroundColor
            : widgetTheme.tabBackgroundColor);
    final borderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : widgetTheme.tabBorderColor;
    final textStyle = isSelected && enabled
        ? widgetTheme.tabSelectedTextStyle
        : widgetTheme.tabTextStyle;

    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: GestureDetector(
      onTap: onTap,
      child: Container(
        height: double.infinity,
        padding: EdgeInsets.symmetric(
          horizontal: widgetTheme.tabHorizontalPadding,
          vertical: widgetTheme.tabVerticalPadding,
        ),
        decoration: BoxDecoration(
          color: backgroundColor,
          border: Border(
            right: BorderSide(
              color: borderColor,
              width: widgetTheme.tabBorderWidth,
            ),
            bottom: !isTabBottom && isSelected && enabled
                ? BorderSide(
                    color: backgroundColor,
                    width: (state.selectionBarThickness != null && state.selectionBarThickness! > 0)
                        ? state.selectionBarThickness!.toDouble()
                        : widgetTheme.tabSelectedBorderWidth,
                  )
                : !isTabBottom
                    ? BorderSide(
                        color: borderColor,
                        width: widgetTheme.tabBorderWidth,
                      )
                    : BorderSide.none,
            top: isTabBottom && isSelected && enabled
                ? BorderSide(
                    color: backgroundColor,
                    width: (state.selectionBarThickness != null && state.selectionBarThickness! > 0)
                        ? state.selectionBarThickness!.toDouble()
                        : widgetTheme.tabSelectedBorderWidth,
                  )
                : isTabBottom
                    ? BorderSide(
                        color: borderColor,
                        width: widgetTheme.tabBorderWidth,
                      )
                    : BorderSide.none,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            tab.customContent ??
                Text(
                  _getTabText(tab.label),
                  style: textStyle?.copyWith(color: textColor) ??
                      TextStyle(color: textColor),
                ),
            if (onClose != null) ...[
              SizedBox(width: widgetTheme.tabCloseButtonSpacing),
              GestureDetector(
                onTap: onClose,
                child: Icon(
                  Icons.close,
                  size: widgetTheme.tabCloseIconSize,
                  color: textColor,
                ),
              ),
            ],
          ],
        ),
      ),
    ),
    );
  }

  Widget _buildAdvancedTab({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
  }) {
    final showUnselectedClose = state.unselectedCloseVisible ?? false;
    final shouldShowClose =
        (onClose != null) && (isSelected || showUnselectedClose);

    final showUnselectedImage = state.unselectedImageVisible ?? false;
    final showSelectedImage = state.selectedImageVisible ?? true;
    final shouldShowImage = (isSelected && showSelectedImage) || (!isSelected && showUnselectedImage);

    final showHighlight = state.highlightEnabled ?? false;
    final enabled = state.enabled ?? false;

    // Determine colors based on enabled state
    final textColor = !enabled
        ? widgetTheme.tabDisabledTextColor
        : (isSelected
            ? getForegroundColor(
                foreground: state.selectionForeground,
                defaultColor: widgetTheme.tabSelectedTextColor,
              )
            : widgetTheme.tabTextColor.withOpacity(widgetTheme.tabUnselectedTextOpacity));
    final backgroundColor = !enabled
        ? widgetTheme.tabDisabledBackgroundColor
        : (isSelected
            ? getBackgroundColor(
                background: state.selectionBackground,
                defaultColor: widgetTheme.tabSelectedBackgroundColor,
              ) ?? widgetTheme.tabSelectedBackgroundColor
            : widgetTheme.tabBackgroundColor);
    final borderColor = !enabled
        ? widgetTheme.tabDisabledBorderColor
        : widgetTheme.tabBorderColor;
    final textStyle = isSelected && enabled
        ? widgetTheme.tabSelectedTextStyle
        : widgetTheme.tabTextStyle;

    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: GestureDetector(
      onTap: onTap,
      child: Container(
        height: double.infinity,
        padding: EdgeInsets.symmetric(
          horizontal: widgetTheme.tabHorizontalPadding,
          vertical: widgetTheme.tabVerticalPadding,
        ),
        decoration: BoxDecoration(
          color: backgroundColor,
          image: isSelected && enabled ? _buildSelectionBgImage() : null,
            border: Border(
              top: !isTabBottom && isSelected && enabled && showHighlight
                  ? BorderSide(
                      color: widgetTheme.tabHighlightColor,
                      width: widgetTheme.tabHighlightBorderWidth,
                    )
                  : isTabBottom && isSelected && enabled
                      ? BorderSide(
                          color: backgroundColor,
                          width: (state.selectionBarThickness != null && state.selectionBarThickness! > 0)
                              ? state.selectionBarThickness!.toDouble()
                              : widgetTheme.tabSelectedBorderWidth,
                        )
                      : isTabBottom
                          ? BorderSide(
                              color: borderColor,
                              width: widgetTheme.tabBorderWidth,
                            )
                          : BorderSide.none,
              right: BorderSide(
                color: borderColor,
                width: widgetTheme.tabBorderWidth,
              ),
              left: isSelected && enabled
                  ? BorderSide(
                      color: borderColor,
                      width: widgetTheme.tabBorderWidth,
                    )
                  : BorderSide.none,
              bottom: !isTabBottom && isSelected && enabled
                  ? BorderSide(
                      color: backgroundColor,
                      width: (state.selectionBarThickness != null && state.selectionBarThickness! > 0)
                          ? state.selectionBarThickness!.toDouble()
                          : widgetTheme.tabSelectedBorderWidth,
                    )
                  : !isTabBottom
                      ? BorderSide(
                          color: borderColor,
                          width: widgetTheme.tabBorderWidth,
                        )
                      : isTabBottom && isSelected && enabled && showHighlight
                          ? BorderSide(
                              color: widgetTheme.tabHighlightColor,
                              width: widgetTheme.tabHighlightBorderWidth,
                            )
                          : BorderSide.none,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent != null
                  ? TabItemContextProvider(
                      isSelected: isSelected,
                      isEnabled: enabled,
                      child: tab.customContent!,
                    )
                  : Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (tab.image != null && shouldShowImage)
                          Padding(
                            padding: EdgeInsets.only(
                              bottom: widgetTheme.tabIconBottomPadding,
                              right: widgetTheme.tabIconTextSpacing,
                            ),
                            child: !iconMap.containsKey(tab.image)
                                ? Image.file(
                                    File(tab.image!),
                                    width: widgetTheme.tabIconSize,
                                    height: widgetTheme.tabIconSize,
                                  )
                                : Icon(
                                    getIconByName(tab.image!),
                                    size: widgetTheme.tabIconSize,
                                    color: textColor,
                                  ),
                          ),
                        Padding(
                          padding: EdgeInsets.only(
                            bottom: widgetTheme.tabTextBottomPadding,
                          ),
                          child: Text(
                            _getTabText(tab.label),
                            style: textStyle?.copyWith(color: textColor) ??
                                TextStyle(color: textColor),
                          ),
                        ),
                      ],
                    ),
              if (shouldShowClose) ...[
                SizedBox(width: widgetTheme.tabCloseButtonSpacing),
                MouseRegion(
                  cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
                  child: GestureDetector(
                    onTap: onClose,
                    child: Padding(
                      padding: EdgeInsets.only(
                        bottom: widgetTheme.tabCloseIconBottomPadding,
                      ),
                      child: Icon(
                        Icons.close,
                        size: widgetTheme.tabCloseIconSize,
                        color: isSelected
                            ? textColor.withOpacity(widgetTheme.tabCloseButtonSelectedOpacity)
                            : textColor.withOpacity(widgetTheme.tabCloseButtonUnselectedOpacity),
                      ),
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
    ),
    );
  }

  Widget _buildTopRightComposite(VComposite composite) {
    return ToolbarComposite(value: composite);
  }

  VComposite? getTopRightComposite() {
    return state.topRight is VComposite ? state.topRight as VComposite : null;
  }

  Widget _buildControlButton({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required IconData icon,
    required VoidCallback onTap,
    required bool isHovered,
    required ValueChanged<bool> onHoverChanged,
    bool enabled = true,
  }) {
    return MouseRegion(
      onEnter: (_) {
        onHoverChanged(true);
      },
      onExit: (_) {
        onHoverChanged(false);
      },
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: enabled ? onTap : null,
          splashColor: widgetTheme.controlButtonHoverColor.withOpacity(0.3),
          highlightColor: widgetTheme.controlButtonHoverColor.withOpacity(0.2),
          borderRadius: BorderRadius.circular(4),
          child: Container(
            height: double.infinity,
            padding: EdgeInsets.symmetric(
              horizontal: widgetTheme.controlButtonHorizontalPadding,
            ),
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: isHovered ? widgetTheme.controlButtonHoverColor.withOpacity(0.1) : Colors.transparent,
              borderRadius: BorderRadius.circular(4),
            ),
            child: Icon(
              icon,
              size: widgetTheme.controlButtonSize,
              color: widgetTheme.controlButtonColor,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHorizontalScrollableTabs({
    required CTabFolderThemeExtension widgetTheme,
    required Widget child,
  }) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return NotificationListener<ScrollUpdateNotification>(
          onNotification: (notification) {
            _showScrollbar();
            _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
            return false;
          },
          child: MouseRegion(
            onEnter: (_) {
              _showScrollbar();
            },
            onExit: (_) {
              _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
            },
            child: Listener(
              onPointerDown: (_) {
                _showScrollbar();
              },
              child: Scrollbar(
                controller: _horizontalScrollController,
                thumbVisibility: _scrollbarVisible,
                thickness: widgetTheme.tabScrollbarThickness,
                child: SingleChildScrollView(
                  controller: _horizontalScrollController,
                  scrollDirection: Axis.horizontal,
                  child: ConstrainedBox(
                    constraints: BoxConstraints(minWidth: constraints.maxWidth),
                    child: child,
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  void _hideScrollbarAfterDelay(Duration delay) {
    _scrollbarHideTimer?.cancel();
    _scrollbarHideTimer = Timer(delay, () {
      if (mounted && _scrollbarVisible) {
        setState(() {
          _scrollbarVisible = false;
        });
      }
    });
  }

  void _showScrollbar() {
    _scrollbarHideTimer?.cancel();
    if (!_scrollbarVisible) {
      setState(() {
        _scrollbarVisible = true;
      });
    }
  }

  Widget _buildTabBarContainer({
    required CTabFolderThemeExtension widgetTheme,
    required double? height,
    required Widget child,
    required bool isTabBottom,
  }) {
    final borderVisible = state.borderVisible ?? true;
    final decoration = BoxDecoration(
      color: widgetTheme.tabBarBackgroundColor,
      border: borderVisible
          ? (isTabBottom
              ? Border(
                  top: BorderSide(
                    color: widgetTheme.tabBarBorderColor,
                    width: widgetTheme.tabBorderWidth,
                  ),
                )
              : Border(
                  bottom: BorderSide(
                    color: widgetTheme.tabBarBorderColor,
                    width: widgetTheme.tabBorderWidth,
                  ),
                ))
          : null,
    );

    if (height != null) {
      return Container(
        height: height,
        decoration: decoration,
        child: child,
      );
    }

    return IntrinsicHeight(
      child: Container(
        decoration: decoration,
        child: child,
      ),
    );
  }

  void _handleTabSelection(int index) {
    if (state.enabled != true) return;
    setState(() {
      _selectedIndex = index;
      state.selection = index;
    });
    var e = VEvent()..index = index;
    widget.sendSelectionSelection(state, e);
  }

  void _toggleMinimize() {
    if (state.enabled != true) return;
    final isMinimized = state.minimized ?? false;
    var e = VEvent();
    if (isMinimized) {
      widget.sendCTabFolder2restore(state, e);
    } else {
      widget.sendCTabFolder2minimize(state, e);
    }
  }

  void _toggleMaximize() {
    if (state.enabled != true) return;
    final isMaximized = state.maximized ?? false;
    var e = VEvent();
    if (isMaximized) {
      widget.sendCTabFolder2restore(state, e);
    } else {
      widget.sendCTabFolder2maximize(state, e);
    }
  }

  List<CTabItem> getTabItems() {
    if (state.items == null) {
      return [];
    }
    return state.items!
        .whereType<VCTabItem>()
        .map((tabItem) => getWidgetForTabItem(tabItem))
        .toList();
  }

  CTabItem getWidgetForTabItem(VCTabItem tabItem) {
    final tabItemWidget = CTabItemSwt(value: tabItem);

    return CTabItem(
      label: tabItem.text ?? "",
      showCloseButton: tabItem.showClose ?? false,
      customContent: tabItemWidget,
      toolTipText: tabItem.toolTipText,
    );
  }

  List<Widget> getTabBodies() {
    if (state.items == null) {
      return <Widget>[];
    }
    return state.items!
        .whereType<VCTabItem>()
        .map((e) => tabBody(e))
        .toList();
  }

  Widget tabBody(VCTabItem e) {
    if (e.control != null) {
      final control = e.control!;

      if (hasBounds(control.bounds)) {
        final bounds = control.bounds!;
        return SizedBox(
          width: bounds.width.toDouble(),
          height: bounds.height.toDouble(),
          child: mapWidgetFromValue(control),
        );
      } else {
        return SizedBox.expand(
          child: mapWidgetFromValue(control),
        );
      }
    }
    return Container();
  }

  String _getTabText(String label) {
    final minChars = state.minimumCharacters;
    if (minChars != null && minChars > 0 && label.length > minChars) {
      return '${label.substring(0, minChars)}...';
    }
    return label;
  }


  DecorationImage? _buildSelectionBgImage() {
    final image = state.selectionBgImage;
    if (image == null) return null;
    
    // For now, we'll use the filename if available
    // In a full implementation, you'd need to handle imageData as well
    if (image.filename != null && image.filename!.isNotEmpty) {
      try {
        return DecorationImage(
          image: FileImage(File(image.filename!)),
          fit: BoxFit.cover,
        );
      } catch (e) {
        // If file doesn't exist, return null
        return null;
      }
    }
    
    return null;
  }

  Widget _buildTopRightControls({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required VComposite? topRightComposite,
    required bool showMinimizeButton,
    required bool showMaximizeButton,
    required bool isMinimized,
    required bool isMaximized,
    required int alignment,
  }) {
    final hasControls = topRightComposite != null || showMinimizeButton || showMaximizeButton;
    if (!hasControls) return const SizedBox.shrink();

    final controls = Row(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (topRightComposite != null)
          _buildTopRightComposite(topRightComposite),
        if (showMinimizeButton)
          Builder(
            builder: (context) {
              final isVisible = getConfigFlags().ctabfolder_visible_controls == true
                  ? true
                  : _hoveringTopBar;
              return _buildControlButton(
                context: context,
                widgetTheme: widgetTheme,
                icon: isMinimized ? Icons.maximize : Icons.minimize,
                onTap: isVisible ? _toggleMinimize : () {},
                isHovered: _isMinimizeHovered,
                onHoverChanged: (hovered) {
                  setState(() {
                    _isMinimizeHovered = hovered;
                  });
                },
                enabled: true, 
              );
            },
          ),
        if (showMaximizeButton)
          Builder(
            builder: (context) {
              final isVisible = getConfigFlags().ctabfolder_visible_controls == true
                  ? true
                  : _hoveringTopBar;
              return _buildControlButton(
                context: context,
                widgetTheme: widgetTheme,
                icon: isMaximized ? Icons.fullscreen_exit : Icons.fullscreen,
                onTap: isVisible ? _toggleMaximize : () {},
                isHovered: _isMaximizeHovered,
                onHoverChanged: (hovered) {
                  setState(() {
                    _isMaximizeHovered = hovered;
                  });
                },
                enabled: true,
              );
            },
          ),
      ],
    );

    final isControlsVisible = getConfigFlags().ctabfolder_visible_controls == true
        ? true
        : _hoveringTopBar;
    
    final revealWidget = _HoverReveal(
      visible: isControlsVisible,
      revealDuration: widgetTheme.hoverRevealDuration,
      hideDuration: widgetTheme.hoverHideDuration,
      child: Align(
        alignment: Alignment.topRight,
        child: Container(
          height: double.infinity,
          decoration: BoxDecoration(
            color: widgetTheme.tabBarBackgroundColor,
            boxShadow: [
              BoxShadow(
                color: widgetTheme.topRightControlsShadowColor.withOpacity(widgetTheme.topRightControlsShadowOpacity),
                blurRadius: widgetTheme.topRightControlsShadowBlurRadius,
                offset: widgetTheme.topRightControlsShadowOffset,
              ),
            ],
          ),
          child: controls,
        ),
      ),
    );

    final scaledWidget = Transform.scale(
      scale: widgetTheme.controlButtonScale,
      alignment: Alignment.topRight,
      child: revealWidget,
    );

    if (alignment == SWT.LEFT) {
      return scaledWidget;
    } else if (alignment == SWT.CENTER) {
      return Expanded(child: Center(child: scaledWidget));
    } else {
      return scaledWidget;
    }
  }

  void _handleTabClose(int index) {
    if (state.enabled != true) return;
    var e = VEvent()..index = index;
    widget.sendCTabFolder2close(state, e);
    widget.sendCTabFolderitemClosed(state, e);
  }
}

class CTabItem {
  final String label;
  final String? image;
  final bool showCloseButton;
  final VoidCallback? onClose;
  final bool alignRight;
  final Widget? customContent;
  final String? toolTipText;

  CTabItem({
    required this.label,
    this.image,
    this.showCloseButton = false,
    this.onClose,
    this.alignRight = false,
    this.customContent,
    this.toolTipText,
  });
}

class _HoverReveal extends StatelessWidget {
  final bool visible;
  final Widget child;
  final Duration revealDuration;
  final Duration hideDuration;

  const _HoverReveal({
    required this.visible,
    required this.child,
    required this.revealDuration,
    required this.hideDuration,
  });

  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: true,
      maintainSize: true,
      maintainAnimation: true,
      maintainState: true,
      child: AnimatedOpacity(
        opacity: visible ? 1.0 : 0.0,
        duration: visible ? revealDuration : hideDuration,
        curve: Curves.easeOut,
        child: child,
      ),
    );
  }
}

class TabItemContext {
  final bool isSelected;
  final bool isEnabled;

  TabItemContext({
    required this.isSelected,
    required this.isEnabled,
  });

  static TabItemContext? of(BuildContext context) {
    final provider =
        context.dependOnInheritedWidgetOfExactType<TabItemContextProvider>();
    return provider?.context;
  }
}

class TabItemContextProvider extends InheritedWidget {
  final TabItemContext context;

  TabItemContextProvider({
    Key? key,
    required bool isSelected,
    required bool isEnabled,
    required Widget child,
  })  : context = TabItemContext(isSelected: isSelected, isEnabled: isEnabled),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(TabItemContextProvider oldWidget) {
    return context.isSelected != oldWidget.context.isSelected ||
        context.isEnabled != oldWidget.context.isEnabled;
  }
}

