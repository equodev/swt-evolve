package dev.equo.swt.awt;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Swing content in an {@code SWT.EMBEDDED} composite fills the composite's client area, whatever
 * the composite's layout or its own {@code getClientArea()} answers.
 */
class SwingSurfaceSizeTest extends SerializeTestBase {

    private Mocks.Delivery delivery;

    @BeforeEach
    void deliverEvents() {
        delivery = Mocks.deliverEvents(Mocks.display());
    }

    @AfterEach
    void stopDeliveringEvents() {
        delivery.close();
    }

    static Stream<Arguments> surfaces() {
        return Stream.of(
                Arguments.of("paint surface", (Function<Composite, Control>) EvolveSwingHost::newPaintSurface),
                Arguments.of("swing-evolve island", (Function<Composite, Control>) SwingIslandHost::newIsland));
    }

    /** An EMBEDDED composite whose client area reports a Swing preferred size of 9x8. */
    private static Composite embeddingComposite() {
        return new Composite(Mocks.shell(), SWT.EMBEDDED) {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 9, 8);
            }
        };
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("surfaces")
    void itFillsACompositeSizedAfterItWasCreated(String name, Function<Composite, Control> create) {
        Composite embedded = embeddingComposite();
        Control surface = create.apply(embedded);

        embedded.setBounds(1, 2, 467, 96);

        assertThat(surface.getBounds()).isEqualTo(new Rectangle(0, 0, 467, 96));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("surfaces")
    void itFillsACompositeThatAlreadyHasItsSize(String name, Function<Composite, Control> create) {
        Composite embedded = embeddingComposite();
        embedded.setBounds(0, 0, 1153, 733);

        Control surface = create.apply(embedded);

        assertThat(surface.getBounds()).isEqualTo(new Rectangle(0, 0, 1153, 733));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("surfaces")
    void aLayoutTheApplicationGivesTheCompositeDoesNotPlaceIt(String name, Function<Composite, Control> create) {
        Composite embedded = embeddingComposite();
        embedded.setLayout(new FillLayout());
        Control surface = create.apply(embedded);

        embedded.setBounds(1, 2, 467, 96);
        embedded.layout(true, true);

        assertThat(surface.getBounds()).isEqualTo(new Rectangle(0, 0, 467, 96));
    }

    static Stream<Arguments> settersOnEachSurface() {
        List<Arguments> setters = List.of(
                Arguments.of("setBounds(int, int, int, int)", (Consumer<Control>) c -> c.setBounds(3, 4, 9, 8)),
                Arguments.of("setBounds(Rectangle)", (Consumer<Control>) c -> c.setBounds(new Rectangle(3, 4, 9, 8))),
                Arguments.of("setSize(int, int)", (Consumer<Control>) c -> c.setSize(9, 8)),
                Arguments.of("setSize(Point)", (Consumer<Control>) c -> c.setSize(new Point(9, 8))),
                Arguments.of("setLocation(int, int)", (Consumer<Control>) c -> c.setLocation(3, 4)),
                Arguments.of("setLocation(Point)", (Consumer<Control>) c -> c.setLocation(new Point(3, 4))));
        return surfaces().flatMap(surface -> setters.stream().map(setter -> Arguments.of(
                surface.get()[0] + " " + setter.get()[0], surface.get()[1], setter.get()[1])));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("settersOnEachSurface")
    void noSetterMovesItOffTheClientArea(String name, Function<Composite, Control> create, Consumer<Control> setter) {
        Composite embedded = embeddingComposite();
        embedded.setBounds(1, 2, 467, 96);
        Control surface = create.apply(embedded);

        setter.accept(surface);

        assertThat(surface.getBounds()).isEqualTo(new Rectangle(0, 0, 467, 96));
    }
}
