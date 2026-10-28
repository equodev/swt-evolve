import '../gen/treecursor.dart';
import '../impl/canvas_evolve.dart';

/// A TreeCursor paints itself from its own SWT.Paint listener -- the cell it covers, in its colours, with
/// the focus rectangle when it has focus -- so on this side it is a Canvas and nothing more. The Tree
/// places it over the cell at the bounds Java gives it.
class TreeCursorImpl<T extends TreeCursorSwt, V extends VTreeCursor>
    extends CanvasImpl<T, V> {}
