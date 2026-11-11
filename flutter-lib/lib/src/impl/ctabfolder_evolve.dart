import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/foundation.dart' show ValueListenable;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:swtflutter/main.dart';
import '../comm/comm.dart';
import '../comm/channel_header.dart';
import '../comm/tagged_values.dart';
import '../comm/v_registry.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/droptarget.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../custom/toolbar_composite.dart';
import 'composite_evolve.dart';
import 'control_evolve.dart';
import 'ctab_tab_compression.dart';
import 'widget_config.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import '../theme/theme_settings/ctabfolder_theme_settings.dart';
import 'utils/dnd_session.dart' show ActiveDragTracker;
import 'utils/dnd_utils.dart';
import 'utils/layered_layout_builder.dart';
import 'utils/tracker_session.dart';
import 'utils/image_utils.dart';
import 'utils/widget_utils.dart';
import 'color_utils.dart';
import 'utils/quiet_animations.dart';

/// Width of the frame a folder draws around its page, on the three edges the tab strip does not
/// take. Java reserves the same width in `Sizes.getClientArea(DartCTabFolder)`, so a page laid out
/// in the client area Java reports lands exactly inside this frame rather than under it.
double ctabFolderBodyBorderWidth(int? style) =>
    ((style != null && (style & SWT.BORDER) != 0) ? ctabFolderOutlineWidth : 0) +
    _ctabFolderHighlightMargin;

/// Width of a bordered folder's outline, which native's renderer fixes at one pixel.
const double ctabFolderOutlineWidth = 1;

/// Band between the outline and the page, which native fills with the folder's ground.
const double _ctabFolderHighlightMargin = 2;

/// Java sends the strip height it reserves, and leaves it out at this default.
const double _defaultTabStripHeight = 32;

