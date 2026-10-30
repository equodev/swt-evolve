package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

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
        press(control, SWT.ESC, SWT.ESC, 0);
    }

    private static void press(Control control, int keyCode, char character, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.character = character;
        key.stateMask = stateMask;
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
    @DisplayName("a Composite with a key listener is offered Escape with doit=false, as a Canvas is")
    void keyListeningCompositeIsOfferedEscapeWithDoitFalse() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.addListener(SWT.KeyDown, e -> { });

        pressEscape(composite);

        assertThat(traversals).containsExactly("Composite doit=false", "Shell doit=false");
    }

    @Test
    @DisplayName("a ScrolledComposite with a KeyUp listener is offered Escape with doit=false")
    void keyListeningScrolledCompositeIsOfferedEscapeWithDoitFalse() {
        ScrolledComposite scrolled = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolled.addListener(SWT.KeyUp, e -> { });

        pressEscape(scrolled);

        assertThat(traversals).containsExactly("ScrolledComposite doit=false", "Shell doit=false");
    }

    /** The Composites the natives build around a native control of their own, without the CANVAS state. */
    static Stream<Arguments> nativeControlComposites() {
        return Stream.<Function<Shell, Composite>>of(
                s -> new Combo(s, SWT.NONE), s -> new CoolBar(s, SWT.NONE), s -> new DateTime(s, SWT.NONE),
                s -> new ExpandBar(s, SWT.NONE), s -> new Group(s, SWT.NONE), s -> new Spinner(s, SWT.NONE),
                s -> new TabFolder(s, SWT.NONE), s -> new Table(s, SWT.NONE), s -> new ToolBar(s, SWT.NONE),
                s -> new Tree(s, SWT.NONE))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("nativeControlComposites")
    @DisplayName("a Composite that is not a canvas natively is offered Escape with doit=true despite a key listener")
    void keyListeningNativeControlCompositeIsOfferedEscapeWithDoitTrue(Function<Shell, Composite> create) {
        Composite composite = create.apply(shell);
        composite.addListener(SWT.KeyDown, e -> { });

        pressEscape(composite);

        assertThat(traversals).containsExactly(composite.getClass().getSimpleName() + " doit=true", "Shell doit=true");
    }

    @Test
    @DisplayName("a Composite with a key listener is offered Return and Tab with doit=false")
    void keyListeningCompositeIsOfferedReturnAndTabWithDoitFalse() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.addListener(SWT.KeyDown, e -> { });
        new Text(shell, SWT.SINGLE);
        java.util.List<String> offered = new ArrayList<>();
        display.addFilter(SWT.Traverse, e -> {
            if (e.widget == composite)
                offered.add(e.detail + " doit=" + e.doit);
        });

        press(composite, SWT.CR, SWT.CR, 0);
        press(composite, SWT.TAB, SWT.TAB, 0);
        press(composite, SWT.TAB, SWT.TAB, SWT.SHIFT);

        assertThat(offered).containsExactly(
                SWT.TRAVERSE_RETURN + " doit=false",
                SWT.TRAVERSE_TAB_NEXT + " doit=false",
                SWT.TRAVERSE_TAB_PREVIOUS + " doit=false");
    }

    @Test
    @DisplayName("a Composite without key listeners is offered Escape with doit=true")
    void compositeWithoutKeyListenersIsOfferedEscapeWithDoitTrue() {
        pressEscape(new Composite(shell, SWT.NONE));

        assertThat(traversals).containsExactly("Composite doit=true", "Shell doit=true");
    }

    @Test
    @DisplayName("a NO_FOCUS Canvas without key listeners is offered Escape with doit=false, as on GTK and Cocoa; Win32 offers true")
    void noFocusCanvasIsOfferedEscapeWithDoitFalseAsOnGtkAndCocoa() {
        pressEscape(new Canvas(shell, SWT.NO_FOCUS));

        assertThat(traversals).containsExactly("Canvas doit=false", "Shell doit=false");
    }

    @Test
    @DisplayName("a NO_FOCUS Composite without key listeners is offered Escape with doit=false, as a Canvas is (GTK, Cocoa)")
    void noFocusCompositeIsOfferedEscapeWithDoitFalseAsOnGtkAndCocoa() {
        pressEscape(new Composite(shell, SWT.NO_FOCUS));

        assertThat(traversals).containsExactly("Composite doit=false", "Shell doit=false");
    }

    @Test
    @DisplayName("a dialog stays open on Escape in a Canvas with a key listener, and closes on it in a StyledText")
    void dialogClosesOnEscapeOnlyWhenTheControlLetsItGo() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM);
        Canvas canvas = new Canvas(dialog, SWT.NONE);
        canvas.addListener(SWT.KeyDown, e -> { });
        StyledText text = new StyledText(dialog, SWT.NONE);
        java.util.List<String> closes = new ArrayList<>();
        dialog.addListener(SWT.Close, e -> {
            closes.add("closed");
            e.doit = false;
        });
        dialog.open();

        pressEscape(canvas);
        assertThat(closes).isEmpty();

        pressEscape(text);
        assertThat(closes).containsExactly("closed");
    }

    @Test
    @DisplayName("a Text is offered Escape with doit=true")
    void textIsOfferedEscapeWithDoitTrue() {
        pressEscape(new Text(shell, SWT.MULTI));

        assertThat(traversals).containsExactly("Text doit=true", "Shell doit=true");
    }
}
