import 'package:flutter/material.dart';
import '../gen/display.dart';
import '../gen/shell.dart';
import '../gen/swt.dart';
import '../gen/tooltip.dart';
import '../gen/widgets.dart' as gen;
import '../comm/comm.dart';
import '../custom/csd/csd_state.dart';
import '../theme/theme_extensions/display_theme_extension.dart';
import 'shell_evolve.dart';

class DisplaySwt extends StatefulWidget {
  final VDisplay value;

  const DisplaySwt({Key? key, required this.value}) : super(key: key);

  @override
  State<DisplaySwt> createState() => _DisplaySwtState();
}

/// Last Display state received per Display id. A [DisplaySwt] state can be
/// recreated mid-session — e.g. when a global config/theme change rebuilds the
/// subtree the Display sits in — which drops the previous State (and its
/// `_display`). The `Display/{id}` update that populated the shells may have
/// already been delivered to that now-discarded State, and Java has no reason to
/// re-send it, so the fresh State would render empty forever. Seeding new States
/// from this cache makes a recreated Display immediately reflect the latest known
/// tree instead of an empty one. (Surfaced on the desktop-native window, where
/// the post-ClientReady properties update races the first Display update.)
final Map<int?, VDisplay> _lastDisplayState = <int?, VDisplay>{};

class _DisplaySwtState extends State<DisplaySwt> {
  late VDisplay _display;

  @override
  void initState() {
    super.initState();
    _display = _lastDisplayState[widget.value.id] ?? widget.value;
    EquoCommService.onRaw('Display/${widget.value.id}', _onUpdate);
  }

  void _onUpdate(dynamic raw) {
    try {
      final updated = VDisplay.fromJson(raw as Map<String, dynamic>);
      _lastDisplayState[widget.value.id] = updated;
      if (mounted) setState(() => _display = updated);
    } catch (e) {
      print('DisplaySwt update error: $e');
    }
  }

  bool _isMainShell(VShell s, BoxConstraints constraints) {
    final b = s.bounds;
    if (b == null || b.x != 0 || b.y != 0) return false;
    if (b.width == 1024 && b.height == 768) return true; // eclipse workbench default
    if (!_isModal(s) &&
        b.width >= (constraints.maxWidth * 0.8).round() &&
        b.height >= (constraints.maxHeight * 0.8).round()) {
      return true;
    }
    return !_isModal(s) && b.width == constraints.maxWidth.toInt() && b.height == constraints.maxHeight.toInt();
  }

  int _shellArea(VShell shell) {
    final bounds = shell.bounds;
    if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
      return -1;
    }
    return bounds.width * bounds.height;
  }

  static bool _isModal(VShell s) {
    final style = s.style;
    return (style & SWT.APPLICATION_MODAL) != 0 ||
        (style & SWT.SYSTEM_MODAL) != 0 ||
        (style & SWT.PRIMARY_MODAL) != 0;
  }

  @override
  Widget build(BuildContext context) {
    final shells = _display.shells ?? [];
    print("Dart Display.build shells: ${shells.length}");
    if (shells.isEmpty) return const SizedBox.shrink();

    return LayoutBuilder(builder: (context, constraints) {
      final mainShells = <VShell>[];
      final dialogShells = <VShell>[];
      VShell? largestNonModalShell;
      var largestNonModalArea = -1;
      for (var s in shells) {
        print("Shell text: ${s.text} ${s.bounds?.x},${s.bounds?.y},${s.bounds?.width},${s.bounds?.height}");
        final isMain = _isMainShell(s, constraints);
        final isModal = _isModal(s);
        if (!isModal) {
          final area = _shellArea(s);
          if (area > largestNonModalArea) {
            largestNonModalArea = area;
            largestNonModalShell = s;
          }
        }
        if (isMain) {
          mainShells.add(s);
        } else {
          dialogShells.add(s);
        }
      }
      if (mainShells.isEmpty && largestNonModalShell != null) {
        dialogShells.remove(largestNonModalShell);
        mainShells.add(largestNonModalShell!);
      }
      for (var s in mainShells) {
        s.bounds!.x = 0;
        s.bounds!.y = 0;
        s.bounds!.width = constraints.maxWidth.toInt();
        s.bounds!.height = constraints.maxHeight.toInt();
      }

      // Feed the main shell's title to the CSD overlay strip (it's already in the Display
      // state, so no separate event is needed). Deferred so we don't notify during build.
      final title = mainShells.isNotEmpty ? (mainShells.first.text ?? '') : '';
      if (csdWindowTitle.value != title) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          csdWindowTitle.value = title;
        });
      }

      return Stack(children: [
        for (final s in mainShells) Positioned.fill(child: gen.mapWidgetFromValue(s)),
        for (final popup in (_display.popups ?? []))
          Positioned.fill(child: gen.mapWidgetFromValue(popup)),
        for (final tooltip in (_display.tooltips ?? []))
          KeyedSubtree(
            key: ValueKey('tooltip_${tooltip.id}'),
            child: Positioned.fill(child: gen.mapWidgetFromValue(tooltip)),
          ),
        if (dialogShells.any(_isModal))
          Positioned.fill(
            child: ColoredBox(
              color: Theme.of(context).extension<DisplayThemeExtension>()!.modalOverlayColor,
            ),
          ),
        for (final dialog in dialogShells)
          KeyedSubtree(
            key: ValueKey(dialog.id),
            child: FloatingShellChromeScope(
              viewportConstraints: constraints,
              child: gen.mapWidgetFromValue(dialog),
            ),
          ),
      ]);
    });
  }
}