class CTabFolderImpl<T extends CTabFolderSwt, V extends VCTabFolder>
    extends CompositeImpl<T, V> {
  /// The widget holding the tabs, one keyed subtree per tab: where measure_ctabfolder reads a tab.
  static const tabRow = 'TabDragRow';

  /// A click on a tab or on the folder focuses no CTabFolder, on any native.
  @override
  void takeFocusOnPress() {}

  // The tab row raises DragDetect from its own Draggable, with the dragged tab's coordinates.
  @override
  bool get raisesDragDetectFromPointer => false;

  late int _selectedIndex;
  int _lastShowListPopupSeq = 0;
  int? _pendingTabIndex;
  /// The mounted folders by id, where [_activationOp] finds the one a frame addresses.
  static final Map<int, CTabFolderImpl> _live = {};

  @override
  Map<int, WidgetSwtState> get liveOfKind => _live;

  static final int _activeKey = TaggedValues.keyNumber('active');

  /// Registers what every CTabFolder answers (see `registerWidgetOps`).
  static void registerOps() =>
      EquoCommService.onWidgetOp(WireType.cTabFolder, WireChannel.activation, _activationOp);

  /// Java's `{active}`: whether this folder holds the active part.
  static FutureOr<bool> _activationOp(int id, Uint8List frame, int start, int end) {
    final folder = _live[id];
    if (folder == null) return false;
    final active = TaggedValues.boolField(frame, start, _activeKey);
    if (active != null && folder.mounted) {
      // Kept on the state too, or a rebuild re-reads the last push's stale flag in extraSetState.
      folder.state.highlight = active;
      // The active tab's label and ring belong to the tab bar, so the folder rebuilds.
      folder.setState(() {});
    }
    return true;
  }

  @override
  void dispose() {
    final popup = _chevronPopupRoute;
    if (popup != null && popup.isActive) {
      // The navigator cannot change routes while the tree that disposes this folder is locked.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (popup.isActive) popup.navigator?.removeRoute(popup);
      });
    }
    super.dispose();
  }

  @override
  void initState() {
    super.initState();
    // -1 means genuinely unselected (not Java's skipDefaultValues null=0); fall back to item 0.
    final selection = state.selection;
    _selectedIndex = (selection != null && selection >= 0) ? selection : 0;
    // Java leaves a zero seq off the wire, so null is 0; a list requested before this mounted is
    // not reopened.
    _lastShowListPopupSeq = state.showListPopupSeq ?? 0;
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final itemCount = state.items?.length ?? 0;

    if (_pendingTabIndex != null) {
      // Java selection caught up (null means 0 due to skipDefaultValues)
      final javaSelection = state.selection ?? 0;
      if (javaSelection == _pendingTabIndex) {
        _pendingTabIndex = null;
      } else {
        // Java sent old state — keep our pending selection to avoid flicker
        _selectedIndex = _pendingTabIndex!;
        state.selection = _pendingTabIndex;
        return;
      }
    }

    // If Java sent a valid selection, use it
    if (state.selection != null &&
        state.selection! >= 0 &&
        state.selection! < itemCount) {
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
      } else if ((state.selection == null || state.selection! < 0) &&
          _selectedIndex != 0) {
        // See initState: null (skipDefaultValues) and -1 (genuine) both fall back to item 0.
        _selectedIndex = 0;
      }
    }

    // Java bumps the seq when no CTabFolder2Listener took the chevron's showList (doit stayed
    // true): the folder's own list is what the user gets then.
    final seq = state.showListPopupSeq ?? 0;
    if (seq != _lastShowListPopupSeq) {
      final requested = seq > _lastShowListPopupSeq;
      _lastShowListPopupSeq = seq;
      if (requested) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) _doShowChevronPopup();
        });
      }
    }

    if (state.minimized != null || state.maximized != null) {
      setState(() {});
    }
  }


  /// A strip height of 0 leaves no strip at all; any other fixed height is fitted by [_CTabBar].
  Widget _fitTabStrip(double height, Widget strip) =>
      height == 0 ? const SizedBox.shrink() : strip;

  @override
  Widget build(BuildContext context) {
    if (hidden) return const SizedBox.shrink();
    final tabItems = getTabItems();
    final tabBodies = getTabBodies();

    final isTabBottom = state.tabPosition == SWT.BOTTOM;

    // Java sends the strip height it reserves: a fixed height, or the default grown by a tall image.
    final double stripHeight = state.tabHeight?.toDouble() ?? _defaultTabStripHeight;
    final double tabRowHeight = math.max(stripHeight, _defaultTabStripHeight);
    final constraints = getConstraintsFromBounds(state.bounds);

    final widgetTheme = Theme.of(context).extension<CTabFolderThemeExtension>()!;
    // With no strip the folder draws no frame either, as Java's client area assumes.
    final bodyFrame = stripHeight == 0
        ? BorderSide.none
        : BorderSide(
            color: widgetTheme.tabContentBorderColor,
            width: ctabFolderBodyBorderWidth(state.style),
          );
    // With use_swt_border, a bordered folder draws one outline, as native's renderer does: round the
    // folder, along the strip's edge on the page side, and over the selected tab, which stays open
    // into its page. Without it the theme's flat frame is drawn.
    final bordered =
        useSwtBorder && (state.borderVisible ?? (state.style & SWT.BORDER) != 0);
    final BorderSide? outline = stripHeight == 0 || !bordered
        ? null
        : BorderSide(
            // Opaque, so the line reads the same over the strip, the tabs and the page.
            color: Color.alphaBlend(widgetTheme.outlineColor, widgetTheme.tabContentBackgroundColor),
            width: ctabFolderOutlineWidth,
          );
    final frameInset = ctabFolderBodyBorderWidth(state.style);
    // Behind the page is the folder's own ground. Native fills the band inside the outline with the
    // selection background while the folder is highlighted and a tab is selected, and with that
    // ground otherwise.
    final highlighted = (state.highlightEnabled ?? false) &&
        (state.highlight ?? false) &&
        (state.selection ?? -1) >= 0 &&
        tabItems.isNotEmpty;
    final Color? folderGround = outline == null
        ? null
        : getBackgroundColor(
                background: state.background,
                defaultColor: widgetTheme.tabBarBackgroundColor,
                context: context,
              ) ??
              widgetTheme.tabBarBackgroundColor;
    final Color? marginColor = !highlighted || folderGround == null
        ? folderGround
        : getBackgroundColor(
                background: state.selectionBackground,
                defaultColor: widgetTheme.tabSelectedBackgroundColor,
              ) ??
              widgetTheme.tabSelectedBackgroundColor;
    final stripClip = math.max(0.0, tabRowHeight - stripHeight);

    Widget column = Column(
      children: [
        if (!isTabBottom)
          _fitTabStrip(stripHeight, _CTabBar(
            state: state,
            folderWidget: widget,
            selectedIndex: _selectedIndex,
            tabItems: tabItems,
            tabHeight: tabRowHeight,
            useSimpleStyle: false,
            outline: outline,
            stripClip: stripClip,
            topRightComposite: getTopRightComposite(),
            onTabSelected: _handleTabSelection,
            onTabClose: _handleTabClose,
            onMinimize: _toggleMinimize,
            onMaximize: _toggleMaximize,
            onTabReorder: _handleTabReorder,
            onTabDrop: _handleTabDrop,
            onTabDragStarted: _handleTabDragStarted,
            onChevronShowList: _handleChevronShowList,
            onStripLayout: _handleStripLayout,
            dragPointFor: _dragPointFor,
            folderPointFor: _folderPointFor,
          )),
        // Render the body regardless of `minimized`: native SWT's `minimized` only
        // shrinks computeSize, it never hides the selected control. A collapsed folder
        // gets a tab-strip-height bounds (Expanded → ~0 height); an E4 minimized-stack
        // fly-out gets a full client area and must show its content.
        Expanded(
          child: Container(
            // The outline is a line inside the frame Java reserves, painted over the band that fills
            // the rest of it.
            decoration: outline != null
                ? BoxDecoration(
                    color: folderGround,
                    border: Border(
                      left: BorderSide(color: marginColor!, width: frameInset),
                      right: BorderSide(color: marginColor, width: frameInset),
                      top: isTabBottom
                          ? BorderSide(color: marginColor, width: frameInset)
                          : BorderSide.none,
                      bottom: isTabBottom
                          ? BorderSide.none
                          : BorderSide(color: marginColor, width: frameInset),
                    ),
                  )
                : BoxDecoration(
                    border: Border(
                      left: bodyFrame,
                      right: bodyFrame,
                      top: isTabBottom ? bodyFrame : BorderSide.none,
                      bottom: isTabBottom ? BorderSide.none : bodyFrame,
                    ),
                  ),
            child: IndexedStack(
              index: _selectedIndex < tabBodies.length ? _selectedIndex : 0,
              children: tabBodies,
            ),
          ),
        ),
        if (isTabBottom)
          _fitTabStrip(stripHeight, _CTabBar(
            state: state,
            folderWidget: widget,
            selectedIndex: _selectedIndex,
            tabItems: tabItems,
            tabHeight: tabRowHeight,
            useSimpleStyle: false,
            outline: outline,
            stripClip: stripClip,
            topRightComposite: getTopRightComposite(),
            onTabSelected: _handleTabSelection,
            onTabClose: _handleTabClose,
            onMinimize: _toggleMinimize,
            onMaximize: _toggleMaximize,
            onTabReorder: _handleTabReorder,
            onTabDrop: _handleTabDrop,
            onTabDragStarted: _handleTabDragStarted,
            onChevronShowList: _handleChevronShowList,
            onStripLayout: _handleStripLayout,
            dragPointFor: _dragPointFor,
            folderPointFor: _folderPointFor,
          )),
      ],
    );
    if (outline != null) {
      column = CustomPaint(
        foregroundPainter: _FolderOutline(
          outline,
          radius: widgetTheme.tabBorderRadius,
          stripAtBottom: isTabBottom,
        ),
        child: column,
      );
    }

    // Tab stops on the folder itself (VControl.tabStop: it hooks KeyDown, as natively), before its
    // pages; Java's FocusIn selects the first tab when none is.
    column = wrapSurfaceFocus(column, tabStop: state.tabStop ?? false);

    // SWT reads enablement down the parent chain - `isEnabled()` is this control's own flag and
    // every ancestor's - so a disabled folder disables the pages it holds. This build does not go
    // through `wrap()`, so it owes them that answer itself, and the folder's own pointer events:
    // SWT's CTabFolder draws its tabs and reports the mouse over them as its own.
    if (constraints != null) {
      return inOwnLayer(SwtControlScope(
          swt: state.swt,
          child: tagSemantics(trackPointer(ParentForegroundScope(
            foreground: state.foreground,
            font: state.font,
            selectionForeground: state.selectionForeground,
            child: ConstrainedBox(constraints: constraints, child: column),
          )))));
    }

    // The tabs letter and colour in the folder's own font/foreground when they carry none.
    return inOwnLayer(SwtControlScope(
        swt: state.swt,
        child: tagSemantics(trackPointer(ParentForegroundScope(
          foreground: state.foreground,
          font: state.font,
          selectionForeground: state.selectionForeground,
          child: column,
        )))));
  }

  /// [detail] is `SWT.DRAG` when a drag selects the tab, which natively never moves the focus.
  void _handleTabSelection(int index, {int detail = 0}) {
    if (state.enabled != true) return;
    _pendingTabIndex = index;
    setState(() {
      _selectedIndex = index;
      state.selection = index;
    });
    var e = VEvent()
      ..index = index
      ..detail = detail;
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

  VComposite? getTopRightComposite() {
    return state.topRight is VComposite ? state.topRight as VComposite : null;
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

    // SWT.CLOSE on the folder puts a close button on every tab, as the tab's own flag does.
    final folderCloses = (state.style & SWT.CLOSE) != 0;
    return CTabItem(
      label: tabItem.text ?? "",
      showCloseButton: (tabItem.showClose ?? false) || folderCloses,
      customContent: tabItemWidget,
      toolTipText: tabItem.toolTipText,
      vItem: tabItem,
    );
  }

  List<Widget> getTabBodies() {
    if (state.items == null) {
      return <Widget>[];
    }
    return state.items!.whereType<VCTabItem>().map((e) => tabBody(e)).toList();
  }

  /// The page's bounds can arrive on its own channel after the folder has rebuilt - a page laid out
  /// a turn after its folder was resized updates only the page - so the body is sized on every
  /// change there, not when the folder last built.
  Widget tabBody(VCTabItem e) {
    if (e.control != null) {
      final control = e.control!;
      return ListenableBuilder(
        listenable: VRegistry.instance.boundsChangesAt([VRegistry.keyOf(control)]),
        builder: (context, page) {
          final bounds = control.bounds;
          if (!hasBounds(bounds)) return SizedBox.expand(child: page);
          return SizedBox(
            width: bounds!.width.toDouble(),
            height: bounds.height.toDouble(),
            child: page,
          );
        },
        child: mapWidgetFromValue(control),
      );
    }
    return Container();
  }

  Rect? _chevronButtonRect;

  /// The chevron's list while it is open; it goes with the folder, as a native's list menu does.
  ModalRoute<dynamic>? _chevronPopupRoute;

  void _handleChevronShowList(BuildContext buttonContext, CTabFolderThemeExtension widgetTheme) {
    final RenderBox? box = buttonContext.findRenderObject() as RenderBox?;
    if (box != null && box.hasSize) {
      // Measured against the overlay showMenu places the list in, not the view: the app's zoom
      // Transform sits above that overlay, so the two spaces differ once SWT asks for a zoom.
      final overlayBox = Navigator.of(buttonContext).overlay?.context.findRenderObject();
      final Offset origin = box.localToGlobal(Offset.zero,
          ancestor: overlayBox is RenderBox ? overlayBox : null);
      _chevronButtonRect = origin & box.size;
    } else {
      _chevronButtonRect = null;
    }
    // The chevron is CTabFolder2Listener.showList with its bounds in folder coordinates: a listener
    // with its own list sets doit=false, otherwise Java bumps showListPopupSeq. An unmeasured chevron
    // sends nothing, or a listener would anchor its list at the folder's origin.
    final folderBox = context.findRenderObject();
    if (box == null || !box.hasSize || folderBox is! RenderBox || !folderBox.hasSize) return;
    final origin = box.localToGlobal(Offset.zero, ancestor: folderBox);
    final e = VEvent()
      ..x = origin.dx.round()
      ..y = origin.dy.round()
      ..width = box.size.width.round()
      ..height = box.size.height.round();
    widget.sendCTabFolder2showList(state, e);
  }

  void _doShowChevronPopup() {
    if (!mounted) return;
    final ctx = context;
    final widgetTheme = Theme.of(ctx).extension<CTabFolderThemeExtension>()!;

    final allItems = state.items?.whereType<VCTabItem>().toList() ?? [];
    if (allItems.isEmpty) return;

    // Java marks every item showing=true in web mode, so when none is flagged hidden fall
    // back to listing every tab -- otherwise the off-view tabs are unreachable.
    var hiddenEntries = allItems.asMap().entries
        .where((e) => e.value.showing == false)
        .toList();
    if (hiddenEntries.isEmpty) {
      hiddenEntries = allItems.asMap().entries.toList();
    }

    final screenSize = MediaQuery.of(ctx).size;
    final buttonRect = _chevronButtonRect;
    final RelativeRect position = buttonRect != null
        ? RelativeRect.fromLTRB(
            buttonRect.left,
            buttonRect.bottom,
            screenSize.width - buttonRect.right,
            0,
          )
        : RelativeRect.fromLTRB(
            screenSize.width - 60,
            40,
            0,
            0,
          );

    showMenu<int>(
      context: ctx,
      position: position,
      items: [
        for (final (i, entry) in hiddenEntries.indexed)
          PopupMenuItem<int>(
            value: entry.key,
            height: widgetTheme.chevronMenuItemHeight,
            padding: widgetTheme.chevronMenuItemPadding,
            // showMenu hands back only the result, so the route it pushes is read from inside it,
            // once, by the first item.
            child: i > 0
                ? Text(entry.value.text ?? '')
                : Builder(builder: (itemContext) {
                    _chevronPopupRoute ??= ModalRoute.of(itemContext);
                    return Text(entry.value.text ?? '');
                  }),
          ),
      ],
    ).then((selectedIndex) {
      _chevronPopupRoute = null;
      if (selectedIndex != null) {
        _handleTabSelection(selectedIndex);
      }
    });
  }

  void _handleTabClose(int index) {
    if (state.enabled != true) return;
    var e = VEvent()..index = index;
    widget.sendCTabFolder2close(state, e);
    widget.sendCTabFolderitemClosed(state, e);
  }

  /// The point to hand the workbench for a drag on this tab, in the folder's own coordinates - the
  /// ones it maps the cursor into before asking which tab is there.
  ///
  /// Where the press went down, when it is on what is DRAWN of the tab; else the centre of that.
  /// Not the centre of the tab: a tab scrolled half out at the strip's left edge has its own centre
  /// at a negative x, which is outside the folder, and a drag whose point finds no tab is read as a
  /// drag of the whole stack.
  Offset? _dragPointFor(Rect tabInGlobal, Offset? pressInGlobal) {
    final box = context.findRenderObject();
    if (box is! RenderBox || !box.hasSize) return null;
    final drawn = _toLocal(box, tabInGlobal).intersect(Offset.zero & box.size);
    if (drawn.isEmpty) return null;
    final press = pressInGlobal == null ? null : box.globalToLocal(pressInGlobal);
    return press != null && drawn.contains(press) ? press : drawn.center;
  }

  /// [global] in the folder's own coordinates, which Java maps to the display for a drop event.
  Offset? _folderPointFor(Offset global) {
    final box = context.findRenderObject();
    return box is RenderBox && box.hasSize ? box.globalToLocal(global) : null;
  }

  static Rect _toLocal(RenderBox box, Rect global) =>
      Rect.fromPoints(box.globalToLocal(global.topLeft), box.globalToLocal(global.bottomRight));

  String _reportedStrip = '';

  /// Tells Java where this side draws each tab (see [stripReport]), only when it changed. False
  /// when the tabs cannot be placed in this frame.
  bool _handleStripLayout(List<DrawnTab> tabs) {
    final box = context.findRenderObject();
    if (box is! RenderBox || !box.hasSize) return true;
    final report = stripReport(box, tabs, layoutSeq: state.stripLayoutSeq ?? 0);
    if (report == null) return false;
    if (report == _reportedStrip) return true;
    _reportedStrip = report;
    widget.sendCTabFolderstripLaidOut(state, VEvent()..text = report);
    return true;
  }

  /// Selects the tab a drag starts from, which a native CTabFolder does on mouse down
  /// (`SwtCTabFolder.onMouse`) before any drag can begin.
  ///
  /// A workbench renders only the selected part of a stack, so a drag from an unselected tab picks
  /// up a part that has no widget yet, and the gesture is dropped or fails on it. An application's
  /// own `dragSetData` needs it too: it identifies the tab being moved through
  /// `folder.getSelection()`, the same idiom Table and Tree DND use.
  void _handleTabDragStarted(int index) {
    _handleTabSelection(index, detail: SWT.DRAG);
  }

  void _handleTabDrop(int insertIndex, int? targetItemId, Offset position) {
    final dropTargetId = state.dropTargetId;
    if (dropTargetId == null) return;
    final dropTargetValue = VDropTarget()..id = dropTargetId;
    final e = VEvent()
      ..x = position.dx.round()
      ..y = position.dy.round()
      ..index = insertIndex;
    if (targetItemId != null) e.itemId = targetItemId;
    DropTargetSwt<VDropTarget>(value: dropTargetValue)
        .sendDropdrop(dropTargetValue, e);
  }

  void _handleTabReorder(int fromIndex, int toIndex) {
    if (state.enabled != true) return;
    if (fromIndex == toIndex) return;
    final e = VEvent()
      ..index = fromIndex
      ..detail = toIndex;
    widget.sendCTabFolderreorderItems(state, e);

    final current = _selectedIndex;
    int newSelected = current;
    if (current == fromIndex) {
      newSelected = toIndex;
    } else if (fromIndex < toIndex && current > fromIndex && current <= toIndex) {
      newSelected = current - 1;
    } else if (fromIndex > toIndex && current >= toIndex && current < fromIndex) {
      newSelected = current + 1;
    }
    if (newSelected != current) {
      setState(() {
        _selectedIndex = newSelected;
        _pendingTabIndex = newSelected;
        state.selection = newSelected;
      });
    }
  }
}

class _CTabBar extends StatefulWidget {
  final VCTabFolder state;
  final CTabFolderSwt folderWidget;
  final int selectedIndex;
  final List<CTabItem> tabItems;
  final double? tabHeight;
  final bool useSimpleStyle;
  /// The folder's outline, which the strip carries along its page-side edge and over the selected
  /// tab; null when the folder draws none.
  final BorderSide? outline;
  /// How much of the laid-out tab row is clipped off on its page side to fit a shorter tabHeight;
  /// the outline runs along the edge that stays visible.
  final double stripClip;
  final VComposite? topRightComposite;
  final ValueChanged<int> onTabSelected;
  final ValueChanged<int> onTabClose;
  final VoidCallback onMinimize;
  final VoidCallback onMaximize;
  final void Function(int fromIndex, int toIndex)? onTabReorder;
  final void Function(int insertIndex, int? targetItemId, Offset position)? onTabDrop;
  final ValueChanged<int>? onTabDragStarted;
  final void Function(BuildContext, CTabFolderThemeExtension widgetTheme)? onChevronShowList;


  /// Where each tab and its close button are drawn, for the side that answers hit tests; false
  /// when they cannot be placed in this frame.
  final bool Function(List<DrawnTab> tabs)? onStripLayout;

  /// The point to report for a drag on a tab, given where that tab is drawn and where the press
  /// went down.
  final Offset? Function(Rect tabInGlobal, Offset? pressInGlobal)? dragPointFor;

