package dev.equo.swt.awt;

import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartSwingIsland;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SwingIsland;

/**
 * {@code SWT_AWT.new_Frame} while swing-evolve's engine owns the JVM's AWT
 * ({@link Config#hasSwingEngine}).
 *
 * <p>The engine gives every AWT window a fake peer and a {@code windowId}, and mirrors its content
 * as Flutter widgets to whoever mounts a {@code SwingMirror} for that id. So the frame is a plain
 * undecorated {@link Frame}, and a {@link SwingIsland} filling the EMBEDDED composite is told the id
 * and the engine's comm port so its Flutter region can mount the mirror. Nothing is blitted, and
 * none of {@link EvolveSwingHost}'s toolkit set-up applies: with fake peers AWT makes no AppKit
 * call.
 *
 * <p>Reaches the engine by reflection only. {@code swt_native} compiles against nothing of
 * swing-evolve, which a host puts on its classpath (and its {@code -javaagent}) at runtime.
 */
public final class SwingIslandHost {

    private SwingIslandHost() {}

    /** The key stock {@code SWT_AWT.getFrame(parent)} reads. */
    private static final String EMBEDDED_FRAME_KEY = "org.eclipse.swt.awt.SWT_AWT.embeddedFrame";

    /** The event the region asks on and is answered on: {@code SwingIsland/<id>/swingIsland}. */
    static final String ISLAND_EVENT = "swingIsland";

    public static Frame newFrame(Composite parent) {
        if (parent == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        parent.setLayout(new FillLayout());
        SwingIsland child = new SwingIsland(parent, SWT.NONE);
        if (!(child.getImpl() instanceof DartSwingIsland)) {
            child.dispose();
            throw new IllegalStateException("swing-evolve's engine owns AWT but " + parent
                    + " is not a Dart widget, so it cannot host a Swing island");
        }
        DartSwingIsland island = (DartSwingIsland) child.getImpl();
        parent.layout(true);

        // Shown on the calling (SWT) thread, where the blit path built its frame too. It opens at
        // the island's current size, 1x1 before layout: the mirror reports its real box on its
        // first layout and Java adopts it.
        Frame frame = new Frame();
        frame.setUndecorated(true);
        Rectangle area = child.getClientArea();
        frame.setSize(Math.max(1, area.width), Math.max(1, area.height));
        frame.setVisible(true);

        Map<String, Object> info = describe(frame);
        // The region asks when it mounts (it may not exist yet, and a push nobody listens to is
        // dropped) and is answered on its own channel; the push covers a region already there.
        FlutterBridge.onPayload(island, ISLAND_EVENT, p -> FlutterBridge.send(island, ISLAND_EVENT, info));
        FlutterBridge.send(island, ISLAND_EVENT, info);

        // The child goes with the EMBEDDED composite, so this covers the parent's dispose too.
        child.addListener(SWT.Dispose, e -> frame.dispose());
        parent.setData(EMBEDDED_FRAME_KEY, frame);
        return frame;
    }

    /** The frame's {@code windowId} and the engine's comm port, which is all the region needs. */
    private static Map<String, Object> describe(Frame frame) {
        EngineCalls calls = EngineCalls.INSTANCE;
        if (calls.unavailable != null) throw new IllegalStateException(calls.unavailable, calls.cause);
        try {
            int windowId = (Integer) calls.windowIdOf.invoke(null, frame);
            Object engine = calls.start.invoke(null);
            Object comm = calls.comm.invoke(engine);
            int port = (Integer) calls.getPort.invoke(comm);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("windowId", windowId);
            info.put("port", port);
            return info;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(NOT_CALLABLE, e);
        }
    }

    private static final String NOT_CALLABLE = "swing-evolve's engine is present but not callable";

    /** The engine's entry points, looked up once, on the first frame. */
    private static final class EngineCalls {
        static final EngineCalls INSTANCE = new EngineCalls();

        Method windowIdOf, start, comm, getPort;
        /** Why the engine cannot be called, or null. */
        String unavailable;
        Throwable cause;

        private EngineCalls() {
            Class<?> engineClass = Config.swingEngineClass("dev.equo.swing.engine.Engine");
            Class<?> commClass = Config.swingEngineClass("dev.equo.swing.bridge.comm.CommService");
            if (engineClass == null || commClass == null) {
                unavailable = "swing-evolve's engine classes are not loadable";
                return;
            }
            try {
                windowIdOf = engineClass.getMethod("windowIdOf", Window.class);
                start = engineClass.getMethod("start");
                comm = engineClass.getMethod("comm");
                getPort = commClass.getMethod("getPort");
            } catch (ReflectiveOperationException e) {
                unavailable = NOT_CALLABLE;
                cause = e;
            }
        }
    }
}
