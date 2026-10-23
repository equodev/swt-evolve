package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.GcFills;
import dev.equo.swt.harness.GcFills.Fill;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Composite.drawBackground fills with the GC's background when nothing paints behind the composite, and with the
 * background of the control that does otherwise: the composite itself when it has a colour or an image, an
 * ancestor whose background mode hands it down. The GC keeps its own background either way.
 */
@ExtendWith(Mocks.class)
class CompositeDrawBackgroundTest {

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);

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

    /**
     * What a GC with a blue background filled while {@code paint} ran, then with one more 1x1 fill at (39, 39)
     * that shows what the GC fills with afterwards.
     */
    private List<Fill> drawn(Composite composite, java.util.function.Consumer<GC> paint) {
        Canvas surface = new Canvas(composite.getShell(), SWT.NONE);
        surface.setBounds(0, 0, 40, 40);
        GC gc = new GC(surface);
        try {
            gc.setBackground(new Color(composite.getDisplay(), BLUE));
            bridge.comm.sent.clear();
            paint.accept(gc);
            gc.fillRectangle(39, 39, 1, 1);
            assertThat(gc.getBackground().getRGB()).as("the GC keeps its background").isEqualTo(BLUE);
        } finally {
            gc.dispose();
            surface.dispose();
        }
        return GcFills.of(bridge.comm.sent);
    }

    private static final Fill BLUE_AFTERWARDS = Fill.colour(39, 39, 1, 1, BLUE);

    @Test
    @DisplayName("with nothing painting behind it, the GC's own background fills the rectangle")
    void nothingBehindFillsWithTheGc() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        assertThat(drawn(composite, gc -> composite.drawBackground(gc, 1, 2, 3, 4, 0, 0)))
                .containsExactly(Fill.colour(1, 2, 3, 4, BLUE), BLUE_AFTERWARDS);
    }

    @Test
    @DisplayName("a composite with a colour fills with it")
    void anOwnColourFills() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setBackground(new Color(shell.getDisplay(), RED));
        Control behind = ((DartControl) composite.getImpl()).findBackgroundControl();
        assertThat(behind).isSameAs(composite);
        assertThat(drawn(composite, gc -> composite.drawBackground(gc, 1, 2, 3, 4, 0, 0)))
                .containsExactly(Fill.colour(1, 2, 3, 4, RED), BLUE_AFTERWARDS);
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
    @DisplayName("a child of a group that forces its colour finds the group, as on Win32 and GTK")
    void aChildOfAGroupFindsItsForcedColour() {
        Shell shell = Mocks.shell();
        Group group = new Group(shell, SWT.NONE);
        group.setBackground(new Color(shell.getDisplay(), RED));
        group.setBackgroundMode(SWT.INHERIT_FORCE);
        Composite inGroup = new Composite(group, SWT.NONE);
        assertThat(((DartControl) inGroup.getImpl()).findBackgroundControl()).isSameAs(group);
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
            // The parent's tiles start at (-3, -4) in the child.
            assertThat(drawn(child, gc -> child.drawBackground(gc, 0, 0, 10, 10, 0, 0)))
                    .containsExactly(Fill.image(0, 0, 10, 10, -3, -4), BLUE_AFTERWARDS);
            assertThat(drawn(child, gc -> child.drawBackground(gc, 0, 0, 7, 6, -3, -4)))
                    .containsExactly(Fill.image(0, 0, 7, 6, 0, 0), BLUE_AFTERWARDS);
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
            assertThatThrownBy(() -> composite.drawBackground(gc, 0, 0, 1, 1, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        } finally {
            image.dispose();
        }
    }
}
