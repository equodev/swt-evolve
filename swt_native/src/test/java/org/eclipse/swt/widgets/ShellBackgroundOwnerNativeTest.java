package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A shell paints behind its children when it has a colour or an image of its own, and only then. */
@Tag("native-unit")
class ShellBackgroundOwnerNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private static Control owner(Control control) {
        return ((DartControl) control.getImpl()).findBackgroundControl();
    }

    @Test
    @DisplayName("a shell answers itself while it has a colour or an image, and nothing otherwise")
    void aShellAnswersItselfWhileItHasABackground() {
        Shell shell = new Shell(display);
        Image image = new Image(display, 4, 4);
        try {
            assertThat(owner(shell)).isNull();
            shell.setBackground(new Color(display, 1, 2, 3));
            assertThat(owner(shell)).isSameAs(shell);
            shell.setBackground(null);
            shell.setBackgroundImage(image);
            assertThat(owner(shell)).isSameAs(shell);
            shell.setBackgroundImage(null);
            assertThat(owner(shell)).isNull();
        } finally {
            image.dispose();
            shell.dispose();
        }
    }

    @Test
    @DisplayName("a child under a shell that forces its background finds the shell")
    void aForcedChildFindsTheShell() {
        Shell shell = new Shell(display);
        try {
            Composite child = new Composite(shell, SWT.NONE);
            shell.setBackground(new Color(display, 1, 2, 3));
            assertThat(owner(child)).isNull();
            shell.setBackgroundMode(SWT.INHERIT_FORCE);
            assertThat(owner(child)).isSameAs(shell);
        } finally {
            shell.dispose();
        }
    }
}
