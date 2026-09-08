package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tool item click reported by the render side has to carry the activation a native click carries.
 * Natively a mouse down runs Shell.setActiveControl(control, SWT.MouseDown), which sends SWT.Activate
 * up the ancestor chain stamped with that detail; a workbench is allowed to react only to a real
 * click, and e4's StackRenderer does exactly that — its CTabFolder listener ignores any SWT.Activate
 * whose detail is not SWT.MouseDown. A view's tool bar (its buttons and the view menu) lives in that
 * folder, so without the stamped event the part behind the tool bar never becomes active and every
 * menu contribution resolved from the active part comes up empty.
 */
@Tag("flutter-it")
class ToolItemClickActivationFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private CTabFolder folder;
    private ToolBar bar;
    private ToolItem item;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.open();
        folder = new CTabFolder(shell, SWT.NONE);
        Composite content = new Composite(folder, SWT.NONE);
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        tab.setText("Problems");
        tab.setControl(content);
        folder.setSelection(0);
        // The view menu's tool bar is a child of the folder, placed in its top right corner.
        bar = new ToolBar(folder, SWT.FLAT);
        item = new ToolItem(bar, SWT.PUSH);
        item.setToolTipText("View Menu");
        folder.setTopRight(bar);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a tool item click activates the folder it sits in, stamped as a mouse down")
    void toolItemClickActivatesEnclosingFolder() {
        int[] activations = {0};
        int[] detail = { SWT.NONE };
        folder.addListener(SWT.Activate, e -> {
            activations[0]++;
            detail[0] = e.detail;
        });
        int[] selections = {0};
        item.addListener(SWT.Selection, e -> selections[0]++);

        clickItem();

        assertThat(selections[0])
                .as("the click still notifies the tool item's own listeners")
                .isOne();
        assertThat(activations[0])
                .as("the enclosing CTabFolder must be told the user clicked inside it")
                .isPositive();
        assertThat(detail[0])
                .as("a workbench that only activates a part on a real click keys off this detail")
                .isEqualTo(SWT.MouseDown);
    }

    @Test
    @DisplayName("the activation reaches every ancestor up to the shell")
    void activationTravelsUpTheChain() {
        int[] shellActivations = {0};
        shell.addListener(SWT.Activate, e -> shellActivations[0]++);

        clickItem();

        assertThat(shellActivations[0])
                .as("SWT.Activate walks the whole parent chain, as Shell.setActiveControl does")
                .isPositive();
    }

    /** Fire the tool item's own Dart→Java Selection handler and drain the asyncExec it hops through. */
    private void clickItem() {
        String channel = FlutterBridge.event((DartWidget) item.getImpl(), "Selection", "Selection");
        bridge.comm.fireContaining(channel, new Event());
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }
    }
}
