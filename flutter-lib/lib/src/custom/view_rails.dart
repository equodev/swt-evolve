import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

import '../comm/comm.dart';
import '../comm/v_registry.dart';
import '../comm/widget_key.dart';
import '../comm/widget_wire.dart';
import '../gen/control.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/widgets.dart';
import '../impl/utils/image_utils.dart';
import '../impl/utils/pending_move.dart';
import '../impl/utils/test_anchors.dart';
import '../theme/theme_extensions/ctabfolder_theme_extension.dart';
import '../theme/theme_extensions/view_rails_theme_extension.dart';

/// Where a view stack's icons go, as Java's `ViewRails` numbers it.
abstract final class ViewRailPlace {
  static const int leftTop = 0;
  static const int leftBottom = 1;
  static const int rightTop = 2;
  static const int rightBottom = 3;

  /// Set on a place when the stack is minimized.
  static const int minimized = 4;

  static bool isRight(int place) => (place & 3) >= rightTop;

  static int of({required bool right, required bool bottom}) => (right ? rightTop : leftTop) + (bottom ? 1 : 0);
  static bool isBottom(int place) => (place & 3) == leftBottom || (place & 3) == rightBottom;
}

/// What a `ViewRails/show` event asks for, in its detail: a press on a view's button, which Java
/// answers by showing the view or, when it is the view being worked in, minimizing its stack; a
/// view's menu (sent on its stack's folder) or a rail's (sent on the rail).
abstract final class ViewRailAction {
  static const int press = 1;
  static const int viewMenu = 3;
  static const int railMenu = 4;

  /// A view's button dropped on a rail. Sent on the view's stack: `index` the view, `start` the rail
  /// end it was dropped at, `x` the stack it was dropped into (0: none, the end as Move To takes it)
  /// and `end` the view of that stack it goes before (-1: last).
  static const int move = 5;
}

/// The rails' sizes, as Java's `ViewRails` numbers them. Their widths are in
/// [ViewRailsThemeExtension], which Java reserves through the `ViewRailsSizes` it generates.
abstract final class ViewRailSize {
  static const int small = 0;
  static const int medium = 1;
  static const int large = 2;

  static int _of(int size) => size < small || size > large ? medium : size;

  /// [size], or medium when it names none.
  static int clamp(int size) => _of(size);
  static double rail(ViewRailsThemeExtension theme, int size) => theme.railWidth(_of(size));
  static double button(ViewRailsThemeExtension theme, int size) => theme.buttonSizes[_of(size)];
  static double icon(ViewRailsThemeExtension theme, int size) => theme.iconSizes[_of(size)];
}

/// The main window's `ViewRails` record: the view stacks, where each one's icons go, the trim
/// composites that draw the rails, and the e4 TrimStacks they stand in for.
class VViewRails implements WireReadable {
  List<VCTabFolder>? stacks;

  /// Each stack's id followed by its place.
  List<int>? places;
  List<VControl>? rails;

  /// Each rail's id followed by its side, `SWT.LEFT` or `SWT.RIGHT`.
  List<int>? railSides;
  List<VControl>? trims;

  /// A [ViewRailSize].
  int? size;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_ViewRails_stacks:
        stacks = r.refs<VCTabFolder>();
      case WireKey.k_ViewRails_places:
        places = r.longs();
      case WireKey.k_ViewRails_rails:
        rails = r.refs<VControl>();
      case WireKey.k_ViewRails_railSides:
        railSides = r.longs();
      case WireKey.k_ViewRails_trims:
        trims = r.refs<VControl>();
      case WireKey.k_ViewRails_size:
        size = r.int64();
      default:
        return false;
    }
    return true;
  }
}

/// The view rails as the last record described them.
class ViewRails {
  ViewRails._(this.order, this.places, this.railSides, this.trims, [this.size = ViewRailSize.medium]);

  static final ViewRails empty = ViewRails._(const [], const {}, const {}, const {});

