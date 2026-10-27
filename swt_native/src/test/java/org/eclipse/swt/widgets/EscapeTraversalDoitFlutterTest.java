package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The doit an Escape traversal is offered with, as a Display filter sees it ahead of the control's
 * own listeners. Win32 offers false to a Canvas whose key listeners make it want every key
 * ({@code DLGC_WANTALLKEYS} without {@code DLGC_HASSETSEL}); GTK and Cocoa's {@code traversalCode}
 * offers it nothing either.
 */
@Tag("flutter-it")
class EscapeTraversalDoitFlutterTest {

    private Display display;
    private Shell shell;
    private final java.util.List<String> traversals = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        shell = new Shell(display);
        display.addFilter(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_ESCAPE)
                traversals.add(e.widget.getClass().getSimpleName() + " doit=" + e.doit);
        });
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void pressEscape(Control control) {
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }

    @Test
    @DisplayName("a StyledText is offered Escape with doit=false; its own listener lets it reach the shell")
    void styledTextIsOfferedEscapeWithDoitFalse() {
        pressEscape(new StyledText(shell, SWT.NONE));

        assertThat(traversals).containsExactly("StyledText doit=false", "Shell doit=true");
    }

    @Test
    @DisplayName("a StyledText that sends its own Escape is offered it with doit=false as well")
    void styledTextSendingItsOwnEscapeIsOfferedDoitFalse() {
        StyledText text = new StyledText(shell, SWT.NONE);
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;

        ControlHelper.sendFlutterKeyDown((DartWidget) text.getImpl(), key);

        assertThat(traversals).containsExactly("StyledText doit=false", "Shell doit=true");
    }

    @Test
    @DisplayName("a Canvas with a key listener is offered Escape with doit=false, and the shell after it")
    void keyListeningCanvasIsOfferedEscapeWithDoitFalse() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.addListener(SWT.KeyDown, e -> { });

        pressEscape(canvas);

        assertThat(traversals).containsExactly("Canvas doit=false", "Shell doit=false");
    }

    @Test
    @DisplayName("a Text is offered Escape with doit=true")
    void textIsOfferedEscapeWithDoitTrue() {
        pressEscape(new Text(shell, SWT.MULTI));

        assertThat(traversals).containsExactly("Text doit=true", "Shell doit=true");
    }
}
