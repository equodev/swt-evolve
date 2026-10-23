package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** The parts of a Composite that keyboard traversal and Evolve's own layout rules reach, driven directly. */
@ExtendWith(Mocks.class)
class CompositeInternalsTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartComposite impl(Composite composite) {
        return (DartComposite) composite.getImpl();
    }

    /** A layout that logs each layout pass. */
    private static Layout logging(List<String> log) {
        return new Layout() {
            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(10, 10);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
                log.add("layout(" + flushCache + ")");
            }
        };
    }

    @Test
    @DisplayName("the tab list a composite traverses is its own list when it has one, else its children")
    void theTraversedListFollowsTheTabList() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        Composite a = new Composite(composite, SWT.NONE);
        Composite b = new Composite(composite, SWT.NONE);
        assertThat(impl(composite).computeTabList()).containsExactly(composite, a, b);
        composite.setTabList(new Control[] {b});
        assertThat(impl(composite).computeTabList()).containsExactly(composite, b);
    }

    @Test
    @DisplayName("a canvas-like composite takes no traversal keys when it refuses the focus or hooks keys itself")
    void aCompositeThatRefusesFocusOrHooksKeysTakesNoTraversal() {
        Shell shell = Mocks.shell();
        Composite plain = new Composite(shell, SWT.NONE);
        assertThat(impl(plain).traversalCode(0, null)).isNotZero();
        Composite noFocus = new Composite(shell, SWT.NO_FOCUS);
        assertThat(impl(noFocus).traversalCode(0, null)).isZero();
        assertThat(impl(noFocus).isTabItem()).isFalse();
        Composite keys = new Composite(shell, SWT.NONE);
        keys.addListener(SWT.KeyUp, e -> { });
        assertThat(impl(keys).traversalCode(0, null)).isZero();
    }

    @Test
    @DisplayName("a tab group that refuses the focus hands it to the first child that is a tab item and takes it")
    void aGroupThatRefusesTheFocusHandsItToATabItem() {
        List<Control> asked = new ArrayList<>();
        FlutterBridge.set(new RecordingBridge() {
            @Override
            public boolean setFocus(DartControl control) {
                asked.add(control.getApi());
                return control.getApi() instanceof Button;
            }
        });
        Shell shell = Mocks.shell();
        when(shell.isVisible()).thenReturn(true);
        Composite group = new Composite(shell, SWT.NO_FOCUS);
        new Composite(group, SWT.NONE);
        Button radio = new Button(group, SWT.RADIO);
        assertThat(impl(group).setTabGroupFocus()).isTrue();
        assertThat(asked).containsExactly(radio);
        Composite empty = new Composite(shell, SWT.NO_FOCUS);
        new Composite(empty, SWT.NONE);
        assertThat(impl(empty).setTabGroupFocus()).isFalse();
    }

    @Test
    @DisplayName("a composite without a client area is not laid out until it has one, deferral or not")
    void aCompositeWithoutAClientAreaWaitsForOne() {
        List<String> log = new ArrayList<>();
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        composite.setLayout(logging(log));
        composite.layout();
        composite.setLayoutDeferred(true);
        composite.setLayoutDeferred(false);
        assertThat(log).isEmpty();
        composite.setSize(0, 10);
        assertThat(log).containsExactly("layout(true)");
        log.clear();
        composite.setSize(10, 0);
        assertThat(log).containsExactly("layout(false)");
    }

    @Test
    @DisplayName("in an info pop-up, a hinted width wraps a wrapping StyledText to that width")
    void anInfoPopupWrapsItsStyledTextToTheHintedWidth() {
        Shell shell = Mocks.shell();
        when(shell.getStyle()).thenReturn(SWT.ON_TOP | SWT.NO_TRIM);
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new FillLayout());
        new Composite(composite, SWT.NONE);
        StyledText text = new StyledText(composite, SWT.WRAP);
        text.setText("one two three four five six seven eight nine ten eleven twelve thirteen fourteen");
        Point size = composite.computeSize(80, SWT.DEFAULT);
        assertThat(size.x).isEqualTo(80);
        Point preferred = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point textPreferred = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int around = Math.max(0, preferred.y - textPreferred.y);
        int width = Math.max(1, 80 - Math.max(0, preferred.x - textPreferred.x));
        assertThat(size.y).isEqualTo(text.computeSize(width, SWT.DEFAULT).y + around);
    }

    @Test
    @DisplayName("outside an info pop-up, or without a wrapping StyledText, a hinted width is the layout's answer")
    void elsewhereTheLayoutAnswers() {
        Shell shell = Mocks.shell();
        when(shell.getStyle()).thenReturn(SWT.ON_TOP | SWT.NO_TRIM);
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(logging(new ArrayList<>()));
        new StyledText(composite, SWT.NONE);
        assertThat(composite.computeSize(80, SWT.DEFAULT)).isEqualTo(new Point(80, 10));
    }

    @Test
    @DisplayName("a disposed background image is passed over for the colour behind it")
    void aDisposedImageFallsBackToTheColour() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        Image image = new Image(shell.getDisplay(), 4, 4);
        composite.setBackgroundImage(image);
        image.dispose();
        composite.setBackground(new Color(shell.getDisplay(), 1, 2, 3));
        Canvas surface = new Canvas(shell, SWT.NONE);
        surface.setBounds(0, 0, 20, 20);
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(surface);
        try {
            bridge.comm.sent.clear();
            composite.drawBackground(gc, 0, 0, 5, 5, 0, 0);
        } finally {
            gc.dispose();
        }
        String wire = String.join("", bridge.comm.sent.stream().map(f -> f.json).toList());
        assertThat(wire).contains("fillRectangleintintintint\",[0,0,5,5]]").contains("\"r\":1").doesNotContain("\"backgroundPattern\":{");
    }

    @Test
    @DisplayName("the implementation names the container its children are drawn in")
    void theContentViewIsTheBridgesContainer() {
        Composite composite = new Composite(Mocks.shell(), SWT.NONE);
        DartComposite impl = impl(composite);
        assertThat(impl.contentView()).isEqualTo(impl.getBridge().container(impl));
    }
}
