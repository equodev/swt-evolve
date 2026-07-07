package org.eclipse.swt.widgets;

import dev.equo.swt.harness.FlutterHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Full-stack reproduction of issue #754: a widget whose ancestor is disabled must render as
 * disabled on the Dart side, even though the widget's own {@code getEnabled()} flag is still
 * {@code true}.
 *
 * <p>The reported case is the "?" help button in a settings group: it is a {@link ToolItem}
 * inside a {@link ToolBar} inside a {@link Composite}. Disabling the group only flips the
 * container's own enabled flag (ToolItems are not returned by {@code Composite.getChildren()},
 * so a recursive {@code setEnabled} never touches the item). Native SWT still blocks such a
 * widget because {@code isEnabled()} consults the parent chain; the value serialized to Dart
 * did not, so the item stayed clickable / highlighted.
 *
 * <p>The fix serializes the effective {@code isEnabled()} (own AND all ancestors) for the
 * {@code enabled} property. Before it, both assertions below fail (the widget still renders
 * enabled); after it, they pass.
 *
 * <p>Run via the {@code webTest} task, which compiles this against the WEB Java backend.
 *
 * <pre>./gradlew :swt-evolve:swt_native:webTest</pre>
 */
@Tag("flutter-it")
class DisabledAncestorRenderedEnabledFlutterTest {

    private FlutterHarness flutter;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        flutter = new FlutterHarness();
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
    @DisplayName("a ToolItem renders disabled when an ancestor is disabled (issue #754)")
    void toolItemDisabledByAncestor() {
        Composite group = new Composite(shell, SWT.NONE);
        ToolBar bar = new ToolBar(group, SWT.FLAT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText("?");
        flutter.show(shell);

        assertThat(renderedEnabled(item)).as("ToolItem enabled while the group is enabled").isTrue();

        // Disable the ancestor group. This never flips the ToolItem's own enabled flag; only its
        // isEnabled() (which walks the parent chain) becomes false.
        group.setEnabled(false);
        flutter.flush();

        assertThat(item.getEnabled()).as("the ToolItem's own flag is untouched").isTrue();
        assertThat(renderedEnabled(item))
                .as("ToolItem must render disabled when an ancestor is disabled")
                .isFalse();
    }

    @Test
    @DisplayName("a Control renders disabled when an ancestor is disabled (issue #754)")
    void controlDisabledByAncestor() {
        Composite group = new Composite(shell, SWT.NONE);
        Button button = new Button(group, SWT.PUSH);
        button.setText("Action");
        flutter.show(shell);

        assertThat(flutter.renderedEnabled(button)).as("Button enabled while the group is enabled").isTrue();

        group.setEnabled(false);
        flutter.flush();

        assertThat(button.getEnabled()).as("the Button's own flag is untouched").isTrue();
        assertThat(flutter.renderedEnabled(button))
                .as("Button must render disabled when an ancestor is disabled")
                .isFalse();
    }

    /** The rendered enabled state of any widget (mirrors {@link FlutterHarness#renderedEnabled}
     *  but accepts a {@link Widget} so it also covers {@link ToolItem}, which is not a Control). */
    @SuppressWarnings("unchecked")
    private boolean renderedEnabled(Widget w) {
        Map<String, Object> resp = flutter.queryState(w);
        if (!Boolean.TRUE.equals(resp.get("found"))) return false;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        Object enabled = state == null ? null : state.get("enabled");
        return Boolean.TRUE.equals(enabled);
    }
}
