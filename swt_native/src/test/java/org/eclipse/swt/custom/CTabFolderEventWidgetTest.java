package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every CTabFolderEvent a folder hands its CTabFolder2Listeners names the folder in
 * {@code event.widget}, as upstream CTabFolder does at each call site. e4's MinMaxAddon resolves the
 * part stack from that field on the first line of minimize/maximize/restore; without it the
 * listener throws and the stack's minimize and maximize buttons do nothing.
 */
@ExtendWith(Mocks.class)
class CTabFolderEventWidgetTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void everyFolderEventCarriesTheFolder() {
        Shell shell = Mocks.swtShell();
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        new CTabItem(folder, SWT.CLOSE);
        List<CTabFolderEvent> events = new ArrayList<>();
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override public void minimize(CTabFolderEvent e) { events.add(e); }
            @Override public void maximize(CTabFolderEvent e) { events.add(e); }
            @Override public void restore(CTabFolderEvent e) { events.add(e); }
            @Override public void showList(CTabFolderEvent e) { events.add(e); }
            @Override public void close(CTabFolderEvent e) { e.doit = false; events.add(e); }
        });
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        Event close = new Event();
        close.index = 0;

        CTabFolderHelper.handleMinimize(impl, new Event());
        CTabFolderHelper.handleMaximize(impl, new Event());
        CTabFolderHelper.handleRestore(impl, new Event());
        CTabFolderHelper.handleShowList(impl, new Event());
        CTabFolderHelper.handleClose(impl, close);

        assertThat(events).hasSize(5);
        assertThat(events).allSatisfy(e -> assertThat(e.widget).isSameAs(folder));
    }
}
