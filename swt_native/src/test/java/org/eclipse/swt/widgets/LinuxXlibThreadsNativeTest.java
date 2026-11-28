package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterLibraryLoader;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * GTK's X connection is shared by GTK's thread and the engine's raster thread, and libX11 before 1.8
 * makes a connection thread-safe only when XInitThreads ran before it was opened. The bridge runs it
 * when it loads, so the bridge has to be in the process once the desktop surface has initialized GTK,
 * and the engine it brings must use the system's GLib, not the copies bundled for WebKitGTK. The web
 * surface initializes GTK too, and runs no engine: an install for it carries no bridge.
 * <p>
 * This checks what is mapped after {@code init()}, not the order within it; with libX11 1.8 or later
 * the order is not observable in the process. The input tier of the event-parity harness, which runs
 * on libX11 1.7, is what fails when GTK opens its display first.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.LINUX)
public class LinuxXlibThreadsNativeTest {

    private static final long TIMEOUT_MS = 60_000;

    @Test
    void initLoadsTheBridgeOnTheSystemsGLib() throws Exception {
        assertThat(runInit(true)).contains("bridgeLoaded=true").contains("bundledGLib=false");
    }

    @Test
    void initLoadsNoBridgeForTheWebSurface() throws Exception {
        assertThat(runInit(false)).contains("bridgeLoaded=false");
    }

    /** Runs {@code init()} for the desktop or the web surface in a child JVM, where nothing has loaded the bridge or GTK yet; its output. */
    private static String runInit(boolean desktop) throws Exception {
        if (System.getenv("DISPLAY") == null)
            Assumptions.abort("no X display to initialize GTK on");
        try {
            FlutterLibraryLoader.initialize();
        } catch (RuntimeException e) {
            Assumptions.abort("no runner built here: " + e.getMessage());
        }
        Path log = Files.createTempFile("xlib-threads", ".log");
        try {
            List<String> command = List.of(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                    "-Djava.awt.headless=true", "-cp", System.getProperty("java.class.path"), InitProbe.class.getName(), String.valueOf(desktop));
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                child.destroyForcibly();
            return Files.readString(log, StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** Initializes the platform state of the desktop surface, or of the web one, then says what is mapped. */
    public static final class InitProbe {

        public static void main(String[] args) throws Exception {
            if (Boolean.parseBoolean(args[0]))
                ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
            DisplayBridgePlatform.init();
            List<String> maps = Files.readAllLines(Path.of("/proc/self/maps"));
            boolean bridge = maps.stream().anyMatch(line -> line.endsWith("/libflutter_bridge.so"));
            boolean bundledGLib = maps.stream().anyMatch(line -> line.contains("/bundle/lib/libgobject-2.0.so"));
            System.out.println("bridgeLoaded=" + bridge + " bundledGLib=" + bundledGLib);
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }
}