  /// A global point in the folder's own coordinates.
  final Offset? Function(Offset global)? folderPointFor;

  const _CTabBar({
    required this.state,
    required this.folderWidget,
    required this.selectedIndex,
    required this.tabItems,
    required this.tabHeight,
    required this.useSimpleStyle,
    this.outline,
    this.stripClip = 0,
    required this.topRightComposite,
    required this.onTabSelected,
    required this.onTabClose,
    required this.onMinimize,
    required this.onMaximize,
    this.onTabReorder,
    this.onTabDrop,
    this.onTabDragStarted,
    this.onChevronShowList,
    this.onStripLayout,
    this.dragPointFor,
    this.folderPointFor,
  });

  @override
  State<_CTabBar> createState() => _CTabBarState();
}

class _CTabBarState extends State<_CTabBar> {
  final GlobalKey<_TabDragRowState> _rowState = GlobalKey<_TabDragRowState>();
  // Hover rebuilds the top-right controls alone; a state change would build every tab with them.
  final ValueNotifier<bool> _hoveringTopBar = ValueNotifier<bool>(false);
  // Rebuilds the scrollbar alone: a state change here would build every tab again with it.
  final ValueNotifier<bool> _scrollbarVisible = ValueNotifier<bool>(false);
  // True when the tab row overflows its width (some tabs scroll off-view). Drives the chevron.
  bool _hasOverflow = false;
  Timer? _scrollbarHideTimer;
  late final ScrollController _horizontalScrollController;
  final ValueNotifier<bool> _minimizeHovered = ValueNotifier<bool>(false);
  final ValueNotifier<bool> _maximizeHovered = ValueNotifier<bool>(false);
  bool _isChevronHovered = false;
  int? _hoveredTabIndex;

  // The whole bar is built again for a hover change, so only for a real one.
  void _hoverTab(int index) {
    if (_hoveredTabIndex != index) setState(() => _hoveredTabIndex = index);
  }

  void _unhoverTab(int index) {
    if (_hoveredTabIndex == index) setState(() => _hoveredTabIndex = null);
  }
  int? _pendingFrom;
  int? _pendingTo;

  // ctabfolder_topright_auto_hide (default true, matches the historical behaviour):
  // the topRight/chevron/minimize/maximize block auto-hides until hovered and floats
  // on top of the tab row without reserving space. Set to false to keep it always
  // shown and reserving its own width in the tab row instead -- a client relying on
  // an always-visible topRight toolbar needs both effects together, so this is one
  // flag, not two: ctabfolder_visible_controls alone only removes the hover-fade, it
  // does not reserve space.
  bool get _autoHide => getConfigFlags().ctabfolder_topright_auto_hide != false;
  bool get _reserveTopRightSpace => !_autoHide;
  bool get _controlsAlwaysVisible =>
      getConfigFlags().ctabfolder_visible_controls == true ||
      _reserveTopRightSpace ||
      // While overflowing, the chevron is the only way to reach the hidden tabs -- keep it visible.
      _hasOverflow;

  @override
  void initState() {
    super.initState();
    _horizontalScrollController = ScrollController();
    _revealSelectedAfterLayout();
  }

