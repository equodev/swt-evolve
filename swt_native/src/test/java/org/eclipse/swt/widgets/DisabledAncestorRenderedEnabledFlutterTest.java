package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * A widget under a disabled ancestor must stop taking input on the Dart side, even though its own
 * {@code getEnabled()} flag is still {@code true} — {@code isEnabled()} consults the parent chain.
 * What is asserted is the rendered tree's refusal (the {@code IgnorePointer}/{@code ExcludeFocus}
 * a disabled control wraps its subtree in), not the flag Dart computed to decide it. Case: a
 * {@link ToolItem}, not returned by {@code Composite.getChildren()}, so a recursive
 * {@code setEnabled} on the group never touches it directly.
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

        assertThat(flutter.renderedTakesInput(item)).as("ToolItem takes input while the group is enabled").isTrue();

        // Disable the ancestor group. This never flips the ToolItem's own enabled flag; only its
        // isEnabled() (which walks the parent chain) becomes false.
        group.setEnabled(false);
        flutter.flush();

        assertThat(item.getEnabled()).as("the ToolItem's own flag is untouched").isTrue();
        assertThat(flutter.renderedTakesInput(item))
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

        assertThat(flutter.renderedTakesInput(button)).as("Button takes input while the group is enabled").isTrue();

        group.setEnabled(false);
        flutter.flush();

        assertThat(button.getEnabled()).as("the Button's own flag is untouched").isTrue();
        assertThat(flutter.renderedTakesInput(button))
                .as("Button must stop taking input when an ancestor is disabled")
                .isFalse();
        assertThat(renderedOwnEnabled(button))
                .as("the Button keeps its own look — the platform never touches a child's handle")
                .isTrue();
    }

    /** The widget's own {@code enabled} as it was serialized to Dart, not the effective answer. */
    @SuppressWarnings("unchecked")
    private boolean renderedOwnEnabled(Widget w) {
        Map<String, Object> resp = flutter.queryState(w);
        if (!Boolean.TRUE.equals(resp.get("found"))) return false;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        return state != null && Boolean.TRUE.equals(state.get("enabled"));
    }
}
