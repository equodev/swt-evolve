package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.custom.E4ModelFixture.area;
import static org.eclipse.swt.custom.E4ModelFixture.sash;
import static org.eclipse.swt.custom.E4ModelFixture.stack;

/**
 * A stack's place comes from the e4 model's sash tree and weights, not from its widget's bounds, so
 * it keeps its place while it is minimized or another stack is maximized over everything.
 */
class ViewRailsModelTest {

    /** The stacks {@link ViewRails.Model#collect} lists from {@code window}, each with its place. */
    static Map<Object, Integer> places(Object window) {
        Map<Object, Rectangle> stacks = new LinkedHashMap<>();
        Rectangle[] area = new Rectangle[1];
        new ViewRails.Model().collect(window, new Rectangle(0, 0, ViewRails.MODEL_SPAN, ViewRails.MODEL_SPAN), stacks, area);
        Rectangle reference = area[0] != null ? area[0] : new Rectangle(0, 0, ViewRails.MODEL_SPAN, ViewRails.MODEL_SPAN);
        Map<Object, Integer> places = new LinkedHashMap<>();
        stacks.forEach((stack, rect) -> places.put(stack, ViewRails.placeOf(rect, reference)));
        return places;
    }

    /** The Java perspective: explorer | (editor over bottom) | outline. */
    @Test
    void theJavaPerspectivesStacksGoWhereItsLayoutPutsThem() {
        E4ModelFixture.Stack explorer = stack("explorer", "2500"), outline = stack("outline", "2500"),
                bottom = stack("bottom", "3000");
        Object window = sash(null, true, explorer, sash("5000", false, area("7000"), bottom), outline);

        assertThat(places(window)).containsExactly(
                Map.entry(explorer, ViewRails.LEFT_TOP),
                Map.entry(bottom, ViewRails.LEFT_BOTTOM),
                Map.entry(outline, ViewRails.RIGHT_TOP));
    }

    @Test
    void aStackUnderAnotherOnTheRightGoesToTheRightRailsBottom() {
        E4ModelFixture.Stack outline = stack("outline", "50"), tasks = stack("tasks", "50");
        Object window = sash(null, true, area("70"), sash("30", false, outline, tasks));

        assertThat(places(window)).containsExactly(
                Map.entry(outline, ViewRails.RIGHT_TOP),
                Map.entry(tasks, ViewRails.RIGHT_BOTTOM));
    }

    @Test
    void editorStacksAreNotListed() {
        E4ModelFixture.Stack view = stack("view", "30");
        Object window = sash(null, true, view, stack("editors", "70").tag("EditorStack"));

        assertThat(places(window)).containsOnlyKeys(view);
    }

    @Test
    void aWindowWithoutAnEditorAreaHasNoViewStacks() {
        Object window = sash(null, true, stack("navigation", "30"), stack("examples", "70"));

        assertThat(ViewRails.viewStacks(new ViewRails.Model(), window, new Rectangle[1])).isEmpty();
    }

    @Test
    void aWindowWithAnEditorAreaListsItsViewStacksAroundIt() {
        E4ModelFixture.Stack left = stack("left", "30");
        Rectangle[] area = new Rectangle[1];

        assertThat(ViewRails.viewStacks(new ViewRails.Model(), sash(null, true, left, area("70")), area))
                .containsOnlyKeys(left);
        assertThat(area[0].x).isEqualTo(ViewRails.MODEL_SPAN * 3 / 10);
    }

    @Test
    void anUnrenderedSiblingTakesNoShare() {
        E4ModelFixture.Stack left = stack("left", "50");
        Object window = sash(null, true, left, stack("gone", "50").unrendered(), area("50"));

        Map<Object, Rectangle> stacks = new LinkedHashMap<>();
        Rectangle[] area = new Rectangle[1];
        new ViewRails.Model().collect(window, new Rectangle(0, 0, ViewRails.MODEL_SPAN, ViewRails.MODEL_SPAN), stacks, area);
        assertThat(stacks).containsOnlyKeys(left);
        assertThat(area[0].x).isEqualTo(ViewRails.MODEL_SPAN / 2);
    }

