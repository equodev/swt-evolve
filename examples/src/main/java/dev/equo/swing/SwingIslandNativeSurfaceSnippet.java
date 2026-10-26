package dev.equo.swing;

/**
 * {@link SwingIslandWorkbenchSnippet}, configured the way a deployment that routes its 3D view
 * through a native surface configures it.
 *
 * <p>It is a separate entry point rather than a flag on the workbench because the properties it
 * sets have to be in place before any component is built — the watcher reads them when it decides
 * what to move into a host window, and a view already laid out is not moved afterwards.
 *
 * <p><b>What it reproduces.</b> A field measurement of a workbench whose 3D view came up blank
 * under the texture route and stayed blank: its producer took the canvas's render seam, the engine
 * asked for sixty paints over fifteen seconds and no frame reached the client, the drive loop gave
 * up — and the surface then opened about twenty client frames later, when something finally
 * rendered. From there every frame the engine traced carried {@code 0 surface frames}, a resize
 * produced no new surface line at all, and the view never showed anything. Pinned to the image
 * route the same build showed the view and froze it instead, which is the other half of the shape.
 *
 * <p>Three narrower reproductions of this in the engine's own tests are all green — a surface that
 * opens after the loop gave up, a seam that fires off the EDT, and a carried panel — so what is
 * left to model is the part they do not: the view lives in an <b>island</b>, not in a plain frame.
 * That is what this snippet puts around it.
 *
 * <h2>Reading a run</h2>
 *
 * <p>The instrument is the engine's own frame trace, which this turns on. A healthy texture route
 * says {@code 1 surface frames} on the frames that carry one; the defect says {@code 0 surface
 * frames} on every frame for the life of the process, with one {@code surface opened} line and
 * nothing after it. There is no GPU behind {@link GlLikeProducer}, so no run of this shows a
 * composited texture — the question it answers is whether the frame reaches the wire, which is
 * where the field measurement stopped.
 *
 * <h2>Switches</h2>
 * <ul>
 *   <li>{@code -Dgl.seamSilentMs=0} — the control: the seam fires from the first render, so the
 *       surface opens while the drive loop is still running.
 *   <li>{@code -Ddev.equo.swing.nativeSurfaceRoute=image} — the other control: the image route,
 *       which in the field showed the view and froze it.
 *   <li>{@code -Dgl.seamThread=edt} — fires the seam on the painting thread instead of a render
 *       thread of the producer's own.
 *   <li>{@code -Dgl.gesturesRepaint=true} — makes the canvas's wheel and drag call
 *       {@code repaint()}, which the measured application's does not.
 * </ul>
 */
public final class SwingIslandNativeSurfaceSnippet {

    public static void main(String[] args) {
        // THESE HAVE TO BE ON THE COMMAND LINE, and setting them here is too late for the one that
        // matters. `NativeSurfaces.OPTED_IN` is a `static final` read when that class loads, and
        // the engine's `-javaagent` runs its premain before this method — so by the time main
        // assigns the property the opt-in set has already been read, and read empty. Measured as a
        // run that logged the canvas rendering 432 times and not one [NativeSurface] line.
        //
        // A deployment never hits this because its properties come from its launcher's .ini, which
        // is the JVM's own command line. This refuses rather than running half-configured, because
        // the half-configured run looks exactly like a deployment that opted nothing in.
        if (System.getProperty("dev.equo.swing.nativeSurfaceClasses") == null) {
            System.err.println("[native-surface] nativeSurfaceClasses is not set, and setting it"
                    + " now would be too late — the opt-in set is read when the engine's agent"
                    + " loads, before main. Run it this way:");
            System.err.println("  ./gradlew :swing-island:runIslandDeskExample"
                    + " -PmainClass=" + SwingIslandNativeSurfaceSnippet.class.getName()
                    + " \\\n      -Ddev.equo.swing.nativeSurfaceClasses="
                    + GlLikeCanvas.class.getName()
                    + " \\\n      -Ddev.equo.swing.nativeSurfaceCarry="
                    + GlLikeCanvasPanel.class.getName()
                    + " \\\n      -Ddev.equo.swing.nativeSurfaceProducer="
                    + GlLikeProducer.class.getName()
                    + " \\\n      -Ddev.equo.swing.nativeSurfaceRoute=auto"
                    + " -Ddev.equo.swing.traceFrames=true");
            System.exit(2);
        }
        // Anything thrown on a thread nobody is joining. On macOS a Java exception inside an
        // AppKit callback is re-thrown as NSGenericException "Java Exception" and aborts the
        // process with the Java side of the story gone, so a run that dies says nothing about why.
        // This is a snippet's own diagnostic, not the engine's: it prints and lets the throw
        // continue.
        Thread.setDefaultUncaughtExceptionHandler((thread, thrown) -> {
            System.err.println("[native-surface] uncaught on " + thread.getName() + ":");
            thrown.printStackTrace();
        });

        // The rest are read per use rather than at class load, so a default here still takes.
        preset("dev.equo.swing.nativeSurfaceCarry", GlLikeCanvasPanel.class.getName());
        preset("dev.equo.swing.nativeSurfaceProducer", GlLikeProducer.class.getName());
        preset("dev.equo.swing.nativeSurfaceRoute", "auto");
        preset("dev.equo.swing.traceFrames", "true");

        System.out.println("[native-surface] classes=" + System.getProperty(
                "dev.equo.swing.nativeSurfaceClasses"));
        System.out.println("[native-surface] carry="
                + System.getProperty("dev.equo.swing.nativeSurfaceCarry"));
        System.out.println("[native-surface] route="
                + System.getProperty("dev.equo.swing.nativeSurfaceRoute")
                + ", seam silent for " + System.getProperty("gl.seamSilentMs", "15000") + "ms");

        SwingIslandWorkbenchSnippet.main(args);
    }

    private static void preset(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    private SwingIslandNativeSurfaceSnippet() {
    }
}
