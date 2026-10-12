package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Composite.drawBackground fills with the GC's background when nothing paints behind the composite, and with the
 * background of the control that does otherwise: the composite itself when it has a colour or an image, an
 * ancestor whose background mode hands it down. The GC keeps its own background either way.
 */
@ExtendWith(Mocks.class)
class CompositeDrawBackgroundTest {

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final String RED_ON_WIRE = "\"background\":{\"a\":255,\"b\":0,\"g\":0,\"r\":255}";
    private static final String BLUE_ON_WIRE = "\"background\":{\"a\":255,\"b\":255,\"g\":0,\"r\":0}";
    /** Blue as a GC state change carries it, 0xFF0000FF. */
    private static final String BLUE_ARGB = "\"background\":4278190335";

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** What the GC put on the wire while {@code paint} ran, up to its disposal. */
    private String drawn(Composite composite, java.util.function.Consumer<GC> paint) {
        Canvas surface = new Canvas(composite.getShell(), SWT.NONE);
        surface.setBounds(0, 0, 40, 40);
        GC gc = new GC(surface);
        try {
            gc.setBackground(new Color(composite.getDisplay(), BLUE));
            bridge.comm.sent.clear();
            paint.accept(gc);
            assertThat(gc.getBackground().getRGB()).as("the GC keeps its background").isEqualTo(BLUE);
        } finally {
            gc.dispose();
            surface.dispose();
        }
        String wire = bridge.comm.sent.stream()
                .filter(f -> f.event.startsWith("GC/") || f.event.equals(MessageBatch.EVENT))
                .map(f -> f.json)
                .collect(Collectors.joining());
        return wire;
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1))
            n++;
        return n;
    }

    @Test
    @DisplayName("with nothing painting behind it, the GC's own background fills the rectangle")
    void nothingBehindFillsWithTheGc() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        String wire = drawn(composite, gc -> composite.drawBackground(gc, 1, 2, 3, 4, 0, 0));
        assertThat(wire).contains("fillRectangleintintintint\",[1,2,3,4]]");
        assertThat(wire).as("filled while the GC's background was its own").contains(BLUE_ON_WIRE).doesNotContain(RED_ON_WIRE);
    }

    @Test
    @DisplayName("a composite with a colour fills with it")
    void anOwnColourFills() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setBackground(new Color(shell.getDisplay(), RED));
        Control behind = ((DartControl) composite.getImpl()).findBackgroundControl();
        assertThat(behind).isSameAs(composite);
        String wire = drawn(composite, gc -> composite.drawBackground(gc, 1, 2, 3, 4, 0, 0));
        assertThat(wire).contains("fillRectangleintintintint\",[1,2,3,4]]");
        assertThat(wire.indexOf(RED_ON_WIRE)).as("filled in the composite's colour").isGreaterThanOrEqualTo(0)
                .isLessThan(wire.indexOf("fillRectangleintintintint"));
        assertThat(wire.indexOf(BLUE_ARGB)).as("then the GC's own background is back")
                .isGreaterThan(wire.indexOf("fillRectangleintintintint"));
    }

    @Test
    @DisplayName("a child under a forcing parent fills with the parent's colour; without the mode it does not")
    void aForcedColourReachesTheChild() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBackground(new Color(shell.getDisplay(), RED));
        Composite child = new Composite(parent, SWT.NONE);
        assertThat(((DartControl) child.getImpl()).findBackgroundControl()).isNull();
        parent.setBackgroundMode(SWT.INHERIT_FORCE);
        assertThat(((DartControl) child.getImpl()).findBackgroundControl()).isSameAs(parent);
        Composite grandchild = new Composite(child, SWT.NONE);
        assertThat(((DartControl) grandchild.getImpl()).findBackgroundControl()).isSameAs(parent);
        child.setBackground(new Color(shell.getDisplay(), BLUE));
        assertThat(((DartControl) grandchild.getImpl()).findBackgroundControl()).isSameAs(child);
    }

    @Test
    @DisplayName("a control is transparent only while it has no colour of its own and its parent is")
    void transparencyFollowsTheOwnColourAndTheParent() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        DartControl impl = (DartControl) composite.getImpl();
        assertThat(impl.isTransparent()).isFalse();
        Group group = new Group(shell, SWT.NONE);
        Composite inGroup = new Composite(group, SWT.NONE);
        assertThat(((DartControl) inGroup.getImpl()).isTransparent()).isTrue();
        inGroup.setBackground(new Color(shell.getDisplay(), RED));
        assertThat(((DartControl) inGroup.getImpl()).isTransparent()).isFalse();
    }

    @Test
    @DisplayName("an image is tiled from the origin of the control that has it, moved by the offsets")
    void anImageIsTiledFromItsOwnersOrigin() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(0, 0, 100, 100);
        Image image = new Image(shell.getDisplay(), 10, 10);
        try {
            parent.setBackgroundImage(image);
            parent.setBackgroundMode(SWT.INHERIT_FORCE);
            Composite child = new Composite(parent, SWT.NONE);
            child.setBounds(3, 4, 50, 50);
            String wire = drawn(child, gc -> child.drawBackground(gc, 0, 0, 10, 10, 0, 0));
            // The parent's tiles start at (-3, -4) in the child: the 10x10 rectangle meets four of them.
            assertThat(count(wire, "\"swt\":\"Image\"")).isEqualTo(4);
            assertThat(wire).contains(",3,4,7,6,0,0,7,6]").contains(",3,0,7,4,0,6,7,4]")
                    .contains(",0,4,3,6,7,0,3,6]").contains(",0,0,3,4,7,6,3,4]");
            assertThat(wire).doesNotContain("fillRectangle");
            String shifted = drawn(child, gc -> child.drawBackground(gc, 0, 0, 7, 6, -3, -4));
            assertThat(count(shifted, "\"swt\":\"Image\"")).isEqualTo(1);
            assertThat(shifted).contains(",0,0,7,6,0,0,7,6]");
        } finally {
            image.dispose();
        }
    }

    @Test
    @DisplayName("a disposed GC is refused before anything is drawn")
    void aDisposedGcIsRefused() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        Image image = new Image(shell.getDisplay(), 4, 4);
        GC gc = new GC(image);
        gc.dispose();
        try {
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                    () -> composite.drawBackground(gc, 0, 0, 1, 1, 0, 0));
        } finally {
            image.dispose();
        }
    }
}