  /// What a rail button is tagged as for a test, followed by its view's CTabItem id.
  static const String railAnchor = 'rail:CTabItem/';

  /// The rails now. A view stack listed here draws only its selected view, under a header.
  static final ValueNotifier<ViewRails> current = ValueNotifier(empty);

  /// The folder holding the active part, as Java's activation tells it; null for none.
  static final ValueNotifier<int?> activeFolder = ValueNotifier(null);

  /// The view stacks' ids, in the model's order.
  final List<int> order;

  /// Each view stack's place, by id.
  final Map<int, int> places;

  /// Each rail's side, by the id of the trim composite that draws it.
  final Map<int, int> railSides;

  /// The ids of the e4 TrimStacks the rails stand in for.
  final Set<int> trims;

  /// A [ViewRailSize].
  final int size;

  double railWidth(BuildContext context) =>
      ViewRailSize.rail(Theme.of(context).extension<ViewRailsThemeExtension>()!, size);

  static void Function()? _stop;

  /// Listens for the record. Once, before any frame is applied: a holder record nothing listens to
  /// is dropped.
  static void start() {
    _stop ??= VRegistry.instance.onHolder<VViewRails>('ViewRails', VViewRails.new, _onRecord);
  }

  static void _onRecord(int id, VViewRails record) {
    // Held here: a minimized stack's folder is in no tree the client draws.
    VRegistry.instance.holdsAt(WidgetKey.of(WireType.viewRails, id), [
      ...?record.stacks,
      ...?record.rails,
      ...?record.trims,
    ]);
    final places = <int, int>{};
    final order = <int>[];
    final pairs = record.places ?? const [];
    for (var i = 0; i + 1 < pairs.length; i += 2) {
      places[pairs[i]] = pairs[i + 1];
      order.add(pairs[i]);
    }
    final sides = <int, int>{};
    final railPairs = record.railSides ?? const [];
    for (var i = 0; i + 1 < railPairs.length; i += 2) {
      sides[railPairs[i]] = railPairs[i + 1];
    }
    current.value = ViewRails._(order, places, sides, {
      for (final t in record.trims ?? const <VControl>[]) t.id,
    }, record.size ?? ViewRailSize.medium);
  }

  /// Called with each activation Java reports for a folder.
  static void folderActivated(int id, bool active) {
    if (active) {
      activeFolder.value = id;
    } else if (activeFolder.value == id) {
      activeFolder.value = null;
    }
  }

  /// Whether the folder [id] is a view stack the rails list.
  bool hostsStack(int id) => places.containsKey(id);

  /// The side of the rail the trim composite [id] draws, or null when it draws none.
  int? railSide(int id) => railSides[id];

  bool isTrim(int id) => trims.contains(id);

  /// Sends [action] for item [index] of the stack [folder].
  static void send(VCTabFolder folder, int action, [int index = -1]) => _send(
    folder,
    VEvent()
      ..detail = action
      ..index = index,
  );

  /// Asks Java for the menu of item [index] of [folder], opened at [at] on its rail.
  static void sendViewMenu(VCTabFolder folder, int index, Offset at) => _send(
    folder,
    _at(
      VEvent()
        ..detail = ViewRailAction.viewMenu
        ..index = index,
      at,
    ),
  );

  /// Moves view [index] of [folder] to rail end [place]: into the stack [targetId] before its view
  /// [at] (-1: last), or with no target as Move To does.
  static void sendMove(VCTabFolder folder, int index, int place, {int targetId = 0, int at = -1}) => _send(
    folder,
    VEvent()
      ..detail = ViewRailAction.move
      ..index = index
      ..start = place
      ..x = targetId
      ..end = at,
  );

  /// Asks Java for the menu of the rail drawn by [rail], for its top or [bottom] end, at [at] on it.
  static void sendRailMenu(VControl rail, bool bottom, Offset at) => _send(
    rail,
    _at(
      VEvent()
        ..detail = ViewRailAction.railMenu
        ..index = bottom ? 1 : 0,
      at,
    ),
  );

