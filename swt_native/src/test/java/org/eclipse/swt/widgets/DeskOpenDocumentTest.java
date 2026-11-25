package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DesktopAppearanceStub;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OS asks the application to open files (Finder's Open With, a file dropped on the Dock icon).
 * Natively each one reaches the Display as {@code SWT.OpenDocument}, which is how an Eclipse IDE
 * opens it in an editor.
 */
class DeskOpenDocumentTest {

    private Display display;
    private String savedMode;
    private DesktopAppearanceStub desktop;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        if (desktop != null) desktop.close();
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
    }

    private OpeningBridge desktopDisplay() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        desktop = new DesktopAppearanceStub("light");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        OpeningBridge bridge = new OpeningBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    @Test
    void eachFileTheOsAsksToOpenIsAnOpenDocumentEvent() {
        OpeningBridge bridge = desktopDisplay();
        List<String> opened = new ArrayList<>();
        display.addListener(SWT.OpenDocument, e -> opened.add(e.text));

        bridge.opened = new String[] {"/work/App.java", "/work/notes.txt"};
        bridge.onUpdate();

        assertThat(opened).containsExactly("/work/App.java", "/work/notes.txt");
    }

    @Test
    void aFileIsOpenedOnce() {
        OpeningBridge bridge = desktopDisplay();
        List<String> opened = new ArrayList<>();
        display.addListener(SWT.OpenDocument, e -> opened.add(e.text));

        bridge.opened = new String[] {"/work/App.java"};
        bridge.onUpdate();
        bridge.onUpdate();

        assertThat(opened).containsExactly("/work/App.java");
    }

    private static final class OpeningBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        /** What the native window was asked to open since the last pump; taken as the runner hands it. */
        String[] opened;

        OpeningBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
        }

        @Override
        protected boolean hasNativeWindow() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected String[] takeOpenedDocuments() {
            String[] taken = opened;
            opened = null;
            return taken;
        }
    }
}