    @Test
    void theEditorAreaIsFoundInTheSelectedPerspective() {
        E4ModelFixture.Area area = area("70");
        E4ModelFixture.Perspective other = new E4ModelFixture.Perspective();
        E4ModelFixture.Perspective shown = new E4ModelFixture.Perspective();
        shown.children.add(sash(null, true, stack("left", "30"), area));
        E4ModelFixture.PerspectiveStack perspectives = new E4ModelFixture.PerspectiveStack();
        perspectives.children.add(other);
        perspectives.children.add(shown);
        perspectives.selectedElement = shown;
        E4ModelFixture.Window window = new E4ModelFixture.Window();
        window.children.add(perspectives);

        ViewRails.Model model = new ViewRails.Model();
        assertThat(model.perspectiveStack(window)).isSameAs(perspectives);
        assertThat(model.editorArea(window)).isSameAs(area);
    }

    @Test
    void aMethodIsInvokedByItsArgumentsWhetherPublicOrTheClassOwn() throws Exception {
        ViewRails.Model model = new ViewRails.Model();
        Renderer renderer = new Renderer();

        assertThat(model.invoke(renderer, "menuFor", "Outline", 3)).isEqualTo("Outline:3");
        assertThat(model.invoke(renderer, "split", 30f)).isEqualTo(30f);
    }

    /** Like a workbench renderer: a method of its own, not public. */
    static final class Renderer {
        private String menuFor(String view, int index) {
            return view + ":" + index;
        }

        public float split(float ratio) {
            return ratio;
        }
    }

    @Test
    void aRecordAnswersWhichStackIsAtAPlaceAndWhichRailIsOnASide() {
        CTabFolder left = org.mockito.Mockito.mock(CTabFolder.class);
        CTabFolder right = org.mockito.Mockito.mock(CTabFolder.class);
        org.eclipse.swt.widgets.Control leftRail = org.mockito.Mockito.mock(org.eclipse.swt.widgets.Control.class);
        ViewRails.Record record = new ViewRails.Record(java.util.List.of(left, right),
                java.util.List.of((long) ViewRails.LEFT_TOP, (long) (ViewRails.RIGHT_TOP | ViewRails.MINIMIZED)),
                java.util.List.of(leftRail), java.util.List.of((long) org.eclipse.swt.SWT.LEFT), java.util.List.of(),
                ViewRails.MEDIUM);

        assertThat(record.placeOf(right)).isEqualTo(ViewRails.RIGHT_TOP);
        assertThat(record.stackAt(ViewRails.RIGHT_TOP, null)).isSameAs(right);
        assertThat(record.stackAt(ViewRails.RIGHT_TOP, right)).isNull();
        assertThat(record.railOn(org.eclipse.swt.SWT.LEFT)).isSameAs(leftRail);
        assertThat(record.railOn(org.eclipse.swt.SWT.RIGHT)).isNull();
        assertThat(record.sideOf(leftRail)).isEqualTo(org.eclipse.swt.SWT.LEFT);
    }

    @Test
    void aDroppedViewLandsBeforeTheViewItWasDroppedOn() {
        java.util.List<String> stack = java.util.List.of("a", "b", "c", "d");

        assertThat(ViewRailsMenus.insertionIndex(stack, "x", "c")).as("from another stack").isEqualTo(2);
        assertThat(ViewRailsMenus.insertionIndex(stack, "a", "c")).as("from further up: c moves up once a leaves").isEqualTo(1);
        assertThat(ViewRailsMenus.insertionIndex(stack, "d", "b")).as("from further down").isEqualTo(1);
        assertThat(ViewRailsMenus.insertionIndex(stack, "b", null)).as("dropped after the last view").isEqualTo(-1);
    }

    /** An e4 stand-in whose one method throws. */
    public static final class Failing {
        public Object get() {
            throw new IllegalStateException("e4 changed");
        }
    }

    @Test
    void aFailedE4CallIsReportedOncePerMethodAndStillAnswersNull() {
        java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();
        java.io.PrintStream saved = System.err;
        System.setErr(new java.io.PrintStream(err, true));
        try {
            ViewRails.Model model = new ViewRails.Model();
            assertThat(model.call(new Failing(), "get")).isNull();
            assertThat(model.call(new Failing(), "get")).isNull();
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> model.invoke(new Failing(), "missing"))
                    .isInstanceOf(NoSuchMethodException.class);
        } finally {
            System.setErr(saved);
        }

        assertThat(err.toString()).containsOnlyOnce(Failing.class.getName() + ".get failed: java.lang.IllegalStateException: e4 changed")
                .containsOnlyOnce(Failing.class.getName() + ".missing failed: java.lang.NoSuchMethodException");
    }
}