  static VEvent _at(VEvent event, Offset at) => event
    ..x = at.dx.round()
    ..y = at.dy.round();

  static void _send(VControl target, VEvent event) {
    PendingMove.flush();
    EquoCommService.sendTo(target.wireType, target.id, WireChannel.viewRailsShow, event);
  }
}

/// [theme] with a view stack's header look: the selected view's title on the bar's own ground,
/// with no tab shape around it.
ThemeData viewHeaderTheme(ThemeData theme) {
  final folder = theme.extension<CTabFolderThemeExtension>();
  if (folder == null) return theme;
  final flat = folder.copyWith(
    tabSelectedBackgroundColor: folder.tabBarBackgroundColor,
    tabHoverBackgroundColor: folder.tabBarBackgroundColor,
    tabSelectedBorderColor: Colors.transparent,
    tabBorderColor: Colors.transparent,
    tabHoverBorderColor: Colors.transparent,
    tabHighlightColor: Colors.transparent,
    tabSelectedTextStyle: (folder.tabSelectedTextStyle ?? const TextStyle()).copyWith(fontWeight: FontWeight.w600),
  );
  return theme.copyWith(
    extensions: [
      for (final e in theme.extensions.values)
        if (e is! CTabFolderThemeExtension) e,
      flat,
    ],
  );
}

/// The icons of one rail: the stacks placed at its top end, then the ones at its bottom end, each
/// stack's views in its own group.
class ViewRail extends StatefulWidget {
  final int side;

  /// The trim composite drawing this rail: a right-click on the rail asks Java on it.
  final VControl? rail;

  /// What else the trim holds, drawn between the two ends.
  final List<Widget> middle;

  const ViewRail({super.key, required this.side, this.rail, this.middle = const []});

  @override
  State<ViewRail> createState() => _ViewRailState();
}

class _ViewRailState extends State<ViewRail> {
  final Set<int> _watched = {};

  /// The press a button opened its view's menu for: the rail behind it does not open its own.
  int? _buttonMenuPointer;

  /// Whether a dragged button is over the empty part of the rail nearer its top or its bottom end.
  final _gapHover = [ValueNotifier(false), ValueNotifier(false)];

  @override
  void initState() {
    super.initState();
    ViewRails.current.addListener(_railsChanged);
    ViewRails.activeFolder.addListener(_changed);
    _watch();
  }

  @override
  void dispose() {
    ViewRails.current.removeListener(_railsChanged);
    ViewRails.activeFolder.removeListener(_changed);
    for (final hover in _gapHover) {
      hover.dispose();
    }
    for (final key in _watched) {
      VRegistry.instance.unwatchAt(key, _folderChanged);
    }
    super.dispose();
  }

  void _changed() {
    if (mounted) setState(() {});
  }

  void _folderChanged(VChange change) => _changed();

  void _railsChanged() {
    _watch();
    _changed();
  }

  /// Every stack's folder, whether or not it is drawn anywhere: a selection or a new view changes
  /// the icons.
  void _watch() {
    final keys = {for (final id in ViewRails.current.value.order) WidgetKey.of(WireType.cTabFolder, id)};
    for (final key in _watched.difference(keys)) {
      VRegistry.instance.unwatchAt(key, _folderChanged);
    }
    for (final key in keys.difference(_watched)) {
      VRegistry.instance.watchAt(key, _folderChanged);
    }
    _watched
      ..clear()
      ..addAll(keys);
  }

