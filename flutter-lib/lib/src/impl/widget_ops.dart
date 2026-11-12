import '../comm/comm.dart';
import '../custom/swing_island_evolve.dart';
import 'browser_evolve.dart';
import 'ctabfolder_evolve.dart';
import 'display_evolve.dart';
import 'gcdrawer_evolve.dart';
import 'menu_evolve.dart';
import 'shell_evolve.dart';
import 'styledtext_evolve.dart';
import 'utils/dnd_session.dart';
import 'utils/tracker_session.dart';

/// Registers, once per comm, the handlers of every kind of widget that answers channels of its own
/// (see `EquoCommService.onWidgetOp`). Before any frame is applied: a frame for a widget can arrive
/// before the widget exists, and is held for it only when its kind is listening.
void registerWidgetOps() => EquoCommService.ensureWidgetOps(() {
      StyledTextImpl.registerOps();
      CTabFolderImpl.registerOps();
      MenuImpl.registerOps();
      BrowserImpl.registerOps();
      GCDrawer.registerOps();
      DndSession.registerOps();
      TrackerSession.registerOps();
      ShellImpl.registerOps();
      SwingIslandRegion.registerOps();
      registerDisplayOps();
    });
