import '../gen/tablecursor.dart';
import '../impl/canvas_evolve.dart';

/// A TableCursor paints itself from its own SWT.Paint listener -- the cell it covers, in its colours, with
/// the focus rectangle when it has focus -- so on this side it is a Canvas and nothing more. The Table
/// places it over the cell at the bounds Java gives it.
class TableCursorImpl<T extends TableCursorSwt, V extends VTableCursor>
    extends CanvasImpl<T, V> {}