  @override
  void didUpdateWidget(_CTabBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_pendingFrom != null) {
      _pendingFrom = null;
      _pendingTo = null;
    }
    if (widget.selectedIndex != oldWidget.selectedIndex ||
        widget.tabItems.length != oldWidget.tabItems.length) {
      _revealSelectedAfterLayout();
    }
  }

  bool _revealPending = false;
  bool _revealing = false;
  double? _revealedViewport;
  double? _revealedExtent;

  /// A native CTabFolder keeps the selected tab in view (`showItem`): where upstream picks which
  /// tabs to show and parks the rest behind the chevron, this strip scrolls to it instead.
  void _revealSelectedAfterLayout() {
    if (_revealPending) return;
    _revealPending = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _revealPending = false;
      if (mounted) _revealSelected();
    });
  }

  void _revealSelected() {
    if (!_horizontalScrollController.hasClients) return;
    final position = _horizontalScrollController.position;
    if (!position.hasContentDimensions || !position.hasViewportDimension) return;
    _revealedViewport = position.viewportDimension;
    _revealedExtent = position.maxScrollExtent;
    final index = (widget.state.single ?? false)
        ? 0
        : _computeEffectiveSelected(widget.selectedIndex);
    final tab = _rowState.currentState?.tabBox(index);
    if (tab == null || !tab.attached || !tab.hasSize) return;
    // The end first, then the start: a tab wider than the strip shows its leading edge.
    _revealing = true;
    try {
      position.ensureVisible(tab, alignmentPolicy: ScrollPositionAlignmentPolicy.keepVisibleAtEnd);
      position.ensureVisible(tab, alignmentPolicy: ScrollPositionAlignmentPolicy.keepVisibleAtStart);
    } finally {
      _revealing = false;
    }
  }

  @override
  void dispose() {
    _scrollbarHideTimer?.cancel();
    _scrollbarVisible.dispose();
    _hoveringTopBar.dispose();
    _minimizeHovered.dispose();
    _maximizeHovered.dispose();
    _horizontalScrollController.dispose();
    super.dispose();
  }

  List<CTabItem> _applyPendingReorder(List<CTabItem> tabs) {
    if (_pendingFrom == null || _pendingTo == null) return tabs;
    if (_pendingFrom! >= tabs.length || _pendingTo! >= tabs.length) return tabs;
    final result = List<CTabItem>.from(tabs);
    result.insert(_pendingTo!, result.removeAt(_pendingFrom!));
    return result;
  }

  int _computeEffectiveSelected(int selected) {
    if (_pendingFrom == null || _pendingTo == null) return selected;
    final from = _pendingFrom!;
    final to = _pendingTo!;
    if (selected == from) return to;
    if (from < to && selected > from && selected <= to) return selected - 1;
    if (from > to && selected >= to && selected < from) return selected + 1;
    return selected;
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(
      context,
    ).extension<CTabFolderThemeExtension>()!;
    final isSingle = widget.state.single ?? false;

    List<CTabItem> visibleTabs = isSingle
        ? (widget.selectedIndex < widget.tabItems.length
            ? [widget.tabItems[widget.selectedIndex]]
            : [])
        : widget.tabItems;

    if (widget.useSimpleStyle) {
      return _buildSimpleTabBar(context, widgetTheme, visibleTabs, widget.tabHeight);
    } else {
      return _buildAdvancedTabBar(
        context,
        widgetTheme,
        visibleTabs,
        widget.tabHeight,
        widget.topRightComposite,
      );
    }
  }

  Widget _buildSimpleTabBar(
    BuildContext context,
    CTabFolderThemeExtension widgetTheme,
    List<CTabItem> tabs,
    double? height,
  ) {
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM;
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
                    isSelected: index == widget.selectedIndex,
                    isHovered: index == _hoveredTabIndex,
                    tab: tab,
                    onTap: () => widget.onTabSelected(index),
                    onClose: tab.showCloseButton
                        ? () => widget.onTabClose(index)
                        : null,
                    isTabBottom: isTabBottom,
                    onHoverEnter: () => _hoverTab(index),
                    onHoverExit: () => _unhoverTab(index),
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

  /// A native CTabFolder draws its chevron, minimize and maximize buttons as a ToolBar of its own,
  /// which takes the pointer there: the folder reports no press, release, move or double click over
  /// them.
  Widget _ownToolbarPointer(Widget buttons) {
    return Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: ControlImpl.pointerTakenBelow,
      onPointerUp: ControlImpl.pointerTakenBelow,
      onPointerMove: ControlImpl.pointerTakenBelow,
      child: buttons,
    );
  }

  Widget _buildAdvancedTabBar(
    BuildContext context,
    CTabFolderThemeExtension widgetTheme,
    List<CTabItem> tabs,
    double? height,
    VComposite? topRightComposite,
  ) {
    final isTabBottom = widget.state.tabPosition == SWT.BOTTOM;
    final showMinimizeButton = widget.state.minimizeVisible ?? false;
    final showMaximizeButton = widget.state.maximizeVisible ?? false;
    final isMinimized = widget.state.minimized ?? false;
    final isMaximized = widget.state.maximized ?? false;
    final topRightAlignment = widget.state.topRightAlignment ?? SWT.RIGHT;

    final displayTabs = _applyPendingReorder(tabs);
    final effectiveSelected = _computeEffectiveSelected(widget.selectedIndex);

    final tabChildren = displayTabs.asMap().entries.map((entry) {
      final index = entry.key;
      final tab = entry.value;
      return _buildAdvancedTab(
        context: context,
        widgetTheme: widgetTheme,
        isSelected: index == effectiveSelected,
        isHovered: index == _hoveredTabIndex,
        tab: tab,
        onTap: () => widget.onTabSelected(index),
        onClose: tab.showCloseButton ? () => widget.onTabClose(index) : null,
        isTabBottom: isTabBottom,
        onHoverEnter: () => _hoverTab(index),
        onHoverExit: () => _unhoverTab(index),
      );
    }).toList();

    final tabRowWithDrag = TabDragRow(
      key: _rowState,
      onStripLayout: widget.onStripLayout,
      dragPointFor: widget.dragPointFor,
      folderPointFor: widget.folderPointFor,
      children: tabChildren,
      tabs: displayTabs,
      folderState: widget.state,
      folderWidget: widget.folderWidget,
      tabHeight: widget.tabHeight,
      rowWrapper: ctabTabScroll()
          ? (row) => _buildHorizontalScrollableTabs(widgetTheme: widgetTheme, child: row)
          : (row) => ClipRect(child: SizedBox(width: double.infinity, child: row)),
      stripBuilder: ctabTabScroll()
          ? null
          : (key, children) => CompressedTabRow(
                key: key,
                selectedIndex: effectiveSelected,
                onOverflowChanged: _setOverflow,
                onTabsMoved: () => _rowState.currentState?.scheduleStripReport(),
                overflowReserve: _overflowChevronWidth(widgetTheme),
                children: children,
              ),
      onReorder: (from, to) {
        setState(() {
          _pendingFrom = from;
          _pendingTo = to;
        });
        widget.onTabReorder?.call(from, to);
      },
      onDrop: widget.onTabDrop,
      onTabDragStarted: widget.onTabDragStarted,
      onDragStart: () => setState(() => _hoveredTabIndex = null),
    );

    final scrollableTabs = Expanded(child: tabRowWithDrag);

    // The height the tab row keeps on screen once cut to a fixed tabHeight.
    final double? shownHeight = height == null ? null : height - widget.stripClip;
    final wrappedBounds = shownHeight == null
        ? null
        : _wrappedTopRightBounds(topRightComposite, topRightAlignment, shownHeight);

    final topRightControls = _buildTopRightControls(
      context: context,
      widgetTheme: widgetTheme,
      topRightComposite: wrappedBounds == null ? topRightComposite : null,
      showMinimizeButton: showMinimizeButton,
      showMaximizeButton: showMaximizeButton,
      isMinimized: isMinimized,
      isMaximized: isMaximized,
      alignment: topRightAlignment,
    );

    // ctabfolder_topright_auto_hide=false: the controls sit as a normal Row
    // sibling, so the scrollable tab area shrinks to make room for them -- no tab
    // ever ends up hidden underneath a floating overlay. Default (true): the
    // original Stack overlay, full-width tabs with the (auto-hidden, hover-revealed)
    // controls on top.
    // While overflowing, lay the controls out as a Row sibling so the chevron reserves space
    // and stays clickable, instead of floating over the tab row where clicks get swallowed.
    // Compressed tabs always leave the controls their width, as CTabFolder.getRightItemEdge does.
    final Widget header = (_reserveTopRightSpace || _hasOverflow || !ctabTabScroll())
        ? Row(children: [scrollableTabs, topRightControls])
        : Stack(
            children: [
              Row(children: [scrollableTabs]),
              Positioned(right: 0, top: 0, bottom: 0, child: topRightControls),
            ],
          );

    Widget bar = header;
    double? barHeight = height;
    if (wrappedBounds != null) {
      final (row, rowHeight) = _buildWrappedTopRightRow(
        topRightComposite!,
        wrappedBounds,
        shownHeight!,
        isTabBottom,
        widgetTheme.tabBarBackgroundColor,
      );
      final tabRow = _clipToShownHeight(SizedBox(height: height, child: header), height!, isTabBottom);
      bar = Column(children: isTabBottom ? [row, tabRow] : [tabRow, row]);
      barHeight = shownHeight + rowHeight;
    }

    final strip = MouseRegion(
      // A held button keeps the mouse with the control it was pressed on: a drag passing over the
      // bar reveals none of its controls; the first move after release does.
      onEnter: (_) {
        if (!ActiveDragTracker.isSuppressingHover) _hoveringTopBar.value = true;
      },
      onHover: (_) {
        if (!ActiveDragTracker.isSuppressingHover) _hoveringTopBar.value = true;
      },
      onExit: (_) => _hoveringTopBar.value = false,
      child: _buildTabBarContainer(
        widgetTheme: widgetTheme,
        height: barHeight,
        child: bar,
        isTabBottom: isTabBottom,
        // A wrapped row keeps the bar's page-side edge on screen.
        clipped: wrappedBounds == null ? _clippedEdge(isTabBottom) : EdgeInsets.zero,
      ),
    );
    if (wrappedBounds != null || height == null) return strip;
    return _clipToShownHeight(strip, height, isTabBottom);
  }

  /// [child], laid out [height] tall, cut on its page side to the tabHeight Java fixed.
  Widget _clipToShownHeight(Widget child, double height, bool isTabBottom) {
    if (widget.stripClip == 0) return child;
    return SizedBox(
      height: height - widget.stripClip,
      child: ClipRect(
        child: OverflowBox(
          alignment: isTabBottom ? Alignment.bottomCenter : Alignment.topCenter,
          minHeight: 0,
          maxHeight: height,
          child: child,
        ),
      ),
    );
  }

  /// Java's bounds for a SWT.WRAP topRight control it moved off the tab row onto a row of its own,
  /// or null while the control sits in the tab row.
  VRectangle? _wrappedTopRightBounds(VComposite? composite, int alignment, double tabRowHeight) {
    final bounds = composite?.bounds;
    if (bounds == null || (alignment & SWT.WRAP) == 0) return null;
    if (widget.state.tabPosition == SWT.BOTTOM) {
      final folder = widget.state.bounds;
      if (folder == null) return null;
      return bounds.y + bounds.height <= folder.height - tabRowHeight ? bounds : null;
    }
    return bounds.y >= tabRowHeight ? bounds : null;
  }

  /// The row a native CTabFolder wraps the topRight control onto, beside the tab row, with the
  /// control drawn at Java's bounds so what Java opens under it lands under it. Returns the row and
  /// its height.
  (Widget, double) _buildWrappedTopRightRow(
    VComposite composite,
    VRectangle bounds,
    double tabRowHeight,
    bool isTabBottom,
    Color backgroundColor,
  ) {
    final controlHeight = bounds.height.toDouble();
    final double top;
    final double rowHeight;
    if (isTabBottom) {
      final folderHeight = widget.state.bounds!.height.toDouble();
      top = 0;
      rowHeight = math.max(controlHeight, folderHeight - tabRowHeight - bounds.y);
    } else {
      top = math.max(0, bounds.y - tabRowHeight);
      rowHeight = top + controlHeight;
    }
    final row = SizedBox(
      height: rowHeight,
      child: Stack(
        children: [
          Positioned(
            left: bounds.x.toDouble(),
            top: top,
            width: bounds.width.toDouble(),
            height: controlHeight,
            child: _buildTopRightComposite(composite, backgroundColor: backgroundColor),
          ),
        ],
      ),
    );
    return (row, rowHeight);
  }

  Widget _buildSimpleTab({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required bool isHovered,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
    required VoidCallback onHoverEnter,
    required VoidCallback onHoverExit,
  }) {
    final enabled = widget.state.enabled ?? false;

    final resolvedSelectionBackground = getBackgroundColor(
      background: widget.state.selectionBackground,
      defaultColor: widgetTheme.tabSelectedBackgroundColor,
    ) ?? widgetTheme.tabSelectedBackgroundColor;
    final resolvedSelectionForeground = getForegroundColor(
      context: context,
      foreground: widget.state.selectionForeground,
      defaultColor: widgetTheme.tabSelectedTextColor,
      minContrast: widgetTheme.minTextContrast,
      background: resolvedSelectionBackground,
    );

    final themeTextColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    // The folder's own colours win over the theme's when the application set them.
    final textColor = getForegroundColor(
      foreground: widget.state.foreground,
      defaultColor: themeTextColor,
      context: context,
      minContrast: widgetTheme.minTextContrast,
    );
    final themeBackground = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      isHovered: isHovered,
    );
    final backgroundColor = isSelected
        ? themeBackground
        : (getBackgroundColor(
              background: widget.state.background,
              defaultColor: themeBackground,
              context: context,
            ) ??
            themeBackground);
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: getCTabTextStyle(widgetTheme, isSelected, enabled),
    );

    final border = Border(
      right: BorderSide(
        color: borderColor,
        width: widgetTheme.tabBorderWidth,
      ),
      bottom: !isTabBottom && isSelected && enabled
          ? BorderSide(
              color: backgroundColor,
              width:
                  (widget.state.selectionBarThickness != null &&
                      widget.state.selectionBarThickness! > 0)
                  ? widget.state.selectionBarThickness!.toDouble()
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
              width:
                  (widget.state.selectionBarThickness != null &&
                      widget.state.selectionBarThickness! > 0)
                  ? widget.state.selectionBarThickness!.toDouble()
                  : widgetTheme.tabSelectedBorderWidth,
            )
          : isTabBottom
          ? BorderSide(
              color: borderColor,
              width: widgetTheme.tabBorderWidth,
            )
          : BorderSide.none,
    );

    // The tab body gets a semantics node of its own, so the tab's node has a child - and
    // therefore a settled role - from the first frame. A tappable node with no children is a
    // button to Flutter Web, and it turns into a plain container once children turn up; the
    // engine answers that role change by rebuilding the node's DOM element and writing back only
    // the properties that changed with it, silently dropping the identifier the tab is found by.
    return Semantics(identifier: '${tab.vItem.swt}/${tab.vItem.id}', child: Semantics(container: true, child: MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      // A held button keeps the mouse with the control it was pressed on, so a drag passing over
      // the tabs lights none of them; the first move after release does.
      onEnter: (_) {
        if (!ActiveDragTracker.isSuppressingHover) onHoverEnter();
      },
      onHover: (_) {
        if (!isHovered && !ActiveDragTracker.isSuppressingHover) onHoverEnter();
      },
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        child: CustomPaint(
          foregroundPainter: _RectangularSides(border),
          child: QuietAnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ).add(border.dimensions),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: isTabBottom
                ? BorderRadius.only(
                    bottomLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    bottomRight: Radius.circular(widgetTheme.tabBorderRadius),
                  )
                : BorderRadius.only(
                    topLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    topRight: Radius.circular(widgetTheme.tabBorderRadius),
                  ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              tab.customContent ??
                  Text(
                    _getTabText(tab.label),
                    style:
                        textStyle?.copyWith(color: textColor) ??
                        TextStyle(color: textColor),
                  ),
              if (onClose != null) ...[
                SizedBox(width: widgetTheme.tabCloseButtonSpacing),
                GestureDetector(
                  onTap: onClose,
                  child: Icon(
                    Icons.close,
                    size: widgetTheme.tabCloseIconSize,
                    color: widgetTheme.tabCloseButtonColor,
                  ),
                ),
              ],
            ],
          ),
          ),
        ),
      ),
    )));
  }

  Widget _buildAdvancedTab({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required bool isSelected,
    required bool isHovered,
    required CTabItem tab,
    required VoidCallback onTap,
    VoidCallback? onClose,
    required bool isTabBottom,
    required VoidCallback onHoverEnter,
    required VoidCallback onHoverExit,
  }) {
    final showUnselectedClose = widget.state.unselectedCloseVisible ?? false;
    final shouldShowClose =
        (onClose != null) && (isSelected || showUnselectedClose);

    final showUnselectedImage = widget.state.unselectedImageVisible ?? false;
    final showSelectedImage = widget.state.selectedImageVisible ?? true;
    final shouldShowImage =
        (isSelected && showSelectedImage) ||
        (!isSelected && showUnselectedImage);

    final showHighlight = widget.state.highlightEnabled ?? false;
    final enabled = widget.state.enabled ?? false;
    final compressTo = ctabTabScroll()
        ? null
        : widget.state.minimumCharacters ?? ctabDefaultMinimumCharacters;
    final useDefaultTheme = getConfigFlags().theme_name == null;
    final active = showHighlight && (widget.state.highlight ?? false);

    final resolvedSelectionBackground = getBackgroundColor(
      background: widget.state.selectionBackground,
      defaultColor: widgetTheme.tabSelectedBackgroundColor,
    ) ?? widgetTheme.tabSelectedBackgroundColor;
    final resolvedSelectionForeground = getForegroundColor(
      context: context,
      foreground: widget.state.selectionForeground,
      defaultColor: widgetTheme.tabSelectedTextColor,
      minContrast: widgetTheme.minTextContrast,
      background: resolvedSelectionBackground,
    );

    final themeTextColor = getCTabTextColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionForeground: resolvedSelectionForeground,
      isHovered: isHovered,
    );
    // The folder's own colours win over the theme's when the application set them.
    final textColor = getForegroundColor(
      foreground: widget.state.foreground,
      defaultColor: themeTextColor,
      context: context,
      minContrast: widgetTheme.minTextContrast,
    );
    final themeBackground = getCTabBackgroundColor(
      widgetTheme,
      isSelected,
      enabled,
      resolvedSelectionBackground: resolvedSelectionBackground,
      useDefaultTheme: useDefaultTheme,
      isHovered: isHovered,
    );
    final backgroundColor = isSelected
        ? themeBackground
        : (getBackgroundColor(
              background: widget.state.background,
              defaultColor: themeBackground,
              context: context,
            ) ??
            themeBackground);
    final borderColor = getCTabBorderColor(widgetTheme, enabled, isHovered: isHovered);
    final textStyle = getTextStyle(
      context: context,
      font: widget.state.font,
      textColor: textColor,
      baseTextStyle: getCTabTextStyle(widgetTheme, isSelected, enabled),
    );

    final selectionBarSide = BorderSide(
      color: backgroundColor,
      width: (widget.state.selectionBarThickness != null && widget.state.selectionBarThickness! > 0)
          ? widget.state.selectionBarThickness!.toDouble()
          : widgetTheme.tabSelectedBorderWidth,
    );
    final outline = widget.outline;
    final highlightSide = isSelected && enabled && showHighlight
        ? BorderSide(color: widgetTheme.tabHighlightColor, width: widgetTheme.tabHighlightBorderWidth)
        : null;
    // Outlined, the selected tab is closed on three sides and open into its page; every other tab
    // carries the outline along the strip's page-side edge.
    final Border? outlined = outline == null
        ? null
        : isSelected
        ? Border(
            left: outline,
            right: outline,
            top: isTabBottom ? selectionBarSide : highlightSide ?? outline,
            bottom: isTabBottom ? highlightSide ?? outline : selectionBarSide,
          )
        : Border(
            right: BorderSide(color: borderColor, width: widgetTheme.tabBorderWidth),
            top: isTabBottom ? outline : BorderSide.none,
            bottom: isTabBottom ? BorderSide.none : outline,
          );
    final border = outlined ?? Border(
      top: !isTabBottom && isSelected && enabled && showHighlight
          ? BorderSide(
              color: widgetTheme.tabHighlightColor,
              width: widgetTheme.tabHighlightBorderWidth,
            )
          : isTabBottom && isSelected && enabled
          ? BorderSide(
              color: backgroundColor,
              width:
                  (widget.state.selectionBarThickness != null &&
                      widget.state.selectionBarThickness! > 0)
                  ? widget.state.selectionBarThickness!.toDouble()
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
              width:
                  (widget.state.selectionBarThickness != null &&
                      widget.state.selectionBarThickness! > 0)
                  ? widget.state.selectionBarThickness!.toDouble()
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
    );

    // The tab body gets a semantics node of its own, so the tab's node has a child - and
    // therefore a settled role - from the first frame. A tappable node with no children is a
    // button to Flutter Web, and it turns into a plain container once children turn up; the
    // engine answers that role change by rebuilding the node's DOM element and writing back only
    // the properties that changed with it, silently dropping the identifier the tab is found by.
    return Semantics(identifier: '${tab.vItem.swt}/${tab.vItem.id}', child: Semantics(container: true, child: MouseRegion(
      cursor: enabled ? SystemMouseCursors.click : SystemMouseCursors.basic,
      // A held button keeps the mouse with the control it was pressed on, so a drag passing over
      // the tabs lights none of them; the first move after release does.
      onEnter: (_) {
        if (!ActiveDragTracker.isSuppressingHover) onHoverEnter();
      },
      onHover: (_) {
        if (!isHovered && !ActiveDragTracker.isSuppressingHover) onHoverEnter();
      },
      onExit: (_) => onHoverExit(),
      child: GestureDetector(
        onTap: onTap,
        child: CustomPaint(
          foregroundPainter: _RectangularSides(
            border,
            clipped: outline == null ? EdgeInsets.zero : _clippedEdge(isTabBottom),
            ring: getCTabFocusRingSide(
              widgetTheme,
              selected: isSelected,
              enabled: enabled,
              active: active,
            ),
          ),
          child: QuietAnimatedContainer(
          duration: widgetTheme.hoverRevealDuration,
          curve: Curves.easeOut,
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.tabHorizontalPadding,
            vertical: widgetTheme.tabVerticalPadding,
          ).add(border.dimensions),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: isTabBottom
                ? BorderRadius.only(
                    bottomLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    bottomRight: Radius.circular(widgetTheme.tabBorderRadius),
                  )
                : BorderRadius.only(
                    topLeft: Radius.circular(widgetTheme.tabBorderRadius),
                    topRight: Radius.circular(widgetTheme.tabBorderRadius),
                  ),
            image: isSelected && enabled ? _buildSelectionBgImage() : null,
          ),
          // Centred on the whole tab, which a border on one edge only would push off, and on the
          // part of it left on screen once the row is cut to the strip's height.
          child: Transform.translate(
            offset: _clippedEdgeShift(isTabBottom) +
                Offset(0, (border.bottom.width - border.top.width) / 2),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _shrinkable(compressTo, tab.customContent != null
                    ? TabItemContextProvider(
                        isSelected: isSelected,
                        isEnabled: enabled,
                        isActive: isSelected && enabled && active,
                        isDimmed: isSelected && enabled && showHighlight && !active,
                        compressMinimumCharacters: compressTo,
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
                              child: FutureBuilder<Widget?>(
                                future: ImageUtils.buildVImageAsync(
                                  VImage()..filename = tab.image,
                                  size: widgetTheme.tabIconSize,
                                  color: textColor,
                                ),
                                builder: (context, snapshot) {
                                  final resolved = snapshot.data;
                                  if (resolved != null) return resolved;
                                  if (snapshot.connectionState !=
                                      ConnectionState.done) {
                                    return SizedBox(
                                      width: widgetTheme.tabIconSize,
                                      height: widgetTheme.tabIconSize,
                                    );
                                  }
                                  return Image.file(
                                    File(tab.image!),
                                    width: widgetTheme.tabIconSize,
                                    height: widgetTheme.tabIconSize,
                                    fit: BoxFit.contain,
                                  );
                                },
                              ),
                            ),
                          _shrinkable(compressTo, Padding(
                            padding: EdgeInsets.only(
                              bottom: widgetTheme.tabTextBottomPadding,
                            ),
                            child: compressTo == null
                                ? Text(
                                    _getTabText(tab.label),
                                    style: textStyle?.copyWith(color: textColor) ??
                                        TextStyle(color: textColor),
                                  )
                                : ShrinkableTabLabel(
                                    tab.label,
                                    style: textStyle?.copyWith(color: textColor) ??
                                        TextStyle(color: textColor),
                                    minimumCharacters: compressTo,
                                  ),
                          )),
                        ],
                      )),
                if (shouldShowClose) ...[
                  SizedBox(width: widgetTheme.tabCloseButtonSpacing),
                  MouseRegion(
                    cursor: enabled
                        ? SystemMouseCursors.click
                        : SystemMouseCursors.basic,
                    child: MetaData(
                      metaData: _tabCloseButton,
                      behavior: HitTestBehavior.opaque,
                      child: GestureDetector(
                        onTap: onClose,
                        child: Padding(
                          padding: EdgeInsets.only(
                            bottom: widgetTheme.tabCloseIconBottomPadding,
                          ),
                          child: Icon(
                            Icons.close,
                            size: widgetTheme.tabCloseIconSize,
                            color: getCTabCloseButtonColor(widgetTheme, isSelected),
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
          ),
        ),
      ),
    )));
  }

  Widget _buildChevronButton(CTabFolderThemeExtension widgetTheme) {
    return Semantics(identifier: 'chevron:${widget.state.swt}/${widget.state.id}', button: true, child: MouseRegion(
      onEnter: (_) => setState(() => _isChevronHovered = true),
      onExit: (_) => setState(() => _isChevronHovered = false),
      cursor: SystemMouseCursors.click,
      child: Builder(builder: (buttonContext) => GestureDetector(
        onTap: () => _handleChevronTap(buttonContext, widgetTheme),
        child: Container(
          width: _chevronWidth(widgetTheme),
          height: double.infinity,
          padding: EdgeInsets.symmetric(
            horizontal: widgetTheme.controlButtonHorizontalPadding,
          ),
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: _isChevronHovered
                ? widgetTheme.controlButtonHoverColor.withOpacity(0.1)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(4),
          ),
          child: Icon(
            Icons.expand_more,
            size: widgetTheme.controlButtonSize,
            color: widgetTheme.controlButtonColor,
          ),
        ),
      )),
    ));
  }

  /// Native SWT's showChevron: chevronVisible (default true) and either forced on or overflowing.
  bool get _showsChevron =>
      (widget.state.chevronVisible ?? true) &&
      ((widget.state.showChevron ?? false) || _hasOverflow);

  double _chevronWidth(CTabFolderThemeExtension widgetTheme) =>
      widgetTheme.controlButtonSize + 2 * widgetTheme.controlButtonHorizontalPadding;

  /// The chevron's width while it shows only because the tabs overflow, else 0.
  double _overflowChevronWidth(CTabFolderThemeExtension widgetTheme) =>
      _showsChevron && !(widget.state.showChevron ?? false) ? _chevronWidth(widgetTheme) : 0;

  void _handleChevronTap(BuildContext buttonContext, CTabFolderThemeExtension widgetTheme) {
    widget.onChevronShowList?.call(buttonContext, widgetTheme);
  }

  /// The controls row gives its children unbounded width, and the toolbar's own row fills whatever
  /// it gets: unbounded, it cannot be laid out, and neither can the strip or the page below it.
  /// The width Java gave the control bounds it, and arrives on the control's own channel.
  Widget _buildTopRightComposite(VComposite composite, {Color? backgroundColor}) {
    return ListenableBuilder(
      listenable: VRegistry.instance.boundsChangesAt([VRegistry.keyOf(composite)]),
      builder: (context, toolbar) {
        final bounds = composite.bounds;
        return SizedBox(width: hasBounds(bounds) ? bounds!.width.toDouble() : 0, child: toolbar);
      },
      child: ToolbarComposite(value: composite, backgroundColor: backgroundColor),
    );
  }

  Widget _buildControlButton({
    required BuildContext context,
    required CTabFolderThemeExtension widgetTheme,
    required String semanticsId,
    required IconData icon,
    required VoidCallback onTap,
    required bool isHovered,
    required ValueChanged<bool> onHoverChanged,
    bool enabled = true,
  }) {
    return Semantics(identifier: semanticsId, button: true, child: MouseRegion(
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
          // The min/max and chevron buttons are no Tab stops of their own: Tab stops on the folder.
          canRequestFocus: false,
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
              color: isHovered
                  ? widgetTheme.controlButtonHoverColor.withOpacity(0.1)
                  : Colors.transparent,
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
    ));
  }


  Widget _buildHorizontalScrollableTabs({
    required CTabFolderThemeExtension widgetTheme,
    required Widget child,
  }) {
    return LayeredLayoutBuilder(
      builder: (context, constraints) {
        // Re-measure overflow after each frame (a toggled _hasOverflow rebuilds this subtree).
        WidgetsBinding.instance.addPostFrameCallback((_) => _checkOverflow());
        return NotificationListener<SizeChangedLayoutNotification>(
          // The row widens as tab icons load async without rebuilding this ancestor, so
          // re-measure on each size change instead of relying on a one-shot check.
          onNotification: (notification) {
            WidgetsBinding.instance.addPostFrameCallback((_) => _checkOverflow());
            // A tab that changes width moves the ones after it without rebuilding the row.
            _rowState.currentState?.scheduleStripReport();
            return false;
          },
          child: NotificationListener<ScrollUpdateNotification>(
            onNotification: (notification) {
              if (!_revealing) {
                _showScrollbar();
                _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
              }
              // Scrolling moves the tabs without rebuilding the row, so the row is asked to say
              // where they are once this frame has painted them at the new offset.
              _rowState.currentState?.scheduleStripReport();
              return false;
            },
            child: MouseRegion(
              // A held button keeps the mouse with the control it was pressed on: a drag passing
              // over the strip does not reveal its scrollbar.
              onEnter: (_) {
                if (!ActiveDragTracker.isSuppressingHover) _showScrollbar();
              },
              onExit: (_) {
                _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
              },
              child: Listener(
                onPointerDown: (_) {
                  _showScrollbar();
                },
                // A vertical wheel doesn't scroll a horizontal viewport by default; map it
                // to horizontal offset.
                onPointerSignal: (event) {
                  if (event is! PointerScrollEvent) return;
                  if (!_horizontalScrollController.hasClients) return;
                  final position = _horizontalScrollController.position;
                  if (!position.hasContentDimensions ||
                      position.maxScrollExtent <= 0) {
                    return;
                  }
                  final delta = event.scrollDelta.dy != 0
                      ? event.scrollDelta.dy
                      : event.scrollDelta.dx;
                  final target = (position.pixels + delta)
                      .clamp(0.0, position.maxScrollExtent);
                  if (target != position.pixels) {
                    _horizontalScrollController.jumpTo(target);
                    _showScrollbar();
                    _hideScrollbarAfterDelay(widgetTheme.scrollbarHideDelay);
                  }
                },
                child: ValueListenableBuilder<bool>(
                  valueListenable: _scrollbarVisible,
                  builder: (context, visible, strip) => Scrollbar(
                    controller: _horizontalScrollController,
                    thumbVisibility: visible,
                    thickness: widgetTheme.tabScrollbarThickness,
                    child: strip!,
                  ),
                  child: SingleChildScrollView(
                    controller: _horizontalScrollController,
                    scrollDirection: Axis.horizontal,
                    child: ConstrainedBox(
                      constraints: BoxConstraints(minWidth: constraints.maxWidth),
                      child: SizeChangedLayoutNotifier(child: child),
                    ),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  // Surface the chevron once the tab row is wider than its viewport (read from the scroll
  // controller, since web-mode Java can't measure the viewport).
  void _checkOverflow() {
    if (!mounted) return;
    final position = _horizontalScrollController.hasClients
        ? _horizontalScrollController.position
        : null;
    final overflow = position != null &&
        position.hasContentDimensions &&
        position.maxScrollExtent > 0.5;
    if (overflow != _hasOverflow) {
      setState(() => _hasOverflow = overflow);
    }
    // A strip that starts to overflow, narrows, or has a tab grow (a dirty marker, the bold label
    // of an active folder) can leave the selected tab out. A user scroll changes neither.
    if (overflow && position.hasViewportDimension &&
        (position.viewportDimension != _revealedViewport ||
            position.maxScrollExtent != _revealedExtent)) {
      _revealSelectedAfterLayout();
    }
  }

  void _setOverflow(bool overflow) {
    if (mounted && overflow != _hasOverflow) setState(() => _hasOverflow = overflow);
  }

  void _hideScrollbarAfterDelay(Duration delay) {
    _scrollbarHideTimer?.cancel();
    _scrollbarHideTimer = Timer(delay, () {
      if (mounted) _scrollbarVisible.value = false;
    });
  }

  // Only a strip wider than its viewport has a thumb to show; revealing one otherwise animates a
  // scrollbar that paints nothing.
  void _showScrollbar() {
    if (!_hasOverflow) return;
    _scrollbarHideTimer?.cancel();
    _scrollbarVisible.value = true;
  }

  /// The part of the laid-out strip the folder clips off on its page side.
  EdgeInsets _clippedEdge(bool isTabBottom) => isTabBottom
      ? EdgeInsets.only(top: widget.stripClip)
      : EdgeInsets.only(bottom: widget.stripClip);

  /// How far a tab's content moves to sit in the middle of what stays of the row after the cut.
  Offset _clippedEdgeShift(bool isTabBottom) =>
      Offset(0, (isTabBottom ? 1 : -1) * widget.stripClip / 2);

  Widget _buildTabBarContainer({
    required CTabFolderThemeExtension widgetTheme,
    required double? height,
    required Widget child,
    required bool isTabBottom,
    EdgeInsets? clipped,
  }) {
    final borderVisible = widget.state.borderVisible ?? true;
    final outline = widget.outline;
    final decoration = BoxDecoration(
      // The band the tabs sit on is the folder's own ground: an application that coloured the
      // CTabFolder expects it here, not just behind the selected tab.
      color: getBackgroundColor(
            background: widget.state.background,
            defaultColor: widgetTheme.tabBarBackgroundColor,
            context: context,
          ) ??
          widgetTheme.tabBarBackgroundColor,
      border: borderVisible && outline == null
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

    // Painted under the tabs, so the selected tab's own ground leaves it open into its page.
    if (outline != null) {
      child = CustomPaint(
        painter: _RectangularSides(
          Border(
            top: isTabBottom ? outline : BorderSide.none,
            bottom: isTabBottom ? BorderSide.none : outline,
          ),
          clipped: clipped ?? _clippedEdge(isTabBottom),
        ),
        child: child,
      );
    }

    if (height != null) {
      return Container(height: height, decoration: decoration, child: child);
    }

    return IntrinsicHeight(
      child: Container(decoration: decoration, child: child),
    );
  }

  /// [child] free to shrink below its natural width when the strip compresses its tabs.
  static Widget _shrinkable(int? compressTo, Widget child) =>
      compressTo == null ? child : Flexible(child: child);

  String _getTabText(String label) {
    final minChars = widget.state.minimumCharacters;
    if (minChars != null && minChars > 0 && label.length > minChars) {
      return '${label.substring(0, minChars)}...';
    }
    return label;
  }

  DecorationImage? _buildSelectionBgImage() {
    final image = widget.state.selectionBgImage;
    if (image == null) return null;

    if (image.filename != null && image.filename!.isNotEmpty) {
      try {
        return DecorationImage(
          image: FileImage(File(image.filename!)),
          fit: BoxFit.cover,
        );
      } catch (e) {
        return null;
      }
    }

    return null;
  }

  /// The width Java leaves between the topRight control and the folder's right edge, or 0 when it
  /// does not place the control from that edge.
  double _javaTrailingWidth(VComposite? topRightComposite, int alignment) {
    final folder = widget.state.bounds;
    final control = topRightComposite?.bounds;
    if (folder == null || control == null) return 0;
    if ((alignment & SWT.FILL) != 0 || alignment == SWT.CENTER) return 0;
    final trailing = folder.width - (control.x + control.width);
    return trailing > 0 ? trailing.toDouble() : 0;
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
    final showChevron = _showsChevron;
    final hasControls =
        topRightComposite != null || showMinimizeButton || showMaximizeButton ||
        showChevron;
    if (!hasControls) return const SizedBox.shrink();

    final scaled = ListenableBuilder(
      listenable: Listenable.merge([_hoveringTopBar, _minimizeHovered, _maximizeHovered]),
      builder: (context, _) {
        final controls = Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (showChevron)
              _ownToolbarPointer(_buildChevronButton(widgetTheme)),
            if (topRightComposite != null)
              _buildTopRightComposite(
                topRightComposite,
                backgroundColor: widgetTheme.tabBarBackgroundColor,
              ),
            // Java packs the folder's own buttons to the right of the topRight control and places the
            // control from that edge; the buttons take the same room here, so the control is drawn where
            // toDisplay() says it is.
            _ownToolbarPointer(ConstrainedBox(
              constraints: BoxConstraints(minWidth: _javaTrailingWidth(topRightComposite, alignment)),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                if (showMinimizeButton)
                  Builder(
                    builder: (context) {
                      final isVisible = _controlsAlwaysVisible ? true : _hoveringTopBar.value;
                      return _buildControlButton(
                        context: context,
                        widgetTheme: widgetTheme,
                        semanticsId: 'minimize:${widget.state.swt}/${widget.state.id}',
                        icon: isMinimized ? Icons.maximize : Icons.minimize,
                        onTap: isVisible ? widget.onMinimize : () {},
                        isHovered: _minimizeHovered.value,
                        onHoverChanged: (hovered) {
                          _minimizeHovered.value = hovered;
                        },
                        enabled: true,
                      );
                    },
                  ),
                if (showMaximizeButton)
                  Builder(
                    builder: (context) {
                      final isVisible = _controlsAlwaysVisible ? true : _hoveringTopBar.value;
                      return _buildControlButton(
                        context: context,
                        widgetTheme: widgetTheme,
                        semanticsId: 'maximize:${widget.state.swt}/${widget.state.id}',
                        icon: isMaximized ? Icons.fullscreen_exit : Icons.fullscreen,
                        onTap: isVisible ? widget.onMaximize : () {},
                        isHovered: _maximizeHovered.value,
                        onHoverChanged: (hovered) {
                          _maximizeHovered.value = hovered;
                        },
                        enabled: true,
                      );
                    },
                  ),
                ],
              ),
            )),
          ],
        );

        final isControlsVisible = _controlsAlwaysVisible ? true : _hoveringTopBar.value;

        final revealWidget = _HoverReveal(
          visible: isControlsVisible,
          revealDuration: widgetTheme.hoverRevealDuration,
          hideDuration: widgetTheme.hoverHideDuration,
          child: Align(
            alignment: Alignment.topRight,
            // Opacity alone doesn't stop hit-testing: while faded out (not hovering)
            // this box still sat on top of the Stack and swallowed clicks meant for
            // a tab hidden underneath it. Only block pointers while genuinely hidden.
            child: IgnorePointer(
              ignoring: !isControlsVisible,
              child: Container(
                height: double.infinity,
                decoration: BoxDecoration(
                  color: widgetTheme.tabBarBackgroundColor,
                  boxShadow: [
                    BoxShadow(
                      color: widgetTheme.topRightControlsShadowColor.withOpacity(
                        widgetTheme.topRightControlsShadowOpacity,
                      ),
                      blurRadius: widgetTheme.topRightControlsShadowBlurRadius,
                      offset: widgetTheme.topRightControlsShadowOffset,
                    ),
                  ],
                ),
                child: controls,
              ),
            ),
          ),
        );

        return Transform.scale(
          scale: widgetTheme.controlButtonScale,
          alignment: Alignment.topRight,
          child: revealWidget,
        );
      },
    );

    if (alignment == SWT.CENTER) {
      return Expanded(child: Center(child: scaled));
    } else {
      return scaled;
    }
  }
}

/// Where a dragged tab would land, drawn on the folder that would receive it.
///
/// Caret heads, not a bare rule: the mark sits in the gap between two tabs, and a plain line there
/// reads as a tab border as easily as an insertion point. The heads overhang the row, so the width
/// is wider than the bar and the marker is positioned by its bar rather than by its box.
class CTabInsertionMarker extends StatelessWidget {
  const CTabInsertionMarker({super.key, required this.color, required this.shown});

  final Color color;

  /// Whether the marker is drawn: a drag over the strip shows it, and it stays mounted otherwise.
  final ValueListenable<bool> shown;

  static const double barWidth = 3;
  static const double caretWidth = 4;
  static const double caretHeight = 4;

  /// How far left of the insertion point the widget starts, its bar being what must land there.
  static const double overhang = caretWidth + barWidth / 2;

  @override
  Widget build(BuildContext context) => SizedBox(
        width: caretWidth * 2 + barWidth,
        child: CustomPaint(painter: _CTabInsertionMarkerPainter(color, shown)),
      );
}

class _CTabInsertionMarkerPainter extends CustomPainter {
  _CTabInsertionMarkerPainter(this.color, this.shown) : super(repaint: shown);

  final Color color;
  final ValueListenable<bool> shown;

  @override
  void paint(Canvas canvas, Size size) {
    if (!shown.value) return;
    final paint = Paint()..color = color;
    final centre = size.width / 2;
    final half = CTabInsertionMarker.barWidth / 2;
    canvas.drawRect(
      Rect.fromLTRB(centre - half, 0, centre + half, size.height),
      paint,
    );
    const caret = CTabInsertionMarker.caretWidth;
    const height = CTabInsertionMarker.caretHeight;
    canvas.drawPath(
      Path()
        ..moveTo(centre - caret - half, 0)
        ..lineTo(centre + caret + half, 0)
        ..lineTo(centre, height)
        ..close(),
      paint,
    );
    canvas.drawPath(
      Path()
        ..moveTo(centre - caret - half, size.height)
        ..lineTo(centre + caret + half, size.height)
        ..lineTo(centre, size.height - height)
        ..close(),
      paint,
    );
  }

  @override
  bool shouldRepaint(_CTabInsertionMarkerPainter oldDelegate) =>
      oldDelegate.color != color;
}

/// Marks a tab's close button, so the strip report can find where it is drawn.
const _tabCloseButton = #ctabCloseButton;

/// The render boxes a tab and its close button are drawn by, and whether its label is drawn
/// shortened.
class DrawnTab {
  final RenderBox tab;
  final RenderBox? close;
  final bool shortened;

  const DrawnTab(this.tab, this.close, {this.shortened = false});
}

/// Where each of [tabs] is drawn in [folder], as `x,width` pairs in the folder's own coordinates -
/// what `CTabFolder.getItem(Point)` is asked about, and how the workbench decides which view a drag
/// picked up - then, after a `;`, each tab's close button as `x,y,width,height` (zeros for a tab
/// drawn without one), then, after another `;`, the part of the strip that is shown as `x,width`:
/// the scrolled viewport, which ends where the chevron and the top-right controls begin (empty when
/// there is none), then, after another `;`, a `1` for each tab whose label is drawn shortened and a
/// `0` for the others, then, after another `;`, the folder's width, then, after another `;`, the
/// `stripLayoutSeq` of the value drawn: Java lays the tabs out again on its own, so it uses a report
/// only when both still match its own.
///
/// Null when a tab cannot be placed: a folder laid out under unbounded constraints for a frame has
/// an infinite size, and aligning its children in it puts NaN in every offset below it.
@visibleForTesting
String? stripReport(RenderBox folder, List<DrawnTab> tabs, {int layoutSeq = 0}) {
  if (!folder.size.width.isFinite) return null;
  final strip = <String>[];
  final close = <String>[];
  final shortened = <String>[];
  for (final tab in tabs) {
    final local = _rectIn(folder, tab.tab);
    final button = tab.close == null ? null : _rectIn(folder, tab.close!);
    if (!local.isFinite || (button != null && !button.isFinite)) return null;
    strip.add('${local.left.round()},${local.width.round()}');
    close.add(button == null
        ? '0,0,0,0'
        : '${button.left.round()},${button.top.round()},'
            '${button.width.round()},${button.height.round()}');
    shortened.add(tab.shortened ? '1' : '0');
  }
  final Object? viewport = tabs.isEmpty ? null : _shownStrip(tabs.first.tab);
  var shown = '';
  if (viewport is RenderBox && viewport.hasSize) {
    final area = _rectIn(folder, viewport);
    if (!area.isFinite) return null;
    shown = '${area.left.round()},${area.width.round()}';
  }
  return '${strip.join(',')};${close.join(',')};$shown;${shortened.join(',')};${folder.size.width.round()};$layoutSeq';
}

/// The box that bounds the shown tabs: the scrolled viewport, or a compressed row, which ends where
/// the chevron begins and parks the tabs that do not fit past that end.
RenderObject? _shownStrip(RenderBox tab) {
  for (RenderObject? node = tab.parent; node != null; node = node.parent) {
    if (node is RenderCompressedTabRow || node is RenderAbstractViewport) return node;
  }
  return null;
}

/// Where [child] is drawn within [folder], mapped without going through global coordinates: a panel
/// squeezed into a slot smaller than its margins has an empty FittedBox above the folder, whose
/// all-zero paint transform maps every point to the global origin or to NaN.
Rect _rectIn(RenderBox folder, RenderBox child) => Rect.fromPoints(
    child.localToGlobal(Offset.zero, ancestor: folder),
    child.localToGlobal(child.size.bottomRight(Offset.zero), ancestor: folder));

Rect _globalRect(RenderBox box) => Rect.fromPoints(
    box.localToGlobal(Offset.zero), box.localToGlobal(box.size.bottomRight(Offset.zero)));

/// The first laid-out box under [root] marked with [marker].
RenderBox? _markedIn(RenderObject root, Object marker) {
  RenderBox? found;
  void visit(RenderObject node) {
    if (found != null) return;
    if (node is RenderMetaData && node.metaData == marker && node.hasSize) {
      found = node;
      return;
    }
    node.visitChildren(visit);
  }

  visit(root);
  return found;
}

/// A CTabFolder's tab row. Every tab is a Flutter [Draggable] carrying a [DndDragPayload] and
/// the row itself is a drop target, so a tab dropped on a *different* folder's row resolves
/// there and reaches Java as a real `DND.Drop`.
///
/// A folder that has an application `DropTarget` hands the drop to it and does not reorder
/// itself: native CTabFolder has no built-in reordering, so running both would move the tab
/// twice and diverge from the native backend.
class TabDragRow extends StatefulWidget {
  final List<Widget> children;
  final List<CTabItem> tabs;
  final VCTabFolder folderState;
  final CTabFolderSwt folderWidget;
  final double? tabHeight;

  /// Wraps the tab row in the folder's scroll view. The drop target goes *outside* it, so the
  /// whole tab strip accepts a drop and not just the width the tabs happen to occupy —
  /// otherwise a tab released over a folder's empty strip resolves to no target at all.
  final Widget Function(Widget row) rowWrapper;

  /// Lays the tabs out in place of the default natural-width Row.
  final Widget Function(Key key, List<Widget> children)? stripBuilder;
  final void Function(int from, int to)? onReorder;
  final void Function(int insertIndex, int? targetItemId, Offset position)? onDrop;
  final ValueChanged<int>? onTabDragStarted;
  final VoidCallback? onDragStart;

  /// Where each tab and its close button are drawn; false when they cannot be placed in this frame.
  final bool Function(List<DrawnTab> tabs)? onStripLayout;

  /// The point to report for a drag on a tab, given where that tab is drawn and where the press
  /// went down, both in global coordinates.
  final Offset? Function(Rect tabInGlobal, Offset? pressInGlobal)? dragPointFor;

  /// A global point in the folder's own coordinates: drop events report positions in those, not
  /// in the row's, which the strip offsets and scrolls.
  final Offset? Function(Offset global)? folderPointFor;

  const TabDragRow({
    super.key,
    required this.children,
    required this.tabs,
    required this.folderState,
    required this.folderWidget,
    required this.rowWrapper,
    this.stripBuilder,
    this.tabHeight,
    this.onReorder,
    this.onDrop,
    this.onTabDragStarted,
    this.onDragStart,
    this.onStripLayout,
    this.dragPointFor,
    this.folderPointFor,
  });

  @override
  State<TabDragRow> createState() => _TabDragRowState();
}

class _TabDragRowState extends State<TabDragRow> {
  /// Where the tabs are drawn, for the side that answers hit tests.
  ///
  /// It cannot work that out: it lays the strip out itself, without a scroll and with whatever does
  /// not fit parked off screen, and it sizes a tab from its own copy of this side's theme. Both
  /// have been wrong by more than a tab's width, which puts a press on the neighbouring one - and
  /// that press is how the workbench decides which view a drag picked up.
  bool _reportPending = false;

  /// Reads the strip once the frame is painted.
  ///
  /// A scroll tells its listeners while the frame is still being built, and where a tab is drawn is
  /// only settled once the viewport has painted at the new offset - so reading on the notification
  /// itself reports the tabs where they were before the scroll.
  void scheduleStripReport() {
    if (_reportPending || widget.onStripLayout == null) return;
    _reportPending = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _reportPending = false;
      if (mounted) reportStripLayout();
    });
  }

  void reportStripLayout() {
    final report = widget.onStripLayout;
    if (report == null) return;
    final tabs = <DrawnTab>[];
    for (var i = 0; i < widget.children.length; i++) {
      final box = _itemBox(i);
      if (box == null || !box.hasSize) return;
      final close = _markedIn(box, _tabCloseButton);
      tabs.add(DrawnTab(box, close, shortened: _markedIn(box, ctabShortenedLabel) != null));
    }
    // The folder can be laid out again without this row being rebuilt or scrolled, which is all
    // that otherwise asks for a report, so a frame that could not place the tabs asks again.
    if (!report(tabs)) scheduleStripReport();
  }

  final GlobalKey _rowKey = GlobalKey();
  final List<GlobalKey> _itemKeys = [];
  final HoverTracker<int> _insertHover = HoverTracker<int>();

  void _ensureItemKeys(int count) {
    while (_itemKeys.length < count) {
      _itemKeys.add(GlobalKey());
    }
  }

  RenderBox? get _row => _rowKey.currentContext?.findRenderObject() as RenderBox?;

  /// The tab at [index] as drawn, for the strip to scroll it into view.
  RenderBox? tabBox(int index) => _itemBox(index);

  /// Where the last press on the strip went down.
  Offset? _pressAt;

  RenderBox? _itemBox(int index) => index < _itemKeys.length
      ? _itemKeys[index].currentContext?.findRenderObject() as RenderBox?
      : null;

  double? _itemLeft(RenderBox row, int index) {
    final box = _itemBox(index);
    if (box == null) return null;
    return row.globalToLocal(box.localToGlobal(Offset.zero)).dx;
  }

  /// The gap the pointer sits at, in `[0, tab count]`.
  int _insertIndexAt(Offset globalPosition) {
    final count = widget.children.length;
    final row = _row;
    if (row == null) return count;
    final x = row.globalToLocal(globalPosition).dx;
    for (int i = 0; i < count; i++) {
      final box = _itemBox(i);
      final left = _itemLeft(row, i);
      if (box == null || left == null) continue;
      if (x < left + box.size.width / 2) return i;
    }
    return count;
  }

  /// The tab under the pointer, or null past the last one.
  int? _itemIdAt(Offset globalPosition) {
    final row = _row;
    if (row == null) return null;
    final x = row.globalToLocal(globalPosition).dx;
    final count = math.min(widget.children.length, widget.tabs.length);
    for (int i = 0; i < count; i++) {
      final box = _itemBox(i);
      final left = _itemLeft(row, i);
      if (box == null || left == null) continue;
      if (x >= left && x < left + box.size.width) return widget.tabs[i].vItem.id;
    }
    return null;
  }

  void _handleDrop(DndDragPayload payload, int? index, int? itemId, Offset position) {
    _insertHover.update(null);
    // An application that answered DragDetect by opening a Tracker owns this gesture -- the Eclipse
    // workbench moves the whole view that way. Reordering here as well would move the tab twice.
    if (TrackerSession.isTracking) return;
    final count = widget.children.length;
    final insertIndex = (index ?? count).clamp(0, count);
    if (widget.folderState.dropTargetId != null) {
      widget.onDrop?.call(insertIndex, (itemId ?? 0) != 0 ? itemId : null, position);
      return;
    }
    if (payload.sourceControlId != widget.folderState.id) return;
    final from = payload.index;
    if (from == null || from >= count) return;
    final to = insertIndex > from ? insertIndex - 1 : insertIndex;
    if (to != from) widget.onReorder?.call(from, to);
  }

  Widget _draggableTab(int index) {
    return wrapDraggable<DndDragPayload>(
      child: widget.children[index],
      data: DndDragPayload(
        sourceControlId: widget.folderState.id,
        index: index,
        itemId: index < widget.tabs.length ? widget.tabs[index].vItem.id : null,
      ),
      widget: widget.folderWidget,
      state: widget.folderState,
      alwaysDraggable: true,
      // The workbench reads DragDetect's x/y to work out which tab is being dragged, and the folder
      // tests it against the drawn close buttons, so the event carries where the press went down on
      // the tab - in the folder's coordinates, which is what it maps the cursor into before asking.
      // Not the row's: the row is the strip's scrolling content, so
      // once the strip has been scrolled its coordinates are the tab's unscrolled position, and the
      // workbench finds no tab there and drags the whole stack instead.
      dragDetectEvent: () {
        final box = _itemBox(index);
        final point = box == null
            ? Offset.zero
            : widget.dragPointFor?.call(_globalRect(box), _pressAt) ?? Offset.zero;
        // A drag is the primary button's, as natively.
        return VEvent()
          ..button = 1
          ..x = point.dx.round()
          ..y = point.dy.round();
      },
      onDragStarted: () {
        widget.onDragStart?.call();
        widget.onTabDragStarted?.call(index);
      },
      // Built lazily: the drag feedback renders in the Overlay, which gives it unbounded
      // height, while a tab's own container asks for `double.infinity`. The measurement is
      // only available once the row has laid out, which is the case by the time a drag starts.
      feedbackBuilder: (child) => Builder(builder: (_) {
        final size = _itemBox(index)?.size;
        return Material(
          elevation: 4,
          child: SizedBox(
            width: size?.width,
            height: size?.height ?? widget.tabHeight,
            child: Opacity(opacity: 0.85, child: child),
          ),
        );
      }),
    );
  }

  /// Where in the row the gap [index] is.
  double _gapLeft(int index) {
    final row = _row;
    if (row == null || widget.children.isEmpty) return 0;
    if (index < widget.children.length) return _itemLeft(row, index) ?? 0;
    final last = widget.children.length - 1;
    final lastLeft = _itemLeft(row, last);
    final box = _itemBox(last);
    return lastLeft != null && box != null ? lastLeft + box.size.width : 0;
  }

  @override
  Widget build(BuildContext context) {
    final count = widget.children.length;
    _ensureItemKeys(count);
    // The tabs have moved or changed width, and only a painted frame knows where they ended up.
    scheduleStripReport();

    // mainAxisSize.min so the row takes its natural width and the scroll view overflows when
    // tabs don't fit; the default (max) fills and clips instead.
    final tabs = List<Widget>.generate(
      count,
      (i) => KeyedSubtree(key: _itemKeys[i], child: _draggableTab(i)),
    );
    final row = Listener(
      onPointerDown: (e) => _pressAt = e.position,
      child: widget.stripBuilder?.call(_rowKey, tabs) ??
          Row(key: _rowKey, mainAxisSize: MainAxisSize.min, children: tabs),
    );

    return wrapDropTarget<DndDragPayload>(
      state: widget.folderState,
      alwaysAccepts: true,
      // The insertion mark is drawn from the resolved index, and the folders that most need it —
      // the Eclipse workbench's view stacks — have no application DropTarget: the workbench moves
      // a view with a Tracker rather than through DND.
      alwaysResolves: true,
      resolveIndex: (details) {
        final index = _insertIndexAt(details.offset);
        _insertHover.update(index);
        return index;
      },
      resolveItemId: (details) => _itemIdAt(details.offset) ?? 0,
      resolvePosition: (details) => widget.folderPointFor?.call(details.offset),
      onDrop: _handleDrop,
      onLeave: () => _insertHover.update(null),
      child: widget.rowWrapper(
        // A Row with no children is zero-high, which would stop the strip hit-testing and leave
        // a folder that lost its last tab unable to be given one back. Hold the strip's height.
        ConstrainedBox(
          constraints: BoxConstraints(minHeight: widget.tabHeight ?? 0),
          child: _StripDropFeedback(
            gap: _insertHover.notifier,
            gapLeft: _gapLeft,
            child: RepaintBoundary(child: row),
          ),
        ),
      ),
    );
  }

}

/// What a strip shows while a tab is dragged over it: a tint, so that with several folders on
/// screen it says which row the tab lands in, and the insertion marker at the gap it lands at,
/// sliding between gaps so that dragging along the tabs reads as one thing moving rather than as
/// the marker blinking out and back.
///
/// Both animate by painting alone, each in a layer of its own: a widget rebuilt for every frame of
/// the fade or the slide would lay the strip out again, and through it the folder.
class _StripDropFeedback extends StatefulWidget {
  const _StripDropFeedback({required this.gap, required this.gapLeft, required this.child});

  final ValueListenable<int?> gap;
  final double Function(int gap) gapLeft;
  final Widget child;

  @override
  State<_StripDropFeedback> createState() => _StripDropFeedbackState();
}

class _StripDropFeedbackState extends State<_StripDropFeedback> with TickerProviderStateMixin {
  /// Enough to read as "this row", not enough to fight the tabs drawn on it.
  static const double _tintAlpha = 0.12;
  static const Duration _duration = Duration(milliseconds: 90);

  late final AnimationController _tint = AnimationController(vsync: this, duration: _duration);
  late final AnimationController _slide = AnimationController(vsync: this, duration: _duration)
    ..addListener(_slid);
  final ValueNotifier<double> _markerLeft = ValueNotifier<double>(0);
  final ValueNotifier<bool> _marked = ValueNotifier<bool>(false);
  double _from = 0;
  double _to = 0;

  @override
  void initState() {
    super.initState();
    widget.gap.addListener(_gapChanged);
  }

  @override
  void didUpdateWidget(_StripDropFeedback oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!identical(oldWidget.gap, widget.gap)) {
      oldWidget.gap.removeListener(_gapChanged);
      widget.gap.addListener(_gapChanged);
    }
  }

  @override
  void dispose() {
    widget.gap.removeListener(_gapChanged);
    _tint.dispose();
    _slide.dispose();
    _markerLeft.dispose();
    _marked.dispose();
    super.dispose();
  }

  void _gapChanged() {
    final gap = widget.gap.value;
    if (gap == null) {
      _tint.reverse();
      _marked.value = false;
      return;
    }
    _tint.forward();
    final left = widget.gapLeft(gap);
    if (!_marked.value) {
      _from = _to = left;
      _markerLeft.value = left;
      _marked.value = true;
    } else if (left != _to) {
      _from = _markerLeft.value;
      _to = left;
      _slide.forward(from: 0);
    }
  }

  void _slid() => _markerLeft.value = _from + (_to - _from) * Curves.easeOut.transform(_slide.value);

  @override
  Widget build(BuildContext context) {
    final color = Theme.of(context).extension<CTabFolderThemeExtension>()!.tabHighlightColor;
    return Stack(
      clipBehavior: Clip.none,
      children: [
        Positioned.fill(
          child: IgnorePointer(
            child: RepaintBoundary(
              child: CustomPaint(painter: _StripTintPainter(_tint, color.withValues(alpha: _tintAlpha))),
            ),
          ),
        ),
        widget.child,
        // Always there, shown or not, so that it comes and goes by painting alone.
        Positioned.fill(
          child: IgnorePointer(
            child: RepaintBoundary(
              child: _PaintedAt(
                left: _markerLeft,
                shift: -CTabInsertionMarker.overhang,
                child: RepaintBoundary(child: CTabInsertionMarker(color: color, shown: _marked)),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _StripTintPainter extends CustomPainter {
  _StripTintPainter(this.amount, this.color) : super(repaint: amount);

  final Animation<double> amount;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    if (amount.value == 0) return;
    canvas.drawRect(Offset.zero & size, Paint()..color = color.withValues(alpha: color.a * amount.value));
  }

  @override
  bool shouldRepaint(_StripTintPainter old) => old.color != color || !identical(old.amount, amount);
}

/// Its child, as tall as it and as wide as the child is, painted [left] (plus [shift]) along it:
/// moving it repaints this and places the child's layer, nothing else.
class _PaintedAt extends SingleChildRenderObjectWidget {
  const _PaintedAt({required this.left, required this.shift, required super.child});

  final ValueListenable<double> left;
  final double shift;

  @override
  _RenderPaintedAt createRenderObject(BuildContext context) => _RenderPaintedAt(left, shift);

  @override
  void updateRenderObject(BuildContext context, _RenderPaintedAt renderObject) {
    renderObject
      ..left = left
      ..shift = shift;
  }
}

class _RenderPaintedAt extends RenderProxyBox {
  _RenderPaintedAt(this._left, this._shift);

  ValueListenable<double> _left;
  double _shift;

  set left(ValueListenable<double> value) {
    if (identical(value, _left)) return;
    if (attached) {
      _left.removeListener(markNeedsPaint);
      value.addListener(markNeedsPaint);
    }
    _left = value;
    markNeedsPaint();
  }

  set shift(double value) {
    if (value == _shift) return;
    _shift = value;
    markNeedsPaint();
  }

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    _left.addListener(markNeedsPaint);
  }

  @override
  void detach() {
    _left.removeListener(markNeedsPaint);
    super.detach();
  }

  @override
  void performLayout() {
    size = constraints.biggest;
    child?.layout(BoxConstraints.tightFor(height: size.height));
  }

  @override
  Size computeDryLayout(BoxConstraints constraints) => constraints.biggest;

  @override
  bool hitTestChildren(BoxHitTestResult result, {required Offset position}) => false;

  @override
  void paint(PaintingContext context, Offset offset) {
    final child = this.child;
    if (child != null) context.paintChild(child, offset + Offset(_left.value + _shift, 0));
  }

  @override
  void applyPaintTransform(RenderBox child, Matrix4 transform) {
    transform.translate(_left.value + _shift, 0.0);
  }
}

/// Side of the square a tab image is drawn in: the theme's icon size, or the image's own height when
/// it is taller.
double ctabIconSize(VImage? image, double iconSize) =>
    math.max(iconSize, (image?.height ?? 0).toDouble());

class CTabItem {
  final String label;
  final String? image;
  final bool showCloseButton;
  final VoidCallback? onClose;
  final bool alignRight;
  final Widget? customContent;
  final String? toolTipText;
  final VCTabItem vItem;

  CTabItem({
    required this.label,
    this.image,
    this.showCloseButton = false,
    this.onClose,
    this.alignRight = false,
    this.customContent,
    this.toolTipText,
    required this.vItem,
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
  // The selected tab of the folder that holds focus.
  final bool isActive;
  // The selected tab of a folder that takes part in activation but does not hold focus.
  final bool isDimmed;
  // The folder's minimumCharacters while it compresses its tabs (ctabfolder_tab_scroll=false).
  final int? compressMinimumCharacters;

  TabItemContext({
    required this.isSelected,
    required this.isEnabled,
    this.isActive = false,
    this.isDimmed = false,
    this.compressMinimumCharacters,
  });

  static TabItemContext? of(BuildContext context) {
    final provider = context
        .dependOnInheritedWidgetOfExactType<TabItemContextProvider>();
    return provider?.context;
  }
}

class TabItemContextProvider extends InheritedWidget {
  final TabItemContext context;

  TabItemContextProvider({
    Key? key,
    required bool isSelected,
    required bool isEnabled,
    bool isActive = false,
    bool isDimmed = false,
    int? compressMinimumCharacters,
    required Widget child,
  }) : context = TabItemContext(
         isSelected: isSelected,
         isEnabled: isEnabled,
         isActive: isActive,
         isDimmed: isDimmed,
         compressMinimumCharacters: compressMinimumCharacters,
       ),
       super(key: key, child: child);

  @override
  bool updateShouldNotify(TabItemContextProvider oldWidget) {
    return context.isSelected != oldWidget.context.isSelected ||
        context.isEnabled != oldWidget.context.isEnabled ||
        context.isActive != oldWidget.context.isActive ||
        context.isDimmed != oldWidget.context.isDimmed ||
        context.compressMinimumCharacters != oldWidget.context.compressMinimumCharacters;
  }
}

/// The tab's sides, drawn as straight strips the way Flutter's release build draws a
/// non-uniform Border under a rounded corner (it ignores the radius for the sides); the debug
/// build refuses that combination in a BoxDecoration, so the sides are painted here instead.
/// A strip paints its page-side outline edge with it too.
class _RectangularSides extends CustomPainter {
  final Border border;
  // Painted after the sides, which would otherwise cover it.
  final BorderSide? ring;
  // Left out of the painted rect: the part of the tab or strip the folder clips off.
  final EdgeInsets clipped;

  const _RectangularSides(this.border, {this.ring, this.clipped = EdgeInsets.zero});

  @override
  void paint(Canvas canvas, Size size) {
    final rect = clipped.deflateRect(Offset.zero & size);
    paintBorder(
      canvas,
      rect,
      top: border.top,
      right: border.right,
      bottom: border.bottom,
      left: border.left,
    );
    final ring = this.ring;
    if (ring != null) {
      paintBorder(canvas, rect, top: ring, right: ring, bottom: ring, left: ring);
    }
  }

  @override
  bool shouldRepaint(_RectangularSides oldDelegate) =>
      oldDelegate.border != border || oldDelegate.ring != ring || oldDelegate.clipped != clipped;
}

/// A bordered folder's outline: both sides the full height, past the strip, the corners on the
/// strip's side rounded, and straight across the far edge of the page.
class _FolderOutline extends CustomPainter {
  final BorderSide side;
  final double radius;
  final bool stripAtBottom;

  const _FolderOutline(this.side, {required this.radius, required this.stripAtBottom});

  @override
  void paint(Canvas canvas, Size size) {
    final rect = (Offset.zero & size).deflate(side.width / 2);
    final corner = Radius.circular(radius);
    final shape = stripAtBottom
        ? RRect.fromRectAndCorners(rect, bottomLeft: corner, bottomRight: corner)
        : RRect.fromRectAndCorners(rect, topLeft: corner, topRight: corner);
    canvas.drawRRect(shape, side.toPaint());
  }

  @override
  bool shouldRepaint(_FolderOutline oldDelegate) =>
      oldDelegate.side != side ||
      oldDelegate.radius != radius ||
      oldDelegate.stripAtBottom != stripAtBottom;
}
