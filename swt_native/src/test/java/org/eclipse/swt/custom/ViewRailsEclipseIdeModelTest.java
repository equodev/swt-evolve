package org.eclipse.swt.custom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The view rails of a real Eclipse IDE window: the layout e4 saved for the PDE perspective while its
 * bottom stack was maximized, so the left stack is minimized by the zoom, and the editor area and
 * the Outline stack sit in a sash e4 hid. Each stack still goes where the perspective's layout puts
 * it, not where its widget happens to be.
 */
class ViewRailsEclipseIdeModelTest {

    private E4ModelFixture.Loaded model;
    private Map<Object, Integer> places;

    @BeforeEach
    void setUp() throws Exception {
        model = E4ModelFixture.load("eclipse-ide-pde-workbench.xmi");
        places = ViewRailsModelTest.places(model.window);
    }

    @Test
    void the_navigation_stack_left_of_the_editor_is_at_the_left_rails_top() {
        assertThat(places.get(model.get("topLeft"))).isEqualTo(ViewRails.LEFT_TOP);
    }

    @Test
    void the_outline_stack_right_of_the_editor_is_at_the_right_rails_top_while_minimized() {
        assertThat(places.get(model.get("org.eclipse.ui.views.ContentOutlineMStack"))).isEqualTo(ViewRails.RIGHT_TOP);
    }

    @Test
    void the_maximized_stack_under_the_editor_is_at_the_left_rails_bottom() {
        assertThat(places.get(model.get("bottomRight"))).isEqualTo(ViewRails.LEFT_BOTTOM);
    }

    @Test
    void only_the_rendered_view_stacks_of_the_current_perspective_are_listed_in_model_order() {
        assertThat(places.keySet()).containsExactly(
                model.get("topLeft"),
                model.get("org.eclipse.ui.views.ContentOutlineMStack"),
                model.get("bottomRight"));
    }
}
