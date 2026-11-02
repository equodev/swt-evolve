package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The client moves the focus on Tab by itself unless Java arms {@code traverse/vetoable} for the
 * focused control, and then waits for Java's verdict. Java arms it whenever that verdict can be
 * false: a Traverse listener may veto the Tab, or the control keeps Tab by default (a key-listening
 * Composite, as every native's {@code traversalCode} gives it).
 */
@Tag("flutter-it")
class TabGateFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void open() {
        web = TestWebBridge.newDisplay();
        display = web.display;
        shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        new Text(shell, SWT.SINGLE);
    }

    private boolean tabGateAfterFocusing(Control control) {
        shell.setSize(400, 300);
        shell.open();
        drain();
        web.comm.sent.clear();
        control.setFocus();
        drain();
        String value = null;
        for (GcFrames.Entry entry : BatchEntries.of(web.comm.sent)) {
            if (entry.channel().endsWith("/traverse/vetoable"))
                value = String.valueOf(entry.value());
        }
        assertThat(value).as("traverse/vetoable sent on focus").isNotNull();
        return value.contains("true");
    }

    @Test
    @DisplayName("a focused key-listening Composite arms the Tab gate: it keeps Tab by default")
    void keyListeningCompositeArmsTheTabGate() {
        open();
        Composite surface = new Composite(shell, SWT.NONE);
        surface.addListener(SWT.KeyDown, e -> { });

        assertThat(tabGateAfterFocusing(surface)).isTrue();
    }

    @Test
    @DisplayName("a focused Button with no Traverse listener leaves the Tab gate unarmed")
    void plainButtonLeavesTheTabGateUnarmed() {
        open();
        Button button = new Button(shell, SWT.PUSH);

        assertThat(tabGateAfterFocusing(button)).isFalse();
    }

    @Test
    @DisplayName("a focused Button with a Traverse listener arms the Tab gate")
    void traverseListeningButtonArmsTheTabGate() {
        open();
        Button button = new Button(shell, SWT.PUSH);
        button.addListener(SWT.Traverse, e -> { });

        assertThat(tabGateAfterFocusing(button)).isTrue();
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

}
