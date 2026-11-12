import '../gen/glcanvas.dart';
import '../impl/canvas_evolve.dart';

/// A GLCanvas's frames arrive as drawing on its GC, so it shows as a Canvas.
class GLCanvasImpl<T extends GLCanvasSwt, V extends VGLCanvas>
    extends CanvasImpl<T, V> {}
