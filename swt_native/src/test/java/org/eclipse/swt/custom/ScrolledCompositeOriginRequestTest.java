package org.eclipse.swt.custom;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.jupiter.api.Test;

/**
 * Every {@code setOrigin}, which {@code showControl} ends with, is a request the render side acts on once: it is counted and
 * named, and it travels with the origin it asked for.
 */
class ScrolledCompositeOriginRequestTest extends SerializeTestBase {

    private ScrolledComposite scrolledWithContent() {
        ScrolledComposite sc = new ScrolledComposite(shell(), SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);
        return sc;
    }

    private static VScrolledComposite value(ScrolledComposite sc) {
        return ((DartScrolledComposite) sc.getImpl()).getValue();
    }

    @Test
    void a_new_composite_has_made_no_request() {
        ScrolledComposite sc = scrolledWithContent();

        assertThat(value(sc).getOriginRequest()).isZero();
    }

    @Test
    void setOrigin_is_a_request_that_is_named() {
        ScrolledComposite sc = scrolledWithContent();
        serialize(sc);

        sc.setOrigin(10, 50);

        assertThat(value(sc).getOriginRequest()).isEqualTo(1);
        assertThat(value(sc).changedKeys()).contains(VScrolledComposite.ORIGIN_REQUEST, VScrolledComposite.ORIGIN);
    }

    @Test
    void asking_for_the_same_origin_again_is_another_request() {
        ScrolledComposite sc = scrolledWithContent();
        sc.setOrigin(0, 50);
        serialize(sc);

        sc.setOrigin(0, 50);

        assertThat(value(sc).getOriginRequest()).isEqualTo(2);
        assertThat(value(sc).changedKeys()).contains(VScrolledComposite.ORIGIN_REQUEST);
    }

    @Test
    void without_content_there_is_nothing_to_move() {
        ScrolledComposite sc = new ScrolledComposite(shell(), SWT.H_SCROLL | SWT.V_SCROLL);

        sc.setOrigin(0, 50);

        assertThat(value(sc).getOriginRequest()).isZero();
    }

    @Test
    void the_request_and_its_origin_are_on_the_wire() {
        ScrolledComposite sc = scrolledWithContent();
        sc.setOrigin(10, 50);

        java.util.Map<String, Object> state = serialize(sc);

        assertThatWire(state).node("originRequest").isEqualTo(1);
        assertThatWire(state).node("origin.x").isEqualTo(10);
        assertThatWire(state).node("origin.y").isEqualTo(50);
    }
}
