package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Full-stack reproduction of a bug where a widget whose ancestor is disabled must stop taking
 * input on the Dart side, even though the widget's own {@code getEnabled()} flag is still
 * {@code true}.
 *
 * <p>The reported case is the "?" help button in a settings group: it is a {@link ToolItem}
 * inside a {@link ToolBar} inside a {@link Composite}. Disabling the group only flips the
 * container's own enabled flag (ToolItems are not returned by {@code Composite.getChildren()},
 * so a recursive {@code setEnabled} never touches the item). Native SWT still blocks such a
 * widget because {@code isEnabled()} consults the parent chain; the value serialized to Dart
 * did not, so the item stayed clickable / highlighted.
 *
 * <p>The payload carries both states, the way SWT itself splits them: {@code enabled} is the
 * control's own flag and drives how it is drawn, while {@code enabledEffective} (own AND all
 * ancestors) is what native SWT consults in its hit test and event dispatch. Natively, disabling a
 * container never touches a child's handle, so the child keeps its own look but stops taking input
 * — which is what these assertions check. Before the fix {@code enabledEffective} did not exist and
 * the item stayed interactive.
 *
 * <p>Run via the {@code webTest} task, which compiles this against the WEB Java backend.
 *
 * <pre>./gradlew :swt-evolve:swt_native:webTest</pre>
 */
@Tag("flutter-it")
class DisabledAncestorRenderedEnabledFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setSize(300, 200);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    @DisplayName("a ToolItem stops taking input when an ancestor is disabled")
    void toolItemDisabledByAncestor() {
        Composite group = new Composite(shell, SWT.NONE);
        ToolBar bar = new ToolBar(group, SWT.FLAT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText("?");
        flutter.show(shell);

        assertThat(renderedEffectivelyEnabled(item)).as("ToolItem enabled while the group is enabled").isTrue();

        // Disable the ancestor group. This never flips the ToolItem's own enabled flag; only its
        // isEnabled() (which walks the parent chain) becomes false.
        group.setEnabled(false);
        flutter.flush();

        assertThat(item.getEnabled()).as("the ToolItem's own flag is untouched").isTrue();
        assertThat(renderedEffectivelyEnabled(item))
                .as("ToolItem must stop taking input when an ancestor is disabled")
                .isFalse();
    }

    @Test
    @DisplayName("a Control stops taking input when an ancestor is disabled")
    void controlDisabledByAncestor() {
        Composite group = new Composite(shell, SWT.NONE);
        Button button = new Button(group, SWT.PUSH);
        button.setText("Action");
        flutter.show(shell);

        assertThat(renderedEffectivelyEnabled(button)).as("Button enabled while the group is enabled").isTrue();

        group.setEnabled(false);
        flutter.flush();

        assertThat(button.getEnabled()).as("the Button's own flag is untouched").isTrue();
        assertThat(renderedEffectivelyEnabled(button))
                .as("Button must stop taking input when an ancestor is disabled")
                .isFalse();
        assertThat(renderedOwnEnabled(button))
                .as("the Button keeps its own look — the platform never touches a child's handle")
                .isTrue();
    }

    private boolean renderedEffectivelyEnabled(Widget w) {
        return Boolean.TRUE.equals(renderedState(w, "enabledEffective"));
    }

    private boolean renderedOwnEnabled(Widget w) {
        return Boolean.TRUE.equals(renderedState(w, "enabled"));
    }

    @SuppressWarnings("unchecked")
    private Object renderedState(Widget w, String field) {
        Map<String, Object> resp = flutter.queryState(w);
        if (!Boolean.TRUE.equals(resp.get("found"))) return null;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        return state == null ? null : state.get(field);
    }
}