  @override
  Widget build(BuildContext context) {
    final rails = ViewRails.current.value;
    final theme = Theme.of(context).extension<ViewRailsThemeExtension>()!;
    final right = widget.side == SWT.RIGHT;
    List<Widget> group(bool bottom) {
      final stacks = <Widget>[];
      for (final id in rails.order) {
        final place = rails.places[id]!;
        if (ViewRailPlace.isRight(place) != right || ViewRailPlace.isBottom(place) != bottom) continue;
        final folder = VRegistry.instance.valueAt(WidgetKey.of(WireType.cTabFolder, id));
        if (folder is! VCTabFolder) continue;
        final icons = _icons(folder, place);
        if (icons.isEmpty) continue;
        if (stacks.isNotEmpty) stacks.add(const _RailSeparator());
        stacks.addAll(icons);
      }
      return stacks;
    }

    // The buttons keep their room on the window's side, where the active view's indicator sits, and
    // little on the views' side.
    final size = ViewRailSize.clamp(rails.size);
    final outer = theme.outerPaddings[size], inner = theme.innerPaddings[size];
    Widget toEdge(Widget item) => Padding(
      padding: EdgeInsets.only(left: right ? inner : outer, right: right ? outer : inner),
      child: item,
    );

    // A right-click on the rail itself, not on a button, opens the rail's menu for the end it is in.
    // Read from the press rather than as a gesture: the control's own menu detection would win the
    // gesture arena.
    return Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: (event) {
        if (event.buttons != kSecondaryMouseButton || event.pointer == _buttonMenuPointer) return;
        final rail = widget.rail;
        if (rail == null) return;
        ViewRails.sendRailMenu(rail, _nearerBottom(event.position), event.localPosition);
      },
      child: SizedBox(
        width: rails.railWidth(context),
        child: Column(
          children: [
            SizedBox(height: theme.endPadding),
            _RailEndDrop(
              place: ViewRailPlace.of(right: right, bottom: false),
              bottom: false,
              gapHover: _gapHover[0],
              children: group(false).map(toEdge).toList(),
            ),
            Expanded(
              child: Stack(
                children: [
                  // The empty part of the rail takes a drop for the end it is nearer, as the rail's
                  // menu does, so an end with no button yet is as easy to reach as one with several.
                  Positioned.fill(
                    child: _RailGapDrop(
                      bottomAt: _nearerBottom,
                      place: (bottom) => ViewRailPlace.of(right: right, bottom: bottom),
                      hover: _gapHover,
                    ),
                  ),
                  ClipRect(
                    child: Align(
                      alignment: Alignment.topLeft,
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: widget.middle,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            _RailEndDrop(
              place: ViewRailPlace.of(right: right, bottom: true),
              bottom: true,
              gapHover: _gapHover[1],
              children: group(true).map(toEdge).toList(),
            ),
            SizedBox(height: theme.endPadding),
          ],
        ),
      ),
    );
  }

  /// Whether [global] is in the rail's lower half, nearer its bottom end.
  bool _nearerBottom(Offset global) {
    final box = context.findRenderObject() as RenderBox?;
    return box != null && box.globalToLocal(global).dy > box.size.height / 2;
  }

  /// [global] in this rail's coordinates, as Java places a menu on the rail.
  Offset _onRail(Offset global) {
    final box = context.findRenderObject() as RenderBox?;
    return box == null ? global : box.globalToLocal(global);
  }

  List<Widget> _icons(VCTabFolder folder, int place) {
    final items = folder.items?.whereType<VCTabItem>().toList() ?? const <VCTabItem>[];
    final minimized = place & ViewRailPlace.minimized != 0;
    final selection = folder.selection ?? 0;
    final active = ViewRails.activeFolder.value == folder.id;
    return [
      for (var i = 0; i < items.length; i++)
        _RailButton(
          key: ValueKey('${folder.id}/${items[i].id}'),
          drag: _RailDrag(folder, i),
          onDrop: (drag, after) => ViewRails.sendMove(
            drag.folder,
            drag.index,
            place & 3,
            targetId: folder.id,
            at: after ? (i + 1 < items.length ? i + 1 : -1) : i,
          ),
          item: items[i],
          shown: !minimized && i == selection,
          active: !minimized && i == selection && active,
          right: widget.side == SWT.RIGHT,
          size: ViewRails.current.value.size,
          onMenu: (event) {
            _buttonMenuPointer = event.pointer;
            ViewRails.sendViewMenu(folder, i, _onRail(event.position));
          },
          onPressed: () => ViewRails.send(folder, ViewRailAction.press, i),
        ),
    ];
  }
}

/// A view's button being dragged: view [index] of [folder].
class _RailDrag {
  final VCTabFolder folder;
  final int index;

  const _RailDrag(this.folder, this.index);
}

/// One end of a rail, with room past its last button, where a dragged button is dropped last.
class _RailEndDrop extends StatelessWidget {
  final int place;
  final bool bottom;

  /// Whether a button is over the empty part of the rail that drops at this end: the end lights up.
  final ValueListenable<bool> gapHover;
  final List<Widget> children;

  const _RailEndDrop({required this.place, required this.bottom, required this.gapHover, required this.children});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ViewRailsThemeExtension>()!;
    return DragTarget<_RailDrag>(
      onAcceptWithDetails: (details) => ViewRails.sendMove(details.data.folder, details.data.index, place),
      builder: (context, candidates, _) {
        final room = SizedBox(height: theme.dropRoom);
        return ValueListenableBuilder<bool>(
          valueListenable: gapHover,
          builder: (context, overGap, child) =>
              Container(color: candidates.isEmpty && !overGap ? null : theme.dropBackground, child: child),
          child: Column(mainAxisSize: MainAxisSize.min, children: bottom ? [room, ...children] : [...children, room]),
        );
      },
    );
  }
}

/// The empty part of a rail: a button dropped there goes last at the end it is nearer. The drag
/// is anchored at the pointer, so its offset is where the pointer is.
class _RailGapDrop extends StatelessWidget {
  final bool Function(Offset global) bottomAt;
  final int Function(bool bottom) place;

  /// The top and bottom ends' highlight.
  final List<ValueNotifier<bool>> hover;

  const _RailGapDrop({required this.bottomAt, required this.place, required this.hover});

  void _over(bool? bottom) {
    hover[0].value = bottom == false;
    hover[1].value = bottom == true;
  }

  @override
  Widget build(BuildContext context) {
    return DragTarget<_RailDrag>(
      onMove: (details) => _over(bottomAt(details.offset)),
      onLeave: (_) => _over(null),
      onAcceptWithDetails: (details) {
        _over(null);
        ViewRails.sendMove(details.data.folder, details.data.index, place(bottomAt(details.offset)));
      },
      builder: (context, candidates, _) => const SizedBox.expand(),
    );
  }
}

class _RailSeparator extends StatelessWidget {
  const _RailSeparator();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ViewRailsThemeExtension>()!;
    return Padding(
      padding: EdgeInsets.symmetric(vertical: theme.separatorSpacing),
      child: SizedBox(
        width: theme.separatorWidth,
        height: theme.separatorThickness,
        child: ColoredBox(color: theme.separatorColor),
      ),
    );
  }
}

/// One view's icon. Shown: its view is on screen. Active: it is the view being worked in.
class _RailButton extends StatefulWidget {
  final VCTabItem item;
  final bool shown;
  final bool active;
  final bool right;
  final int size;
  final VoidCallback onPressed;

  /// What dragging this button carries, and what a button dropped on it does: [after] when it was
  /// dropped on the lower half.
  final _RailDrag drag;
  final void Function(_RailDrag drag, bool after) onDrop;

  /// A right-click on the button.
  final ValueChanged<PointerDownEvent> onMenu;

  const _RailButton({
    super.key,
    required this.item,
    required this.shown,
    required this.active,
    required this.right,
    required this.size,
    required this.onPressed,
    required this.onMenu,
    required this.drag,
    required this.onDrop,
  });

  @override
  State<_RailButton> createState() => _RailButtonState();
}

class _RailButtonState extends State<_RailButton> {
  bool _hovered = false;

  /// While a button is dragged over this one: whether it would land after it, else before.
  bool? _dropAfter;

  bool _lowerHalf(Offset global) {
    final box = context.findRenderObject() as RenderBox?;
    return box != null && box.globalToLocal(global).dy > box.size.height / 2;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ViewRailsThemeExtension>()!;
    final Color? ground = widget.active
        ? theme.activeBackground
        : widget.shown
        ? theme.shownBackground
        : _hovered
        ? theme.hoverBackground
        : null;
    final label = widget.item.text ?? '';
    final buttonSize = ViewRailSize.button(theme, widget.size);
    final icon =
        ImageUtils.buildVImage(widget.item.image, size: ViewRailSize.icon(theme, widget.size)) ??
        Text(label.isEmpty ? '?' : label.characters.first.toUpperCase(), style: theme.letterStyle);
    final tooltip = widget.item.toolTipText?.isNotEmpty == true ? widget.item.toolTipText! : label;

    Widget button = tagSemantics(
      widget.item,
      Listener(
        onPointerDown: (event) {
          if (event.buttons == kSecondaryMouseButton) widget.onMenu(event);
        },
        child: MouseRegion(
          cursor: SystemMouseCursors.click,
          onEnter: (_) => setState(() => _hovered = true),
          onExit: (_) => setState(() => _hovered = false),
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: widget.onPressed,
            child: Container(
              width: buttonSize,
              height: buttonSize,
              alignment: Alignment.center,
              decoration: BoxDecoration(color: ground, borderRadius: BorderRadius.circular(theme.buttonRadius)),
              child: Opacity(opacity: widget.shown || _hovered ? 1 : theme.idleOpacity, child: icon),
            ),
          ),
        ),
      ),
      id: '${ViewRails.railAnchor}${widget.item.id}',
    );
    if (widget.active) {
      button = Stack(
        clipBehavior: Clip.none,
        children: [
          button,
          Positioned(
            left: widget.right ? null : -theme.indicatorOffset,
            right: widget.right ? -theme.indicatorOffset : null,
            top: theme.indicatorInset,
            bottom: theme.indicatorInset,
            child: Container(
              width: theme.indicatorWidth,
              decoration: BoxDecoration(
                color: theme.indicatorColor,
                borderRadius: BorderRadius.circular(theme.indicatorRadius),
              ),
            ),
          ),
        ],
      );
    }
    final padded = Padding(
      padding: EdgeInsets.symmetric(vertical: theme.buttonSpacing),
      child: Tooltip(message: tooltip, waitDuration: theme.tooltipWait, child: button),
    );
    return DragTarget<_RailDrag>(
      onMove: (details) {
        final after = _lowerHalf(details.offset);
        if (after != _dropAfter) setState(() => _dropAfter = after);
      },
      onLeave: (_) => setState(() => _dropAfter = null),
      onAcceptWithDetails: (details) {
        final after = _dropAfter ?? _lowerHalf(details.offset);
        setState(() => _dropAfter = null);
        widget.onDrop(details.data, after);
      },
      builder: (context, candidates, _) => Stack(
        clipBehavior: Clip.none,
        children: [
          Draggable<_RailDrag>(
            data: widget.drag,
            dragAnchorStrategy: pointerDragAnchorStrategy,
            feedback: Material(
              type: MaterialType.transparency,
              child: FractionalTranslation(
                translation: const Offset(-0.5, -0.5),
                child: Container(
                  width: buttonSize,
                  height: buttonSize,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: theme.dropBackground,
                    borderRadius: BorderRadius.circular(theme.buttonRadius),
                  ),
                  child: icon,
                ),
              ),
            ),
            childWhenDragging: Opacity(opacity: theme.draggingOpacity, child: padded),
            child: padded,
          ),
          if (candidates.isNotEmpty && _dropAfter != null)
            Positioned(
              left: 0,
              right: 0,
              top: _dropAfter! ? null : 0,
              bottom: _dropAfter! ? 0 : null,
              child: Container(height: theme.dropLineWidth, color: theme.indicatorColor),
            ),
        ],
      ),
    );
  }
}
